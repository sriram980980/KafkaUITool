package com.kafkatool.model;

import javafx.beans.property.*;

/**
 * Model class representing a Kafka topic
 */
public class TopicInfo {
    
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty partitions = new SimpleIntegerProperty();
    private final IntegerProperty replicationFactor = new SimpleIntegerProperty();
    private final StringProperty config = new SimpleStringProperty();
    
    public TopicInfo() {}
    
    public TopicInfo(String name, int partitions, int replicationFactor) {
        setName(name);
        setPartitions(partitions);
        setReplicationFactor(replicationFactor);
    }
    
    // Name property
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    // Partitions property
    public int getPartitions() {
        return partitions.get();
    }
    
    public void setPartitions(int partitions) {
        this.partitions.set(partitions);
    }
    
    public IntegerProperty partitionsProperty() {
        return partitions;
    }
    
    // Replication factor property
    public int getReplicationFactor() {
        return replicationFactor.get();
    }
    
    public void setReplicationFactor(int replicationFactor) {
        this.replicationFactor.set(replicationFactor);
    }
    
    public IntegerProperty replicationFactorProperty() {
        return replicationFactor;
    }
    
    // Config property
    public String getConfig() {
        return config.get();
    }
    
    public void setConfig(String config) {
        this.config.set(config);
    }
    
    public StringProperty configProperty() {
        return config;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TopicInfo topicInfo = (TopicInfo) obj;
        return getName().equals(topicInfo.getName());
    }
    
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}