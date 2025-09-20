package top.zfmx.neokgbackend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import top.zfmx.neokgbackend.model.DocumentNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface DocumentNodeRepository
        extends Neo4jRepository<DocumentNode, String> {
    Optional<DocumentNode> findByDocId(Long id);

    @Query("MATCH (d:DocumentNode)-[r:HAS_KEYWORD]->() RETURN count(r)")
    long countRelationships();

    @Query("MATCH (d:DocumentNode) WHERE size((d)--()) = 0 RETURN d")
    List<DocumentNode> findIsolatedNodes();

    @Query("MATCH (d:DocumentNode) WHERE NOT (d)-[:HAS_KEYWORD]->(:KeywordNode) RETURN d")
    List<DocumentNode> findOrphanDocs();

    @Query("CALL gds.wcc.stream({nodeProjection: 'DocumentNode', relationshipProjection: 'HAS_KEYWORD'}) "
            + "YIELD nodeId, componentId RETURN componentId, count(nodeId) AS size")
    List<Map<String, Object>> getConnectedComponents();
}
