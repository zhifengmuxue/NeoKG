package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.apache.tika.exception.TikaException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.model.Document;
import top.zfmx.neokgbackend.response.Result;
import top.zfmx.neokgbackend.service.DocumentService;

import java.io.IOException;
import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/file")
public class DocumentController {


    @Resource
    private DocumentService documentService;

    @PostMapping("/upload")
    public Result<List<Document>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            List<Document> documents = documentService.parseAndSaveFile(file);
            return Result.ok(documents);
        } catch (IOException | TikaException e) {
            return Result.error("文件解析失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("处理失败: " + e.getMessage());
        }
    }
}
