package top.zfmx.neokgbackend.repository.impl;

import jakarta.annotation.Resource;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;
import top.zfmx.neokgbackend.repository.GraphRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Repository
public class GraphRepositoryImpl implements GraphRepository {
    @Resource
    private Neo4jClient neo4jClient;

    private static final String GRAPH_NAME = "myGraph";

    /**
     * 社区发现：Louvain
     */
    @Override
    public List<Map<String, Object>> detectCommunities() {
        String cypher = String.format("""
                CALL gds.louvain.stream('%s')
                YIELD nodeId, communityId
                RETURN
                    gds.util.asNode(nodeId).docId AS docId,
                    gds.util.asNode(nodeId).keywordId AS keywordId,
                    gds.util.asNode(nodeId).title AS title,
                    gds.util.asNode(nodeId).name AS name,
                    communityId AS community
                """, GRAPH_NAME);

        return new ArrayList<>(neo4jClient.query(cypher).fetch().all());
    }

    /**
     * 查询文档-关键词关系
     */
    @Override
    public List<Map<String, Object>> findDocKeywordRelations() {
        String cypher = """
            MATCH (n)-[r:HAS_KEYWORD]-(m)
            RETURN id(n) AS sourceId, id(m) AS targetId
            """;
        return new ArrayList<>(neo4jClient.query(cypher).fetch().all());
    }


    /**
     * 查询两节点之间路径
     */
    @Override
    public Map<String, Object> findPath(String startType, Long startId, String endType, Long endId) {
        String startLabel = startType.equalsIgnoreCase("DOCUMENT") ? "DocumentNode" : "KeywordNode";
        String endLabel = endType.equalsIgnoreCase("DOCUMENT") ? "DocumentNode" : "KeywordNode";
        String startProp = startType.equalsIgnoreCase("DOCUMENT") ? "docId" : "keywordId";
        String endProp = endType.equalsIgnoreCase("DOCUMENT") ? "docId" : "keywordId";
        System.out.println(startLabel + " " + endLabel + " " + startProp + " " + endProp);
        System.out.println(startId + " " + endId);
        String cypher;

        int maxHops = 100;
        cypher = String.format("""
            MATCH p = (start:%s {%s: $startId})-[:HAS_KEYWORD*1..%d]-(end:%s {%s: $endId})
            WHERE id(start) <> id(end)
            RETURN
                [n IN nodes(p) | {
                    id: coalesce(n.docId, n.keywordId),
                    name: coalesce(n.title, n.name),
                    type: CASE WHEN n:DocumentNode THEN 'DOCUMENT' ELSE 'KEYWORD' END
                }] AS pathNodes,
                [r IN relationships(p) | {
                    start: coalesce(startNode(r).docId, startNode(r).keywordId),
                    end: coalesce(endNode(r).docId, endNode(r).keywordId),
                    type: type(r)
                }] AS pathRels
            ORDER BY length(p) ASC
            LIMIT 1
        """, startLabel, startProp, maxHops, endLabel, endProp);



        return neo4jClient.query(cypher)
                .bind(startId).to("startId")
                .bind(endId).to("endId")
                .fetch()
                .one()
                .orElse(Map.of());
    }

    @Override
    public void ensureGraphExists() {
        // 检查 myGraph 是否存在
        String existsQuery = "CALL gds.graph.exists('myGraph') YIELD exists";
        Boolean exists = neo4jClient.query(existsQuery)
                .fetchAs(Boolean.class)
                .mappedBy((typeSystem, record) -> record.get("exists").asBoolean())
                .one()
                .orElse(false);

        if (exists) {
            // 删除旧的 myGraph 投影
            String dropQuery = "CALL gds.graph.drop('myGraph', false)";
            neo4jClient.query(dropQuery).run();
        }

        // 重新创建投影图
        String projectQuery = """
        CALL gds.graph.project(
          'myGraph',
          ['DocumentNode', 'KeywordNode'],
          { HAS_KEYWORD: { orientation: 'UNDIRECTED' } }
        )
        """;
        neo4jClient.query(projectQuery).run();
    }


}
