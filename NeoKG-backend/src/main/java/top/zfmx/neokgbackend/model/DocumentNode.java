package top.zfmx.neokgbackend.model;

import lombok.Data;
import org.springframework.data.annotation.Version;
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
    private String id = UUID.randomUUID().toString(); // 外部生成 ID
    private String title;
    @Version
    private Long version;   // SDN 自动维护

    private Long docId;

    @Relationship(type = "HAS_KEYWORD")
    private Set<KeywordNode> keywords = new HashSet<>();
}
