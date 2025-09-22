package top.zfmx.neokgbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.Resource;
import org.apache.tika.exception.TikaException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.enums.MatchMode;
import top.zfmx.neokgbackend.pojo.dto.ExtractedGraph;
import top.zfmx.neokgbackend.pojo.dto.GraphMetaDTO;
import top.zfmx.neokgbackend.pojo.entity.Document;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;
import top.zfmx.neokgbackend.pojo.response.Result;
import top.zfmx.neokgbackend.service.DataImportMetaService;
import top.zfmx.neokgbackend.service.MetaService;

import java.io.IOException;
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

    @Resource
    private DataImportMetaService dataImportMetaService;
    /**
     * 上传文件并解析成知识图谱
     * - file: 支持 csv, md, markdown, doc, docx, pdf
     * - meta: 元模型（包含实体类型 + 关系类型）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ExtractedGraph> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("meta") String metaStr
    ) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        GraphMetaDTO meta = mapper.readValue(metaStr, GraphMetaDTO.class);

        // 根据 ID 查完整的元模型
        List<EntityType> entityTypes = metaService.selectEntityBatchIds(meta.getEntityTypeIds());
        List<RelationType> relationTypes = metaService.selectRelationBatchIds(meta.getRelationTypeIds());

        String filename = file.getOriginalFilename();
        if (filename == null) throw new RuntimeException("文件名不能为空");

        ExtractedGraph graph=dataImportMetaService.parseFile(file, entityTypes, relationTypes);

        return Result.ok(graph);
    }


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
