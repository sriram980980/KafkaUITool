package com.kafkatool.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Model class representing a Kafka message
 */
public class KafkaMessage {
    
    private final LongProperty offset = new SimpleLongProperty();
    private final IntegerProperty partition = new SimpleIntegerProperty();
    private final StringProperty key = new SimpleStringProperty();
    private final StringProperty value = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> timestamp = new SimpleObjectProperty<>();
    private final StringProperty headers = new SimpleStringProperty();
    
    public KafkaMessage() {}
    
    public KafkaMessage(long offset, int partition, String key, String value, 
                       LocalDateTime timestamp, Map<String, String> headers) {
        setOffset(offset);
        setPartition(partition);
        setKey(key);
        setValue(value);
        setTimestamp(timestamp);
        setHeaders(formatHeaders(headers));
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
    public String getHeaders() {
        return headers.get();
    }
    
    public void setHeaders(String headers) {
        this.headers.set(headers);
    }
    
    public StringProperty headersProperty() {
        return headers;
    }
    
    private String formatHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        headers.forEach((k, v) -> {
            if (sb.length() > 0) sb.append(", ");
            sb.append(k).append("=").append(v);
        });
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("Message[offset=%d, partition=%d, key=%s, value=%s]",
                getOffset(), getPartition(), getKey(), getValue());
    }
}