package top.zfmx.neokgbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import top.zfmx.neokgbackend.pojo.entity.Keyword;

import java.util.List;

/**
 * @author li ma
 * @version 0.0.1
 **/
public interface KeywordService extends IService<Keyword> {
    List<Keyword> findAllKeywords();
    IPage<Keyword> findAllKeywordsPage(int pageNum, int pageSize);

    boolean saveWithVec(Keyword keyword);

    boolean updateByIdWithVec(Keyword keyword);
}
