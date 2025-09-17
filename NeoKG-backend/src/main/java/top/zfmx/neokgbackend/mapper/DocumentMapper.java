package top.zfmx.neokgbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.zfmx.neokgbackend.model.Document;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Mapper
public interface DocumentMapper
        extends BaseMapper<Document> {
}
