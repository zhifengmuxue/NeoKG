package top.zfmx.neokgbackend.model;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zfmx.neokgbackend.handle.StringListTypeHandler;
import top.zfmx.neokgbackend.handle.VectorTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Vector;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Keyword {
    private Long id;
    private String name;
    private String description;
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> alias;
    @TableField(exist = false)
    DocumentRef ref;
    @TableField(typeHandler = VectorTypeHandler.class)
    private Vector<Float> vec;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
