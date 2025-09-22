package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;
import top.zfmx.neokgbackend.pojo.response.Result;
import top.zfmx.neokgbackend.service.MetaService;

import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/api/meta")
public class MetaController {
    @Resource
    private MetaService metaService;

    // ------------------- EntityType -------------------
    @GetMapping("/entities")
    public Result<List<EntityType>> listEntities() {
        return Result.ok(metaService.listEntities());
    }

    @GetMapping("/entities/{id}")
    public Result<EntityType> getEntity(@PathVariable Long id) {
        return Result.ok(metaService.getEntity(id));
    }

    @PostMapping("/entities")
    public Result<String> createEntity(@RequestBody EntityType entityType) {
        metaService.createEntity(entityType);
        return Result.ok("成功创建");
    }

    @PutMapping("/entities")
    public Result<String> updateEntity(@RequestBody EntityType entityType) {
        metaService.updateEntity(entityType);
        return Result.ok("成功更新");
    }

    @DeleteMapping("/entities/{id}")
    public Result<String> deleteEntity(@PathVariable Long id) {
        metaService.deleteEntity(id);
        return Result.ok("成功删除");
    }

    // ------------------- RelationType -------------------
    @GetMapping("/relations")
    public Result<List<RelationType>> listRelations() {
        return Result.ok(metaService.listRelations());
    }

    @GetMapping("/relations/{id}")
    public Result<RelationType> getRelation(@PathVariable Long id) {
        return Result.ok(metaService.getRelation(id));
    }

    @PostMapping("/relations")
    public Result<String> createRelation(@RequestBody RelationType relationType) {
        metaService.createRelation(relationType);
        return Result.ok("成功创建");
    }

    @PutMapping("/relations")
    public Result<String> updateRelation(@RequestBody RelationType relationType) {
        metaService.updateRelation(relationType);
        return Result.ok("成功更新");
    }

    @DeleteMapping("/relations/{id}")
    public Result<String> deleteRelation(@PathVariable Long id) {
        metaService.deleteRelation(id);
        return Result.ok("成功删除");
    }

}
