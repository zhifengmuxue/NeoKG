package top.zfmx.neokgbackend.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.zfmx.neokgbackend.model.Document;

import java.util.List;
import java.util.Vector;

/**
 * @author li ma
 * @version 0.0.1
 **/
@SpringBootTest
public class DocumentMapperTest {
    @Autowired
    private DocumentMapper documentMapper;

    @Test
    public void testInsertAndSearchVector() {
        // 1. 构造一个 1536 维向量
        Vector<Float> vec = new Vector<>();
        for (int i = 0; i < 1536; i++) {
            vec.add((float) Math.random()); // 随机数模拟 embedding
        }

        // 2. 插入文档
        Document doc = Document.builder()
                .title("测试文档")
                .content("这是测试内容")
                .vec(vec)
                .build();

        int inserted = documentMapper.insert(doc);
        System.out.println("插入成功条数: " + inserted);
        System.out.println("文档ID: " + doc.getId());

        // 3. 使用相同向量进行搜索
        List<Document> results = documentMapper.searchByVector(vec, 5);

        System.out.println("搜索结果数量: " + results.size());
        results.forEach(d -> System.out.println(d.getId() + " | " + d.getTitle()));
    }
}
