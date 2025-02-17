package com.sf.graph.controller;

import com.azure.ai.openai.models.ChatCompletions;

import com.sf.graph.service.OpenAIService;
import com.sf.graph.dto.ChatResponse;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/graph/api/openai-chat")
public class OpenAIController {

    private final OpenAIService openAIService;

    @Autowired
    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> getChatCompletion(@RequestBody ChatRequest request) {
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ChatResponse("Prompt cannot be empty."));
        }

        ChatCompletions chatCompletions = openAIService.getChatCompletions(request.getPrompt());

        if (chatCompletions == null || chatCompletions.getChoices().isEmpty()) {
            return ResponseEntity.status(500).body(new ChatResponse("Failed to generate a response."));
        }

        String messageContent = chatCompletions.getChoices().get(0).getMessage().getContent();

        return ResponseEntity.ok(new ChatResponse(messageContent));
    }

    @Data
    public static class ChatRequest {
        private String prompt;
    }
}