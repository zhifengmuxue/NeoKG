package top.zfmx.neokgbackend.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import top.zfmx.neokgbackend.pojo.entity.DocumentNode;

import java.util.Optional;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface DocumentNodeRepository
        extends Neo4jRepository<DocumentNode, String> {
    Optional<DocumentNode> findByDocId(Long id);

}
