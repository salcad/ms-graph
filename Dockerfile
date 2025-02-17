FROM openjdk:18-jdk
WORKDIR /app
COPY target/ms-graph-*.jar /app/ms-graph.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/ms-graph.jar"]
