package com.kafkatool.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Model class representing a Kafka message
 */
public class KafkaMessage {
    
    private final StringProperty topic = new SimpleStringProperty();
    private final LongProperty offset = new SimpleLongProperty();
    private final IntegerProperty partition = new SimpleIntegerProperty();
    private final StringProperty key = new SimpleStringProperty();
    private final StringProperty value = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> timestamp = new SimpleObjectProperty<>();
    private final ObjectProperty<Map<String, String>> headers = new SimpleObjectProperty<>();
    
    public KafkaMessage() {}
    
    public KafkaMessage(String topic, long offset, int partition, String key, String value, 
                       LocalDateTime timestamp, Map<String, String> headers) {
        setTopic(topic);
        setOffset(offset);
        setPartition(partition);
        setKey(key);
        setValue(value);
        setTimestamp(timestamp);
        setHeaders(headers);
    }
    
    // Topic property
    public String getTopic() {
        return topic.get();
    }
    
    public void setTopic(String topic) {
        this.topic.set(topic);
    }
    
    public StringProperty topicProperty() {
        return topic;
    }
    
    // Offset property
    public long getOffset() {
        return offset.get();
    }
    
    public void setOffset(long offset) {
        this.offset.set(offset);
    }
    
    public LongProperty offsetProperty() {
        return offset;
    }
    
    // Partition property
    public int getPartition() {
        return partition.get();
    }
    
    public void setPartition(int partition) {
        this.partition.set(partition);
    }
    
    public IntegerProperty partitionProperty() {
        return partition;
    }
    
    // Key property
    public String getKey() {
        return key.get();
    }
    
    public void setKey(String key) {
        this.key.set(key);
    }
    
    public StringProperty keyProperty() {
        return key;
    }
    
    // Value property
    public String getValue() {
        return value.get();
    }
    
    public void setValue(String value) {
        this.value.set(value);
    }
    
    public StringProperty valueProperty() {
        return value;
    }
    
    // Timestamp property
    public LocalDateTime getTimestamp() {
        return timestamp.get();
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp.set(timestamp);
    }
    
    public ObjectProperty<LocalDateTime> timestampProperty() {
        return timestamp;
    }
    
    // Headers property
    public Map<String, String> getHeaders() {
        return headers.get();
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers.set(headers);
    }
    
    public ObjectProperty<Map<String, String>> headersProperty() {
        return headers;
    }
    
    // Helper method to get headers as formatted string for display
    public String getHeadersAsString() {
        Map<String, String> headerMap = getHeaders();
        if (headerMap == null || headerMap.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        headerMap.forEach((k, v) -> {
            if (sb.length() > 0) sb.append(", ");
            sb.append(k).append("=").append(v);
        });
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("Message[topic=%s, offset=%d, partition=%d, key=%s, value=%s]",
                getTopic(), getOffset(), getPartition(), getKey(), getValue());
    }
}