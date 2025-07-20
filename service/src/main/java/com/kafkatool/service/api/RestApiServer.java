package com.kafkatool.api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.kafkatool.service.*;
import com.kafkatool.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST API server for Kafka UI Tool
 */
public class RestApiServer {
    
    private static final Logger logger = LoggerFactory.getLogger(RestApiServer.class);
    private final Javalin app;
    private final ObjectMapper objectMapper;
    private final EnhancedKafkaService kafkaService;
    private final SchemaRegistryService schemaRegistryService;
    private final KafkaConnectService connectService;
    
    public RestApiServer(EnhancedKafkaService kafkaService, 
                        SchemaRegistryService schemaRegistryService,
                        KafkaConnectService connectService) {
        this.kafkaService = kafkaService;
        this.schemaRegistryService = schemaRegistryService;
        this.connectService = connectService;
        this.objectMapper = new ObjectMapper();
        this.app = Javalin.create();
        setupRoutes();
    }
    
    public void start(int port) {
        app.start(port);
        logger.info("REST API server started on port {}", port);
    }
    
    public void stop() {
        app.stop();
        logger.info("REST API server stopped");
    }
    
    private void setupRoutes() {
        // Health check
        app.get("/api/health", this::healthCheck);
        
        // Cluster operations
        app.get("/api/clusters/{cluster}/topics", this::getTopics);
        app.post("/api/clusters/{cluster}/topics", this::createTopic);
        app.delete("/api/clusters/{cluster}/topics/{topic}", this::deleteTopic);
        app.get("/api/clusters/{cluster}/topics/{topic}/config", this::getTopicConfig);
        app.put("/api/clusters/{cluster}/topics/{topic}/config", this::updateTopicConfig);
        
        // Message operations
        app.get("/api/clusters/{cluster}/topics/{topic}/messages", this::getMessages);
        app.post("/api/clusters/{cluster}/topics/{topic}/messages", this::produceMessage);
        app.post("/api/clusters/{cluster}/topics/{topic}/messages/search", this::searchMessages);
        app.post("/api/clusters/{cluster}/topics/{topic}/messages/export", this::exportMessages);
        app.post("/api/clusters/{cluster}/topics/{topic}/messages/import", this::importMessages);
        
        // Consumer group operations
        app.get("/api/clusters/{cluster}/consumer-groups", this::getConsumerGroups);
        app.get("/api/clusters/{cluster}/consumer-groups/{group}", this::getConsumerGroupDetails);
        app.get("/api/clusters/{cluster}/consumer-groups/{group}/offsets", this::getConsumerGroupOffsets);
        app.post("/api/clusters/{cluster}/consumer-groups/{group}/reset-offsets", this::resetConsumerGroupOffsets);
        app.delete("/api/clusters/{cluster}/consumer-groups/{group}", this::deleteConsumerGroup);
        
        // Broker operations
        app.get("/api/clusters/{cluster}/brokers", this::getBrokers);
        app.get("/api/clusters/{cluster}/brokers/{broker}/config", this::getBrokerConfig);
        app.get("/api/clusters/{cluster}/config", this::getClusterConfig);
        
        // ACL operations
        app.get("/api/clusters/{cluster}/acls", this::getAcls);
        app.post("/api/clusters/{cluster}/acls", this::createAcl);
        app.delete("/api/clusters/{cluster}/acls", this::deleteAcl);
        
        // Metrics operations
        app.get("/api/clusters/{cluster}/metrics", this::getClusterMetrics);
        app.get("/api/clusters/{cluster}/topics/{topic}/metrics", this::getTopicMetrics);
        app.get("/api/clusters/{cluster}/consumer-groups/{group}/lag", this::getConsumerGroupLag);
        
        // Schema Registry operations
        app.get("/api/schema-registry/{url}/subjects", this::getSchemaSubjects);
        app.get("/api/schema-registry/{url}/subjects/{subject}/versions", this::getSubjectVersions);
        app.get("/api/schema-registry/{url}/subjects/{subject}/versions/{version}", this::getSchema);
        app.post("/api/schema-registry/{url}/subjects/{subject}/versions", this::registerSchema);
        app.delete("/api/schema-registry/{url}/subjects/{subject}", this::deleteSubject);
        
        // Kafka Connect operations
        app.get("/api/connect/{url}/connectors", this::getConnectors);
        app.get("/api/connect/{url}/connectors/{connector}", this::getConnector);
        app.post("/api/connect/{url}/connectors", this::createConnector);
        app.put("/api/connect/{url}/connectors/{connector}/config", this::updateConnectorConfig);
        app.delete("/api/connect/{url}/connectors/{connector}", this::deleteConnector);
        app.put("/api/connect/{url}/connectors/{connector}/pause", this::pauseConnector);
        app.put("/api/connect/{url}/connectors/{connector}/resume", this::resumeConnector);
        app.post("/api/connect/{url}/connectors/{connector}/restart", this::restartConnector);
    }
    
    // Route handlers
    private void healthCheck(Context ctx) {
        ctx.json(Map.of("status", "healthy", "timestamp", System.currentTimeMillis()));
    }
    
    private void getTopics(Context ctx) {
        String cluster = ctx.pathParam("cluster");
        kafkaService.getTopicsAsync(cluster)
            .thenAccept(topics -> ctx.json(topics))
            .exceptionally(throwable -> {
                ctx.status(500).json(Map.of("error", throwable.getMessage()));
                return null;
            });
    }
    
    private void createTopic(Context ctx) {
        String cluster = ctx.pathParam("cluster");
        Map<String, Object> request = ctx.bodyAsClass(Map.class);
        String topicName = (String) request.get("name");
        int partitions = (Integer) request.get("partitions");
        int replicationFactor = (Integer) request.get("replicationFactor");
        
        kafkaService.createTopicAsync(cluster, topicName, partitions, replicationFactor)
            .thenRun(() -> ctx.json(Map.of("success", true)))
            .exceptionally(throwable -> {
                ctx.status(500).json(Map.of("error", throwable.getMessage()));
                return null;
            });
    }
    
    private void deleteTopic(Context ctx) {
        String cluster = ctx.pathParam("cluster");
        String topic = ctx.pathParam("topic");
        
        kafkaService.deleteTopicAsync(cluster, topic)
            .thenRun(() -> ctx.json(Map.of("success", true)))
            .exceptionally(throwable -> {
                ctx.status(500).json(Map.of("error", throwable.getMessage()));
                return null;
            });
    }
    
    private void getTopicConfig(Context ctx) {
        String cluster = ctx.pathParam("cluster");
        String topic = ctx.pathParam("topic");
        
        kafkaService.getTopicConfigAsync(cluster, topic)
            .thenAccept(config -> ctx.json(config))
            .exceptionally(throwable -> {
                ctx.status(500).json(Map.of("error", throwable.getMessage()));
                return null;
            });
    }
    
    private void updateTopicConfig(Context ctx) {
        String cluster = ctx.pathParam("cluster");
        String topic = ctx.pathParam("topic");
        Map<String, String> config = ctx.bodyAsClass(Map.class);
        
        kafkaService.updateTopicConfigAsync(cluster, topic, config)
            .thenRun(() -> ctx.json(Map.of("success", true)))
            .exceptionally(throwable -> {
                ctx.status(500).json(Map.of("error", throwable.getMessage()));
                return null;
            });
    }
    
    private void getMessages(Context ctx) {
        String cluster = ctx.pathParam("cluster");
        String topic = ctx.pathParam("topic");
        int partition = Integer.parseInt(ctx.queryParam("partition") != null ? ctx.queryParam("partition") : "0");
        long fromOffset = Long.parseLong(ctx.queryParam("fromOffset") != null ? ctx.queryParam("fromOffset") : "0");
        long toOffset = Long.parseLong(ctx.queryParam("toOffset") != null ? ctx.queryParam("toOffset") : "100");
        
        kafkaService.getMessagesBetweenOffsetsAsync(cluster, topic, partition, fromOffset, toOffset)
            .thenAccept(messages -> ctx.json(messages))
            .exceptionally(throwable -> {
                ctx.status(500).json(Map.of("error", throwable.getMessage()));
                return null;
            });
    }
    
    private void produceMessage(Context ctx) {
        String cluster = ctx.pathParam("cluster");
        String topic = ctx.pathParam("topic");
        Map<String, Object> request = ctx.bodyAsClass(Map.class);
        
        String key = (String) request.get("key");
        String value = (String) request.get("value");
        Map<String, String> headers = (Map<String, String>) request.get("headers");
        int partition = (Integer) request.getOrDefault("partition", -1);
        
        kafkaService.produceMessageAsync(cluster, topic, key, value, headers, partition)
            .thenRun(() -> ctx.json(Map.of("success", true)))
            .exceptionally(throwable -> {
                ctx.status(500).json(Map.of("error", throwable.getMessage()));
                return null;
            });
    }
    
    private void searchMessages(Context ctx) {
        String cluster = ctx.pathParam("cluster");
        String topic = ctx.pathParam("topic");
        Map<String, Object> request = ctx.bodyAsClass(Map.class);
        
        int partition = (Integer) request.get("partition");
        String searchPattern = (String) request.get("searchPattern");
        boolean searchInKey = (Boolean) request.getOrDefault("searchInKey", true);
        boolean searchInValue = (Boolean) request.getOrDefault("searchInValue", true);
        int maxResults = (Integer) request.getOrDefault("maxResults", 100);
        
        kafkaService.searchMessagesAsync(cluster, topic, partition, searchPattern, searchInKey, searchInValue, maxResults)
            .thenAccept(messages -> ctx.json(messages))
            .exceptionally(throwable -> {
                ctx.status(500).json(Map.of("error", throwable.getMessage()));
                return null;
            });
    }
    
    // Placeholder implementations for other endpoints
    private void exportMessages(Context ctx) { ctx.json(Map.of("message", "Export not implemented")); }
    private void importMessages(Context ctx) { ctx.json(Map.of("message", "Import not implemented")); }
    private void getConsumerGroups(Context ctx) { ctx.json(Map.of("message", "Consumer groups not implemented")); }
    private void getConsumerGroupDetails(Context ctx) { ctx.json(Map.of("message", "Consumer group details not implemented")); }
    private void getConsumerGroupOffsets(Context ctx) { ctx.json(Map.of("message", "Consumer group offsets not implemented")); }
    private void resetConsumerGroupOffsets(Context ctx) { ctx.json(Map.of("message", "Reset offsets not implemented")); }
    private void deleteConsumerGroup(Context ctx) { ctx.json(Map.of("message", "Delete consumer group not implemented")); }
    private void getBrokers(Context ctx) { ctx.json(Map.of("message", "Brokers not implemented")); }
    private void getBrokerConfig(Context ctx) { ctx.json(Map.of("message", "Broker config not implemented")); }
    private void getClusterConfig(Context ctx) { ctx.json(Map.of("message", "Cluster config not implemented")); }
    private void getAcls(Context ctx) { ctx.json(Map.of("message", "ACLs not implemented")); }
    private void createAcl(Context ctx) { ctx.json(Map.of("message", "Create ACL not implemented")); }
    private void deleteAcl(Context ctx) { ctx.json(Map.of("message", "Delete ACL not implemented")); }
    private void getClusterMetrics(Context ctx) { ctx.json(Map.of("message", "Cluster metrics not implemented")); }
    private void getTopicMetrics(Context ctx) { ctx.json(Map.of("message", "Topic metrics not implemented")); }
    private void getConsumerGroupLag(Context ctx) { ctx.json(Map.of("message", "Consumer group lag not implemented")); }
    private void getSchemaSubjects(Context ctx) { ctx.json(Map.of("message", "Schema subjects not implemented")); }
    private void getSubjectVersions(Context ctx) { ctx.json(Map.of("message", "Subject versions not implemented")); }
    private void getSchema(Context ctx) { ctx.json(Map.of("message", "Schema not implemented")); }
    private void registerSchema(Context ctx) { ctx.json(Map.of("message", "Register schema not implemented")); }
    private void deleteSubject(Context ctx) { ctx.json(Map.of("message", "Delete subject not implemented")); }
    private void getConnectors(Context ctx) { ctx.json(Map.of("message", "Connectors not implemented")); }
    private void getConnector(Context ctx) { ctx.json(Map.of("message", "Connector not implemented")); }
    private void createConnector(Context ctx) { ctx.json(Map.of("message", "Create connector not implemented")); }
    private void updateConnectorConfig(Context ctx) { ctx.json(Map.of("message", "Update connector config not implemented")); }
    private void deleteConnector(Context ctx) { ctx.json(Map.of("message", "Delete connector not implemented")); }
    private void pauseConnector(Context ctx) { ctx.json(Map.of("message", "Pause connector not implemented")); }
    private void resumeConnector(Context ctx) { ctx.json(Map.of("message", "Resume connector not implemented")); }
    private void restartConnector(Context ctx) { ctx.json(Map.of("message", "Restart connector not implemented")); }
}