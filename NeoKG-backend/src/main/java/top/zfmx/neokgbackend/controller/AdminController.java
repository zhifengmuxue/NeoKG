package top.zfmx.neokgbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zfmx.neokgbackend.response.Result;
import top.zfmx.neokgbackend.service.DocumentRefService;
import top.zfmx.neokgbackend.service.DocumentService;
import top.zfmx.neokgbackend.service.KeywordService;
import top.zfmx.neokgbackend.service.KeywordsAiService;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
public class AdminController {

    @Resource
    private KeywordsAiService keywordsAiServiceService;
    @Resource
    private KeywordService keywordService;
    @Resource
    private DocumentService documentService;
    @Resource
    private DocumentRefService documentRefService;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public String chat(String message) {
        return keywordsAiServiceService.explain(message);
    }

    @RequestMapping("clean")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> clean() {
        keywordService.remove(null);
        documentService.remove(null);
        return Result.ok(null);
    }
}
