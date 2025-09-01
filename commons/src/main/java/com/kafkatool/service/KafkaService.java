package com.kafkatool.service;

import com.kafkatool.model.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for Kafka operations
 */
public interface KafkaService {
    
    /**
     * Test connection to a Kafka cluster
     */
    CompletableFuture<Boolean> testConnectionAsync(String brokerUrls);
    
    /**
     * Test connection to a Kafka cluster with authentication
     */
    CompletableFuture<Boolean> testConnectionAsync(ClusterInfo clusterInfo);
    
    /**
     * Get Kafka version information
     */
    CompletableFuture<String> getKafkaVersionAsync(String brokerUrls);
    
    /**
     * Get Kafka version information with authentication
     */
    CompletableFuture<String> getKafkaVersionAsync(ClusterInfo clusterInfo);
    
    /**
     * Get list of topics in the cluster
     */
    CompletableFuture<List<TopicInfo>> getTopicsAsync(String brokerUrls);
    
    /**
     * Get list of topics in the cluster with authentication
     */
    CompletableFuture<List<TopicInfo>> getTopicsAsync(ClusterInfo clusterInfo);
    
    /**
     * Create a new topic
     */
    CompletableFuture<Void> createTopicAsync(String brokerUrls, String topicName, 
                                            int partitions, int replicationFactor);
    
    /**
     * Delete a topic
     */
    CompletableFuture<Void> deleteTopicAsync(String brokerUrls, String topicName);
    
    /**
     * Add partitions to an existing topic
     */
    CompletableFuture<Void> addPartitionsToTopicAsync(String brokerUrls, String topicName, int newPartitionCount);
    
    /**
     * Get topic configuration
     */
    CompletableFuture<Map<String, String>> getTopicConfigAsync(String brokerUrls, String topicName);
    
    /**
     * Update topic configuration
     */
    CompletableFuture<Void> updateTopicConfigAsync(String brokerUrls, String topicName, 
                                                   Map<String, String> config);
    
    /**
     * Get partition count for a topic
     */
    CompletableFuture<List<Integer>> getPartitionsAsync(String brokerUrls, String topicName);
    
    /**
     * Get partition offset information (low, high watermarks)
     */
    CompletableFuture<PartitionOffsets> getPartitionOffsetsAsync(String brokerUrls, 
                                                                String topicName, int partition);
    
    /**
     * Get latest messages from a partition
     */
    CompletableFuture<List<KafkaMessage>> getLatestMessagesAsync(String brokerUrls, 
                                                                String topicName, 
                                                                int partition, int count);
    
    /**
     * Get messages between specific offsets
     */
    CompletableFuture<List<KafkaMessage>> getMessagesBetweenOffsetsAsync(String brokerUrls,
                                                                        String topicName,
                                                                        int partition,
                                                                        long fromOffset,
                                                                        long toOffset);
    
    /**
     * Produce a message to a topic
     */
    CompletableFuture<Void> produceMessageAsync(String brokerUrls, String topicName,
                                               String key, String value,
                                               Map<String, String> headers, int partition);
    
    /**
     * Search messages by key or value pattern
     */
    CompletableFuture<List<KafkaMessage>> searchMessagesAsync(String brokerUrls,
                                                             String topicName,
                                                             int partition,
                                                             String searchPattern,
                                                             boolean searchInKey,
                                                             boolean searchInValue,
                                                             boolean searchInHeaders,
                                                             int maxResults);
                                                             
    // Backward compatibility method
    default CompletableFuture<List<KafkaMessage>> searchMessagesAsync(String brokerUrls,
                                                                     String topicName,
                                                                     int partition,
                                                                     String searchPattern,
                                                                     boolean searchInKey,
                                                                     boolean searchInValue,
                                                                     int maxResults) {
        return searchMessagesAsync(brokerUrls, topicName, partition, searchPattern,
                                 searchInKey, searchInValue, false, maxResults);
    }
    
    // ===== CONSUMER GROUP MANAGEMENT =====
    
    /**
     * Get all consumer groups in the cluster
     */
    CompletableFuture<List<ConsumerGroupInfo>> getConsumerGroupsAsync(String brokerUrls);
    
    /**
     * Get detailed information about a specific consumer group
     */
    CompletableFuture<ConsumerGroupInfo> getConsumerGroupDetailsAsync(String brokerUrls, String groupId);
    
    /**
     * Get offset information for all partitions consumed by a consumer group
     */
    CompletableFuture<List<ConsumerGroupOffsets>> getConsumerGroupOffsetsAsync(String brokerUrls, String groupId);
    
    /**
     * Reset consumer group offsets to earliest
     */
    CompletableFuture<Void> resetConsumerGroupOffsetsToEarliestAsync(String brokerUrls, String groupId, String topicName);
    
    /**
     * Reset consumer group offsets to latest
     */
    CompletableFuture<Void> resetConsumerGroupOffsetsToLatestAsync(String brokerUrls, String groupId, String topicName);
    
    /**
     * Reset consumer group offsets to specific offset
     */
    CompletableFuture<Void> resetConsumerGroupOffsetsToOffsetAsync(String brokerUrls, String groupId, 
                                                                  String topicName, int partition, long offset);
    
    /**
     * Delete a consumer group
     */
    CompletableFuture<Void> deleteConsumerGroupAsync(String brokerUrls, String groupId);
    
    // ===== BROKER AND CLUSTER MANAGEMENT =====
    
    /**
     * Get all brokers in the cluster
     */
    CompletableFuture<List<BrokerInfo>> getBrokersAsync(String brokerUrls);
    
    /**
     * Get cluster configuration properties
     */
    CompletableFuture<List<ClusterConfig>> getClusterConfigAsync(String brokerUrls);
    
    /**
     * Update cluster configuration
     */
    CompletableFuture<Void> updateClusterConfigAsync(String brokerUrls, Map<String, String> config);
    
    /**
     * Get broker configuration
     */
    CompletableFuture<List<ClusterConfig>> getBrokerConfigAsync(String brokerUrls, int brokerId);
    
    /**
     * Update broker configuration
     */
    CompletableFuture<Void> updateBrokerConfigAsync(String brokerUrls, int brokerId, Map<String, String> config);
    
    /**
     * Inner class for partition offset information
     */
    class PartitionOffsets {
        private final long lowWatermark;
        private final long highWatermark;
        
        public PartitionOffsets(long lowWatermark, long highWatermark) {
            this.lowWatermark = lowWatermark;
            this.highWatermark = highWatermark;
        }
        
        public long getLowWatermark() {
            return lowWatermark;
        }
        
        public long getHighWatermark() {
            return highWatermark;
        }
        
        public long getMessageCount() {
            return highWatermark - lowWatermark;
        }
    }
}