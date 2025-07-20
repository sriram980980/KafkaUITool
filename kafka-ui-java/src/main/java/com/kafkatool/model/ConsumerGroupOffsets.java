package com.kafkatool.model;

import javafx.beans.property.*;

/**
 * Model class representing consumer group offset information for a topic partition
 */
public class ConsumerGroupOffsets {
    
    private final StringProperty groupId = new SimpleStringProperty();
    private final StringProperty topicName = new SimpleStringProperty();
    private final IntegerProperty partition = new SimpleIntegerProperty();
    private final LongProperty currentOffset = new SimpleLongProperty();
    private final LongProperty logEndOffset = new SimpleLongProperty();
    private final LongProperty lag = new SimpleLongProperty();
    private final StringProperty memberId = new SimpleStringProperty();
    private final StringProperty clientId = new SimpleStringProperty();
    private final StringProperty host = new SimpleStringProperty();
    
    public ConsumerGroupOffsets() {}
    
    public ConsumerGroupOffsets(String groupId, String topicName, int partition, 
                               long currentOffset, long logEndOffset, String memberId) {
        setGroupId(groupId);
        setTopicName(topicName);
        setPartition(partition);
        setCurrentOffset(currentOffset);
        setLogEndOffset(logEndOffset);
        setLag(logEndOffset - currentOffset);
        setMemberId(memberId);
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
    
    // Topic name property
    public String getTopicName() {
        return topicName.get();
    }
    
    public void setTopicName(String topicName) {
        this.topicName.set(topicName);
    }
    
    public StringProperty topicNameProperty() {
        return topicName;
    }
    
    // Partition property
    public int getPartition() {
        return partition.get();
    }
    
    public void setPartition(int partition) {
        this.partition.set(partition);
    }
    
    public IntegerProperty partitionProperty() {
        return partition;
    }
    
    // Current offset property
    public long getCurrentOffset() {
        return currentOffset.get();
    }
    
    public void setCurrentOffset(long currentOffset) {
        this.currentOffset.set(currentOffset);
        updateLag();
    }
    
    public LongProperty currentOffsetProperty() {
        return currentOffset;
    }
    
    // Log end offset property
    public long getLogEndOffset() {
        return logEndOffset.get();
    }
    
    public void setLogEndOffset(long logEndOffset) {
        this.logEndOffset.set(logEndOffset);
        updateLag();
    }
    
    public LongProperty logEndOffsetProperty() {
        return logEndOffset;
    }
    
    // Lag property
    public long getLag() {
        return lag.get();
    }
    
    public void setLag(long lag) {
        this.lag.set(lag);
    }
    
    public LongProperty lagProperty() {
        return lag;
    }
    
    // Member ID property
    public String getMemberId() {
        return memberId.get();
    }
    
    public void setMemberId(String memberId) {
        this.memberId.set(memberId);
    }
    
    public StringProperty memberIdProperty() {
        return memberId;
    }
    
    // Client ID property
    public String getClientId() {
        return clientId.get();
    }
    
    public void setClientId(String clientId) {
        this.clientId.set(clientId);
    }
    
    public StringProperty clientIdProperty() {
        return clientId;
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
    
    private void updateLag() {
        setLag(getLogEndOffset() - getCurrentOffset());
    }
    
    @Override
    public String toString() {
        return String.format("%s:%d [offset=%d, lag=%d]", 
            getTopicName(), getPartition(), getCurrentOffset(), getLag());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConsumerGroupOffsets that = (ConsumerGroupOffsets) obj;
        return getGroupId().equals(that.getGroupId()) && 
               getTopicName().equals(that.getTopicName()) &&
               getPartition() == that.getPartition();
    }
    
    @Override
    public int hashCode() {
        return getGroupId().hashCode() * 31 * 31 + 
               getTopicName().hashCode() * 31 + 
               getPartition();
    }
}