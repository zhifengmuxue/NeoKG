package top.zfmx.neokgbackend.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zfmx.neokgbackend.handle.VectorTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Vector;

/**
* 
* @author li ma
* @version 0.0.1
**/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    @TableId
    private Long id;
    private String title;
    private String content;

    @TableField(typeHandler = VectorTypeHandler.class)
    private Vector<Float> vec;
    @TableField(exist = false)
    private List<Keyword> keywords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
