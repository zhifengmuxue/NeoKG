package top.zfmx.neokgbackend.scheduler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.zfmx.neokgbackend.service.GraphAnalysisService;
import top.zfmx.neokgbackend.service.GraphMetricsCacheService;

import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Slf4j
@Component
public class GraphMetricsScheduler {

    @Resource
    private GraphAnalysisService graphAnalysisService;

    @Resource
    private GraphMetricsCacheService cacheService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void refreshMetrics() {
        try {
            log.info("开始刷新图指标...");

            Map<String, Object> metrics = graphAnalysisService.calculateMetrics();
            cacheService.cacheMetrics(metrics);
            log.info("指标刷新完成：{}", metrics);

            Map<String, Object> anomalies = graphAnalysisService.detectAnomalies();
            cacheService.cacheAnomalies(anomalies);
            log.info("异常检测完成：{}", anomalies);
        } catch (Exception e) {
            log.error("刷新图指标失败：{}", e.getMessage(), e);
        }
    }

    @PostConstruct
    public void init() {
        try {
            log.info("GraphMetricsScheduler 初始化执行...");
            refreshMetrics();
        } catch (Exception e) {
            log.error("GraphMetricsScheduler 初始化失败，进入降级模式：{}", e.getMessage());
        }
    }
}


