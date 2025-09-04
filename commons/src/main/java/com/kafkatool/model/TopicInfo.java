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
    
    // Schema configuration overrides
    private String schemaFormat; // Can override cluster default
    private String inlineAvroSchema;
    private String inlineProtobufSchema;
    private boolean useInlineSchema = false;
    
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
    
    // Schema configuration methods
    public String getSchemaFormat() {
        return schemaFormat;
    }
    
    public void setSchemaFormat(String schemaFormat) {
        this.schemaFormat = schemaFormat;
    }
    
    public String getInlineAvroSchema() {
        return inlineAvroSchema;
    }
    
    public void setInlineAvroSchema(String inlineAvroSchema) {
        this.inlineAvroSchema = inlineAvroSchema;
    }
    
    public String getInlineProtobufSchema() {
        return inlineProtobufSchema;
    }
    
    public void setInlineProtobufSchema(String inlineProtobufSchema) {
        this.inlineProtobufSchema = inlineProtobufSchema;
    }
    
    public boolean isUseInlineSchema() {
        return useInlineSchema;
    }
    
    public void setUseInlineSchema(boolean useInlineSchema) {
        this.useInlineSchema = useInlineSchema;
    }
    
    @Override
    public String toString() {
        return name != null ? name : "Unnamed Topic";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TopicInfo that = (TopicInfo) obj;
        return java.util.Objects.equals(name, that.name);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(name);
    }
}