package top.zfmx.neokgbackend.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opencsv.exceptions.CsvValidationException;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.annotation.Resource;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.mapper.DocumentMapper;
import top.zfmx.neokgbackend.mapper.DocumentRefMapper;
import top.zfmx.neokgbackend.mapper.KeywordMapper;
import top.zfmx.neokgbackend.model.Document;
import top.zfmx.neokgbackend.model.DocumentRef;
import top.zfmx.neokgbackend.model.Keyword;
import top.zfmx.neokgbackend.service.DataImportService;
import top.zfmx.neokgbackend.service.DocumentService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {
    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private KeywordMapper keywordMapper;
    @Resource
    private DataImportService dataImportService;
    @Resource
    private DocumentRefMapper documentRefMapper;
    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private Snowflake snowflake;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Document> parseAndSaveFile(MultipartFile file) throws IOException, TikaException, CsvValidationException {
//         读取文件后缀
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("文件名为空");
        }
        List<Document> documents;
        if (filename.endsWith(".csv")) {
            documents = dataImportService.parseCsvToDocuments(file);
        } else if (filename.endsWith(".md") || filename.endsWith(".markdown")) {
            documents = dataImportService.parseMarkdownToDocuments(file);
        } else if (filename.endsWith(".doc") || filename.endsWith(".docx") || filename.endsWith(".pdf")) {
            documents = dataImportService.parseWordToDocuments(file);
        } else {
            throw new RuntimeException("不支持的文件类型");
        }

        // 保存 Document 和 Keyword
        for (Document doc : documents) {
            Long docId = snowflake.nextId();
            doc.setId(docId);

//            Embedding docEmbedding = embeddingModel.embed(doc.getContent()).content();
//            List<Float> embeddingList = docEmbedding.vectorAsList();
//            doc.setVec(new Vector<>(embeddingList));

            documentMapper.insert(doc);

            if (doc.getKeywords() != null) {
                for (int i = 0; i < doc.getKeywords().size(); i++) {
                    Keyword kw = doc.getKeywords().get(i);

                    // 生成关键词 id
                    Long kwId = snowflake.nextId();
                    kw.setId(kwId);

//                    Embedding kwEmbedding = embeddingModel.embed(kw.getName()).content();
//                    List<Float> kwList = kwEmbedding.vectorAsList();
//                    kw.setVec(new Vector<>(kwList));
                    keywordMapper.insert(kw);

                    // 保存 DocumentRef
                    DocumentRef ref = DocumentRef.builder()
                            .id(snowflake.nextId())
                            .documentId(docId)
                            .keywordId(kwId)
                            .build();

                    documentRefMapper.insert(ref);
                }
            }
        }
        return documents;
    }

}
