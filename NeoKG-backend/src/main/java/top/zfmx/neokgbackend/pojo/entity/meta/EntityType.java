package top.zfmx.neokgbackend.pojo.entity.meta;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zfmx.neokgbackend.handle.StringListTypeHandler;

import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(autoResultMap = true)
public class EntityType {
    private Long id;
    private String name;
    private String description;
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> properties;
}
