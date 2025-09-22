package top.zfmx.neokgbackend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.service.GraphMetricsCacheService;

import java.util.Map;

/**
 * GraphMetricsCacheServiceImpl
 * 使用 JSON 序列化存储 Redis 数据，避免类型转换警告
 * @author li ma
 * @version 0.0.2
 **/
@Service
public class GraphMetricsCacheServiceImpl implements GraphMetricsCacheService {

    private static final String METRICS_KEY = "graph:metrics";
    private static final String ANOMALIES_KEY = "graph:anomalies";

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void cacheMetrics(Map<String, Object> metrics) {
        try {
            String json = objectMapper.writeValueAsString(metrics);
            redisTemplate.opsForValue().set(METRICS_KEY, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize metrics to JSON", e);
        }
    }

    @Override
    public Map<String, Object> getMetrics() {
        String data = redisTemplate.opsForValue().get(METRICS_KEY);
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize metrics from JSON", e);
        }
    }

    @Override
    public void cacheAnomalies(Map<String, Object> anomalies) {
        try {
            String json = objectMapper.writeValueAsString(anomalies);
            redisTemplate.opsForValue().set(ANOMALIES_KEY, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize anomalies to JSON", e);
        }
    }

    @Override
    public Map<String, Object> getAnomalies() {
        String data = redisTemplate.opsForValue().get(ANOMALIES_KEY);
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize anomalies from JSON", e);
        }
    }
}
