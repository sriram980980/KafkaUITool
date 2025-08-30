package com.kafkatool.model;

import javafx.beans.property.*;
import java.util.Map;

/**
 * Model class representing connector information
 */
public class ConnectorInfo {
    private StringProperty name = new SimpleStringProperty();
    private StringProperty type = new SimpleStringProperty();
    private StringProperty state = new SimpleStringProperty();
    private StringProperty status = new SimpleStringProperty(); // alias for state
    private StringProperty workerUrl = new SimpleStringProperty();
    private Map<String, String> config;
    private StringProperty errorMessage = new SimpleStringProperty();
    private StringProperty className = new SimpleStringProperty();
    private IntegerProperty tasksRunning = new SimpleIntegerProperty();
    
    public ConnectorInfo() {}
    
    public ConnectorInfo(String name, String type, String state) {
        setName(name);
        setType(type);
        setState(state);
    }
    
    // JavaFX Property methods
    public StringProperty nameProperty() { return name; }
    public StringProperty typeProperty() { return type; }
    public StringProperty stateProperty() { return state; }
    public StringProperty statusProperty() { return status; }
    public StringProperty workerUrlProperty() { return workerUrl; }
    public StringProperty errorMessageProperty() { return errorMessage; }
    public StringProperty classNameProperty() { return className; }
    public IntegerProperty tasksRunningProperty() { return tasksRunning; }
    
    // Regular getters and setters
    public String getName() { return name.get(); }
    public void setName(String name) { 
        this.name.set(name); 
    }
    
    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    
    public String getState() { return state.get(); }
    public void setState(String state) { 
        this.state.set(state); 
        this.status.set(state);
    }
    
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { 
        this.status.set(status); 
        this.state.set(status);
    }
    
    public String getWorkerUrl() { return workerUrl.get(); }
    public void setWorkerUrl(String workerUrl) { this.workerUrl.set(workerUrl); }
    
    public Map<String, String> getConfig() { return config; }
    public void setConfig(Map<String, String> config) { this.config = config; }
    
    public String getErrorMessage() { return errorMessage.get(); }
    public void setErrorMessage(String errorMessage) { this.errorMessage.set(errorMessage); }
    
    public String getClassName() { return className.get(); }
    public void setClassName(String className) { this.className.set(className); }
    
    public int getTasksRunning() { return tasksRunning.get(); }
    public void setTasksRunning(int tasksRunning) { this.tasksRunning.set(tasksRunning); }
    
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
        ConnectorInfo that = (ConnectorInfo) obj;
        return java.util.Objects.equals(getName(), that.getName());
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(getName());
    }
}