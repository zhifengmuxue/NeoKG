package top.zfmx.neokgbackend.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExtractedRelation {
    private String id;         // 唯一标识
    private String type;       // 关系类型，对应 RelationType.name
    private String startEntity;// 起点实体 ID
    private String endEntity;  // 终点实体 ID
    private Map<String, Object> properties; // 动态属性
}

