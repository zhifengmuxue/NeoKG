package top.zfmx.neokgbackend.pojo.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Data
@Node
public class DocumentNode {
    @Id
    private String id; // 外部生成 ID
    private String title;

    private Long version;

    private Long docId;

    @Relationship(type = "HAS_KEYWORD")
    private Set<KeywordNode> keywords = new HashSet<>();

    public DocumentNode() {
        this.id = UUID.randomUUID().toString();
    }
}
