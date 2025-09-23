package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import top.zfmx.neokgbackend.pojo.entity.Document;
import top.zfmx.neokgbackend.pojo.response.Result;
import top.zfmx.neokgbackend.service.*;

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

    @Resource
    private PathService pathService;

    @Resource
    private GraphMetricsCacheService cacheService;

    @Resource
    private GraphAnalysisService graphAnalysisService;


    /**
     * 获取文档-关键词图
     */
    @GetMapping("/doc-keyword")
    public Map<String, Object> getDocKeywordGraph() {
        return graphNeo4jService.getGraphWithRedis();
    }

    @GetMapping("/all")
    public Map<String, Object> getAllGraph() {
        return graphNeo4jService.getAllGraph();
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

    /**
     * 路径查询：查找两个节点之间的最短路径 BFS
     *
     * @param startType 起始节点类型，可选值：DOCUMENT（默认）、KEYWORD
     * @param startId 起始节点ID
     * @param endType  目的节点类型，可选值：DOCUMENT（默认）、KEYWORD
     * @param endId   目的节点ID
     * @return 返回路径信息json
     */
    @GetMapping("/path")
    public Map<String, Object> findPath(
            @RequestParam(defaultValue = "KEYWORD") String startType, // DOCUMENT / KEYWORD
            @RequestParam Long startId,
            @RequestParam(defaultValue = "KEYWORD") String endType,   // DOCUMENT / KEYWORD
            @RequestParam Long endId
    ) {
        return pathService.getPath(startType, startId, endType, endId);
    }

    /**
     * 社区发现：基于 Louvain 算法
     */
    @GetMapping("/community")
    public Map<String, Object> getCommunityGraph() {
        return graphNeo4jService.getCommunityGraph();
    }

    /**
     * PageRank 排名：基于图算法
     */
    @GetMapping("/pagerank")
    public Map<String, Object> getPageRank(
            @RequestParam(defaultValue = "KEYWORD") String type, // 默认对关键词跑 PageRank
            @RequestParam(defaultValue = "20") int limit         // 默认取前 20 个
    ) {
        return graphNeo4jService.getPageRank(type, limit);
    }


    @GetMapping("/analysis/metrics")
    public Result<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = cacheService.getMetrics();
        return metrics != null ? Result.ok(metrics) : Result.cacheError("图异常尚未生成，请稍后再试");
    }

    @GetMapping("/analysis/anomalies")
    public Result<Map<String, Object>> getAnomalies() {
        Map<String, Object> anomalies = cacheService.getAnomalies();
        return anomalies != null ? Result.ok(anomalies) : Result.cacheError("图异常尚未生成，请稍后再试");
    }

    @GetMapping("/analysis/refresh")
    public Result<String> refreshAnalysis() {
        Map<String, Object> metrics = graphAnalysisService.calculateMetrics();
        cacheService.cacheMetrics(metrics);
        Map<String, Object> anomalies = graphAnalysisService.detectAnomalies();
        cacheService.cacheAnomalies(anomalies);
        return Result.ok("图分析刷新完成");
    }
}


