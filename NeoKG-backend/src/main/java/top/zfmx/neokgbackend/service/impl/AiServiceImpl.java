package top.zfmx.neokgbackend.service.impl;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;
import top.zfmx.neokgbackend.service.AiService;

import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class AiServiceImpl implements AiService {

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

    @Override
    public String explainMeta(String content, List<EntityType> entityTypes, List<RelationType> relationTypes) {
        StringBuilder sb = new StringBuilder();
//        System.out.println("实体类型: " + entityTypes);
//        System.out.println("关系类型: " + relationTypes);

        sb.append("你是一个知识图谱抽取助手。请根据用户定义的元模型，从文本中提取实体和关系。\n\n");

        sb.append("用户定义的实体类型及属性：\n");
        for (EntityType et : entityTypes) {
            sb.append("- 实体类型: ").append(et.getName()).append("\n");
            if (et.getProperties() != null) {
                for (String prop : et.getProperties()) {
                    sb.append("  - 属性: ").append(prop).append("\n");
                }
            }
        }

        sb.append("\n用户定义的关系类型：\n");
        for (RelationType rt : relationTypes) {
            sb.append("- 关系类型: ").append(rt.getName())
                    .append(" (startEntityId=").append(rt.getStartEntityId())
                    .append(" -> endEntityId=").append(rt.getEndEntityId()).append(")\n");

            if (rt.getProperties() != null) {
                for (String prop : rt.getProperties()) {
                    sb.append("  - 属性: ").append(prop).append("\n");
                }
            }
        }

        // ⚡重点：明确要求和输出格式
        sb.append("\n要求：\n")
                .append("1. 返回严格的 JSON 格式，包含 entities 和 relations 两个数组。\n")
                .append("2. 每个 entity 必须有字段：id, type, 以及其定义的属性。\n")
                .append("3. 每个 relation 必须有字段：id, type, startEntityId, endEntityId 以及属性。\n")
                .append("4. 所有属性必须有值，不能为 null 或空字符串。\n")
                .append("5. 不要输出任何解释，只返回 JSON。\n")
                .append("6. 确保生成的 JSON 可被直接反序列化为 Java 对象。\n");

        // ⚡重点：提供输出示例
        sb.append("\n输出 JSON 示例：\n")
                .append("{\n")
                .append("  \"entities\": [\n")
                .append("    { \"id\": \"1\", \"type\": \"Document\", \"title\": \"图数据库简介\", \"content\": \"图数据库是一种以节点和边为基本结构的数据存储方式。\" },\n")
                .append("    { \"id\": \"2\", \"type\": \"Keyword\", \"name\": \"图数据库\", \"description\": \"一种数据库类型\", \"alias\": \"Graph DB\" }\n")
                .append("  ],\n")
                .append("  \"relations\": [\n")
                .append("    { \"id\": \"10\", \"type\": \"HAS_KEYWORD\", \"startEntityId\": \"1\", \"endEntityId\": \"2\", \"index\": 1 }\n")
                .append("  ]\n")
                .append("}\n");

        sb.append("\n文本内容：\n").append(content);

        return chatModel.call(sb.toString());
    }
}
