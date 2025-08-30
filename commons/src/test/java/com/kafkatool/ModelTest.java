package com.kafkatool.service;

import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.TopicInfo;
import com.kafkatool.model.ConnectorInfo;
import com.kafkatool.model.LLMProviderInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for model classes
 */
public class ModelTest {
    
    private ClusterInfo cluster;
    private TopicInfo topic;
    private ConnectorInfo connector;
    private LLMProviderInfo llmProvider;
    
    @BeforeEach
    void setUp() {
        cluster = new ClusterInfo("test-cluster", "localhost:9092");
        topic = new TopicInfo("test-topic", 3, (short) 1);
        connector = new ConnectorInfo("test-connector", "source", "RUNNING");
        llmProvider = new LLMProviderInfo("Test LLM", "ollama", "http://localhost:11434");
    }
    
    @Test
    void testClusterInfo() {
        assertEquals("test-cluster", cluster.getName());
        assertEquals("localhost:9092", cluster.getBrokerUrls());
        assertEquals("Disconnected", cluster.getStatus());
        assertFalse(cluster.isConnectByDefault());
        
        cluster.setStatus("Connected");
        assertEquals("Connected", cluster.getStatus());
        
        cluster.setConnectByDefault(true);
        assertTrue(cluster.isConnectByDefault());
    }
    
    @Test
    void testTopicInfo() {
        assertEquals("test-topic", topic.getName());
        assertEquals(3, topic.getPartitions());
        assertEquals(1, topic.getReplicationFactor());
        
        topic.setPartitions(5);
        assertEquals(5, topic.getPartitions());
    }
    
    @Test
    void testClusterToString() {
        String expected = "test-cluster (localhost:9092)";
        assertEquals(expected, cluster.toString());
    }
    
    @Test
    void testTopicToString() {
        assertEquals("test-topic", topic.toString());
    }
    
    @Test
    void testClusterEquality() {
        ClusterInfo cluster2 = new ClusterInfo("test-cluster", "localhost:9092");
        assertEquals(cluster, cluster2);
        assertEquals(cluster.hashCode(), cluster2.hashCode());
        
        ClusterInfo cluster3 = new ClusterInfo("different-cluster", "localhost:9092");
        assertNotEquals(cluster, cluster3);
    }
    
    @Test
    void testTopicEquality() {
        TopicInfo topic2 = new TopicInfo("test-topic", 5, (short) 2);
        assertEquals(topic, topic2); // Equal because name is the same
        assertEquals(topic.hashCode(), topic2.hashCode());
        
        TopicInfo topic3 = new TopicInfo("different-topic", 3, (short) 1);
        assertNotEquals(topic, topic3);
    }
    
    @Test
    void testConnectorInfo() {
        assertEquals("test-connector", connector.getName());
        assertEquals("source", connector.getType());
        assertEquals("RUNNING", connector.getStatus());
        
        connector.setTasksRunning(2);
        assertEquals(2, connector.getTasksRunning());
    }
    
    @Test 
    void testConnectorToString() {
        // Test the new toString method that formats name (type) for dropdown display
        String expected = "test-connector (source)";
        assertEquals(expected, connector.toString());
        
        // Test that getMapKey returns just the name for map lookups
        assertEquals("test-connector", connector.getMapKey());
    }
    
    @Test
    void testConnectorDropdownMappingFix() {
        // This test validates the fix for the dropdown mapping issue
        // Where dropdown shows "name (type)" but map.get() should use just the name
        
        // Simulate a connections map that uses name as key
        java.util.Map<String, ConnectorInfo> connectionsMap = new java.util.HashMap<>();
        connectionsMap.put(connector.getMapKey(), connector); // Use getMapKey() for map operations
        
        // Simulate dropdown selection - user sees "test-connector (source)"
        String dropdownDisplayValue = connector.toString();
        assertEquals("test-connector (source)", dropdownDisplayValue);
        
        // But for map lookup, we use getMapKey() to get just the name
        ConnectorInfo retrieved = connectionsMap.get(connector.getMapKey());
        assertNotNull(retrieved, "Map lookup should succeed using getMapKey()");
        assertEquals(connector, retrieved);
        
        // This would fail before the fix (using toString() for map lookup)
        ConnectorInfo shouldBeNull = connectionsMap.get(connector.toString());
        assertNull(shouldBeNull, "Map lookup should fail when using toString() directly");
    }
    
    @Test
    void testLLMProviderInfo() {
        assertEquals("Test LLM", llmProvider.getName());
        assertEquals("ollama", llmProvider.getType());
        assertEquals("http://localhost:11434", llmProvider.getUrl());
        assertFalse(llmProvider.isDefault());
        assertFalse(llmProvider.isConnected());
        
        llmProvider.setDefault(true);
        llmProvider.setConnected(true);
        assertTrue(llmProvider.isDefault());
        assertTrue(llmProvider.isConnected());
    }
    
    @Test
    void testLLMProviderToString() {
        // Test the same pattern as ConnectorInfo for dropdown display
        String expected = "Test LLM (ollama)";
        assertEquals(expected, llmProvider.toString());
        
        // Test that getMapKey returns just the name for map lookups
        assertEquals("Test LLM", llmProvider.getMapKey());
    }
    
    @Test
    void testLLMProviderDropdownMappingFix() {
        // Test the same dropdown mapping fix pattern for LLM providers
        java.util.Map<String, LLMProviderInfo> providersMap = new java.util.HashMap<>();
        providersMap.put(llmProvider.getMapKey(), llmProvider);
        
        // Dropdown shows "Test LLM (ollama)"
        String dropdownDisplayValue = llmProvider.toString();
        assertEquals("Test LLM (ollama)", dropdownDisplayValue);
        
        // Map lookup uses getMapKey() to get just the name
        LLMProviderInfo retrieved = providersMap.get(llmProvider.getMapKey());
        assertNotNull(retrieved, "Map lookup should succeed using getMapKey()");
        assertEquals(llmProvider, retrieved);
        
        // This demonstrates the fixed mapping issue
        LLMProviderInfo shouldBeNull = providersMap.get(llmProvider.toString());
        assertNull(shouldBeNull, "Map lookup should fail when using toString() directly");
    }
}