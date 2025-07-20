package com.kafkatool.model;

/**
 * Model class representing metrics information
 */
public class MetricsInfo {
    private String metricName;
    private String metricType;
    private double value;
    private String unit;
    private long timestamp;
    private String description;
    
    public MetricsInfo() {}
    
    public MetricsInfo(String metricName, String metricType, double value) {
        this.metricName = metricName;
        this.metricType = metricType;
        this.value = value;
    }
    
    // Getters and setters
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    
    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }
    
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}