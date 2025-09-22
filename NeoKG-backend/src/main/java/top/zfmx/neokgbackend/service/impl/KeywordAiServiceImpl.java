package top.zfmx.neokgbackend.service.impl;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.service.KeywordsAiService;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class KeywordAiServiceImpl implements KeywordsAiService {

    @Resource
    private ChatModel chatModel;

    @Override
    public String explain(String content) {
        // 使用原来的提示词
        String prompt = "你是一个文本结构化专家。请根据以下文本内容，提取文档列表和文章的关键词(keyword)，并返回 JSON 格式，严格遵循 Document 实体类结构：\n" +
                "\n" +
                "Document 实体包含：\n" +
                "- title: 文档标题（字符串）\n" +
                "- content: 文档正文（字符串）\n" +
                "- keywords: 文档关键词列表，每个 Keyword 包含：\n" +
                "    - name: 关键词名字（字符串）\n" +
                "    - description: 关键词描述（字符串）一句话描述\n" +
                "    - alias: 关键词别名列表（字符串数组）\n" +
                "    - ref: 文档引用对象，包含：\n" +
                "        - index: 这是指当前关键词出现在文档哪一行，如果没有的话请你给出-1 \n" +
                "\n" +
                "请注意：\n" +
                "1. 返回 JSON 严格遵循 Document -> Keyword -> DocumentRef 的嵌套结构。\n" +
                "2. JSON 中不要包含多余字段。\n" +
                "3. 关键词的语义不要重复。\n" +
                "4. 文本内容用 content 字段完整保留。\n" +
                "5. 注意一定要存在keywords\n"+
                "6. 我给出实体结构不能出现空的情况" +
                "7. 请保证每一个属性均有值，不能为null\n" +
                "请直接输出 JSON 数组，不要输出解释性文字。\n" +
                "文本内容：\n" + content;

        return chatModel.call(prompt);
    }
}
