package top.zfmx.neokgbackend.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.mapper.KeywordMapper;
import top.zfmx.neokgbackend.model.Keyword;
import top.zfmx.neokgbackend.service.KeywordService;

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

    @Override
    public List<Keyword> findAllKeywords() {
        return keywordMapper.findAllKeywords();
    }

    @Override
    public IPage<Keyword> findAllKeywordsPage(int pageNum, int pageSize) {
        Page<Keyword> page = new Page<>(pageNum, pageSize);
        return keywordMapper.selectPage(page, new QueryWrapper<>());
    }

    @Override
    public boolean saveWithVec(Keyword kw) {
        float[] kwEmbedding = embeddingModel.embed(
                kw.getName() + " " +
                        String.join(" ", kw.getAlias() != null ? kw.getAlias() : List.of()) + " " +
                        Optional.ofNullable(kw.getDescription()).orElse("")
        );
        kw.generateEmbedding(embeddingModel);
        kw.setId(snowflake.nextId());
        return this.save(kw);
    }
}
