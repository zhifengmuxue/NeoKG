package top.zfmx.neokgbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import top.zfmx.neokgbackend.pojo.dto.Vec2D;
import top.zfmx.neokgbackend.pojo.response.Result;
import top.zfmx.neokgbackend.service.DimReduceService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 降维控制器
 * 提供全量降维和查询 Redis 中的结果
 *
 * @author li ma
 * @version 0.0.1
 */
@RestController
@RequestMapping("/api/dim")
public class DimReduceController {

    @Resource
    private DimReduceService dimReduceService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String REDIS_KEY = "keyword:vec:2d";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 手动触发全量降维并替换 Redis
     */
    @PostMapping("/replaceAll")
    public Result<String> replaceAll() throws JsonProcessingException {
        dimReduceService.reduceAndReplaceAll();
        return Result.ok("Redis 已成功替换为最新降维结果");
    }

    /**
     * 获取 Redis 中所有降维后的结果
     */
    @GetMapping("/all")
    public Result<List<Vec2D>> getAll() {
        Map<Object, Object> redisData = stringRedisTemplate.opsForHash().entries(REDIS_KEY);

        List<Vec2D> result = redisData.values().stream()
                .map(v -> {
                    try {
                        return objectMapper.readValue(v.toString(), Vec2D.class);
                    } catch (Exception e) {
                        throw new RuntimeException("解析 Redis 中的数据失败: " + v, e);
                    }
                })
                .collect(Collectors.toList());

        return Result.ok(result);
    }
}
