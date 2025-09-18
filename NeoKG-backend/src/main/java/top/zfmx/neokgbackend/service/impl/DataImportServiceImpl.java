package top.zfmx.neokgbackend.service.impl;

import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.Resource;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.model.Document;
import top.zfmx.neokgbackend.service.DataImportService;
import top.zfmx.neokgbackend.service.KeywordsAiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class DataImportServiceImpl implements DataImportService {

    @Resource
    private KeywordsAiService keywordsAiService;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public List<Document> parseCsvToDocuments(MultipartFile file) throws IOException {
        // 读取 CSV 内容作为纯文本
        StringBuilder textBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                textBuilder.append(line).append("\n");
            }
        }
        // 调用 AiService 获取 JSON 文本
        String json = keywordsAiService.explain(textBuilder.toString());

        if (json.startsWith("```")) {
            json = json.replaceAll("```(json)?", "").trim();
        }

        // 解析 JSON 为 Document 对象列表
        return objectMapper.readValue(json, new TypeReference<>() {
        });
    }

    @Override
    public List<Document> parseMarkdownToDocuments(MultipartFile file) throws IOException {
        // 读取 Markdown 内容
        StringBuilder textBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                textBuilder.append(line).append("\n");
            }
        }

        // 调用 AiService 获取 JSON 文本
        String json = keywordsAiService.explain(textBuilder.toString());

        if (json.startsWith("```")) {
            json = json.replaceAll("```(json)?", "").trim();
        }

        // 解析 JSON 为 Document 对象列表
        return objectMapper.readValue(json, new TypeReference<>() {
        });
    }

    @Override
    public List<Document> parseWordToDocuments(MultipartFile file) throws IOException, TikaException {
        // 使用 Apache Tika 提取 Word 文本
        org.apache.tika.Tika tika = new org.apache.tika.Tika();
        String text = tika.parseToString(file.getInputStream());

        // 调用 AiService 获取 JSON 文本
        String json = keywordsAiService.explain(text);

        if (json.startsWith("```")) {
            json = json.replaceAll("```(json)?", "").trim();
        }

        // 解析 JSON 为 Document 对象列表
        return objectMapper.readValue(json, new TypeReference<>() {
        });
    }
}
