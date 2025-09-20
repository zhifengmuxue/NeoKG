package top.zfmx.neokgbackend.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.repository.DocumentNodeRepository;
import top.zfmx.neokgbackend.repository.KeywordNodeRepository;
import top.zfmx.neokgbackend.model.*;
import top.zfmx.neokgbackend.service.GraphNeo4jService;
import top.zfmx.neokgbackend.service.KeywordService;

import java.util.*;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class GraphNeo4jServiceImpl implements GraphNeo4jService {

    @Resource
    private DocumentNodeRepository documentNodeRepository;
    @Resource
    private KeywordNodeRepository keywordNodeRepository;
    @Resource
    private KeywordService keywordService;

    /**
     * 保存文档和关键词到图数据库
     * @param documents 文档列表
     * @param fullUpdate 是否全量更新（true：先清空再同步，false：增量更新）
     */
    @Override
    public void saveDocumentsToNeo4j(List<Document> documents, boolean fullUpdate) {
        if (fullUpdate) {
            clearAll();
        }
        List<Keyword> allKeywords = keywordService.findAllKeywords();
        Map<Long, KeywordNode> keywordCache = new HashMap<>();

        // 先保存所有关键词（不管是否关联文档）
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

        // 再保存文档和关系
        for (Document doc : documents) {
            DocumentNode docNode = documentNodeRepository.findByDocId(doc.getId())
                    .orElseGet(() -> {
                        DocumentNode newNode = new DocumentNode();
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
    }


    /**
     * 清空所有文档和关键词节点（以及关系）
     */
    @Override
    public void clearAll() {
        documentNodeRepository.deleteAll();
        keywordNodeRepository.deleteAll();
    }

    /**
     * 构建文档-关键词图数据（前端可直接渲染）
     */
    @Override
    public Map<String, Object> getDocKeywordGraph() {
        List<DocumentNode> docs = documentNodeRepository.findAll();
        List<KeywordNode> allKeywords = keywordNodeRepository.findAll();

        List<GraphNode> nodes = new ArrayList<>();
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> nodeIds = new HashSet<>();

        // 遍历文档和它们的关键词
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

        // 补充独立关键词（未被任何文档引用）
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
