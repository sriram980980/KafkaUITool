package com.kafkatool.model;

import java.util.Map;

/**
 * Model class representing cluster configuration
 */
public class ClusterConfig {
    private String configName;
    private String name; // alias for configName
    private String configValue;
    private String value; // alias for configValue  
    private String configSource;
    private String source; // alias for configSource
    private boolean isDefault;
    private boolean isSensitive;
    private boolean readOnly;
    private String documentation;
    private String type;
    
    public ClusterConfig() {}
    
    public ClusterConfig(String configName, String configValue) {
        this.configName = configName;
        this.name = configName;
        this.configValue = configValue;
        this.value = configValue;
    }
    
    // Getters and setters
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { 
        this.configName = configName; 
        this.name = configName;
    }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        this.configName = name;
    }
    
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { 
        this.configValue = configValue; 
        this.value = configValue;
    }
    
    public String getValue() { return value; }
    public void setValue(String value) { 
        this.value = value; 
        this.configValue = value;
    }
    
    public String getConfigSource() { return configSource; }
    public void setConfigSource(String configSource) { 
        this.configSource = configSource; 
        this.source = configSource;
    }
    
    public String getSource() { return source; }
    public void setSource(String source) { 
        this.source = source; 
        this.configSource = source;
    }
    
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    
    public boolean isSensitive() { return isSensitive; }
    public void setSensitive(boolean isSensitive) { this.isSensitive = isSensitive; }
    
    public boolean isReadOnly() { return readOnly; }
    public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }
    
    public String getDocumentation() { return documentation; }
    public void setDocumentation(String documentation) { this.documentation = documentation; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}