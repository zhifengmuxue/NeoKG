package top.zfmx.neokgbackend.model;

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
public class GraphEdge {
    private String source;  // 起始节点 id
    private String target;  // 目标节点 id
}