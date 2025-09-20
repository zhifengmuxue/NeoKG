package top.zfmx.neokgbackend.service.impl;

import jakarta.annotation.Resource;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.service.GraphAnalysisService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class GraphAnalysisServiceImpl implements GraphAnalysisService {

    @Resource
    private Neo4jClient neo4jClient;

    private static final String GRAPH_NAME = "docKeywordGraph";

    private void ensureGraphProjection() {
        neo4jClient.query("CALL gds.graph.exists($graphName) YIELD exists")
                .bind(GRAPH_NAME).to("graphName")
                .fetchAs(Boolean.class)
                .first()
                .ifPresentOrElse(exists -> {
                    if (!exists) {
                        createGraphProjection();
                    }
                }, this::createGraphProjection);
    }

    private void createGraphProjection() {
        neo4jClient.query("""
            CALL gds.graph.project(
              $graphName,
              ['DocumentNode','KeywordNode'],
              {HAS_KEYWORD:{orientation:'UNDIRECTED'}}
            )
        """).bind(GRAPH_NAME).to("graphName").run();
    }

    @Override
    public Map<String, Object> calculateMetrics() {
        ensureGraphProjection();
        Map<String, Object> metrics = new HashMap<>();

        // 节点总数
        long totalNodes = neo4jClient.query("MATCH (n) RETURN count(n) AS cnt")
                .fetchAs(Long.class).one().orElse(0L);

        // 关系总数
        long totalEdges = neo4jClient.query("MATCH ()-[r]->() RETURN count(r) AS cnt")
                .fetchAs(Long.class).one().orElse(0L);

        // 图密度（按无向图算）
        double density = (totalNodes > 1)
                ? (2.0 * totalEdges) / (totalNodes * (totalNodes - 1))
                : 0.0;

        // 连通性（依旧用 GDS wcc）
        List<Map<String, Object>> connectivity = (List<Map<String, Object>>) neo4jClient.query("""
            CALL gds.wcc.stream($graphName)
            YIELD componentId, nodeId
            RETURN componentId, count(nodeId) AS size
            ORDER BY size DESC
        """).bind(GRAPH_NAME).to("graphName")
                .fetch().all();

        // 孤立节点比例
        long isolatedNodes = neo4jClient.query("MATCH (n) WHERE NOT (n)--() RETURN count(n) AS cnt")
                .fetchAs(Long.class).one().orElse(0L);
        double isolatedRatio = totalNodes == 0 ? 0.0 : (double) isolatedNodes / totalNodes;

        metrics.put("density", density);
        metrics.put("connectivity", connectivity);
        metrics.put("isolatedRatio", isolatedRatio);

        return metrics;
    }


    @Override
    public Map<String, Object> detectAnomalies() {
        Map<String, Object> anomalies = new HashMap<>();

        // 无效关系链
        List<Map<String, Object>> invalidRelations = (List<Map<String, Object>>) neo4jClient.query("""
            MATCH (d:DocumentNode)-[r:HAS_KEYWORD]->(k:KeywordNode)
            WHERE d.title IS NULL OR k.name IS NULL
            RETURN d.docId AS docId, d.title AS title, k.keywordId AS keywordId, k.name AS name
        """).fetch().all();

        // 孤立节点
        List<Map<String, Object>> isolatedNodes = (List<Map<String, Object>>) neo4jClient.query("""
            MATCH (n)
            WHERE NOT (n)--()
            RETURN labels(n) AS labels, n.id AS id
        """).fetch().all();

        // 自环
        List<Map<String, Object>> selfLoops = (List<Map<String, Object>>) neo4jClient.query("""
            MATCH (n)-[r]->(n)
            RETURN labels(n) AS labels, n.id AS id, type(r) AS relation
        """).fetch().all();

        // 重复关系
        List<Map<String, Object>> duplicateRelations = (List<Map<String, Object>>) neo4jClient.query("""
            MATCH (d:DocumentNode)-[r:HAS_KEYWORD]->(k:KeywordNode)
            WITH d, k, count(r) AS relCount
            WHERE relCount > 1
            RETURN d.docId AS docId, k.keywordId AS keywordId, relCount
        """).fetch().all();

        anomalies.put("invalidRelations", invalidRelations);
        anomalies.put("isolatedNodes", isolatedNodes);
        anomalies.put("selfLoops", selfLoops);
        anomalies.put("duplicateRelations", duplicateRelations);

        return anomalies;
    }
}
