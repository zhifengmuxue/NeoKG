package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
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
    private AiService aiServiceService;
    @Resource
    private KeywordService keywordService;
    @Resource
    private DocumentService documentService;
    @Resource
    private DocumentRefService documentRefService;

//    @PostMapping(value = "/chat", produces = "text/html;charset=utf-8")
//    public String chat(String message) {
//        return aiServiceService.explain(message);
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
