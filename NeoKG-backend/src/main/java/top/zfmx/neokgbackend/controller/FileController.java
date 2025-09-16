package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.response.Result;
import top.zfmx.neokgbackend.result.FileParseResult;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private Tika tika;

    @PostMapping("/upload")
    public Result<FileParseResult> uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();

        parser.parse(inputStream, handler, metadata);

        // 构建 metadata Map
        Map<String, String> metadataMap = new HashMap<>();
        for (String name : metadata.names()) {
            metadataMap.put(name, metadata.get(name));
        }

        // 构建返回对象
        FileParseResult result = new FileParseResult();
        result.setContent(handler.toString());
        result.setMetadata(metadataMap);
        result.setMimeType(tika.detect(file.getInputStream(), file.getOriginalFilename()));

        return Result.ok(result);

    }


}
