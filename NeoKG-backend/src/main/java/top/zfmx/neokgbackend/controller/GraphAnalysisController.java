package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zfmx.neokgbackend.response.Result;
import top.zfmx.neokgbackend.service.GraphAnalysisService;

import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/api/graph/analysis")
public class GraphAnalysisController {

    @Resource
    private GraphAnalysisService graphAnalysisService;

    /**
     * 图谱指标：密度、连通性、孤立节点比例
     */
    @GetMapping("/metrics")
    public Result<Map<String, Object>> getMetrics() {
        return Result.ok(graphAnalysisService.calculateMetrics());
    }

    /**
     * 图谱异常检测：无效关系链、孤立节点、环路、重复关系等
     */
    @GetMapping("/anomalies")
    public Result<Map<String, Object>> getAnomalies() {
        return Result.ok(graphAnalysisService.detectAnomalies());
    }
}


