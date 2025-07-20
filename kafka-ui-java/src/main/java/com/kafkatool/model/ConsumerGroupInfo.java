package com.kafkatool.model;

import javafx.beans.property.*;

/**
 * Model class representing a Kafka consumer group
 */
public class ConsumerGroupInfo {
    
    private final StringProperty groupId = new SimpleStringProperty();
    private final StringProperty state = new SimpleStringProperty();
    private final IntegerProperty memberCount = new SimpleIntegerProperty();
    private final StringProperty coordinator = new SimpleStringProperty();
    private final StringProperty protocolType = new SimpleStringProperty();
    private final StringProperty protocol = new SimpleStringProperty();
    private final LongProperty totalLag = new SimpleLongProperty(0);
    
    public ConsumerGroupInfo() {}
    
    public ConsumerGroupInfo(String groupId, String state, int memberCount, String coordinator) {
        setGroupId(groupId);
        setState(state);
        setMemberCount(memberCount);
        setCoordinator(coordinator);
    }
    
    // Group ID property
    public String getGroupId() {
        return groupId.get();
    }
    
    public void setGroupId(String groupId) {
        this.groupId.set(groupId);
    }
    
    public StringProperty groupIdProperty() {
        return groupId;
    }
    
    // State property
    public String getState() {
        return state.get();
    }
    
    public void setState(String state) {
        this.state.set(state);
    }
    
    public StringProperty stateProperty() {
        return state;
    }
    
    // Member count property
    public int getMemberCount() {
        return memberCount.get();
    }
    
    public void setMemberCount(int memberCount) {
        this.memberCount.set(memberCount);
    }
    
    public IntegerProperty memberCountProperty() {
        return memberCount;
    }
    
    // Coordinator property
    public String getCoordinator() {
        return coordinator.get();
    }
    
    public void setCoordinator(String coordinator) {
        this.coordinator.set(coordinator);
    }
    
    public StringProperty coordinatorProperty() {
        return coordinator;
    }
    
    // Protocol type property
    public String getProtocolType() {
        return protocolType.get();
    }
    
    public void setProtocolType(String protocolType) {
        this.protocolType.set(protocolType);
    }
    
    public StringProperty protocolTypeProperty() {
        return protocolType;
    }
    
    // Protocol property
    public String getProtocol() {
        return protocol.get();
    }
    
    public void setProtocol(String protocol) {
        this.protocol.set(protocol);
    }
    
    public StringProperty protocolProperty() {
        return protocol;
    }
    
    // Total lag property
    public long getTotalLag() {
        return totalLag.get();
    }
    
    public void setTotalLag(long totalLag) {
        this.totalLag.set(totalLag);
    }
    
    public LongProperty totalLagProperty() {
        return totalLag;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s, %d members)", getGroupId(), getState(), getMemberCount());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConsumerGroupInfo that = (ConsumerGroupInfo) obj;
        return getGroupId().equals(that.getGroupId());
    }
    
    @Override
    public int hashCode() {
        return getGroupId().hashCode();
    }
}