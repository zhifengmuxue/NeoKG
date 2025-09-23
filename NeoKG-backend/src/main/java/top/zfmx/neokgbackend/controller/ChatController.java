package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import top.zfmx.neokgbackend.service.AiService;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Resource
    private AiService aiService;

    @GetMapping(value = "/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatWithGraph(String message, String sessionId) {
        return aiService.askQuestion(message, sessionId);
    }
}
