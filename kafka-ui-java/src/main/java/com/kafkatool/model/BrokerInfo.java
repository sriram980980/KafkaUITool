package com.kafkatool.model;

import javafx.beans.property.*;

/**
 * Model class representing a Kafka broker
 */
public class BrokerInfo {
    
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty host = new SimpleStringProperty();
    private final IntegerProperty port = new SimpleIntegerProperty();
    private final StringProperty rack = new SimpleStringProperty();
    private final BooleanProperty controller = new SimpleBooleanProperty(false);
    private final StringProperty version = new SimpleStringProperty();
    private final StringProperty jmxPort = new SimpleStringProperty();
    
    public BrokerInfo() {}
    
    public BrokerInfo(int id, String host, int port, String rack) {
        setId(id);
        setHost(host);
        setPort(port);
        setRack(rack);
    }
    
    // ID property
    public int getId() {
        return id.get();
    }
    
    public void setId(int id) {
        this.id.set(id);
    }
    
    public IntegerProperty idProperty() {
        return id;
    }
    
    // Host property
    public String getHost() {
        return host.get();
    }
    
    public void setHost(String host) {
        this.host.set(host);
    }
    
    public StringProperty hostProperty() {
        return host;
    }
    
    // Port property
    public int getPort() {
        return port.get();
    }
    
    public void setPort(int port) {
        this.port.set(port);
    }
    
    public IntegerProperty portProperty() {
        return port;
    }
    
    // Rack property
    public String getRack() {
        return rack.get();
    }
    
    public void setRack(String rack) {
        this.rack.set(rack);
    }
    
    public StringProperty rackProperty() {
        return rack;
    }
    
    // Controller property
    public boolean isController() {
        return controller.get();
    }
    
    public void setController(boolean controller) {
        this.controller.set(controller);
    }
    
    public BooleanProperty controllerProperty() {
        return controller;
    }
    
    // Version property
    public String getVersion() {
        return version.get();
    }
    
    public void setVersion(String version) {
        this.version.set(version);
    }
    
    public StringProperty versionProperty() {
        return version;
    }
    
    // JMX port property
    public String getJmxPort() {
        return jmxPort.get();
    }
    
    public void setJmxPort(String jmxPort) {
        this.jmxPort.set(jmxPort);
    }
    
    public StringProperty jmxPortProperty() {
        return jmxPort;
    }
    
    public String getAddress() {
        return getHost() + ":" + getPort();
    }
    
    @Override
    public String toString() {
        return String.format("Broker %d (%s:%d)%s", 
            getId(), getHost(), getPort(), 
            isController() ? " [Controller]" : "");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BrokerInfo that = (BrokerInfo) obj;
        return getId() == that.getId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}