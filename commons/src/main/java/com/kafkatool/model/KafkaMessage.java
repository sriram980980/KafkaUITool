package com.kafkatool.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Model class representing a Kafka message
 * Plain POJO version for use in commons module
 */
public class KafkaMessage {
    
    private String topic;
    private long offset;
    private int partition;
    private String key;
    private String value;
    private LocalDateTime timestamp;
    private Map<String, String> headers;
    
    public KafkaMessage() {}
    
    public KafkaMessage(String topic, int partition, long offset, String key, String value) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.key = key;
        this.value = value;
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor that matches what KafkaServiceImpl expects
    public KafkaMessage(String topic, long offset, int partition, String key, String value, LocalDateTime timestamp, Map<String, String> headers) {
        this.topic = topic;
        this.offset = offset;
        this.partition = partition;
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
        this.headers = headers;
    }
    
    // Getters and setters
    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public long getOffset() {
        return offset;
    }
    
    public void setOffset(long offset) {
        this.offset = offset;
    }
    
    public int getPartition() {
        return partition;
    }
    
    public void setPartition(int partition) {
        this.partition = partition;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    @Override
    public String toString() {
        return String.format("Message[topic=%s, partition=%d, offset=%d]", topic, partition, offset);
    }
}