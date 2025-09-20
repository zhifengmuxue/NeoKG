package top.zfmx.neokgbackend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import top.zfmx.neokgbackend.model.KeywordNode;

import java.util.List;
import java.util.Optional;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface KeywordNodeRepository
        extends Neo4jRepository<KeywordNode, String> {
    Optional<KeywordNode> findByKeywordId(Long id);
    @Query("MATCH (k:KeywordNode) WHERE size((k)--()) = 0 RETURN k")
    List<KeywordNode> findIsolatedNodes();

    @Query("MATCH (k:KeywordNode) WHERE NOT (:DocumentNode)-[:HAS_KEYWORD]->(k) RETURN k")
    List<KeywordNode> findOrphanKeywords();
}
