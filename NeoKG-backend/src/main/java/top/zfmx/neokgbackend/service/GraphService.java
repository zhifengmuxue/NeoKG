package top.zfmx.neokgbackend.service;

import top.zfmx.neokgbackend.model.Document;

import java.util.List;
import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface GraphService {
    Map<String, Object> buildDocumentKeywordGraph(List<Document> documents);
}
