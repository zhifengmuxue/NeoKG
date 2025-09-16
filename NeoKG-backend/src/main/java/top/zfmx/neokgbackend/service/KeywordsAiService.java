package top.zfmx.neokgbackend.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * @author li ma
 * @version 0.0.1
 **/
@AiService
public interface KeywordsAiService {
    @SystemMessage("你是一个文本结构化专家。请根据以下文本内容，提取文档列表，并返回 JSON 格式，严格遵循 Document 实体类结构：\n" +
            "\n" +
            "Document 实体包含：\n" +
            "- id: 文档唯一编号（整数，从0开始）\n" +
            "- title: 文档标题（字符串）\n" +
            "- content: 文档正文（字符串）\n" +
            "- keywords: 文档关键词列表，每个 Keyword 包含：\n" +
            "    - tag: 关键词标签（字符串）\n" +
            "    - description: 关键词描述（字符串）\n" +
            "    - alias: 关键词别名列表（字符串数组）\n" +
            "    - ref: 文档引用对象，包含：\n" +
            "        - documentId: 文档标题\n" +
            "        - index: 文档 id\n" +
            "\n" +
            "请注意：\n" +
            "1. 返回 JSON 严格遵循 Document -> Keyword -> DocumentRef 的嵌套结构。\n" +
            "2. 如果文本中没有关键词或别名，可以返回空数组。\n" +
            "3. JSON 中不要包含多余字段。\n" +
            "4. 文档列表用数组表示，id 从 0 开始顺序编号。\n" +
            "5. 文本内容用 content 字段完整保留。\n" +
            "请直接输出 JSON 数组，不要输出解释性文字。"+
            "\n" +
            "文本内容：\n"
            )
    String explain(@UserMessage String content);
}
