package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zfmx.neokgbackend.service.KeywordsAiService;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
public class AiController {
    @Resource
    private KeywordsAiService keywordsAiServiceService;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public String chat(String message) {
        return keywordsAiServiceService.explain(message);
    }
}
