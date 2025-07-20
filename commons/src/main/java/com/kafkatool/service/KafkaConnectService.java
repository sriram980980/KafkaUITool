package com.kafkatool.service;

import com.kafkatool.model.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for Kafka Connect operations
 */
public interface KafkaConnectService {
    
    /**
     * Test connection to Kafka Connect cluster
     */
    CompletableFuture<Boolean> testConnectionAsync(String connectUrl);
    
    /**
     * Get Kafka Connect cluster information
     */
    CompletableFuture<Map<String, Object>> getClusterInfoAsync(String connectUrl);
    
    /**
     * Get all connectors
     */
    CompletableFuture<List<ConnectorInfo>> getConnectorsAsync(String connectUrl);
    
    /**
     * Get connector details
     */
    CompletableFuture<ConnectorInfo> getConnectorAsync(String connectUrl, String connectorName);
    
    /**
     * Get connector configuration
     */
    CompletableFuture<Map<String, String>> getConnectorConfigAsync(String connectUrl, String connectorName);
    
    /**
     * Create or update a connector
     */
    CompletableFuture<ConnectorInfo> createOrUpdateConnectorAsync(String connectUrl, String connectorName, 
                                                                 Map<String, String> config);
    
    /**
     * Delete a connector
     */
    CompletableFuture<Void> deleteConnectorAsync(String connectUrl, String connectorName);
    
    /**
     * Pause a connector
     */
    CompletableFuture<Void> pauseConnectorAsync(String connectUrl, String connectorName);
    
    /**
     * Resume a connector
     */
    CompletableFuture<Void> resumeConnectorAsync(String connectUrl, String connectorName);
    
    /**
     * Restart a connector
     */
    CompletableFuture<Void> restartConnectorAsync(String connectUrl, String connectorName);
    
    /**
     * Get connector tasks
     */
    CompletableFuture<List<Map<String, Object>>> getConnectorTasksAsync(String connectUrl, String connectorName);
    
    /**
     * Restart a specific task
     */
    CompletableFuture<Void> restartTaskAsync(String connectUrl, String connectorName, int taskId);
    
    /**
     * Get connector status
     */
    CompletableFuture<Map<String, Object>> getConnectorStatusAsync(String connectUrl, String connectorName);
    
    /**
     * Get available connector plugins
     */
    CompletableFuture<List<Map<String, Object>>> getConnectorPluginsAsync(String connectUrl);
    
    /**
     * Validate connector configuration
     */
    CompletableFuture<Map<String, Object>> validateConnectorConfigAsync(String connectUrl, String connectorClass, 
                                                                       Map<String, String> config);
}