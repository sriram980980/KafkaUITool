package com.kafkatool.service;

import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.TopicInfo;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.model.AuthenticationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for model classes
 */
public class ModelTest {
    
    private ClusterInfo cluster;
    private TopicInfo topic;
    
    @BeforeEach
    void setUp() {
        cluster = new ClusterInfo("test-cluster", "localhost:9092");
        topic = new TopicInfo("test-topic", 3, (short) 1);
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
    void testSchemaRegistryFields() {
        // Test default values
        assertFalse(cluster.isSchemaRegistryEnabled());
        assertNull(cluster.getSchemaRegistryUrl());
        assertEquals(AuthenticationType.NONE, cluster.getSchemaRegistryAuthType());
        assertNull(cluster.getSchemaRegistryAuthConfig());
        assertFalse(cluster.requiresSchemaRegistryAuthentication());
        
        // Test setting Schema Registry fields
        cluster.setSchemaRegistryEnabled(true);
        cluster.setSchemaRegistryUrl("http://localhost:8081");
        cluster.setSchemaRegistryAuthType(AuthenticationType.SASL_PLAIN);
        
        assertTrue(cluster.isSchemaRegistryEnabled());
        assertEquals("http://localhost:8081", cluster.getSchemaRegistryUrl());
        assertEquals(AuthenticationType.SASL_PLAIN, cluster.getSchemaRegistryAuthType());
        assertTrue(cluster.requiresSchemaRegistryAuthentication());
    }
    
    @Test
    void testSchemaRegistryWithAuthentication() {
        AuthenticationConfig authConfig = new AuthenticationConfig("user", "pass");
        
        ClusterInfo clusterWithSchemaRegistry = new ClusterInfo(
            "test-cluster", "localhost:9092", false,
            AuthenticationType.NONE, null,
            true, "http://localhost:8081",
            AuthenticationType.SASL_PLAIN, authConfig
        );
        
        assertTrue(clusterWithSchemaRegistry.isSchemaRegistryEnabled());
        assertEquals("http://localhost:8081", clusterWithSchemaRegistry.getSchemaRegistryUrl());
        assertEquals(AuthenticationType.SASL_PLAIN, clusterWithSchemaRegistry.getSchemaRegistryAuthType());
        assertEquals(authConfig, clusterWithSchemaRegistry.getSchemaRegistryAuthConfig());
        assertTrue(clusterWithSchemaRegistry.requiresSchemaRegistryAuthentication());
    }
    
    @Test
    void testCreateMaskedCopyWithSchemaRegistry() {
        AuthenticationConfig authConfig = new AuthenticationConfig("user", "password");
        cluster.setSchemaRegistryEnabled(true);
        cluster.setSchemaRegistryUrl("http://localhost:8081");
        cluster.setSchemaRegistryAuthType(AuthenticationType.SASL_PLAIN);
        cluster.setSchemaRegistryAuthConfig(authConfig);
        
        ClusterInfo masked = cluster.createMaskedCopy();
        
        assertTrue(masked.isSchemaRegistryEnabled());
        assertEquals("http://localhost:8081", masked.getSchemaRegistryUrl());
        assertEquals(AuthenticationType.SASL_PLAIN, masked.getSchemaRegistryAuthType());
        assertNotNull(masked.getSchemaRegistryAuthConfig());
        assertEquals("****", masked.getSchemaRegistryAuthConfig().getPassword()); // Should be masked
    }
}