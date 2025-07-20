package com.kafkatool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standalone REST API server launcher for Kafka UI Tool
 * This is a simplified version that starts a basic API server
 */
public class RestApiMain {
    
    private static final Logger logger = LoggerFactory.getLogger(RestApiMain.class);
    
    public static void main(String[] args) {
        int port = 8080; // default port
        
        // Parse command line arguments for port
        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    logger.error("Invalid port number: {}", args[i + 1]);
                    System.exit(1);
                }
            }
        }
        
        logger.info("Starting Kafka UI Tool REST API Server on port {}", port);
        
        try {
            // Simple HTTP server implementation using Javalin
            io.javalin.Javalin app = io.javalin.Javalin.create()
                .get("/api/health", ctx -> {
                    ctx.json(java.util.Map.of(
                        "status", "healthy", 
                        "timestamp", System.currentTimeMillis(),
                        "service", "Kafka UI Tool REST API"
                    ));
                })
                .get("/api/info", ctx -> {
                    ctx.json(java.util.Map.of(
                        "name", "Kafka UI Tool REST API",
                        "version", "2.0.0",
                        "description", "REST API for Kafka UI Tool management operations"
                    ));
                });
            
            app.start(port);
            
            logger.info("REST API Server started successfully on port {}", port);
            logger.info("Health check available at: http://localhost:{}/api/health", port);
            logger.info("Info endpoint available at: http://localhost:{}/api/info", port);
            logger.info("Press Ctrl+C to stop the server");
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down REST API Server...");
                app.stop();
                logger.info("REST API Server stopped");
            }));
            
            // Keep the main thread alive
            Thread.currentThread().join();
            
        } catch (Exception e) {
            logger.error("Failed to start REST API Server", e);
            System.exit(1);
        }
    }
}