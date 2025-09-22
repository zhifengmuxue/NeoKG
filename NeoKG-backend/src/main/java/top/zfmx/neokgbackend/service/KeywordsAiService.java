package top.zfmx.neokgbackend.service;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public interface KeywordsAiService {


    String explain(String content);
}
