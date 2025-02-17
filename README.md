# MS Graph
MS Graph is a Spring Boot application web api to connect to Neo4j and Azure openAI. 
## Prerequisites

Before you begin, ensure you meet the following requirements:
- Java 18 or above
- Maven 3.8.6 or above
- Docker 24.0.2 or above
- Neo4j latest

## Build & run

1. Build project:
   ```bash
    mvn clean install -DskipTests
   ```

2. Run application:
   ```bash
    java -jar target/ms-graph-*.jar  
   ```
   
## Build and Run Docker container

Before running docker build command make sure you have build project using maven command above. 
1. To build Docker image, run the following command:

   ```bash
    docker build -t ms-graph . 
   ```

2. To run your Docker container using the newly created image:

   ```bash
    docker run -p 8080:8080 ms-graph 
   ```

## Neo4j Docker Composed Environment
Setup neo4j on docker

* go to `./docker` folder
* run `docker compose up -d`



# Setup Docker Compose For  ms-graph, graph-app and neo4j

To set up your Neo4j, Java Spring backend (ms-graph), and Next.js frontend (graph-app) in Docker containers so they can communicate with each other, you can create a single docker-compose.yml file that defines all services and their configurations. Below is how you can achieve this:

**docker-compose.yml**
```yml
version: '3.8'

services:

  neo4j:
    image: neo4j:latest
    container_name: neo4j
    ports:
      - "7474:7474"  # HTTP
      - "7687:7687"  # Bolt
    environment:
      - NEO4J_AUTH=neo4j/T3stT3st!
      - NEO4J_dbms_memory_pagecache_size=512M
      - NEO4J_dbms_memory_heap_initial__size=1G
      - NEO4J_dbms_memory_heap_max__size=2G
      - NEO4J_dbms_logs_debug_level=INFO
    networks:
      - app-network
    volumes:
      - neo4j-data:/data
      - neo4j-logs:/logs

  ms-graph:
    build:
      context: ./ms-graph
      dockerfile: Dockerfile
    container_name: ms-graph
    depends_on:
      - neo4j
    environment:
      - SPRING_NEO4J_URI=bolt://neo4j:7687
    ports:
      - "8080:8080"
    networks:
      - app-network

  graph-app:
    build:
      context: ./graph-app
      dockerfile: Dockerfile
      args:
        NEXT_PUBLIC_API_BASE_URL: http://ms-graph:8080
    container_name: graph-app
    depends_on:
      - ms-graph
    ports:
      - "3000:3000"
    networks:
      - app-network

networks:
  app-network:

volumes:
  neo4j-data:
  neo4j-logs:
  ```

  Connecting the Services:

    Frontend to Backend:
      - The frontend (graph-app) connects to the backend (ms-graph) using the URL http://ms-graph:8080.
      - This is set via the NEXT_PUBLIC_API_BASE_URL environment variable passed as a build argument in the Dockerfile.

    Backend to Neo4j:
      - The backend (ms-graph) connects to the Neo4j database using the URI bolt://neo4j:7687.
      - This is configured via the SPRING_NEO4J_URI environment variable in docker-compose.yml.
