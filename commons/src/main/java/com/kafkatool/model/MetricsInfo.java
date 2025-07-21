package com.kafkatool.model;

import javafx.beans.property.*;

/**
 * Model class representing metrics information
 */
public class MetricsInfo {
    private StringProperty metricName = new SimpleStringProperty();
    private StringProperty metricType = new SimpleStringProperty();
    private DoubleProperty value = new SimpleDoubleProperty();
    private StringProperty unit = new SimpleStringProperty();
    private LongProperty timestamp = new SimpleLongProperty();
    private StringProperty description = new SimpleStringProperty();
    
    public MetricsInfo() {}
    
    public MetricsInfo(String metricName, String metricType, double value) {
        setMetricName(metricName);
        setMetricType(metricType);
        setValue(value);
    }
    
    // JavaFX Property methods
    public StringProperty metricNameProperty() { return metricName; }
    public StringProperty metricTypeProperty() { return metricType; }
    public DoubleProperty valueProperty() { return value; }
    public StringProperty unitProperty() { return unit; }
    public LongProperty timestampProperty() { return timestamp; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty categoryProperty() { return metricType; } // Alias
    
    // Regular getters and setters
    public String getMetricName() { return metricName.get(); }
    public void setMetricName(String metricName) { this.metricName.set(metricName); }
    
    public String getMetricType() { return metricType.get(); }
    public void setMetricType(String metricType) { this.metricType.set(metricType); }
    
    public double getValue() { return value.get(); }
    public void setValue(double value) { this.value.set(value); }
    
    public String getUnit() { return unit.get(); }
    public void setUnit(String unit) { this.unit.set(unit); }
    
    public long getTimestamp() { return timestamp.get(); }
    public void setTimestamp(long timestamp) { this.timestamp.set(timestamp); }
    
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    
    // Method expected by UI - alias for metricType as category  
    public String getCategory() { return metricType.get(); }
    public void setCategory(String category) { this.metricType.set(category); }
}