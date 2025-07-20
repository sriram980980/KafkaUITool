package com.kafkatool.model;

/**
 * Model class representing broker information
 */
public class BrokerInfo {
    private int id;
    private String host;
    private int port;
    private String rack;
    private boolean controller;
    
    public BrokerInfo() {}
    
    public BrokerInfo(int id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    
    public String getRack() { return rack; }
    public void setRack(String rack) { this.rack = rack; }
    
    public boolean isController() { return controller; }
    public void setController(boolean controller) { this.controller = controller; }
}