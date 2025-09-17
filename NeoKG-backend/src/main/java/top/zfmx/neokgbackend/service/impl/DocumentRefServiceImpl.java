package top.zfmx.neokgbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.mapper.DocumentRefMapper;
import top.zfmx.neokgbackend.model.DocumentRef;
import top.zfmx.neokgbackend.service.DocumentRefService;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class DocumentRefServiceImpl
        extends ServiceImpl<DocumentRefMapper, DocumentRef> implements DocumentRefService {
}
