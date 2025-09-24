package top.zfmx.neokgbackend.service.impl;

import jakarta.annotation.Resource;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.pojo.entity.Document;
import top.zfmx.neokgbackend.service.AiService;
import top.zfmx.neokgbackend.service.DataImportService;
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
    private AiService aiService;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 公共方法：AI 转换 JSON -> Document 列表
     */
    private List<Document> convertToDocuments(String text) throws IOException {
        String json = aiService.explain(text);
        if (json.startsWith("```")) {
            json = json.replaceAll("```(json)?", "").trim();
        }
        return objectMapper.readValue(json, new TypeReference<>() {});
    }

    /**
     * 公共方法：读取 MultipartFile 中的文本
     */
    private String readFileAsString(MultipartFile file, boolean skipFirstLine) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (skipFirstLine && firstLine) {
                    firstLine = false;
                    continue;
                }
                textBuilder.append(line).append("\n");
            }
        }
        return textBuilder.toString();
    }

    @Override
    public List<Document> parseCsvToDocuments(MultipartFile file) throws IOException {
        String text = readFileAsString(file, true); // 跳过第一行表头
        return convertToDocuments(text);
    }

    @Override
    public List<Document> parseMarkdownToDocuments(MultipartFile file) throws IOException {
        String text = readFileAsString(file, false);
        return convertToDocuments(text);
    }

    @Override
    public List<Document> parseWordToDocuments(MultipartFile file) throws IOException, TikaException {
        org.apache.tika.Tika tika = new org.apache.tika.Tika();
        String text = tika.parseToString(file.getInputStream());
        return convertToDocuments(text);
    }

    @Override
    public List<Document> parseJsonToDocuments(MultipartFile file) throws IOException {
        String text = readFileAsString(file, false);
        return convertToDocuments(text);
    }

    @Override
    public List<Document> parseXmlToDocuments(MultipartFile file) throws IOException {
        String text = readFileAsString(file, false);
        return convertToDocuments(text);
    }

    @Override
    public List<Document> parseTextToDocuments(MultipartFile file) throws IOException {
        String text = readFileAsString(file, false);
        return convertToDocuments(text);
    }
}
