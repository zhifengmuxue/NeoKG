package top.zfmx.neokgbackend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import smile.feature.extraction.PCA;
import top.zfmx.neokgbackend.mapper.KeywordMapper;
import top.zfmx.neokgbackend.pojo.entity.Keyword;
import top.zfmx.neokgbackend.pojo.dto.Vec2D;
import top.zfmx.neokgbackend.service.DimReduceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class DimReduceServiceImpl implements DimReduceService {

    @Resource
    private KeywordMapper keywordMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String REDIS_KEY = "keyword:vec:2d";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 全量降维并替换Redis
     */
    @Override
    public void reduceAndReplaceAll() {
        List<Keyword> keywords = keywordMapper.selectList(null);

        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        // 转换 Vector<Float> -> double[]
        double[][] vectors = keywords.stream()
                .map(Keyword::getVec)
                .filter(Objects::nonNull)
                .map(vec -> vec.stream().mapToDouble(Float::doubleValue).toArray())
                .toArray(double[][]::new);

        if (vectors.length == 0) {
            throw new RuntimeException("没有有效的向量数据，无法进行 PCA");
        }

        // PCA 降到 2 维
        var pca = PCA.fit(vectors);
        var projection = pca.getProjection(2);
        double[][] reduced = projection.apply(vectors);

        // 清空旧数据
        stringRedisTemplate.delete(REDIS_KEY);

        // 写入新数据
        Map<String, String> redisData = new HashMap<>();
        for (int i = 0; i < reduced.length; i++) {
            Keyword keyword = keywords.get(i);
            if (keyword.getVec() == null) continue; // 跳过无效 vec

            double[] coords = reduced[i];
            Vec2D dto = Vec2D.builder()
                    .id(keyword.getId())
                    .name(keyword.getName())
                    .x(coords[0])
                    .y(coords[1])
                    .build();
            try {
                redisData.put(keyword.getId().toString(), objectMapper.writeValueAsString(dto));
            } catch (Exception e) {
                throw new RuntimeException("序列化 Vec2D 失败: " + keyword.getId(), e);
            }
        }
        stringRedisTemplate.opsForHash().putAll(REDIS_KEY, redisData);
    }
}
