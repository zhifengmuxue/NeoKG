package top.zfmx.neokgbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.mapper.DocumentKeywordMapper;
import top.zfmx.neokgbackend.model.DocumentKeyword;
import top.zfmx.neokgbackend.service.DocumentKeywordService;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class DocumentKeywordServiceImpl
        extends ServiceImpl<DocumentKeywordMapper, DocumentKeyword> implements DocumentKeywordService {
}
