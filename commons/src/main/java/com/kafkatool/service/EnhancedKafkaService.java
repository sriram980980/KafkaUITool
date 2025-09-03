package com.kafkatool.service;

import com.kafkatool.model.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.io.File;

/**
 * Enhanced Kafka service interface with advanced features
 */
public interface EnhancedKafkaService extends KafkaService {
    
    // ===== SECURITY AND ACL MANAGEMENT =====
    
    /**
     * Get all ACLs in the cluster
     */
    CompletableFuture<List<AclInfo>> getAclsAsync(String brokerUrls);
    
    /**
     * Create ACL
     */
    CompletableFuture<Void> createAclAsync(String brokerUrls, AclInfo acl);
    
    /**
     * Delete ACL
     */
    CompletableFuture<Void> deleteAclAsync(String brokerUrls, AclInfo acl);
    
    /**
     * Get ACLs for a specific resource
     */
    CompletableFuture<List<AclInfo>> getResourceAclsAsync(String brokerUrls, String resourceType, String resourceName);
    
    // ===== METRICS AND MONITORING =====
    
    /**
     * Get cluster metrics
     */
    CompletableFuture<List<MetricsInfo>> getClusterMetricsAsync(String brokerUrls);
    
    /**
     * Get topic metrics
     */
    CompletableFuture<List<MetricsInfo>> getTopicMetricsAsync(String brokerUrls, String topicName);
    
    /**
     * Get consumer group lag metrics
     */
    CompletableFuture<List<MetricsInfo>> getConsumerGroupLagAsync(String brokerUrls, String groupId);
    
    /**
     * Get broker metrics
     */
    CompletableFuture<List<MetricsInfo>> getBrokerMetricsAsync(String brokerUrls, int brokerId);
    
    // ===== ADVANCED MESSAGE OPERATIONS =====
    
    /**
     * Export messages to JSON file
     */
    CompletableFuture<Void> exportMessagesToJsonAsync(String brokerUrls, String topicName, 
                                                      int partition, long fromOffset, long toOffset, File outputFile);
    
    /**
     * Export messages to CSV file
     */
    CompletableFuture<Void> exportMessagesToCsvAsync(String brokerUrls, String topicName, 
                                                     int partition, long fromOffset, long toOffset, File outputFile);
    
    /**
     * Import messages from JSON file
     */
    CompletableFuture<Integer> importMessagesFromJsonAsync(String brokerUrls, String topicName, File inputFile);
    
    /**
     * Import messages from CSV file
     */
    CompletableFuture<Integer> importMessagesFromCsvAsync(String brokerUrls, String topicName, File inputFile);
    
    /**
     * Bulk produce messages
     */
    CompletableFuture<Integer> bulkProduceMessagesAsync(String brokerUrls, String topicName, 
                                                       List<KafkaMessage> messages);
    
    /**
     * Replay messages from one topic to another
     */
    CompletableFuture<Integer> replayMessagesAsync(String brokerUrls, String sourceTopic, String targetTopic,
                                                  int sourcePartition, long fromOffset, long toOffset);
    
    /**
     * Advanced search with regex patterns
     */
    CompletableFuture<List<KafkaMessage>> searchMessagesRegexAsync(String brokerUrls, String topicName,
                                                                  int partition, String regexPattern,
                                                                  boolean searchInKey, boolean searchInValue,
                                                                  int maxResults);
    
    /**
     * Advanced search with regex patterns and timestamp filtering
     */
    CompletableFuture<List<KafkaMessage>> searchMessagesRegexWithTimestampAsync(String brokerUrls, String topicName,
                                                                               int partition, String regexPattern,
                                                                               boolean searchInKey, boolean searchInValue,
                                                                               long fromTimestamp, long toTimestamp,
                                                                               int maxResults);
    
    /**
     * Get messages between specific timestamps (similar to offset-based search but time-based)
     */
    CompletableFuture<List<KafkaMessage>> getMessagesBetweenTimestampsAsync(String brokerUrls,
                                                                           String topicName,
                                                                           int partition,
                                                                           long fromTimestamp,
                                                                           long toTimestamp,
                                                                           int maxResults);
    
    /**
     * Transform messages with custom logic
     */
    CompletableFuture<List<KafkaMessage>> transformMessagesAsync(String brokerUrls, String topicName,
                                                                int partition, long fromOffset, long toOffset,
                                                                String transformationScript);
    
    // ===== DATA MASKING AND SECURITY =====
    
    /**
     * Get messages with field masking applied
     */
    CompletableFuture<List<KafkaMessage>> getMaskedMessagesAsync(String brokerUrls, String topicName,
                                                               int partition, long fromOffset, long toOffset,
                                                               Map<String, String> maskingRules);
    
    /**
     * Validate message against schema
     */
    CompletableFuture<Boolean> validateMessageAsync(String schemaRegistryUrl, String subject, 
                                                   KafkaMessage message);
    
    // ===== MULTI-CLUSTER OPERATIONS =====
    
    /**
     * Mirror topics between clusters
     */
    CompletableFuture<Void> mirrorTopicAsync(String sourceCluster, String targetCluster, 
                                           String topicName, Map<String, String> config);
    
    /**
     * Compare topics between clusters
     */
    CompletableFuture<Map<String, Object>> compareTopicsAsync(String cluster1, String cluster2, String topicName);
    
    // ===== BACKUP AND RESTORE =====
    
    /**
     * Create topic backup
     */
    CompletableFuture<Void> backupTopicAsync(String brokerUrls, String topicName, File backupDirectory);
    
    /**
     * Restore topic from backup
     */
    CompletableFuture<Void> restoreTopicAsync(String brokerUrls, String topicName, File backupDirectory);
}