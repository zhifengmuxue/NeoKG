package top.zfmx.neokgbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import top.zfmx.neokgbackend.pojo.entity.Keyword;

import java.util.List;
import java.util.Vector;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Mapper
public interface KeywordMapper
        extends BaseMapper<Keyword> {
    @Select("SELECT * FROM keyword ORDER BY vec <-> #{vec,typeHandler=top.zfmx.neokgbackend.handle.VectorTypeHandler} LIMIT #{limit}")
    List<Keyword> searchByVector(@Param("vec") Vector<Float> vec, @Param("limit") int limit);

    @Select("SELECT id, name, description, alias, vec " +
            "FROM keyword " +
            "WHERE vec <-> #{embedding, typeHandler=top.zfmx.neokgbackend.handle.VectorTypeHandler} <= #{threshold} " +
            "ORDER BY vec <-> #{embedding, typeHandler=top.zfmx.neokgbackend.handle.VectorTypeHandler} " +
            "LIMIT 1")
    Keyword findMostSimilar(
            @Param("embedding") Vector<Float> embedding,
            @Param("threshold") double threshold
    );

    @Select("SELECT id, name, description, alias, vec, created_at, updated_at FROM keyword")
    List<Keyword> listKeywordWithNullVec();


}
