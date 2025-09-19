package top.zfmx.neokgbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
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
    @DeleteMapping("/batch")
    public Result<Boolean> deleteKeywords(@RequestBody List<Long> ids) {
        boolean removed = keywordService.removeBatchByIds(ids);
        return Result.ok(removed);
    }
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteKeyword(@PathVariable Long id) {
        boolean removed = keywordService.removeById(id);
        return Result.ok(removed);
    }
    @PutMapping
    public Result<Boolean> updateKeyword(@RequestBody Keyword keyword) {
        boolean updated = keywordService.updateById(keyword);
        return Result.ok(updated);
    }
    @PostMapping
    public Result<Boolean> addKeyword(@RequestBody Keyword keyword) {
        boolean saved = keywordService.save(keyword);
        return Result.ok(saved);
    }
}
