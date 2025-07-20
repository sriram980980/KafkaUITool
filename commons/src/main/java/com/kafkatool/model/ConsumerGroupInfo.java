package com.kafkatool.model;

/**
 * Model class representing consumer group information
 * Plain POJO version for use in commons module
 */
public class ConsumerGroupInfo {
    
    private String groupId;
    private String state;
    private String protocol;
    private String protocolType;
    private int memberCount;
    private long lag;
    private String coordinator;
    
    public ConsumerGroupInfo() {}
    
    public ConsumerGroupInfo(String groupId, String state) {
        this.groupId = groupId;
        this.state = state;
    }
    
    // Getters and setters
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getProtocolType() {
        return protocolType;
    }
    
    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }
    
    public int getMemberCount() {
        return memberCount;
    }
    
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
    
    public long getLag() {
        return lag;
    }
    
    public void setLag(long lag) {
        this.lag = lag;
    }
    
    public String getCoordinator() {
        return coordinator;
    }
    
    public void setCoordinator(String coordinator) {
        this.coordinator = coordinator;
    }
    
    @Override
    public String toString() {
        return groupId != null ? groupId : "Unknown Group";
    }
}