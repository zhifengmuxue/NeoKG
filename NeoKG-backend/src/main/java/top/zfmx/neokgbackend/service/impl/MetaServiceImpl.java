package top.zfmx.neokgbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.mapper.EntityTypeMapper;
import top.zfmx.neokgbackend.mapper.RelationTypeMapper;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;
import top.zfmx.neokgbackend.service.MetaService;

import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class MetaServiceImpl implements MetaService {
    @Resource
    private EntityTypeMapper entityTypeMapper;

    @Resource
    private RelationTypeMapper relationTypeMapper;

    @Override
    public List<EntityType> listEntities() {
        return entityTypeMapper.selectList(null);
    }

    @Override
    public EntityType getEntity(Long id) {
        return entityTypeMapper.selectById(id);
    }

    @Override
    public void createEntity(EntityType entityType) {
        entityTypeMapper.insert(entityType);
    }

    @Override
    public void updateEntity(EntityType entityType) {
        entityTypeMapper.updateById(entityType);
    }

    @Override
    public void deleteEntity(Long id) {
        entityTypeMapper.deleteById(id);
    }

    @Override
    public List<RelationType> listRelations() {
        return relationTypeMapper.selectList(null);
    }

    @Override
    public RelationType getRelation(Long id) {
        return relationTypeMapper.selectById(id);
    }

    @Override
    public void createRelation(RelationType relationType) {
        relationTypeMapper.insert(relationType);
    }

    @Override
    public void updateRelation(RelationType relationType) {
        relationTypeMapper.updateById(relationType);
    }

    @Override
    public void deleteRelation(Long id) {
        relationTypeMapper.deleteById(id);
    }

    @Override
    public List<EntityType> selectEntityBatchIds(List<Long> entityTypeIds) {
        return entityTypeMapper.selectList(
                new LambdaQueryWrapper<EntityType>().in(EntityType::getId, entityTypeIds)
        );
    }

    @Override
    public List<RelationType> selectRelationBatchIds(List<Long> relationTypeIds) {
        return relationTypeMapper.selectList(
                new LambdaQueryWrapper<RelationType>().in(RelationType::getId, relationTypeIds)
        );
    }
}
