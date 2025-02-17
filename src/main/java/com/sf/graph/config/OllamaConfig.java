package com.sf.graph.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.time.Duration;

@Configuration
public class OllamaConfig {

    @Value("${ollama.endpoint}")
    private String endpoint;

    @Value("${ollama.model}")
    private String model;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl(endpoint)
                .modelName(model)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(120))
                .build();
    }
}
