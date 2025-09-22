package top.zfmx.neokgbackend.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Graph {
    private Long id;
    private Long useId;
    private String name;
    private String description;
}
