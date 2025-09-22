package top.zfmx.neokgbackend.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;

import java.util.List;

/**
 * 元模型 DTO，用于前端传入用户自定义的实体类型和关系类型
 * @author li ma
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GraphMetaDTO {
    private List<Long> entityTypeIds;
    private List<Long> relationTypeIds;
}
