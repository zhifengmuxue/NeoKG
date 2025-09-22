package top.zfmx.neokgbackend.service;

import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;

import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface MetaService {
    List<EntityType> listEntities();
    EntityType getEntity(Long id);
    void createEntity(EntityType entityType);
    void updateEntity(EntityType entityType);
    void deleteEntity(Long id);

    List<RelationType> listRelations();
    RelationType getRelation(Long id);
    void createRelation(RelationType relationType);
    void updateRelation(RelationType relationType);
    void deleteRelation(Long id);

    List<EntityType> selectEntityBatchIds(List<Long> entityTypeIds);

    List<RelationType> selectRelationBatchIds(List<Long> relationTypeIds);
}
