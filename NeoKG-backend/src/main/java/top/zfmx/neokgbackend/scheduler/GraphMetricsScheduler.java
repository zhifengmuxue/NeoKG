package top.zfmx.neokgbackend.scheduler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.zfmx.neokgbackend.service.GraphAnalysisService;
import top.zfmx.neokgbackend.service.GraphMetricsCacheService;

import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Component
public class GraphMetricsScheduler {

    @Resource
    private GraphAnalysisService graphAnalysisService;

    @Resource
    private GraphMetricsCacheService cacheService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void refreshMetrics() {
        Map<String, Object> metrics = graphAnalysisService.calculateMetrics();
        cacheService.cacheMetrics(metrics);

        Map<String, Object> anomalies = graphAnalysisService.detectAnomalies();
        cacheService.cacheAnomalies(anomalies);
    }
    @PostConstruct
    public void init() {
        refreshMetrics();
    }
}

