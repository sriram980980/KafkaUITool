package com.kafkatool.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Model class representing metrics and monitoring data
 */
public class MetricsInfo {
    
    private final StringProperty metricName = new SimpleStringProperty();
    private final DoubleProperty value = new SimpleDoubleProperty();
    private final StringProperty unit = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> timestamp = new SimpleObjectProperty<>();
    private final StringProperty category = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    
    public MetricsInfo() {}
    
    public MetricsInfo(String metricName, double value, String unit, String category) {
        setMetricName(metricName);
        setValue(value);
        setUnit(unit);
        setCategory(category);
        setTimestamp(LocalDateTime.now());
    }
    
    // Metric name property
    public String getMetricName() {
        return metricName.get();
    }
    
    public void setMetricName(String metricName) {
        this.metricName.set(metricName);
    }
    
    public StringProperty metricNameProperty() {
        return metricName;
    }
    
    // Value property
    public double getValue() {
        return value.get();
    }
    
    public void setValue(double value) {
        this.value.set(value);
    }
    
    public DoubleProperty valueProperty() {
        return value;
    }
    
    // Unit property
    public String getUnit() {
        return unit.get();
    }
    
    public void setUnit(String unit) {
        this.unit.set(unit);
    }
    
    public StringProperty unitProperty() {
        return unit;
    }
    
    // Timestamp property
    public LocalDateTime getTimestamp() {
        return timestamp.get();
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp.set(timestamp);
    }
    
    public ObjectProperty<LocalDateTime> timestampProperty() {
        return timestamp;
    }
    
    // Category property
    public String getCategory() {
        return category.get();
    }
    
    public void setCategory(String category) {
        this.category.set(category);
    }
    
    public StringProperty categoryProperty() {
        return category;
    }
    
    // Description property
    public String getDescription() {
        return description.get();
    }
    
    public void setDescription(String description) {
        this.description.set(description);
    }
    
    public StringProperty descriptionProperty() {
        return description;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %.2f %s", getMetricName(), getValue(), getUnit());
    }
}