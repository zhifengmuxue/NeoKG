package top.zfmx.neokgbackend.repository;

import jakarta.annotation.Resource;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface GraphRepository {


    List<Map<String, Object>> detectCommunities();

    List<Map<String, Object>> findDocKeywordRelations();

    Map<String, Object> findPath(String startType, Long startId, String endType, Long endId);
}
