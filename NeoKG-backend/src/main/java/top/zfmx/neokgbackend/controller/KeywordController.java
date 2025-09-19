package top.zfmx.neokgbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @GetMapping("/page")
    public Result<IPage<Keyword>> getKeywordsPage(@RequestParam(defaultValue = "1") int currentPage,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(keywordService.findAllKeywordsPage(currentPage, pageSize));
    }
}
