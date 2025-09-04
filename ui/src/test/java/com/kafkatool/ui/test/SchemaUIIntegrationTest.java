package com.kafkatool.ui.test;

import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.TopicInfo;
import com.kafkatool.ui.DialogHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify UI integration without requiring GUI
 */
public class SchemaUIIntegrationTest {
    
    @Test
    void testClusterInfoSchemaFieldsPopulation() {
        // Test that ClusterInfo can be created with schema format preferences
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092", false,
                null, null, false, null, null, null,
                true, true, "AVRO");
        
        assertTrue(cluster.isAvroSupportEnabled());
        assertTrue(cluster.isProtobufSupportEnabled());
        assertEquals("AVRO", cluster.getDefaultMessageFormat());
        
        // Test copy functionality includes new fields
        ClusterInfo copy = cluster.createMaskedCopy();
        assertTrue(copy.isAvroSupportEnabled());
        assertTrue(copy.isProtobufSupportEnabled());
        assertEquals("AVRO", copy.getDefaultMessageFormat());
    }
    
    @Test
    void testTopicInfoSchemaOverrides() {
        TopicInfo topic = new TopicInfo("test-topic", 1, (short) 1);
        
        // Test schema format override
        topic.setSchemaFormat("PROTOBUF");
        assertEquals("PROTOBUF", topic.getSchemaFormat());
        
        // Test inline schema functionality
        topic.setUseInlineSchema(true);
        topic.setInlineAvroSchema("{ \"type\": \"string\" }");
        topic.setInlineProtobufSchema("syntax = \"proto3\"; message Test { string id = 1; }");
        
        assertTrue(topic.isUseInlineSchema());
        assertNotNull(topic.getInlineAvroSchema());
        assertNotNull(topic.getInlineProtobufSchema());
        assertTrue(topic.getInlineAvroSchema().contains("string"));
        assertTrue(topic.getInlineProtobufSchema().contains("proto3"));
    }
    
    @Test
    void testMessageFormatEnum() {
        // Verify MessageFormat enum exists and has required values
        DialogHelper.MessageFormat[] formats = DialogHelper.MessageFormat.values();
        
        boolean hasAvro = false;
        boolean hasProtobuf = false;
        boolean hasString = false;
        boolean hasJson = false;
        
        for (DialogHelper.MessageFormat format : formats) {
            switch (format.name()) {
                case "AVRO" -> hasAvro = true;
                case "PROTOBUF" -> hasProtobuf = true;
                case "STRING" -> hasString = true;
                case "JSON" -> hasJson = true;
            }
        }
        
        assertTrue(hasAvro, "MessageFormat should include AVRO");
        assertTrue(hasProtobuf, "MessageFormat should include PROTOBUF");
        assertTrue(hasString, "MessageFormat should include STRING");
        assertTrue(hasJson, "MessageFormat should include JSON");
    }
    
    @Test
    void testCompleteSchemaConfiguration() {
        // Test complete schema configuration workflow
        ClusterInfo cluster = new ClusterInfo("production", "prod-kafka:9092");
        
        // Enable schema support
        cluster.setAvroSupportEnabled(true);
        cluster.setProtobufSupportEnabled(true);
        cluster.setDefaultMessageFormat("AVRO");
        
        // Enable schema registry
        cluster.setSchemaRegistryEnabled(true);
        cluster.setSchemaRegistryUrl("https://schema-registry.prod.com");
        
        // Create topic with overrides
        TopicInfo topic = new TopicInfo("user-events", 10, (short) 3);
        topic.setSchemaFormat("PROTOBUF"); // Override cluster default
        topic.setUseInlineSchema(true);
        topic.setInlineProtobufSchema("""
            syntax = "proto3";
            message UserEvent {
              string user_id = 1;
              string event_type = 2;
              int64 timestamp = 3;
            }
            """);
        
        // Verify configuration
        assertTrue(cluster.isAvroSupportEnabled());
        assertTrue(cluster.isProtobufSupportEnabled());
        assertEquals("AVRO", cluster.getDefaultMessageFormat());
        assertTrue(cluster.isSchemaRegistryEnabled());
        assertEquals("https://schema-registry.prod.com", cluster.getSchemaRegistryUrl());
        
        assertEquals("PROTOBUF", topic.getSchemaFormat());
        assertTrue(topic.isUseInlineSchema());
        assertTrue(topic.getInlineProtobufSchema().contains("UserEvent"));
        
        System.out.println("✓ Complete schema configuration test passed");
        System.out.println("  - Cluster: " + cluster.getName());
        System.out.println("  - Avro Support: " + cluster.isAvroSupportEnabled());
        System.out.println("  - Protobuf Support: " + cluster.isProtobufSupportEnabled());
        System.out.println("  - Default Format: " + cluster.getDefaultMessageFormat());
        System.out.println("  - Schema Registry: " + cluster.getSchemaRegistryUrl());
        System.out.println("  - Topic: " + topic.getName());
        System.out.println("  - Topic Format Override: " + topic.getSchemaFormat());
        System.out.println("  - Uses Inline Schema: " + topic.isUseInlineSchema());
    }
}