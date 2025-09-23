package top.zfmx.neokgbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.tika.exception.TikaException;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.enums.MatchMode;
import top.zfmx.neokgbackend.pojo.entity.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface DocumentService
        extends IService<Document> {
    /**
     * 上传文件并生成 Document 和 Keyword，持久化到数据库
     */
    List<Document> parseAndSaveFile(MultipartFile file, Double keywordSimilarRatio, MatchMode matchMode) throws IOException, TikaException, CsvValidationException;

    List<Document> listDocumentsWithKeywords();

    IPage<Document> findAllDocumentPage(int currentPage, int pageSize);

    boolean updateByIdWithVec(Document document);

    List<Map<String, Object>> countDocumentsByTypeInLastWeek();
}
