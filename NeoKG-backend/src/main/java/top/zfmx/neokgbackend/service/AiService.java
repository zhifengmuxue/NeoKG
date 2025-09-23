package top.zfmx.neokgbackend.service;


import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import top.zfmx.neokgbackend.pojo.entity.meta.EntityType;
import top.zfmx.neokgbackend.pojo.entity.meta.RelationType;

import java.util.List;

@Service
public interface AiService {
    Flux<ServerSentEvent<String>> askQuestion(String question, String sessionId);
    String explain(String content);
    String explainMeta(String content, List<EntityType> entityTypes, List<RelationType> relationTypes);
}
