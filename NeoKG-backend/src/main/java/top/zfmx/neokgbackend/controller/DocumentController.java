package top.zfmx.neokgbackend.controller;

import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.Resource;
import org.apache.tika.exception.TikaException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.enums.MatchMode;
import top.zfmx.neokgbackend.pojo.entity.Document;
import top.zfmx.neokgbackend.pojo.response.Result;
import top.zfmx.neokgbackend.service.DocumentService;

import java.io.IOException;
import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/api/file")
public class DocumentController {


    @Resource
    private DocumentService documentService;

    /**
     * 上传文件，解析生成 Document 和 Keyword，持久化到数据库
     * 支持结构化(csv)、半结构化(markdown)和非结构化(Word/pdf)文件
     * @param file 上传的文件
     * @param threshold 关键词相似度阈值，范围0-1，默认0.8
     * @param matchModeStr 关键词匹配模式，STRING, SEMANTIC, BOTH;
     */
    @PostMapping("/upload")
    public Result<List<Document>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("threshold") Double threshold,
            @RequestParam(value = "matchMode", defaultValue = "BOTH") String matchModeStr
    ) throws TikaException, CsvValidationException, IOException {

        MatchMode matchMode = MatchMode.fromString(matchModeStr);
        List<Document> documents = documentService.parseAndSaveFile(file, threshold, matchMode);

        return Result.ok(documents);
    }



    @GetMapping("/num")
    public Result<String> getDocumentNum() {
        long count = documentService.count();
        return Result.ok(String.valueOf(count));
    }
}
