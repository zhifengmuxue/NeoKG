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
@TableName(value = "relation_instance",autoResultMap = true)
public class RelationInstance {

    @TableId
    private Long id;          // AI 返回的关系 id
    private Long relationTypeId; // 对应元模型 RelationType 的 id
    private String type;        // 关系类型名称
    private Long startEntityId; // 实例实体 id
    private Long endEntityId;   // 实例实体 id
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> properties; // JSON 存储关系属性
}
