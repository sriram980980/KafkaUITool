package com.kafkatool.model;

import javafx.beans.property.*;

/**
 * Model class representing an LLM provider connection configuration
 */
public class LLMProviderInfo {
    private StringProperty name = new SimpleStringProperty();
    private StringProperty type = new SimpleStringProperty(); // ollama, openai, anthropic, etc.
    private StringProperty url = new SimpleStringProperty();
    private StringProperty apiKey = new SimpleStringProperty();
    private StringProperty model = new SimpleStringProperty();
    private BooleanProperty isDefault = new SimpleBooleanProperty(false);
    private BooleanProperty isConnected = new SimpleBooleanProperty(false);
    
    public LLMProviderInfo() {}
    
    public LLMProviderInfo(String name, String type, String url) {
        setName(name);
        setType(type);
        setUrl(url);
    }
    
    // JavaFX Property methods
    public StringProperty nameProperty() { return name; }
    public StringProperty typeProperty() { return type; }
    public StringProperty urlProperty() { return url; }
    public StringProperty apiKeyProperty() { return apiKey; }
    public StringProperty modelProperty() { return model; }
    public BooleanProperty isDefaultProperty() { return isDefault; }
    public BooleanProperty isConnectedProperty() { return isConnected; }
    
    // Regular getters and setters
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    
    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    
    public String getUrl() { return url.get(); }
    public void setUrl(String url) { this.url.set(url); }
    
    public String getApiKey() { return apiKey.get(); }
    public void setApiKey(String apiKey) { this.apiKey.set(apiKey); }
    
    public String getModel() { return model.get(); }
    public void setModel(String model) { this.model.set(model); }
    
    public boolean isDefault() { return isDefault.get(); }
    public void setDefault(boolean isDefault) { this.isDefault.set(isDefault); }
    
    public boolean isConnected() { return isConnected.get(); }
    public void setConnected(boolean isConnected) { this.isConnected.set(isConnected); }
    
    /**
     * Returns a display-friendly string representation for UI dropdowns and lists.
     * Format: "name (type)" - matches the UX pattern mentioned in the issue.
     */
    @Override
    public String toString() {
        String displayName = getName() != null ? getName() : "Unknown";
        String displayType = getType() != null ? getType() : "Unknown";
        return String.format("%s (%s)", displayName, displayType);
    }
    
    /**
     * Returns just the name for Map key lookups.
     * Use this method when you need the original name for map.get() operations.
     */
    public String getMapKey() {
        return getName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LLMProviderInfo that = (LLMProviderInfo) obj;
        return java.util.Objects.equals(getName(), that.getName());
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(getName());
    }
}