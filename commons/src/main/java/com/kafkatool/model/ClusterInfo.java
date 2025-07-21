package com.kafkatool.model;

/**
 * Model class representing a Kafka cluster configuration
 * Plain POJO version for use in commons module
 */
public class ClusterInfo {
    
    private String name;
    private String brokerUrls;
    private String status = "Disconnected";
    private boolean connectByDefault = false;
    private String kafkaVersion = "";
    
    public ClusterInfo() {}
    
    public ClusterInfo(String name, String brokerUrls) {
        this.name = name;
        this.brokerUrls = brokerUrls;
    }
    
    public ClusterInfo(String name, String brokerUrls, boolean connectByDefault) {
        this(name, brokerUrls);
        this.connectByDefault = connectByDefault;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getBrokerUrls() {
        return brokerUrls;
    }
    
    public void setBrokerUrls(String brokerUrls) {
        this.brokerUrls = brokerUrls;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isConnectByDefault() {
        return connectByDefault;
    }
    
    public void setConnectByDefault(boolean connectByDefault) {
        this.connectByDefault = connectByDefault;
    }
    
    public String getKafkaVersion() {
        return kafkaVersion;
    }
    
    public void setKafkaVersion(String kafkaVersion) {
        this.kafkaVersion = kafkaVersion;
    }
    
    @Override
    public String toString() {
        if (name != null && brokerUrls != null) {
            return String.format("%s (%s)", name, brokerUrls);
        }
        return name != null ? name : "Unnamed Cluster";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClusterInfo that = (ClusterInfo) obj;
        return java.util.Objects.equals(name, that.name) && 
               java.util.Objects.equals(brokerUrls, that.brokerUrls);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, brokerUrls);
    }
}