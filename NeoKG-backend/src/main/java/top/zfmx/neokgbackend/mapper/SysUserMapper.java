package top.zfmx.neokgbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jakarta.validation.constraints.NotBlank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.zfmx.neokgbackend.pojo.entity.SysUser;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
