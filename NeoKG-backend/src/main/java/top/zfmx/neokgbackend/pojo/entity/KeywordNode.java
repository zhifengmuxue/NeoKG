package top.zfmx.neokgbackend.pojo.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@Node
public class KeywordNode {

    @Id
    private String id; // 外部生成 ID

    private Long version;
    private Long keywordId;
    private String name;
    public KeywordNode() {
        this.id = UUID.randomUUID().toString();
    }
}
