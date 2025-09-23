package top.zfmx.neokgbackend.service.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import jakarta.annotation.Resource;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
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
import org.neo4j.driver.*;
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
    @Resource
    private Driver driver;

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
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
        }
        redisTemplate.delete(DOC_KEYWORD_CACHE);
    }

    @Override
    public Map<String, Object> getGraphWithRedis() {
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
            // TODO 这里其实有点问题 测试中
            return buildGraphFromNeo4j();
//            return getAllGraph();
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

            // 🎨 动态颜色分配（基于 HSL）
            String color = "hsl(" + (community * 47 % 360) + ", 70%, 50%)";

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

    @Override
    public Map<String, Object> getAllGraph() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        Map<String, String> idToFrontendId = new HashMap<>(); // neo4j 内部 id -> 前端 id

        try (Session session = driver.session()) {

            // 查询所有节点
            session.executeRead(tx -> {
                Result rs = tx.run("MATCH (n) RETURN n");
                while (rs.hasNext()) {
                    Record record = rs.next();
                    Node node = record.get("n").asNode();

                    Object rawIdObj = node.get("id").asObject(); // 支持 Long / Integer / String
                    String rawId = rawIdObj.toString();
                    String label = node.labels().iterator().next();
                    String frontendId = label.substring(0, 2).toLowerCase() + "-" + rawId;

                    idToFrontendId.put(rawId, frontendId);

                    Map<String, Object> nodeMap = new HashMap<>();
                    nodeMap.put("id", frontendId);
                    Object nameProp = node.get("name").isNull() ? null : node.get("name").asObject();
                    Object titleProp = node.get("title").isNull() ? null : node.get("title").asObject();
                    String labelText = nameProp != null ? nameProp.toString()
                            : titleProp != null ? titleProp.toString()
                            : label;
                    nodeMap.put("label", labelText);

                    nodeMap.put("size", label.equalsIgnoreCase("Document") ? 30 : 20);
                    nodeMap.put("color", label.equalsIgnoreCase("Document") ? "#5470c6" : "#91cc75");
                    nodes.add(nodeMap);
                }
                return null;
            });

            // 查询关系
            session.executeRead(tx -> {
                Result rs = tx.run("MATCH (a)-[r]->(b) RETURN a.id AS startId, b.id AS endId");
                while (rs.hasNext()) {
                    Record record = rs.next();

                    String startIdRaw = record.get("startId").asObject().toString();
                    String endIdRaw = record.get("endId").asObject().toString();

                    Map<String, Object> edge = new HashMap<>();
                    edge.put("source", idToFrontendId.get(startIdRaw));
                    edge.put("target", idToFrontendId.get(endIdRaw));
                    edges.add(edge);
                }
                return null;
            });


        }

        result.put("nodes", nodes);
        result.put("edges", edges);
        return result;
    }

    @Override
    public Map<String, Object> getPageRank(String type, int limit) {
        try (Session session = driver.session()) {

            // 先删除旧的 projection（避免重复）
            session.executeWrite(tx -> {
                tx.run("CALL gds.graph.drop('myGraph', false)");
                return null;
            });

            // 根据类型决定 nodeProjection
            String projection;
            if (type.equalsIgnoreCase("DOCUMENT")) {
                projection = "['Document']";
            } else if (type.equalsIgnoreCase("KEYWORD")) {
                projection = "['Keyword']";
            } else { // ALL
                projection = "['Document','Keyword']";
            }

            // 创建新 projection
            session.executeWrite(tx -> {
                tx.run("CALL gds.graph.project('myGraph', " + projection + ", 'HAS_KEYWORD')");
                return null;
            });

            // 执行 PageRank
            return session.executeRead(tx -> {
                Result result = tx.run(
                        "CALL gds.pageRank.stream('myGraph') " +
                                "YIELD nodeId, score " +
                                "RETURN gds.util.asNode(nodeId).id AS id, " +
                                "       coalesce(gds.util.asNode(nodeId).name, gds.util.asNode(nodeId).title) AS label, " +
                                "       labels(gds.util.asNode(nodeId))[0] AS type, " +
                                "       score " +
                                "ORDER BY score DESC LIMIT $limit",
                        Map.of("limit", limit)
                );

                List<Map<String, Object>> ranked = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    ranked.add(Map.of(
                            "id", record.get("id").asString(),
                            "label", record.get("label").asString(),
                            "type", record.get("type").asString(),
                            "score", record.get("score").asDouble()
                    ));
                }

                return Map.of("mode", type.toUpperCase(), "results", ranked);
            });
        }
    }

    /**
     * 根据 Document ID 查找它的子图证据
     */
    public Map<String, Object> findEvidenceSubgraph(Long docId) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        Map<String, String> idMap = new HashMap<>();

        try (Session session = driver.session()) {
            session.executeRead(tx -> {
                // 查找文档节点及其相邻关键词
                Result rs = tx.run("""
                        MATCH (d:Document {id: $docId})-[:HAS_KEYWORD]->(k:Keyword)
                        RETURN d, k
                        """, Map.of("docId", docId));

                while (rs.hasNext()) {
                    Record record = rs.next();

                    Node docNode = record.get("d").asNode();
                    Node kwNode = record.get("k").asNode();

                    // 文档节点
                    String docFrontendId = "doc-" + docNode.get("id").asLong();
                    if (!idMap.containsKey("doc-" + docNode.get("id").asLong())) {
                        Map<String, Object> docMap = Map.of(
                                "id", docFrontendId,
                                "label", docNode.get("title").asString("文档"),
                                "type", "Document"
                        );
                        nodes.add(docMap);
                        idMap.put("doc-" + docNode.get("id").asLong(), docFrontendId);
                    }

                    // 关键词节点
                    String kwFrontendId = "kw-" + kwNode.get("id").asLong();
                    if (!idMap.containsKey("kw-" + kwNode.get("id").asLong())) {
                        Map<String, Object> kwMap = Map.of(
                                "id", kwFrontendId,
                                "label", kwNode.get("name").asString("关键词"),
                                "type", "Keyword"
                        );
                        nodes.add(kwMap);
                        idMap.put("kw-" + kwNode.get("id").asLong(), kwFrontendId);
                    }

                    // 边
                    edges.add(Map.of(
                            "source", docFrontendId,
                            "target", kwFrontendId,
                            "type", "HAS_KEYWORD"
                    ));
                }
                return null;
            });
        }

        return Map.of(
                "nodes", nodes,
                "edges", edges
        );
    }
}
