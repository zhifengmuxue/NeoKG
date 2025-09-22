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
public class ExtractedEntity {
    private Long id;
    private String type;
    private Map<String, Object> properties;
}
