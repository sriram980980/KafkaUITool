package com.kafkatool.model;

import java.util.Map;

/**
 * Model class representing a Kafka topic
 * Plain POJO version for use in commons module
 */
public class TopicInfo {
    
    private String name;
    private int partitions;
    private short replicationFactor;
    private Map<String, String> configs;
    
    public TopicInfo() {}
    
    public TopicInfo(String name, int partitions, short replicationFactor) {
        this.name = name;
        this.partitions = partitions;
        this.replicationFactor = replicationFactor;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getPartitions() {
        return partitions;
    }
    
    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }
    
    public short getReplicationFactor() {
        return replicationFactor;
    }
    
    public void setReplicationFactor(short replicationFactor) {
        this.replicationFactor = replicationFactor;
    }
    
    public Map<String, String> getConfigs() {
        return configs;
    }
    
    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }
    
    @Override
    public String toString() {
        return name != null ? name : "Unnamed Topic";
    }
}