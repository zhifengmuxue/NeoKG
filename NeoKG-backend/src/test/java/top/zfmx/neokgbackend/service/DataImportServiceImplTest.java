package top.zfmx.neokgbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import top.zfmx.neokgbackend.pojo.entity.Document;
import top.zfmx.neokgbackend.service.impl.DataImportServiceImpl;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataImportServiceImplTest {

    @Mock
    private KeywordsAiService keywordsAiService;

    @InjectMocks
    private DataImportServiceImpl dataImportService;

    @BeforeEach
    void setUp() {
        // 手动注入真实的 ObjectMapper
        ReflectionTestUtils.setField(dataImportService, "objectMapper", new ObjectMapper());
    }

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

        // 现在这个 when() 会正常工作，因为 keywordsAiService 是 mock 对象
        when(keywordsAiService.explain(anyString())).thenReturn(aiJson);

        // 调用方法
        List<Document> documents = dataImportService.parseCsvToDocuments(mockFile);

        System.out.println(documents);

        // 添加断言验证结果
        assertThat(documents).hasSize(2);
        assertThat(documents.get(0).getTitle()).isEqualTo("Document1");
    }

    @Test
    public void testParseMarkdownToDocuments() throws Exception {
        // 模拟 Markdown 文件
        String markdownContent = "---\n" +
                "title: Document1\n" +
                "keyword: tag1\n" +
                "description: desc1\n" +
                "alias: alias1|alias2\n" +
                "---\n" +
                "\n" +
                "This is a test content.\n" +
                "\n" +
                "---\n" +
                "title: Document2\n" +
                "keyword: tag2\n" +
                "description: desc2\n" +
                "alias: alias3\n" +
                "---\n" +
                "\n" +
                "Another content.";

        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.md", "text/markdown", markdownContent.getBytes()
        );

        // 模拟 AiService 返回 JSON
        String aiJson = "[{" +
                "\"id\":0," +
                "\"title\":\"Document1\"," +
                "\"content\":\"This is a test content.\"," +
                "\"keywords\":[{\"tag\":\"tag1\",\"description\":\"desc1\",\"alias\":[\"alias1\",\"alias2\"],\"ref\":{\"documentId\":\"Document1\",\"index\":0}}]" +
                "},{" +
                "\"id\":1," +
                "\"title\":\"Document2\"," +
                "\"content\":\"Another content.\"," +
                "\"keywords\":[{\"tag\":\"tag2\",\"description\":\"desc2\",\"alias\":[\"alias3\"],\"ref\":{\"documentId\":\"Document2\",\"index\":1}}]" +
                "}]";

        when(keywordsAiService.explain(anyString())).thenReturn(aiJson);

        // 调用方法
        List<Document> documents = dataImportService.parseMarkdownToDocuments(mockFile);

        System.out.println(documents);

        // 添加断言验证结果
        assertThat(documents).hasSize(2);
        assertThat(documents.get(0).getTitle()).isEqualTo("Document1");
    }
}
