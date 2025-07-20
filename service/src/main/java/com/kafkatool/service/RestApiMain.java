package com.kafkatool.service;

import com.kafkatool.service.KafkaService;
import com.kafkatool.service.KafkaServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standalone REST API server launcher for Kafka UI Tool
 * This server provides REST endpoints for Kafka management operations
 * without requiring JavaFX dependencies.
 */
public class RestApiMain {
    
    private static final Logger logger = LoggerFactory.getLogger(RestApiMain.class);
    private static io.javalin.Javalin app;
    private static KafkaService kafkaService;
    
    public static void main(String[] args) {
        int port = 8080; // default port
        
        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                    if (port < 1 || port > 65535) {
                        logger.error("Port must be between 1 and 65535: {}", port);
                        System.exit(1);
                    }
                } catch (NumberFormatException e) {
                    logger.error("Invalid port number: {}", args[i + 1]);
                    System.exit(1);
                }
            } else if ("--help".equals(args[i]) || "-h".equals(args[i])) {
                printUsage();
                System.exit(0);
            }
        }
        
        logger.info("Starting Kafka UI Tool REST API Server on port {}", port);
        
        try {
            // Initialize Kafka service
            kafkaService = new KafkaServiceImpl();
            
            // Create and configure Javalin app
            app = io.javalin.Javalin.create(config -> {
                config.showJavalinBanner = false;
            });
            
            // Configure routes
            setupRoutes();
            
            // Start server
            app.start(port);
            
            logger.info("REST API Server started successfully on port {}", port);
            printServerInfo(port);
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down REST API Server...");
                if (app != null) {
                    app.stop();
                }
                logger.info("REST API Server stopped");
            }));
            
            // Keep the main thread alive
            Thread.currentThread().join();
            
        } catch (Exception e) {
            logger.error("Failed to start REST API Server", e);
            System.err.println("ERROR: Failed to start REST API Server: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void setupRoutes() {
        // Health and info endpoints
        app.get("/api/health", ctx -> {
            ctx.contentType("application/json").json(java.util.Map.of(
                "status", "healthy", 
                "timestamp", System.currentTimeMillis(),
                "service", "Kafka UI Tool REST API",
                "version", "2.0.0"
            ));
        });
        
        app.get("/api/info", ctx -> {
            ctx.contentType("application/json").json(java.util.Map.of(
                "name", "Kafka UI Tool REST API",
                "version", "2.0.0",
                "description", "REST API for Kafka UI Tool management operations",
                "endpoints", java.util.List.of(
                    "/api/health - Health check",
                    "/api/info - Service information",
                    "/api/clusters - Cluster management",
                    "/api/topics - Topic operations",
                    "/api/messages - Message operations"
                )
            ));
        });
        
        // Cluster endpoints
        app.get("/api/clusters", ctx -> {
            try {
                // This would typically return saved cluster configurations
                ctx.contentType("application/json").json(java.util.Map.of(
                    "clusters", java.util.List.of(),
                    "message", "Cluster management endpoints available"
                ));
            } catch (Exception e) {
                logger.error("Error retrieving clusters", e);
                ctx.status(500).contentType("application/json").json(java.util.Map.of("error", e.getMessage()));
            }
        });
        
        // Topic endpoints
        app.get("/api/topics", ctx -> {
            try {
                String brokers = ctx.queryParam("brokers");
                if (brokers == null || brokers.trim().isEmpty()) {
                    ctx.status(400).contentType("application/json").json(java.util.Map.of(
                        "error", "Missing required parameter 'brokers'"
                    ));
                    return;
                }
                
                // Connect and get topics
                // Note: This is a simplified implementation
                ctx.contentType("application/json").json(java.util.Map.of(
                    "topics", java.util.List.of(),
                    "message", "Topic operations endpoints available",
                    "brokers", brokers
                ));
            } catch (Exception e) {
                logger.error("Error retrieving topics", e);
                ctx.status(500).contentType("application/json").json(java.util.Map.of("error", e.getMessage()));
            }
        });
        
        // Message endpoints
        app.get("/api/messages", ctx -> {
            try {
                ctx.contentType("application/json").json(java.util.Map.of(
                    "message", "Message operations endpoints available",
                    "note", "Use POST to produce messages, GET with topic parameter to consume"
                ));
            } catch (Exception e) {
                logger.error("Error in message endpoint", e);
                ctx.status(500).contentType("application/json").json(java.util.Map.of("error", e.getMessage()));
            }
        });
        
        // Error handling
        app.exception(Exception.class, (exception, ctx) -> {
            logger.error("Unhandled exception in API", exception);
            ctx.status(500).contentType("application/json").json(java.util.Map.of(
                "error", "Internal server error",
                "message", exception.getMessage()
            ));
        });
    }
    
    private static void printUsage() {
        System.out.println("Kafka UI Tool REST API Server");
        System.out.println("Usage: java -jar kafka-ui-tool.jar --api-server [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --port <port>    Server port (default: 8080)");
        System.out.println("  --help, -h       Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar kafka-ui-tool.jar --api-server");
        System.out.println("  java -jar kafka-ui-tool.jar --api-server --port 9090");
    }
    
    private static void printServerInfo(int port) {
        System.out.println("========================================");
        System.out.println("Kafka UI Tool REST API Server");
        System.out.println("========================================");
        System.out.println("Server running on: http://localhost:" + port);
        System.out.println();
        System.out.println("Available endpoints:");
        System.out.println("  Health check: http://localhost:" + port + "/api/health");
        System.out.println("  Info:         http://localhost:" + port + "/api/info");
        System.out.println("  Clusters:     http://localhost:" + port + "/api/clusters");
        System.out.println("  Topics:       http://localhost:" + port + "/api/topics");
        System.out.println("  Messages:     http://localhost:" + port + "/api/messages");
        System.out.println();
        System.out.println("Press Ctrl+C to stop the server");
        System.out.println("========================================");
    }
}