package com.kafkatool.model;

import java.util.Map;

/**
 * Model class representing cluster configuration
 */
public class ClusterConfig {
    private String configName;
    private String configValue;
    private String configSource;
    private boolean isDefault;
    private boolean isSensitive;
    private String documentation;
    
    public ClusterConfig() {}
    
    public ClusterConfig(String configName, String configValue) {
        this.configName = configName;
        this.configValue = configValue;
    }
    
    // Getters and setters
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    
    public String getConfigSource() { return configSource; }
    public void setConfigSource(String configSource) { this.configSource = configSource; }
    
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    
    public boolean isSensitive() { return isSensitive; }
    public void setSensitive(boolean isSensitive) { this.isSensitive = isSensitive; }
    
    public String getDocumentation() { return documentation; }
    public void setDocumentation(String documentation) { this.documentation = documentation; }
}