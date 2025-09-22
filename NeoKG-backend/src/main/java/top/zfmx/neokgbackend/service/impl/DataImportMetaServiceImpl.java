package top.zfmx.neokgbackend.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.mapper.EntityInstanceMapper;
import top.zfmx.neokgbackend.mapper.RelationInstanceMapper;
import top.zfmx.neokgbackend.pojo.dto.ExtractedEntity;
import top.zfmx.neokgbackend.pojo.dto.ExtractedGraph;
import top.zfmx.neokgbackend.pojo.dto.ExtractedRelation;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityInstance;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationInstance;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;
import top.zfmx.neokgbackend.service.AiService;
import top.zfmx.neokgbackend.service.DataImportMetaService;
import top.zfmx.neokgbackend.service.Neo4jSyncMetaService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class DataImportMetaServiceImpl implements DataImportMetaService {

    @Resource
    private AiService aiService;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private Snowflake snowflake;
    @Resource
    private EntityInstanceMapper entityInstanceMapper;
    @Resource
    private RelationInstanceMapper relationInstanceMapper;
    @Resource
    private Neo4jSyncMetaService neo4jSyncMetaService;

    @Override
    public ExtractedGraph parseCsv(MultipartFile file,
                                   List<EntityType> entityTypes,
                                   List<RelationType> relationTypes) throws IOException {
        String content = readCsvContent(file);
        return callAiAndParse(content, entityTypes, relationTypes);
    }

    @Override
    public ExtractedGraph parseMarkdown(MultipartFile file,
                                        List<EntityType> entityTypes,
                                        List<RelationType> relationTypes) throws IOException {
        String content = readPlainText(file);
        return callAiAndParse(content, entityTypes, relationTypes);
    }

    @Override
    public ExtractedGraph parseWordOrPdf(MultipartFile file,
                                         List<EntityType> entityTypes,
                                         List<RelationType> relationTypes) throws IOException, TikaException {
        org.apache.tika.Tika tika = new org.apache.tika.Tika();
        String content = tika.parseToString(file.getInputStream());
        return callAiAndParse(content, entityTypes, relationTypes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExtractedGraph parseFile(MultipartFile file,
                                    List<EntityType> entityTypes,
                                    List<RelationType> relationTypes) throws IOException, TikaException {
        String filename = file.getOriginalFilename();
        if (filename == null) throw new RuntimeException("文件名为空");

        ExtractedGraph graph;
        if (filename.endsWith(".csv")) {
            graph = parseCsv(file, entityTypes, relationTypes);
        } else if (filename.endsWith(".md") || filename.endsWith(".markdown")) {
            graph = parseMarkdown(file, entityTypes, relationTypes);
        } else if (filename.endsWith(".doc") || filename.endsWith(".docx") || filename.endsWith(".pdf")) {
            graph = parseWordOrPdf(file, entityTypes, relationTypes);
        } else {
            throw new RuntimeException("不支持的文件类型: " + filename);
        }

        // 持久化到数据库
        persistExtractedGraph(graph, entityTypes, relationTypes);

        return graph;
    }

    /**
     * AI 调用 + JSON 解析 + 类型安全 Long ID
     */
    private ExtractedGraph callAiAndParse(String content,
                                          List<EntityType> entityTypes,
                                          List<RelationType> relationTypes) throws IOException {
        String rawJson = aiService.explainMeta(content, entityTypes, relationTypes);
        String cleanJson = cleanJson(rawJson);

        Map<String, Object> root = objectMapper.readValue(cleanJson, new TypeReference<Map<String, Object>>() {});

        List<Map<String, Object>> rawEntities = (List<Map<String, Object>>) root.getOrDefault("entities", Collections.emptyList());
        List<ExtractedEntity> entities = new ArrayList<>();
        for (Map<String, Object> re : rawEntities) {
            Long id = parseLongId(re.get("id"));
            String type = re.get("type") == null ? null : String.valueOf(re.get("type"));

            Map<String, Object> props = new LinkedHashMap<>();
            Object propObj = re.get("properties");
            if (propObj instanceof Map) props.putAll((Map<String, Object>) propObj);
            for (Map.Entry<String, Object> e : re.entrySet()) {
                String k = e.getKey();
                if ("id".equals(k) || "type".equals(k) || "properties".equals(k)) continue;
                props.put(k, e.getValue());
            }

            entities.add(new ExtractedEntity(id, type, props));
        }

        List<Map<String, Object>> rawRelations = (List<Map<String, Object>>) root.getOrDefault("relations", Collections.emptyList());
        List<ExtractedRelation> relations = new ArrayList<>();
        for (Map<String, Object> rr : rawRelations) {
            Long id = parseLongId(rr.get("id"));
            String type = rr.get("type") == null ? null : String.valueOf(rr.get("type"));
            Long startId = extractEntityId(rr.get("startEntityId"), rr.get("startEntity"));
            Long endId = extractEntityId(rr.get("endEntityId"), rr.get("endEntity"));

            Map<String, Object> rprops = new LinkedHashMap<>();
            Object rpropsObj = rr.get("properties");
            if (rpropsObj instanceof Map) rprops.putAll((Map<String, Object>) rpropsObj);
            for (Map.Entry<String, Object> e : rr.entrySet()) {
                String k = e.getKey();
                if (Set.of("id","type","startEntity","endEntity","startEntityId","endEntityId","properties").contains(k)) continue;
                rprops.put(k, e.getValue());
            }

            relations.add(new ExtractedRelation(id, type, startId, endId, rprops));
        }

        return new ExtractedGraph(entities, relations);
    }

    private Long parseLongId(Object rawId) {
        if (rawId == null) return snowflake.nextId();
        if (rawId instanceof Number) return ((Number) rawId).longValue();
        try {
            return Long.parseLong(rawId.toString());
        } catch (NumberFormatException e) {
            return snowflake.nextId();
        }
    }

    private Long extractEntityId(Object idField, Object entityField) {
        Object raw = idField != null ? idField : entityField instanceof Map ? ((Map<?, ?>) entityField).get("id") : entityField;
        return parseLongId(raw);
    }


    private void persistExtractedGraph(ExtractedGraph graph,
                                       List<EntityType> entityTypes,
                                       List<RelationType> relationTypes) {

        Map<String, Long> typeNameToId = new HashMap<>();
        for (EntityType et : entityTypes) typeNameToId.put(et.getName(), et.getId());
        Map<String, Long> relationNameToId = new HashMap<>();
        for (RelationType rt : relationTypes) relationNameToId.put(rt.getName(), rt.getId());

        List<EntityInstance> entityInstances = new ArrayList<>();
        List<RelationInstance> relationInstances = new ArrayList<>();

        // 实体入库
        for (ExtractedEntity e : graph.getEntities()) {
            Long id = snowflake.nextId();
            EntityInstance ei = EntityInstance.builder()
                    .id(id)
                    .entityTypeId(typeNameToId.get(e.getType()))
                    .type(e.getType())
                    .properties(e.getProperties())
                    .build();
            entityInstanceMapper.insert(ei);
            entityInstances.add(ei); // 保存到列表，准备同步 Neo4j
        }

        // 关系入库
        for (ExtractedRelation r : graph.getRelations()) {
            Long id = snowflake.nextId();
            RelationInstance ri = RelationInstance.builder()
                    .id(id)
                    .relationTypeId(relationNameToId.get(r.getType()))
                    .type(r.getType())
                    .startEntityId(r.getStartEntityId())
                    .endEntityId(r.getEndEntityId())
                    .properties(r.getProperties())
                    .build();
            relationInstanceMapper.insert(ri);
            relationInstances.add(ri); // 保存到列表，准备同步 Neo4j
        }

        // PostgreSQL 入库完成后立即同步 Neo4j
        neo4jSyncMetaService.syncGraph(entityInstances, relationInstances);
    }



    private String readCsvContent(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private String readPlainText(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        if (raw.startsWith("```")) return raw.replaceAll("```(json)?", "").trim();
        return raw.trim();
    }
}
