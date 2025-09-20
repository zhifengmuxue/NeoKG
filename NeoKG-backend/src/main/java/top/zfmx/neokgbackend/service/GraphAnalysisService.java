package top.zfmx.neokgbackend.service;

import java.util.List;
import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface GraphAnalysisService {
    Map<String, Object> calculateMetrics();
    Map<String, Object> detectAnomalies();
}
