package top.zfmx.neokgbackend.pojo.entity.meta;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zfmx.neokgbackend.handle.JsonbTypeHandler;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "entity_instance",autoResultMap = true)
public class EntityInstance {

    @TableId
    private Long id;          // AI 返回的 id，也可以自增主键
    private Long entityTypeId;  // 对应元模型 EntityType 的 id
    private String type;        // 实体类型名称
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> properties; // JSON 存储其他属性
}
