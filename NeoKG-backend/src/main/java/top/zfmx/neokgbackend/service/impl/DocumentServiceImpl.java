package top.zfmx.neokgbackend.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opencsv.exceptions.CsvValidationException;

import jakarta.annotation.Resource;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.zfmx.neokgbackend.enums.DocType;
import top.zfmx.neokgbackend.mapper.DocumentMapper;
import top.zfmx.neokgbackend.mapper.DocumentRefMapper;
import top.zfmx.neokgbackend.mapper.KeywordMapper;
import top.zfmx.neokgbackend.model.Document;
import top.zfmx.neokgbackend.model.DocumentRef;
import top.zfmx.neokgbackend.model.Keyword;
import top.zfmx.neokgbackend.service.DataImportService;
import top.zfmx.neokgbackend.service.DocumentService;
import top.zfmx.neokgbackend.utils.KeywordMatcher;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public List<Document> parseAndSaveFile(MultipartFile file, Double keywordSimilarRatio)
            throws IOException, TikaException, CsvValidationException {

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("文件名为空");
        }
        DocType type = DocType.fromFilename(filename);

        // 解析不同类型的文件
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

        // 预先获取数据库中所有关键词
        List<Keyword> allKeywords = keywordMapper.findAllKeywords();

        for (Document doc : documents) {
            Long docId = snowflake.nextId();
            doc.setId(docId);
            doc.setType(type);
            // 文档 embedding
            float[] docEmbedding = embeddingModel.embed(doc.getContent());
            List<Float> docEmbeddingList = new ArrayList<>(docEmbedding.length);
            for (float f : docEmbedding) docEmbeddingList.add(f);
            doc.setVec(new Vector<>(docEmbeddingList));
            documentMapper.insert(doc);

            if (doc.getKeywords() != null) {
                for (Keyword kw : doc.getKeywords()) {

                    // 先做字符匹配
                    Keyword matchedKeyword = KeywordMatcher.findByName(allKeywords, kw);

                    if (matchedKeyword == null) {
                        // 生成关键词 embedding
                        kw.generateEmbedding(embeddingModel);

                        // 再做语义匹配
                        matchedKeyword = KeywordMatcher.findMostSimilar(allKeywords, kw, kw.getVec(), keywordSimilarRatio);
                    }

                    if (matchedKeyword != null) {
                        // 复用已有关键词
                        kw.setId(matchedKeyword.getId());
                        kw.setVec(matchedKeyword.getVec());
                    } else {
                        // 新建关键词
                        kw.setId(snowflake.nextId());
                        keywordMapper.insert(kw);
                        allKeywords.add(kw); // 同步到内存列表
                    }

                    // 建立 DocumentRef
                    DocumentRef ref = DocumentRef.builder()
                            .id(snowflake.nextId())
                            .documentId(docId)
                            .keywordId(kw.getId())
                            .build();
                    documentRefMapper.insert(ref);
                }
            }
        }

        return documents;
    }



    @Override
    public List<Document> listDocumentsWithKeywords() {
        // 1. 查询所有文档
        List<Document> documents = documentMapper.selectList(null);

        if (documents.isEmpty()) {
            return documents;
        }

        // 2. 查询所有关键词
        List<Keyword> keywords = keywordMapper.selectList(null);

        // 3. 查询所有 DocumentRef
        List<DocumentRef> refs = documentRefMapper.selectList(null);

        // 4. 建立 documentId -> Document 的 map，方便组装
        Map<Long, Document> docMap = documents.stream()
                .collect(Collectors.toMap(Document::getId, d -> d));

        // 5. 建立 keywordId -> Keyword 的 map
        Map<Long, Keyword> kwMap = keywords.stream()
                .collect(Collectors.toMap(Keyword::getId, k -> k));

        // 6. 遍历 ref，把 keyword 塞进对应的 document.keywords
        for (DocumentRef ref : refs) {
            Document doc = docMap.get(ref.getDocumentId());
            Keyword kw = kwMap.get(ref.getKeywordId());

            if (doc != null && kw != null) {
                // 设置 ref 到 keyword 上
                kw.setRef(ref);

                // 确保 document.keywords 已初始化
                if (doc.getKeywords() == null) {
                    doc.setKeywords(new ArrayList<>());
                }
                doc.getKeywords().add(kw);
            }
        }

        return documents;
    }


}
