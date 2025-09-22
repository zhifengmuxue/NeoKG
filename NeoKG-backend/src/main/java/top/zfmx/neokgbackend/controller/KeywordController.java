package top.zfmx.neokgbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import top.zfmx.neokgbackend.pojo.entity.Document;
import top.zfmx.neokgbackend.pojo.entity.Keyword;
import top.zfmx.neokgbackend.pojo.response.Result;
import top.zfmx.neokgbackend.service.DocumentService;
import top.zfmx.neokgbackend.service.GraphNeo4jService;
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
    @Resource
    private GraphNeo4jService graphNeo4jService;
    @Resource
    private DocumentService documentService;

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
        boolean updated = keywordService.updateByIdWithVec(keyword);
        return Result.ok(updated);
    }
    @PostMapping
    public Result<Boolean> addKeyword(@RequestBody Keyword keyword) {
        boolean saved = keywordService.saveWithVec(keyword);
        List<Document> documents = documentService.listDocumentsWithKeywords();
        graphNeo4jService.saveDocumentsToNeo4j(documents, false);
        return Result.ok(saved);
    }
}
