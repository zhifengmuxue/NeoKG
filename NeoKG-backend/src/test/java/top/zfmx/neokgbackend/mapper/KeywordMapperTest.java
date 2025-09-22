package top.zfmx.neokgbackend.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.zfmx.neokgbackend.pojo.entity.Keyword;

import java.util.List;
import java.util.Vector;

/**
 * @author li ma
 * @version 0.0.1
 **/
@SpringBootTest
public class KeywordMapperTest {

    @Autowired
    private KeywordMapper keyWordMapper;

    @Test
    public void testInsertAndSearchVector() {
        // 1. 构造一个 1536 维向量
        Vector<Float> vec = new Vector<>();
        for (int i = 0; i < 1536; i++) {
            vec.add((float) Math.random()); // 随机数模拟 embedding
        }

        // 2. 插入文档
        Keyword keyword = Keyword.builder()
                .name("测试关键词")
                .description("test")
                .alias(List.of("测试", "关键词"))
                .vec(vec)
                .build();

        int inserted = keyWordMapper.insert(keyword);
        System.out.println("插入成功条数: " + inserted);
        System.out.println("关键词ID: " + keyword.getId());

        // 3. 使用相同向量进行搜索
        List<Keyword> results = keyWordMapper.searchByVector(vec, 5);

        System.out.println("搜索结果数量: " + results.size());
        results.forEach(d -> System.out.println(d.getId() + " | " + d.getName()));
    }

}
