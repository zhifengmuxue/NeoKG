package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.noear.solon.ai.annotation.ToolMapping;
import org.noear.solon.ai.mcp.McpChannel;
import org.noear.solon.ai.mcp.server.IMcpServerEndpoint;
import org.noear.solon.ai.mcp.server.annotation.McpServerEndpoint;
import org.noear.solon.annotation.Param;
import org.springframework.stereotype.Controller;
import top.zfmx.neokgbackend.pojo.response.Result;
import top.zfmx.neokgbackend.service.*;

import java.util.Map;


@Controller
@McpServerEndpoint(channel = McpChannel.STREAMABLE, mcpEndpoint = "/mcp")
public class McpController implements IMcpServerEndpoint {
    @Resource
    private GraphNeo4jService graphNeo4jService;

    @Resource
    private PathService pathService;

    @Resource
    private GraphMetricsCacheService cacheService;

    @ToolMapping(name = "getDocuments", description = "关键词图")
    public Map<String, Object> getDocuments() {
        return graphNeo4jService.getGraphWithRedis();
    }

    @ToolMapping(name = "getAllGraph", description = "全部图谱")
    public Map<String, Object> getAllGraph() {
        return graphNeo4jService.getAllGraph();
    }

    @ToolMapping(name = "findPath", description = "查找两个节点之间的最短路径 BFS")
    public Map<String, Object> findPath(
            @Param(defaultValue = "KEYWORD", description = "枚举类 DOCUMENT / KEYWORD") String startType,
            @Param(description = "起始ID") Long startId,
            @Param(defaultValue = "KEYWORD", description = "枚举类 DOCUMENT / KEYWORD") String endType,
            @Param(description = "结束ID") Long endId
    ) {
        return pathService.getPath(startType, startId, endType, endId);
    }

    @ToolMapping(name = "getCommunityGraph", description = "基于 Louvain 算法")
    public Map<String, Object> getCommunityGraph() {
        return graphNeo4jService.getCommunityGraph();
    }

    @ToolMapping(name = "getPageRank", description = "基于图算法")
    public Map<String, Object> getPageRank(
            @Param(defaultValue = "KEYWORD", description = "默认对关键词跑 PageRank") String type,
            @Param(defaultValue = "20", description = "默认取前 20 个") int limit
    ) {
        return graphNeo4jService.getPageRank(type, limit);
    }


    @ToolMapping(name = "getMetrics", description = "获取图的基本指标")
    public Result<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = cacheService.getMetrics();
        return metrics != null ? Result.ok(metrics) : Result.cacheError("图异常尚未生成，请稍后再试");
    }

    @ToolMapping(name = "getAnomalies", description = "获取图的分析结果")
    public Result<Map<String, Object>> getAnomalies() {
        Map<String, Object> anomalies = cacheService.getAnomalies();
        return anomalies != null ? Result.ok(anomalies) : Result.cacheError("图异常尚未生成，请稍后再试");
    }

}
