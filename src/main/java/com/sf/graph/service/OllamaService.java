package com.sf.graph.service;

import org.springframework.stereotype.Service;
import dev.langchain4j.model.chat.ChatLanguageModel;

@Service
public class OllamaService {

    private final ChatLanguageModel chatLanguageModel;

    public OllamaService(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    public String chat(String message) {
        return chatLanguageModel.chat(message);
    }
}
