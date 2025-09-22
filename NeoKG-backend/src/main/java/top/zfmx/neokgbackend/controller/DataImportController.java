package top.zfmx.neokgbackend.controller;

import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.Resource;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.pojo.entity.Document;
import top.zfmx.neokgbackend.pojo.response.Result;
import top.zfmx.neokgbackend.service.DataImportService;

import java.io.IOException;
import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/api/import")
public class DataImportController {

    @Resource
    private DataImportService dataImportService;

    @Resource
    private Tika tika;

    /**
     * @since 0.0.2
     */
    @PostMapping
    public Result<List<Document>> importFile(
            @RequestParam("file") MultipartFile file) throws IOException, CsvValidationException, TikaException {

        String mimeType = tika.detect(file.getInputStream(), file.getOriginalFilename());
        List<Document> documents;

        // 通过tika判断文件类型，调用对应的解析方法
        switch (mimeType) {
            // JSON
            case "application/json", "application/ld+json" -> throw new UnsupportedOperationException("JSON not supported yet");
            // XML
            case "application/xml", "text/xml" -> throw new UnsupportedOperationException("XML not supported yet");
            // csv
            case "text/csv" -> documents = dataImportService.parseCsvToDocuments(file);
            // md
            case "text/markdown", "text/x-markdown", "text/x-web-markdown" -> documents = dataImportService.parseMarkdownToDocuments(file);
            // word
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                 "application/msword", "text/plain" -> documents = dataImportService.parseWordToDocuments(file);
            // pdf
            case "application/pdf" -> throw new UnsupportedOperationException("PDF not supported yet");
            default -> throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
        }

        return Result.ok(documents);
    }

}
