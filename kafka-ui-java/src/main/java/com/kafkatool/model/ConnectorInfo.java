package com.kafkatool.model;

import javafx.beans.property.*;

/**
 * Model class representing Kafka Connect connector information
 */
public class ConnectorInfo {
    
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty className = new SimpleStringProperty();
    private final IntegerProperty tasksMax = new SimpleIntegerProperty();
    private final IntegerProperty tasksRunning = new SimpleIntegerProperty();
    private final IntegerProperty tasksFailed = new SimpleIntegerProperty();
    private final StringProperty worker = new SimpleStringProperty();
    
    public ConnectorInfo() {}
    
    public ConnectorInfo(String name, String status, String type, String className) {
        setName(name);
        setStatus(status);
        setType(type);
        setClassName(className);
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
    
    // Type property
    public String getType() {
        return type.get();
    }
    
    public void setType(String type) {
        this.type.set(type);
    }
    
    public StringProperty typeProperty() {
        return type;
    }
    
    // Class name property
    public String getClassName() {
        return className.get();
    }
    
    public void setClassName(String className) {
        this.className.set(className);
    }
    
    public StringProperty classNameProperty() {
        return className;
    }
    
    // Tasks max property
    public int getTasksMax() {
        return tasksMax.get();
    }
    
    public void setTasksMax(int tasksMax) {
        this.tasksMax.set(tasksMax);
    }
    
    public IntegerProperty tasksMaxProperty() {
        return tasksMax;
    }
    
    // Tasks running property
    public int getTasksRunning() {
        return tasksRunning.get();
    }
    
    public void setTasksRunning(int tasksRunning) {
        this.tasksRunning.set(tasksRunning);
    }
    
    public IntegerProperty tasksRunningProperty() {
        return tasksRunning;
    }
    
    // Tasks failed property
    public int getTasksFailed() {
        return tasksFailed.get();
    }
    
    public void setTasksFailed(int tasksFailed) {
        this.tasksFailed.set(tasksFailed);
    }
    
    public IntegerProperty tasksFailedProperty() {
        return tasksFailed;
    }
    
    // Worker property
    public String getWorker() {
        return worker.get();
    }
    
    public void setWorker(String worker) {
        this.worker.set(worker);
    }
    
    public StringProperty workerProperty() {
        return worker;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", getName(), getStatus());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConnectorInfo that = (ConnectorInfo) obj;
        return getName().equals(that.getName());
    }
    
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}