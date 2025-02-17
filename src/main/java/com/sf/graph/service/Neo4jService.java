package com.sf.graph.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class Neo4jService {

    private final Driver neo4jDriver;
    private static final Logger logger = LoggerFactory.getLogger(Neo4jService.class);

    public Neo4jService(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }

    public List<Map<String, Object>> findAllNodeAndRelation() {
        String cypherQuery = "MATCH (n) OPTIONAL MATCH (n)-[r]-(m) RETURN n, r, m";
        List<Map<String, Object>> results = new ArrayList<>();

        try (Session session = neo4jDriver.session(SessionConfig.forDatabase("neo4j"))) {
            Result result = session.run(cypherQuery);

            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> recordMap = new LinkedHashMap<>();

                if (record.containsKey("n")) {
                    recordMap.put("n", nodeToMap(record.get("n").asNode()));
                }

                if (record.containsKey("r") && !record.get("r").isNull()) {
                    recordMap.put("r", relationshipToMap(record.get("r").asRelationship()));
                } else {
                    recordMap.put("r", null);
                }

                if (record.containsKey("m") && !record.get("m").isNull()) {
                    recordMap.put("m", nodeToMap(record.get("m").asNode()));
                } else {
                    recordMap.put("m", null);
                }

                results.add(recordMap);
            }
        }

        return results;
    }

    public void executeCypher(String json) {
        /*
            Parse the JSON string and process each item to generate Cypher statements.
            Example:
            ["Java", "technology", "Mongo", "database", "works-with"]
            Generates:
            MERGE (java:Technology {name: 'Java'})
            MERGE (mongo:Database {name: 'Mongo'})
            MERGE (java)-[:WORKS_WITH]->(mongo);
            Insert each statement into the statements array.
        */
        List<String> statements = new ArrayList<>();
        try {
            JSONArray dataArray = new JSONArray(json);

            for (int i = 0; i < dataArray.length(); i++) {
                JSONArray item = dataArray.getJSONArray(i);
                String sourceName = item.getString(0);
                String sourceLabel = item.getString(1);
                String targetName = item.getString(2);
                String targetLabel = item.getString(3);
                String relationType = item.getString(4);

                String sourceVar = sanitizeVariableName(sourceName);
                sourceVar = sourceVar.replace('-', '_').replace(' ','_').replace("+","plus").replace("#","sharp").trim();
                String targetVar = sanitizeVariableName(targetName);
                targetVar = targetVar.replace('-', '_').replace(' ','_').replace("+","plus").replace("#","sharp").trim();
                String sourceLabelCap = capitalize(sourceLabel);
                sourceLabelCap = sourceLabelCap.replace('-', '_').replace(' ','_').trim();
                String targetLabelCap = capitalize(targetLabel);
                targetLabelCap = targetLabelCap.replace('-', '_').replace(' ','_').trim();

                String relationTypeUpper = relationType.toUpperCase().replace('-', '_').replace(' ', '_').trim();

                String statement1 = String.format("MERGE (%s:%s {name: '%s'})", sourceVar, sourceLabelCap, escapeApostrophes(sourceName));
                String statement2 = String.format("MERGE (%s:%s {name: '%s'})", targetVar, targetLabelCap, escapeApostrophes(targetName));
                String statement3 = String.format("MERGE (%s)-[:%s]->(%s);", sourceVar, relationTypeUpper, targetVar);

                String combinedStatement = String.join("\n", statement1, statement2, statement3);

                statements.add(combinedStatement);
            }
        } catch (JSONException e) {
            logger.warn("JSON parsing failed: " + e.getMessage(), e);
            return;
        }

        try (Session session = neo4jDriver.session(SessionConfig.forDatabase("neo4j"))) {
            session.writeTransaction(tx -> {
                for (String statement : statements) {
                    statement = statement.trim();
                    if (!statement.isEmpty()) {
                        try {
                            tx.run(statement);
                        } catch (Exception e) {
                            logger.warn("Failed to execute statement: " + statement + "; Error: " + e.getMessage(), e);
                            // Continue with next statement
                        }
                    }
                }
                return null;
            });
        } catch (Exception e) {
            logger.warn("Cypher execution failed: " + e.getMessage(), e);
            return;
        }
    }

    // Helper method to capitalize the first letter of a string
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // Helper method to sanitize variable names for Cypher queries
    private String sanitizeVariableName(String name) {
        if (name == null) {
            return "";
        }
        // Remove non-alphanumeric characters except '+' and '#' and convert to lowercase
        return name.replaceAll("[^a-zA-Z0-9+#]", "").toLowerCase();
    }

    // Helper method to escape apostrophes in strings
    private String escapeApostrophes(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("'", "\\'");
    }

    private Map<String, Object> nodeToMap(Node node) {
        Map<String, Object> nodeMap = new LinkedHashMap<>();
        nodeMap.put("id", node.id());
        nodeMap.put("labels", node.labels());
        nodeMap.put("properties", node.asMap());
        return nodeMap;
    }

    private Map<String, Object> relationshipToMap(Relationship relationship) {
        Map<String, Object> relMap = new LinkedHashMap<>();
        relMap.put("id", relationship.id());
        relMap.put("type", relationship.type());
        relMap.put("startNodeId", relationship.startNodeId());
        relMap.put("endNodeId", relationship.endNodeId());
        relMap.put("properties", relationship.asMap());
        return relMap;
    }
}