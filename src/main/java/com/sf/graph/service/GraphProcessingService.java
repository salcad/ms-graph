package com.sf.graph.service;

import com.azure.ai.openai.models.ChatCompletions;
import org.springframework.stereotype.Service;

@Service
public class GraphProcessingService {

    private final OpenAIService openAIService;
    private final OllamaService ollamaService;
    private final Neo4jService neo4jService;

    public GraphProcessingService(OpenAIService openAIService, OllamaService ollamaService, Neo4jService neo4jService) {
        this.openAIService = openAIService;
        this.ollamaService = ollamaService;
        this.neo4jService = neo4jService;
    }

    public void openAIExtractNodeAndSaveToGraph(String text) throws Exception {
        String finalPrompt = getTemplateForCreateNode(text);

        ChatCompletions chatCompletions;
        try {
            chatCompletions = this.openAIService.getChatCompletions(finalPrompt);
        } catch (Exception e) {
            throw new Exception("Failed to get chat completions from OpenAI service: " + e.getMessage(), e);
        }

        String messageContent = chatCompletions.getChoices().get(0).getMessage().getContent();

        try {
            neo4jService.executeCypher(messageContent);
        } catch (Exception e) {
            throw new Exception("Failed to execute Cypher query: " + e.getMessage(), e);
        }
    }

    public void ollamaExtractNodeAndSaveToGraph(String text) throws Exception {
        String finalPrompt = getTemplateForCreateNode(text);
        String response;

        try {
            response = this.ollamaService.chat(finalPrompt);
        } catch (Exception e) {
            throw new Exception("Failed to get chat completions from Ollama service: " + e.getMessage(), e);
        }

        try {
            neo4jService.executeCypher(response);
        } catch (Exception e) {
            throw new Exception("Failed to execute Cypher query: " + e.getMessage(), e);
        }
    }

    private static String getTemplateForCreateNode(String text) {
        String promptTemplate = String.format(
                "%s\n" +
                "your task is to make list of entity from extracting narration above and " +
                "put that on array {[\"node\",\"label\", \"node\", \"label\", \"relation\"]} " +
                "like this for example {[\"java\",\"technology\", \"oop\", \"concept\", \"used-for\"], [\"java\",\"technology\",\"webapplication\", \"domain\",\"used-for\"]} ," +
                "no need explanation, write only plain json script without markdown tag like ```json",
                text);
        return String.format(promptTemplate, text);
    }
}