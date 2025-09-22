package top.zfmx.neokgbackend.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import top.zfmx.neokgbackend.handle.StringListTypeHandler;
import top.zfmx.neokgbackend.handle.VectorTypeHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(autoResultMap = true)
public class Keyword {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> alias;

    @TableField(exist = false)
    DocumentRef ref;

    @TableField(typeHandler = VectorTypeHandler.class)
    private Vector<Float> vec;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 使用 embeddingModel 生成向量
     */
    public void generateEmbedding(EmbeddingModel embeddingModel) {
        String text = name + " " +
                String.join(" ", alias != null ? alias : List.of()) + " " +
                Optional.ofNullable(description).orElse("");

        float[] embedding = embeddingModel.embed(text);
        Vector<Float> vector = new Vector<>();
        for (float f : embedding) vector.add(f);
        this.vec = vector;
    }
}
