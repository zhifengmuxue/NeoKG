package top.zfmx.neokgbackend.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.mapper.KeywordMapper;
import top.zfmx.neokgbackend.model.Keyword;
import top.zfmx.neokgbackend.service.KeywordService;

import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class KeywordServiceImpl
        extends ServiceImpl<KeywordMapper, Keyword> implements KeywordService {

    @Resource
    private KeywordMapper keywordMapper;

    @Override
    public List<Keyword> findAllKeywords() {
        return keywordMapper.findAllKeywords();
    }
}
