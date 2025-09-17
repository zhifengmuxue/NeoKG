package top.zfmx.neokgbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.mapper.DocumentMapper;
import top.zfmx.neokgbackend.model.Document;
import top.zfmx.neokgbackend.service.DocumentService;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class DocumentServiceImpl
        extends ServiceImpl<DocumentMapper, Document> implements DocumentService {
}
