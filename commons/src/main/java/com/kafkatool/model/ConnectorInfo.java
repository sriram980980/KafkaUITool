package com.kafkatool.model;

import java.util.Map;

/**
 * Model class representing connector information
 */
public class ConnectorInfo {
    private String name;
    private String type;
    private String state;
    private String workerUrl;
    private Map<String, String> config;
    private String errorMessage;
    
    public ConnectorInfo() {}
    
    public ConnectorInfo(String name, String type, String state) {
        this.name = name;
        this.type = type;
        this.state = state;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getWorkerUrl() { return workerUrl; }
    public void setWorkerUrl(String workerUrl) { this.workerUrl = workerUrl; }
    
    public Map<String, String> getConfig() { return config; }
    public void setConfig(Map<String, String> config) { this.config = config; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}