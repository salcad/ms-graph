package com.sf.graph.controller;

import com.sf.graph.service.OllamaService;
import com.sf.graph.dto.ChatResponse;

import lombok.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/graph/api/ollama-chat")
public class OllamaController {

    private final OllamaService ollamaService;

    @Autowired
    public OllamaController(OllamaService openAIService) {
        this.ollamaService = openAIService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> getChatCompletion(@RequestBody ChatRequest request) {
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ChatResponse("Prompt cannot be empty."));
        }

        String chatResponse = ollamaService.chat(request.getPrompt());

        if (chatResponse == null || chatResponse.isEmpty()) {
            return ResponseEntity.status(500).body(new ChatResponse("Failed to generate a response."));
        }

        return ResponseEntity.ok(new ChatResponse(chatResponse));
    }

    @Data
    public static class ChatRequest {
        private String prompt;
    }
}