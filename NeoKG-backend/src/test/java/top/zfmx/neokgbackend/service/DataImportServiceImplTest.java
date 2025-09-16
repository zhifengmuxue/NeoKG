package top.zfmx.neokgbackend.service.impl;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import top.zfmx.neokgbackend.model.Document;
import top.zfmx.neokgbackend.service.KeywordsAiService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class DataImportServiceImplTest {

    @Resource
    private KeywordsAiService keywordsAiService;
    @Resource
    private DataImportServiceImpl dataImportService;

    @Test
    void testParseCsvToDocuments() throws Exception {
        // 模拟 CSV 文件
        String csvContent = "id,title,content,keyword,description,alias\n" +
                "1,Document1,This is a test content,tag1,desc1,alias1|alias2\n" +
                "2,Document2,Another content,tag2,desc2,alias3";

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.csv", "text/csv", csvContent.getBytes()
        );

        // 模拟 AiService 返回 JSON
        String aiJson = "[{" +
                "\"id\":0," +
                "\"title\":\"Document1\"," +
                "\"content\":\"This is a test content\"," +
                "\"keywords\":[{\"tag\":\"tag1\",\"description\":\"desc1\",\"alias\":[\"alias1\",\"alias2\"],\"ref\":{\"documentId\":\"Document1\",\"index\":0}}]" +
                "},{" +
                "\"id\":1," +
                "\"title\":\"Document2\"," +
                "\"content\":\"Another content\"," +
                "\"keywords\":[{\"tag\":\"tag2\",\"description\":\"desc2\",\"alias\":[\"alias3\"],\"ref\":{\"documentId\":\"Document2\",\"index\":1}}]" +
                "}]";

        when(keywordsAiService.explain(anyString())).thenReturn(aiJson);

        // 调用方法
        List<Document> documents = dataImportService.parseCsvToDocuments(mockFile);

        System.out.println(documents);
    }

    @Test
    void testParseMarkdownToDocuments() throws Exception {
        // 模拟 Markdown 文件
        String mdContent = "# Document1\nThis is a test content\n## 关键词\n- tag1: desc1 (别名: alias1|alias2)\n# Document2\nAnother content\n## 关键词\n- tag2: desc2 (别名: alias3)";

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.md", "text/markdown", mdContent.getBytes()
        );

        // 模拟 AiService 返回 JSON
        String aiJson = "[{" +
                "\"id\":0," +
                "\"title\":\"Document1\"," +
                "\"content\":\"This is a test content\"," +
                "\"keywords\":[{\"tag\":\"tag1\",\"description\":\"desc1\",\"alias\":[\"alias1\",\"alias2\"],\"ref\":{\"documentId\":\"Document1\",\"index\":0}}]" +
                "},{" +
                "\"id\":1," +
                "\"title\":\"Document2\"," +
                "\"content\":\"Another content\"," +
                "\"keywords\":[{\"tag\":\"tag2\",\"description\":\"desc2\",\"alias\":[\"alias3\"],\"ref\":{\"documentId\":\"Document2\",\"index\":1}}]" +
                "}]";

        when(keywordsAiService.explain(anyString())).thenReturn(aiJson);

        // 调用方法
        List<Document> documents = dataImportService.parseMarkdownToDocuments(mockFile);

        assertNotNull(documents);
        assertEquals(2, documents.size());
        assertEquals("Document1", documents.get(0).getTitle());
        assertEquals("Document2", documents.get(1).getTitle());
        assertEquals(1, documents.get(0).getKeywords().size());
        assertEquals("tag1", documents.get(0).getKeywords().get(0).getTag());
    }
}
