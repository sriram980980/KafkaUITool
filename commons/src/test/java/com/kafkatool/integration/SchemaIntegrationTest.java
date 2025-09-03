package com.kafkatool.integration;

import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.TopicInfo;
import com.kafkatool.service.SchemaAwareMessageService;
import com.kafkatool.util.schema.DynamicSchemaCompiler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for schema support functionality
 */
public class SchemaIntegrationTest {
    
    @Test
    void testEndToEndSchemaWorkflow() {
        System.out.println("=== End-to-End Schema Integration Test ===\n");
        
        // 1. Create cluster with schema support
        ClusterInfo cluster = createTestCluster();
        validateClusterConfiguration(cluster);
        
        // 2. Create topics with different schema configurations
        TopicInfo avroTopic = createAvroTopic();
        TopicInfo protobufTopic = createProtobufTopic();
        TopicInfo defaultTopic = createDefaultTopic();
        
        validateTopicConfigurations(avroTopic, protobufTopic, defaultTopic);
        
        // 3. Test message parsing with different configurations
        testMessageParsingScenarios(cluster, avroTopic, protobufTopic, defaultTopic);
        
        // 4. Test dynamic compilation
        testDynamicCompilation();
        
        System.out.println("✓ All integration tests passed successfully!\n");
        System.out.println("=== Integration Test Complete ===");
    }
    
    private ClusterInfo createTestCluster() {
        System.out.println("1. Creating test cluster with schema support...");
        
        ClusterInfo cluster = new ClusterInfo("integration-test-cluster", "localhost:9092");
        cluster.setAvroSupportEnabled(true);
        cluster.setProtobufSupportEnabled(true);
        cluster.setDefaultMessageFormat("JSON");
        cluster.setSchemaRegistryEnabled(true);
        cluster.setSchemaRegistryUrl("http://localhost:8081");
        
        System.out.println("   ✓ Cluster created with comprehensive schema support");
        return cluster;
    }
    
    private void validateClusterConfiguration(ClusterInfo cluster) {
        assertTrue(cluster.isAvroSupportEnabled(), "Avro support should be enabled");
        assertTrue(cluster.isProtobufSupportEnabled(), "Protobuf support should be enabled");
        assertEquals("JSON", cluster.getDefaultMessageFormat(), "Default format should be JSON");
        assertTrue(cluster.isSchemaRegistryEnabled(), "Schema registry should be enabled");
        assertEquals("http://localhost:8081", cluster.getSchemaRegistryUrl(), "Schema registry URL should be set");
        
        System.out.println("   ✓ Cluster configuration validated");
    }
    
    private TopicInfo createAvroTopic() {
        TopicInfo topic = new TopicInfo("avro-user-events", 5, (short) 3);
        topic.setSchemaFormat("AVRO");
        topic.setUseInlineSchema(true);
        topic.setInlineAvroSchema("""
            {
              "type": "record",
              "name": "UserEvent",
              "namespace": "com.example.events",
              "fields": [
                {"name": "userId", "type": "string"},
                {"name": "eventType", "type": "string"},
                {"name": "timestamp", "type": "long"},
                {"name": "properties", "type": ["null", {"type": "map", "values": "string"}], "default": null}
              ]
            }
            """);
        return topic;
    }
    
    private TopicInfo createProtobufTopic() {
        TopicInfo topic = new TopicInfo("protobuf-order-events", 3, (short) 2);
        topic.setSchemaFormat("PROTOBUF");
        topic.setUseInlineSchema(true);
        topic.setInlineProtobufSchema("""
            syntax = "proto3";
            
            package com.example.orders;
            
            message OrderEvent {
              string order_id = 1;
              string customer_id = 2;
              double amount = 3;
              int64 timestamp = 4;
              OrderStatus status = 5;
              repeated string tags = 6;
              
              enum OrderStatus {
                PENDING = 0;
                CONFIRMED = 1;
                SHIPPED = 2;
                DELIVERED = 3;
                CANCELLED = 4;
              }
            }
            """);
        return topic;
    }
    
    private TopicInfo createDefaultTopic() {
        TopicInfo topic = new TopicInfo("json-generic-events", 1, (short) 1);
        // This topic will use cluster default (JSON) format
        return topic;
    }
    
    private void validateTopicConfigurations(TopicInfo avroTopic, TopicInfo protobufTopic, TopicInfo defaultTopic) {
        System.out.println("2. Validating topic configurations...");
        
        // Validate Avro topic
        assertEquals("AVRO", avroTopic.getSchemaFormat());
        assertTrue(avroTopic.isUseInlineSchema());
        assertNotNull(avroTopic.getInlineAvroSchema());
        assertTrue(avroTopic.getInlineAvroSchema().contains("UserEvent"));
        
        // Validate Protobuf topic
        assertEquals("PROTOBUF", protobufTopic.getSchemaFormat());
        assertTrue(protobufTopic.isUseInlineSchema());
        assertNotNull(protobufTopic.getInlineProtobufSchema());
        assertTrue(protobufTopic.getInlineProtobufSchema().contains("OrderEvent"));
        
        // Validate default topic
        assertNull(defaultTopic.getSchemaFormat()); // Should use cluster default
        assertFalse(defaultTopic.isUseInlineSchema());
        
        System.out.println("   ✓ All topic configurations validated");
    }
    
    private void testMessageParsingScenarios(ClusterInfo cluster, TopicInfo avroTopic, TopicInfo protobufTopic, TopicInfo defaultTopic) {
        System.out.println("3. Testing message parsing scenarios...");
        
        SchemaAwareMessageService messageService = new SchemaAwareMessageService();
        
        // Test Avro message parsing
        String avroMessage = """
            {
              "userId": "user123",
              "eventType": "login",
              "timestamp": 1693737600000,
              "properties": {"ip": "192.168.1.1", "browser": "Chrome"}
            }
            """;
        
        SchemaAwareMessageService.MessageParseResult avroResult = 
            messageService.parseMessage(avroMessage, cluster, avroTopic);
        
        assertTrue(avroResult.isSuccess(), "Avro message parsing should succeed");
        assertEquals("AVRO", avroResult.getSchemaType());
        assertTrue(avroResult.getProcessingInfo().contains("inline Avro schema"));
        
        // Test Protobuf message parsing
        String protobufMessage = """
            {
              "order_id": "order456",
              "customer_id": "customer789",
              "amount": 99.99,
              "timestamp": 1693737600000,
              "status": "CONFIRMED",
              "tags": ["priority", "vip"]
            }
            """;
        
        SchemaAwareMessageService.MessageParseResult protobufResult = 
            messageService.parseMessage(protobufMessage, cluster, protobufTopic);
        
        assertTrue(protobufResult.isSuccess(), "Protobuf message parsing should succeed");
        assertEquals("PROTOBUF", protobufResult.getSchemaType());
        assertTrue(protobufResult.getProcessingInfo().contains("inline Protobuf schema"));
        
        // Test default (JSON) message parsing
        String jsonMessage = """
            {
              "event": "generic_event",
              "data": {"key": "value"},
              "timestamp": 1693737600000
            }
            """;
        
        SchemaAwareMessageService.MessageParseResult jsonResult = 
            messageService.parseMessage(jsonMessage, cluster, defaultTopic);
        
        assertTrue(jsonResult.isSuccess(), "JSON message parsing should succeed");
        assertEquals("JSON", jsonResult.getSchemaType());
        
        System.out.println("   ✓ Avro message parsing: SUCCESS");
        System.out.println("   ✓ Protobuf message parsing: SUCCESS");
        System.out.println("   ✓ JSON message parsing: SUCCESS");
    }
    
    private void testDynamicCompilation() {
        System.out.println("4. Testing dynamic schema compilation...");
        
        DynamicSchemaCompiler compiler = new DynamicSchemaCompiler();
        
        // Test Avro compilation
        String avroSchema = """
            {
              "type": "record",
              "name": "TestRecord",
              "fields": [
                {"name": "id", "type": "string"},
                {"name": "value", "type": "int"}
              ]
            }
            """;
        
        String testMessage = """
            {
              "id": "test123",
              "value": 42
            }
            """;
        
        DynamicSchemaCompiler.ParseResult result = compiler.parseAvroMessage(avroSchema, testMessage);
        assertTrue(result.isSuccess(), "Dynamic Avro compilation should succeed");
        assertEquals("AVRO", result.getSchemaType());
        
        // Test class compilation
        Class<?> compiledClass = compiler.compileParserClass("TestParser", "AVRO", avroSchema);
        assertNotNull(compiledClass, "Dynamic class compilation should succeed");
        assertEquals("TestParser", compiledClass.getSimpleName());
        
        System.out.println("   ✓ Dynamic Avro schema compilation: SUCCESS");
        System.out.println("   ✓ Dynamic class compilation: SUCCESS");
    }
    
    @Test
    void testSchemaCapabilityChecking() {
        System.out.println("=== Schema Capability Testing ===\n");
        
        SchemaAwareMessageService messageService = new SchemaAwareMessageService();
        
        // Test cluster with full schema support
        ClusterInfo fullSupport = new ClusterInfo("full-support", "localhost:9092");
        fullSupport.setAvroSupportEnabled(true);
        fullSupport.setProtobufSupportEnabled(true);
        fullSupport.setSchemaRegistryEnabled(true);
        fullSupport.setDefaultMessageFormat("AVRO");
        
        TopicInfo avroTopic = new TopicInfo("avro-topic", 1, (short) 1);
        avroTopic.setSchemaFormat("AVRO");
        avroTopic.setUseInlineSchema(true);
        avroTopic.setInlineAvroSchema("{\"type\": \"string\"}");
        
        assertTrue(messageService.canParseMessage(fullSupport, avroTopic));
        
        // Test cluster with limited support
        ClusterInfo limitedSupport = new ClusterInfo("limited-support", "localhost:9092");
        limitedSupport.setAvroSupportEnabled(false);
        limitedSupport.setDefaultMessageFormat("AVRO");
        
        assertFalse(messageService.canParseMessage(limitedSupport, avroTopic));
        
        System.out.println("✓ Schema capability checking works correctly");
        System.out.println("=== Capability Testing Complete ===");
    }
}