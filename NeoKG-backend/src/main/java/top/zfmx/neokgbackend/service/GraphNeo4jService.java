package top.zfmx.neokgbackend.service;

import top.zfmx.neokgbackend.pojo.entity.Document;

import java.util.List;
import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface GraphNeo4jService {

    void saveDocumentsToNeo4j(List<Document> documents, boolean fullUpdate);

    void clearAll();

    Map<String, Object> getGraphWithRedis();

    Map<String, Object> getCommunityGraph();

    Map<String, Object> getAllGraph();
}
