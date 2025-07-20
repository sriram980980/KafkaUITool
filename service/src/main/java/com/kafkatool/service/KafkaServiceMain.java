package com.kafkatool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Kafka UI Tool Service
 * This launches the REST API server without JavaFX dependencies
 */
public class KafkaServiceMain {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaServiceMain.class);
    
    public static void main(String[] args) {
        logger.info("Starting Kafka UI Tool Service");
        
        try {
            // Delegate to the RestApiMain for actual service implementation
            RestApiMain.main(args);
        } catch (Exception e) {
            logger.error("Failed to start Kafka UI Tool Service", e);
            System.err.println("ERROR: Failed to start service: " + e.getMessage());
            System.exit(1);
        }
    }
}