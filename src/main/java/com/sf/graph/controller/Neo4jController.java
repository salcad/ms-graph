package com.sf.graph.controller;

import com.sf.graph.service.GraphProcessingService;
import com.sf.graph.service.Neo4jService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/graph/api")
public class Neo4jController {

    private final Neo4jService neo4jService;

    private final GraphProcessingService graphProcessingService;

    public Neo4jController(Neo4jService neo4jService, GraphProcessingService graphProcessingService) {
        this.neo4jService = neo4jService;
        this.graphProcessingService = graphProcessingService;
    }

    @GetMapping("/findAllNode")
    public List<Map<String, Object>> findAllNodeAndRelation() {
        return neo4jService.findAllNodeAndRelation();
    }

    @PostMapping("/saveToGraph")
    public ResponseEntity<?> extractNodeAndSaveToGraph(@RequestBody String text) {
        try {
            graphProcessingService.openAIExtractNodeAndSaveToGraph(text);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}