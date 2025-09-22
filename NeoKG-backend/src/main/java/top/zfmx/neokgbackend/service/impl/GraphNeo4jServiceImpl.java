package top.zfmx.neokgbackend.service.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.pojo.dto.GraphEdge;
import top.zfmx.neokgbackend.pojo.dto.GraphNode;
import top.zfmx.neokgbackend.pojo.entity.Document;
import top.zfmx.neokgbackend.pojo.entity.Keyword;
import top.zfmx.neokgbackend.pojo.entity.DocumentNode;
import top.zfmx.neokgbackend.pojo.entity.KeywordNode;
import top.zfmx.neokgbackend.repository.DocumentNodeRepository;
import top.zfmx.neokgbackend.repository.GraphRepository;
import top.zfmx.neokgbackend.repository.KeywordNodeRepository;
import top.zfmx.neokgbackend.service.GraphNeo4jService;
import top.zfmx.neokgbackend.service.KeywordService;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class GraphNeo4jServiceImpl implements GraphNeo4jService {

    @Resource
    private DocumentNodeRepository documentNodeRepository;
    @Resource
    private KeywordNodeRepository keywordNodeRepository;
    @Resource
    private GraphRepository graphRepository;
    @Resource
    private KeywordService keywordService;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    private static final String DOC_KEYWORD_CACHE = "graph:docKeyword";
    private static final Gson gson = new Gson();

    // 基础 TTL，单位秒
    private static final long BASE_TTL = 600; // 10 分钟

    @Override
    public void saveDocumentsToNeo4j(List<Document> documents, boolean fullUpdate) {
        if (fullUpdate) {
            clearAll();
        }

        List<Keyword> allKeywords = keywordService.findAllKeywords();
        Map<Long, KeywordNode> keywordCache = new HashMap<>();

        if (allKeywords != null) {
            for (Keyword kw : allKeywords) {
                KeywordNode kwNode = keywordNodeRepository.findByKeywordId(kw.getId())
                        .orElseGet(() -> {
                            KeywordNode newKw = new KeywordNode();
                            newKw.setKeywordId(kw.getId());
                            newKw.setName(kw.getName());
                            return newKw;
                        });
                keywordCache.put(kw.getId(), keywordNodeRepository.save(kwNode));
            }
        }

        for (Document doc : documents) {
            DocumentNode docNode = documentNodeRepository.findByDocId(doc.getId())
                    .orElseGet(() -> {
                        DocumentNode newNode = new DocumentNode();
                        newNode.setId(UUID.randomUUID().toString());
                        newNode.setDocId(doc.getId());
                        return newNode;
                    });

            docNode.setTitle(doc.getTitle());

            if (doc.getKeywords() != null) {
                for (Keyword kw : doc.getKeywords()) {
                    KeywordNode kwNode = keywordCache.get(kw.getId());
                    if (kwNode != null) {
                        docNode.getKeywords().add(kwNode);
                    }
                }
            }

            documentNodeRepository.save(docNode);
        }

        // 数据更新 → 删除缓存（保证一致性）
        redisTemplate.delete(DOC_KEYWORD_CACHE);
    }

    @Override
    public void clearAll() {
        documentNodeRepository.deleteAll();
        keywordNodeRepository.deleteAll();
        redisTemplate.delete(DOC_KEYWORD_CACHE);
    }

    @Override
    public Map<String, Object> getDocKeywordGraph() {
        // 1. 先查缓存
        String cacheJson = redisTemplate.opsForValue().get(DOC_KEYWORD_CACHE);
        if (cacheJson != null) {
            // 使用 TypeToken 指定泛型类型
            return gson.fromJson(cacheJson, new TypeToken<Map<String, Object>>() {}.getType());
        }

        String lockKey = DOC_KEYWORD_CACHE + ":lock";
        boolean lockAcquired = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS)
        );

        if (lockAcquired) {
            try {
                // 2. 缓存未命中且获取到锁 → 查询 Neo4j
                Map<String, Object> graph = buildGraphFromNeo4j();

                // 3. 写入缓存，加随机过期时间，避免雪崩
                long ttl = 600 + ThreadLocalRandom.current().nextInt(120); // 10-12 分钟
                redisTemplate.opsForValue().set(DOC_KEYWORD_CACHE, gson.toJson(graph), ttl, TimeUnit.SECONDS);

                return graph;
            } finally {
                // 释放锁
                redisTemplate.delete(lockKey);
            }
        } else {
            // 没拿到锁，短轮询缓存，防止击穿
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(50); // 等 50ms
                } catch (InterruptedException ignored) { }
                cacheJson = redisTemplate.opsForValue().get(DOC_KEYWORD_CACHE);
                if (cacheJson != null) {
                    // 使用 TypeToken 指定泛型类型
                    return gson.fromJson(cacheJson, new TypeToken<Map<String, Object>>() {}.getType());
                }
            }
            // 最后兜底直接查询 Neo4j
            return buildGraphFromNeo4j();
        }
    }

    @Override
    public Map<String, Object> getCommunityGraph() {
        String cacheKey = "graph:community";
        String cacheJson = redisTemplate.opsForValue().get(cacheKey);
        if (cacheJson != null) {
            return gson.fromJson(cacheJson, new TypeToken<Map<String, Object>>() {}.getType());
        }

        // 1️⃣ 社区划分
        List<Map<String, Object>> results = graphRepository.detectCommunities();

        List<GraphNode> nodes = new ArrayList<>();
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> nodeIds = new HashSet<>();

        for (Map<String, Object> row : results) {
            String docId = row.get("docId") != null ? String.valueOf(row.get("docId")) : null;
            String keywordId = row.get("keywordId") != null ? String.valueOf(row.get("keywordId")) : null;
            String title = (String) row.get("title");
            String name = (String) row.get("name");
            long community = ((Number) row.get("community")).longValue();

            String nodeId = docId != null ? "doc-" + docId :
                    keywordId != null ? "kw-" + keywordId :
                            UUID.randomUUID().toString();

            if (!nodeIds.add(nodeId)) {
                continue;
            }

            String label = docId != null ? title : name;

            String color = switch ((int) community % 5) {
                case 0 -> "#5470C6";
                case 1 -> "#91CC75";
                case 2 -> "#FAC858";
                case 3 -> "#EE6666";
                default -> "#73C0DE";
            };

            nodes.add(new GraphNode(nodeId, label, 20.0, color));
        }

        // 2️⃣ 关系查询
        List<Map<String, Object>> rels = graphRepository.findDocKeywordRelations();
        for (Map<String, Object> rel : rels) {
            edges.add(new GraphEdge(String.valueOf(rel.get("sourceId")), String.valueOf(rel.get("targetId"))));
        }

        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", nodes);
        graph.put("edges", edges);

        // 3️⃣ 缓存
        long ttl = BASE_TTL + ThreadLocalRandom.current().nextInt(120);
        redisTemplate.opsForValue().set(cacheKey, gson.toJson(graph), ttl, TimeUnit.SECONDS);

        return graph;
    }



    /**
     * 从 Neo4j 构建文档-关键词图
     */
    private Map<String, Object> buildGraphFromNeo4j() {
        List<DocumentNode> docs = documentNodeRepository.findAll();
        List<KeywordNode> allKeywords = keywordNodeRepository.findAll();

        List<GraphNode> nodes = new ArrayList<>();
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> nodeIds = new HashSet<>();

        for (DocumentNode doc : docs) {
            String docNodeId = "doc-" + doc.getDocId();
            if (nodeIds.add(docNodeId)) {
                nodes.add(new GraphNode(docNodeId, doc.getTitle(), 30.0, "#5470c6"));
            }
            for (KeywordNode kw : doc.getKeywords()) {
                String kwNodeId = "kw-" + kw.getKeywordId();
                if (nodeIds.add(kwNodeId)) {
                    nodes.add(new GraphNode(kwNodeId, kw.getName(), 20.0, "#91cc75"));
                }
                edges.add(new GraphEdge(docNodeId, kwNodeId));
            }
        }

        for (KeywordNode kw : allKeywords) {
            String kwNodeId = "kw-" + kw.getKeywordId();
            if (nodeIds.add(kwNodeId)) {
                nodes.add(new GraphNode(kwNodeId, kw.getName(), 20.0, "#91cc75"));
            }
        }

        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", nodes);
        graph.put("edges", edges);

        return graph;
    }
}
