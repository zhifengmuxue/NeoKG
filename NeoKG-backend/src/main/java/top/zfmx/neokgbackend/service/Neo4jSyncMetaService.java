package top.zfmx.neokgbackend.service;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityInstance;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationInstance;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class Neo4jSyncMetaService {

    private final Driver driver;

    public Neo4jSyncMetaService(Driver driver) {
        this.driver = driver;
    }

    // 同步实体（批量事务）
    public void syncEntities(List<EntityInstance> entities) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                for (EntityInstance e : entities) {
                    tx.run(
                            "MERGE (n:" + e.getType() + " {id:$id}) " +
                                    "SET n += $props",
                            Map.of(
                                    "id", e.getId(),
                                    "props", e.getProperties() != null ? e.getProperties() : Map.of()
                            )
                    );
                }
                return null; // ✅ 这里必须返回 null
            });
        }
    }

    // 同步关系（批量事务）
    public void syncRelations(List<RelationInstance> relations) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                for (RelationInstance r : relations) {
                    tx.run(
                            "MATCH (a {id:$startId}), (b {id:$endId}) " +
                                    "MERGE (a)-[rel:" + r.getType() + "]->(b) " +
                                    "SET rel += $props",
                            Map.of(
                                    "startId", r.getStartEntityId(),
                                    "endId", r.getEndEntityId(),
                                    "props", r.getProperties() != null ? r.getProperties() : Map.of()
                            )
                    );
                }
                return null; // ✅ 这里必须返回 null
            });
        }
    }

    // 一次同步整个图
    public void syncGraph(List<EntityInstance> entities, List<RelationInstance> relations) {
        syncEntities(entities);
        syncRelations(relations);
    }
}
