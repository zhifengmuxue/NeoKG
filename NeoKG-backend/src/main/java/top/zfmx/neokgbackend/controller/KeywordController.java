package top.zfmx.neokgbackend.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.zfmx.neokgbackend.model.Keyword;
import top.zfmx.neokgbackend.response.Result;
import top.zfmx.neokgbackend.service.KeywordService;

import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
@RestController
@RequestMapping("/api/keyword")
public class KeywordController {

    @Resource
    private KeywordService keywordService;

    @GetMapping
    public Result<List<Keyword>> getKeywords() {
        return Result.ok(keywordService.findAllKeywords());
    }
}
