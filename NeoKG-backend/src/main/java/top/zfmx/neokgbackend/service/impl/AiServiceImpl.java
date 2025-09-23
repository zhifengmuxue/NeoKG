package top.zfmx.neokgbackend.service.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import top.zfmx.neokgbackend.mapper.DocumentMapper;
import top.zfmx.neokgbackend.pojo.entity.Document;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;
import top.zfmx.neokgbackend.service.AiService;
import top.zfmx.neokgbackend.service.GraphNeo4jService;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author li ma
 * @version 0.0.1
 **/
@Service
public class AiServiceImpl implements AiService {

    @Resource
    private ChatModel chatModel;
    @Resource
    private GraphNeo4jService graphNeo4jService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private EmbeddingModel embeddingModel;
    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private Gson gson;

    @Override
    public Flux<ServerSentEvent<String>> askQuestion(String question, String sessionId) {
        // Redis key
        String contextKey = "chat:context:" + sessionId;

        // 安全获取上下文，第一次对话也不会 NPE
        Object contextObj = redisTemplate.opsForValue().get(contextKey);
        List<Map<String, String>> context = contextObj instanceof String contextJson
                ? gson.fromJson(contextJson, new TypeToken<List<Map<String, String>>>() {}.getType())
                : new ArrayList<>();

        // 限制上下文最大轮数
        final int maxHistory = 5;
        int start = Math.max(0, context.size() - maxHistory);
        List<Map<String, String>> truncatedContext = new ArrayList<>(context.subList(start, context.size()));

        // 文本向量检索
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(question));
        float[] queryVector = response.getResult().getOutput();
        Optional<Document> topDoc = documentMapper.findTopByVector(queryVector);
        Long focusDocId = topDoc.map(Document::getId).orElse(null);

        // 构建证据图
        Map<String, Object> evidenceGraph = graphNeo4jService.findEvidenceSubgraph(focusDocId);

        // 获取文档内容
        Document doc = focusDocId != null ? documentMapper.selectById(focusDocId) : null;

        // 构建 prompt，将上下文加入
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是知识图谱领域问答助手，必须严格基于证据回答。"));

        // 添加历史上下文
        for (Map<String, String> round : truncatedContext) {
            messages.add(new UserMessage(round.get("question")));
            messages.add(new AssistantMessage(round.get("answer")));
        }

        // 当前问题
        messages.add(new UserMessage(
                "问题：" + question +
                        "\n证据：" + gson.toJson(evidenceGraph) +
                        (doc != null ? ("\n相关文档内容：" + doc.getContent()) : "") +
                        "\n请基于以上内容，简洁、专业、准确地回答用户的问题。" +
                        "并阐明你是从图谱中获取到了怎样的知识证明了你的回答" +
                        "或者是从文档内容中找到了相关的内容进行回答" +
                        "如果无法从中得到答案，请回复“抱歉，我无法回答这个问题。”"
        ));

        Prompt prompt = new Prompt(messages);

        // 流式返回 + 更新上下文
        return chatModel.stream(prompt)
                .map(resp -> {
                    String answer = Optional.ofNullable(resp.getResult().getOutput())
                            .map(output -> output.getText())
                            .orElse("抱歉，未能生成答案。");

                    // 更新上下文（保留最近 maxHistory 轮）
                    truncatedContext.add(Map.of("question", question, "answer", answer));

                    // 保存到 Redis，设置 TTL
                    redisTemplate.opsForValue().set(contextKey, gson.toJson(truncatedContext), 24, TimeUnit.HOURS);

                    return ServerSentEvent.builder(answer).build();
                });
    }


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
                "8. 文档实体的title和关键词的name不能重复\n" +
                "请直接输出 JSON 数组，不要输出解释性文字。\n" +
                "文本内容：\n" + content;

        return chatModel.call(prompt);
    }


    @Override
    public String explainMeta(String content, List<EntityType> entityTypes, List<RelationType> relationTypes) {
        StringBuilder sb = new StringBuilder();

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
