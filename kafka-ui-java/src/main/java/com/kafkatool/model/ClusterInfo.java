package com.kafkatool.model;

import javafx.beans.property.*;

/**
 * Model class representing a Kafka cluster configuration
 */
public class ClusterInfo {
    
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty brokerUrls = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty("Disconnected");
    private final BooleanProperty connectByDefault = new SimpleBooleanProperty(false);
    private final StringProperty kafkaVersion = new SimpleStringProperty("");
    
    public ClusterInfo() {}
    
    public ClusterInfo(String name, String brokerUrls) {
        setName(name);
        setBrokerUrls(brokerUrls);
    }
    
    public ClusterInfo(String name, String brokerUrls, boolean connectByDefault) {
        this(name, brokerUrls);
        setConnectByDefault(connectByDefault);
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
    
    // Broker URLs property
    public String getBrokerUrls() {
        return brokerUrls.get();
    }
    
    public void setBrokerUrls(String brokerUrls) {
        this.brokerUrls.set(brokerUrls);
    }
    
    public StringProperty brokerUrlsProperty() {
        return brokerUrls;
    }
    
    // Status property
    public String getStatus() {
        return status.get();
    }
    
    public void setStatus(String status) {
        this.status.set(status);
    }
    
    public StringProperty statusProperty() {
        return status;
    }
    
    // Connect by default property
    public boolean isConnectByDefault() {
        return connectByDefault.get();
    }
    
    public void setConnectByDefault(boolean connectByDefault) {
        this.connectByDefault.set(connectByDefault);
    }
    
    public BooleanProperty connectByDefaultProperty() {
        return connectByDefault;
    }
    
    // Kafka version property
    public String getKafkaVersion() {
        return kafkaVersion.get();
    }
    
    public void setKafkaVersion(String kafkaVersion) {
        this.kafkaVersion.set(kafkaVersion);
    }
    
    public StringProperty kafkaVersionProperty() {
        return kafkaVersion;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", getName(), getBrokerUrls());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClusterInfo that = (ClusterInfo) obj;
        return getName().equals(that.getName()) && 
               getBrokerUrls().equals(that.getBrokerUrls());
    }
    
    @Override
    public int hashCode() {
        return getName().hashCode() * 31 + getBrokerUrls().hashCode();
    }
}