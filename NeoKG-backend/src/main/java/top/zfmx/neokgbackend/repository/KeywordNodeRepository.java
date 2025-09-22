package top.zfmx.neokgbackend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import top.zfmx.neokgbackend.pojo.entity.KeywordNode;

import java.util.Optional;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface KeywordNodeRepository
        extends Neo4jRepository<KeywordNode, String> {
    Optional<KeywordNode> findByKeywordId(Long id);
}
