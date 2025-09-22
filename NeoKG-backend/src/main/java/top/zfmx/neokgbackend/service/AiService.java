package top.zfmx.neokgbackend.service;


import org.springframework.stereotype.Service;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;

import java.util.List;

@Service
public interface AiService {

    String explain(String content);

    String explainMeta(String content, List<EntityType> entityTypes, List<RelationType> relationTypes);
}
