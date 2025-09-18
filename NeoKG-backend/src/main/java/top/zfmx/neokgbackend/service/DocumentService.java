package top.zfmx.neokgbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.tika.exception.TikaException;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.model.Document;

import java.io.IOException;
import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface DocumentService
        extends IService<Document> {
    /**
     * 上传文件并生成 Document 和 Keyword，持久化到数据库
     */
    List<Document> parseAndSaveFile(MultipartFile file) throws IOException, TikaException, CsvValidationException;
}
