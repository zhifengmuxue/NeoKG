package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import top.zfmx.neokgbackend.model.Document;
import top.zfmx.neokgbackend.response.Result;
import top.zfmx.neokgbackend.service.DocumentService;
import top.zfmx.neokgbackend.service.GraphNeo4jService;

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
    private GraphNeo4jService graphNeo4jService;

    @Resource
    private DocumentService documentService;

    /**
     * 获取文档-关键词图
     */
    @GetMapping("/doc-keyword")
    public Map<String, Object> getDocKeywordGraph() {
        return graphNeo4jService.getDocKeywordGraph();
    }

    /**
     * 同步文档和关键词到图数据库
     */
    @PostMapping("/sync-documents")
    public Result<String> syncDocuments(@RequestParam("fullUpdate") boolean fullUpdate) {
        List<Document> documents = documentService.listDocumentsWithKeywords();
        graphNeo4jService.saveDocumentsToNeo4j(documents, fullUpdate);
        return Result.ok("同步完成");
    }
}
