package top.zfmx.neokgbackend.service;

import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface GraphMetricsCacheService {
    void cacheMetrics(Map<String, Object> metrics);

    Map<String, Object> getMetrics();

    void cacheAnomalies(Map<String, Object> anomalies);

    Map<String, Object> getAnomalies();
}
