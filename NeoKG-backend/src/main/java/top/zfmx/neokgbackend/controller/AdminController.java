package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import top.zfmx.neokgbackend.pojo.response.Result;
import top.zfmx.neokgbackend.service.DocumentRefService;
import top.zfmx.neokgbackend.service.DocumentService;
import top.zfmx.neokgbackend.service.KeywordService;
import top.zfmx.neokgbackend.service.AiService;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
public class AdminController {

    @Resource
    private AiService aiService;
    @Resource
    private KeywordService keywordService;
    @Resource
    private DocumentService documentService;
    @Resource
    private DocumentRefService documentRefService;

//    @PostMapping(value = "/chat", produces = "text/html;charset=utf-8")
//    public String chat(String message) {
//        return aiService.explain(message);
//    }

//    @GetMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ServerSentEvent<String>> chatWithGraph(String message, String sessionId) {
//        return aiService.askQuestion(message, sessionId);
//    }

    /**
     * 清空所有数据
     * @return 返回操作结果
     * @since 0.0.2
     */
    @GetMapping("clean")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> clean() {
        keywordService.remove(null);
        documentService.remove(null);
        return Result.ok(null);
    }
}
