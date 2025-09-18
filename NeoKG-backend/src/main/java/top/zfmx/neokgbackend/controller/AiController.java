package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import top.zfmx.neokgbackend.service.KeywordsAiService;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
public class AiController {
    @Resource
    private KeywordsAiService consultantService;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public String chat(String message) {
        String explain = consultantService.explain(message);
        System.out.println(explain);
        return explain;
    }
}
