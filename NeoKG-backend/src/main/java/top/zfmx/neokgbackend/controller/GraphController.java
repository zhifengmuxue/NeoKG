package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zfmx.neokgbackend.model.Document;
import top.zfmx.neokgbackend.service.DocumentService;
import top.zfmx.neokgbackend.service.GraphService;

import java.util.List;
import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/api/graph")
public class GraphController {
    @Resource
    private DocumentService documentService;
    @Resource
    private GraphService graphService;

    @GetMapping("/doc-keyword")
    public Map<String, Object> getDocKeywordGraph() {
        List<Document> documents = documentService.listDocumentsWithKeywords();
        return graphService.buildDocumentKeywordGraph(documents);
    }
}
