package com.kafkatool.service;

import com.kafkatool.model.KafkaMessage;
import com.kafkatool.model.TopicInfo;

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
     * Get Kafka version information
     */
    CompletableFuture<String> getKafkaVersionAsync(String brokerUrls);
    
    /**
     * Get list of topics in the cluster
     */
    CompletableFuture<List<TopicInfo>> getTopicsAsync(String brokerUrls);
    
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
                                                             int maxResults);
    
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