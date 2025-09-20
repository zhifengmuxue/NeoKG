package top.zfmx.neokgbackend.model;

import lombok.Data;
import org.springframework.data.annotation.Version;
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
    private String id = UUID.randomUUID().toString(); // 外部生成 ID
    @Version
    private Long version;   // SDN 自动维护
    private Long keywordId;
    private String name;
}
