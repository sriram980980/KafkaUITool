package com.kafkatool.model;

/**
 * Model class representing consumer group offsets
 */
public class ConsumerGroupOffsets {
    private String groupId;
    private String topic;
    private int partition;
    private long currentOffset;
    private long logEndOffset;
    private long lag;
    
    public ConsumerGroupOffsets() {}
    
    public ConsumerGroupOffsets(String groupId, String topic, int partition) {
        this.groupId = groupId;
        this.topic = topic;
        this.partition = partition;
    }
    
    // Getters and setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    
    public int getPartition() { return partition; }
    public void setPartition(int partition) { this.partition = partition; }
    
    public long getCurrentOffset() { return currentOffset; }
    public void setCurrentOffset(long currentOffset) { this.currentOffset = currentOffset; }
    
    public long getLogEndOffset() { return logEndOffset; }
    public void setLogEndOffset(long logEndOffset) { this.logEndOffset = logEndOffset; }
    
    public long getLag() { return lag; }
    public void setLag(long lag) { this.lag = lag; }
}