package top.zfmx.neokgbackend.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphNode {
    private String id;      // 唯一 id
    private String label;   // 显示文字
    private Double size;
    private String color;
}
