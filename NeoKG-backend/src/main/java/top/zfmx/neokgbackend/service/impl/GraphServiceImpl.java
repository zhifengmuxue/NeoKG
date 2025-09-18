package top.zfmx.neokgbackend.service.impl;

import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.model.Document;
import top.zfmx.neokgbackend.model.GraphEdge;
import top.zfmx.neokgbackend.model.GraphNode;
import top.zfmx.neokgbackend.model.Keyword;
import top.zfmx.neokgbackend.service.GraphService;

import java.util.*;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class GraphServiceImpl implements GraphService {

    @Override
    public Map<String, Object> buildDocumentKeywordGraph(List<Document> documents) {
        List<GraphNode> nodes = new ArrayList<>();
        List<GraphEdge> edges = new ArrayList<>();

        Set<String> keywordIdsAdded = new HashSet<>();

        for (Document doc : documents) {
            // 添加 Document 节点
            GraphNode docNode = new GraphNode();
            docNode.setId("doc-" + doc.getId());
            docNode.setLabel(doc.getTitle());
            docNode.setColor("#5470c6"); // 文档节点颜色
            docNode.setSize(30.0);
            nodes.add(docNode);

            if (doc.getKeywords() != null) {
                for (Keyword kw : doc.getKeywords()) {
                    String kwNodeId = "kw-" + kw.getId();

                    // 避免重复添加 Keyword 节点
                    if (!keywordIdsAdded.contains(kwNodeId)) {
                        GraphNode kwNode = new GraphNode();
                        kwNode.setId(kwNodeId);
                        kwNode.setLabel(kw.getName());
                        kwNode.setColor("#91cc75"); // 关键词节点颜色
                        kwNode.setSize(20.0);
                        nodes.add(kwNode);
                        keywordIdsAdded.add(kwNodeId);
                    }

                    // 添加边 Document -> Keyword
                    GraphEdge edge = new GraphEdge();
                    edge.setSource(docNode.getId());
                    edge.setTarget(kwNodeId);
                    edges.add(edge);
                }
            }
        }

        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        return graph;
    }
}
