package top.zfmx.neokgbackend.service;

import com.opencsv.exceptions.CsvValidationException;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.model.Document;

import java.io.IOException;
import java.util.List;

/**
 * 数据导入
 * 从上传的结构化(csv/xml/json)、半结构化(markdown)和非结构化(Word/pdf)文件抽取实体类
 *
 * @author li ma
 * @version 0.0.1
 **/
public interface DataImportService {
    List<Document> parseCsvToDocuments(MultipartFile file) throws IOException, CsvValidationException;
    List<Document> parseMarkdownToDocuments(MultipartFile file) throws IOException;
}
