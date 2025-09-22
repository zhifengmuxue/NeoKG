package top.zfmx.neokgbackend.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.zfmx.neokgbackend.mapper.KeywordMapper;
import top.zfmx.neokgbackend.pojo.entity.Keyword;
import top.zfmx.neokgbackend.service.KeywordService;
import top.zfmx.neokgbackend.utils.KeywordMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class KeywordServiceImpl
        extends ServiceImpl<KeywordMapper, Keyword> implements KeywordService {

    @Resource
    private KeywordMapper keywordMapper;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private Snowflake snowflake;

    @Value("${threshold.vector-similarity}")
    private Double DEFAULT_SIMILAR_RATIO;

    @Override
    public List<Keyword> findAllKeywords() {
        return keywordMapper.selectList(null);
    }

    @Override
    public IPage<Keyword> findAllKeywordsPage(int pageNum, int pageSize) {
        Page<Keyword> page = new Page<>(pageNum, pageSize);
        return keywordMapper.selectPage(page, new QueryWrapper<>());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithVec(Keyword kw) {
        // 1️⃣ 预加载已有关键词（可带向量）
        List<Keyword> allKeywords = keywordMapper.listKeywordWithNullVec();

        // 2️⃣ 先字符串匹配
        Keyword matchedKeyword = KeywordMatcher.findByName(allKeywords, kw);

        // 3️⃣ 如果没有命中，再语义匹配
        if (matchedKeyword == null) {
            // 先生成 embedding
            kw.generateEmbedding(embeddingModel);

            // 再语义匹配
            matchedKeyword = KeywordMatcher.findMostSimilar(allKeywords, kw, kw.getVec(), DEFAULT_SIMILAR_RATIO);
        }

        // 4️⃣ 处理结果
        if (matchedKeyword != null) {
            // 找到 → 复用已有关键词
            kw.setId(matchedKeyword.getId());
            kw.setVec(matchedKeyword.getVec());
            return true; // 不重复保存
        } else {
            // 没找到 → 新建
            kw.setId(snowflake.nextId());
            return this.save(kw);
        }
    }

    @Override
    public boolean updateByIdWithVec(Keyword kw) {
        kw.generateEmbedding(embeddingModel);
        return this.updateById(kw);
    }

}
