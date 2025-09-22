package top.zfmx.neokgbackend.pojo.dto;

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
@NoArgsConstructor
@AllArgsConstructor
public class Vec2D {
    private Long id;
    private String name;
    private double x;
    private double y;
}
