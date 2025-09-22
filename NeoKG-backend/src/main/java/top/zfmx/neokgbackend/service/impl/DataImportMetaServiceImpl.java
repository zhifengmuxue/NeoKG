package top.zfmx.neokgbackend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.pojo.dto.ExtractedEntity;
import top.zfmx.neokgbackend.pojo.dto.ExtractedGraph;
import top.zfmx.neokgbackend.pojo.dto.ExtractedRelation;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;
import top.zfmx.neokgbackend.service.AiService;
import top.zfmx.neokgbackend.service.DataImportMetaService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class DataImportMetaServiceImpl implements DataImportMetaService {
    @Resource
    private AiService aiService;
    @Resource
    private ObjectMapper objectMapper;

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

    /**
     * 公共方法：AI 调用 + JSON 清理 + 归一化成 ExtractedGraph
     */
    private ExtractedGraph callAiAndParse(String content,
                                          List<EntityType> entityTypes,
                                          List<RelationType> relationTypes) throws IOException {
        String rawJson = aiService.explainMeta(content, entityTypes, relationTypes);
        String cleanJson = cleanJson(rawJson);
        System.out.println("AI 返回 JSON:\n" + cleanJson);

        Map<String, Object> root = objectMapper.readValue(cleanJson, new TypeReference<Map<String, Object>>() {});

        // 解析 entities
        List<Map<String, Object>> rawEntities = (List<Map<String, Object>>) root.getOrDefault("entities", Collections.emptyList());
        List<ExtractedEntity> entities = new ArrayList<>();
        for (Map<String, Object> re : rawEntities) {
            String id = re.get("id") == null ? UUID.randomUUID().toString() : String.valueOf(re.get("id"));
            String type = re.get("type") == null ? null : String.valueOf(re.get("type"));

            Map<String, Object> props = new LinkedHashMap<>();
            Object propObj = re.get("properties");
            if (propObj instanceof Map) {
                props.putAll((Map<String, Object>) propObj);
            }
            for (Map.Entry<String, Object> e : re.entrySet()) {
                String k = e.getKey();
                if ("id".equals(k) || "type".equals(k) || "properties".equals(k)) continue;
                props.put(k, e.getValue());
            }

            entities.add(new ExtractedEntity(id, type, props));
        }

        // 解析 relations
        List<Map<String, Object>> rawRelations = (List<Map<String, Object>>) root.getOrDefault("relations", Collections.emptyList());
        List<ExtractedRelation> relations = new ArrayList<>();
        for (Map<String, Object> rr : rawRelations) {
            String id = rr.get("id") == null ? UUID.randomUUID().toString() : String.valueOf(rr.get("id"));
            String type = rr.get("type") == null ? null : String.valueOf(rr.get("type"));

            String startId = extractEntityId(rr.get("startEntityId"), rr.get("startEntity"));
            String endId = extractEntityId(rr.get("endEntityId"), rr.get("endEntity"));

            Map<String, Object> rprops = new LinkedHashMap<>();
            Object rpropsObj = rr.get("properties");
            if (rpropsObj instanceof Map) {
                rprops.putAll((Map<String, Object>) rpropsObj);
            }
            for (Map.Entry<String, Object> e : rr.entrySet()) {
                String k = e.getKey();
                if (Set.of("id","type","startEntity","endEntity","startEntityId","endEntityId","properties").contains(k)) continue;
                rprops.put(k, e.getValue());
            }

            relations.add(new ExtractedRelation(id, type, startId, endId, rprops));
        }

        return new ExtractedGraph(entities, relations);
    }

    private String extractEntityId(Object idField, Object entityField) {
        if (idField != null) return String.valueOf(idField);
        if (entityField instanceof Map) {
            Object id = ((Map<?, ?>) entityField).get("id");
            return id == null ? null : String.valueOf(id);
        }
        if (entityField != null) return String.valueOf(entityField);
        return null;
    }

    /**
     * 读取 CSV 跳过首行（表头）
     */
    private String readCsvContent(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // 跳过表头
                    continue;
                }
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 读取 Markdown / 普通文本
     */
    private String readPlainText(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 去掉 ```json 包裹
     */
    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        if (raw.startsWith("```")) {
            return raw.replaceAll("```(json)?", "").trim();
        }
        return raw.trim();
    }

    @Override
    public ExtractedGraph parseFile(MultipartFile file,
                                    List<EntityType> entityTypes,
                                    List<RelationType> relationTypes) throws IOException, TikaException {
        String filename = file.getOriginalFilename();
        if (filename == null) throw new RuntimeException("文件名为空");

        if (filename.endsWith(".csv")) {
            return parseCsv(file, entityTypes, relationTypes);
        } else if (filename.endsWith(".md") || filename.endsWith(".markdown")) {
            return parseMarkdown(file, entityTypes, relationTypes);
        } else if (filename.endsWith(".doc") || filename.endsWith(".docx") || filename.endsWith(".pdf")) {
            return parseWordOrPdf(file, entityTypes, relationTypes);
        } else {
            throw new RuntimeException("不支持的文件类型: " + filename);
        }
    }
}
