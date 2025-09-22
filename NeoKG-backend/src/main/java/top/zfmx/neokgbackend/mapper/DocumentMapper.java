package top.zfmx.neokgbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.zfmx.neokgbackend.pojo.entity.Document;

import java.util.List;
import java.util.Vector;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Mapper
public interface DocumentMapper
        extends BaseMapper<Document> {

    @Select("SELECT * FROM document ORDER BY vec <-> " +
            "#{vec,typeHandler=top.zfmx.neokgbackend.handle.VectorTypeHandler}" +
            " LIMIT #{limit}")
    List<Document> searchByVector(@Param("vec") Vector<Float> vec, @Param("limit") int limit);

}
