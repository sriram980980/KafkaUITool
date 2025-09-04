package com.kafkatool.service;

import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.TopicInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for schema-aware message service
 */
public class SchemaAwareMessageServiceTest {
    
    private SchemaAwareMessageService service;
    
    @BeforeEach
    void setUp() {
        service = new SchemaAwareMessageService();
    }
    
    @Test
    void testStringMessageParsing() {
        ClusterInfo clusterInfo = new ClusterInfo("test", "localhost:9092");
        clusterInfo.setDefaultMessageFormat("STRING");
        
        String message = "Hello World";
        SchemaAwareMessageService.MessageParseResult result = service.parseMessage(message, clusterInfo, null);
        
        assertTrue(result.isSuccess());
        assertEquals("STRING", result.getSchemaType());
        assertEquals(message, result.getFormattedMessage());
    }
    
    @Test
    void testJsonMessageParsing() {
        ClusterInfo clusterInfo = new ClusterInfo("test", "localhost:9092");
        clusterInfo.setDefaultMessageFormat("JSON");
        
        String message = "{\"name\":\"John\",\"age\":30}";
        SchemaAwareMessageService.MessageParseResult result = service.parseMessage(message, clusterInfo, null);
        
        assertTrue(result.isSuccess());
        assertEquals("JSON", result.getSchemaType());
        assertNotNull(result.getFormattedMessage());
    }
    
    @Test
    void testTopicSchemaOverride() {
        ClusterInfo clusterInfo = new ClusterInfo("test", "localhost:9092");
        clusterInfo.setDefaultMessageFormat("STRING");
        
        TopicInfo topicInfo = new TopicInfo("test-topic", 1, (short) 1);
        topicInfo.setSchemaFormat("JSON");
        
        String message = "{\"test\":\"value\"}";
        SchemaAwareMessageService.MessageParseResult result = service.parseMessage(message, clusterInfo, topicInfo);
        
        assertTrue(result.isSuccess());
        assertEquals("JSON", result.getSchemaType());
    }
    
    @Test
    void testInlineAvroSchema() {
        ClusterInfo clusterInfo = new ClusterInfo("test", "localhost:9092");
        clusterInfo.setAvroSupportEnabled(true);
        clusterInfo.setDefaultMessageFormat("AVRO");
        
        TopicInfo topicInfo = new TopicInfo("test-topic", 1, (short) 1);
        topicInfo.setUseInlineSchema(true);
        topicInfo.setSchemaFormat("AVRO");
        topicInfo.setInlineAvroSchema("""
            {
              "type": "record",
              "name": "User",
              "fields": [
                {"name": "id", "type": "string"},
                {"name": "name", "type": "string"}
              ]
            }
            """);
        
        String message = "{\"id\":\"123\",\"name\":\"John\"}";
        SchemaAwareMessageService.MessageParseResult result = service.parseMessage(message, clusterInfo, topicInfo);
        
        assertTrue(result.isSuccess());
        assertEquals("AVRO", result.getSchemaType());
        assertTrue(result.getProcessingInfo().contains("inline Avro schema"));
    }
    
    @Test
    void testInlineProtobufSchema() {
        ClusterInfo clusterInfo = new ClusterInfo("test", "localhost:9092");
        clusterInfo.setProtobufSupportEnabled(true);
        clusterInfo.setDefaultMessageFormat("PROTOBUF");
        
        TopicInfo topicInfo = new TopicInfo("test-topic", 1, (short) 1);
        topicInfo.setUseInlineSchema(true);
        topicInfo.setSchemaFormat("PROTOBUF");
        topicInfo.setInlineProtobufSchema("""
            syntax = "proto3";
            message User {
              string id = 1;
              string name = 2;
            }
            """);
        
        String message = "{\"id\":\"123\",\"name\":\"John\"}";
        SchemaAwareMessageService.MessageParseResult result = service.parseMessage(message, clusterInfo, topicInfo);
        
        assertTrue(result.isSuccess());
        assertEquals("PROTOBUF", result.getSchemaType());
        assertTrue(result.getProcessingInfo().contains("inline Protobuf schema"));
    }
    
    @Test
    void testSchemaRegistryIndication() {
        ClusterInfo clusterInfo = new ClusterInfo("test", "localhost:9092");
        clusterInfo.setSchemaRegistryEnabled(true);
        clusterInfo.setSchemaRegistryUrl("http://localhost:8081");
        clusterInfo.setAvroSupportEnabled(true);
        clusterInfo.setDefaultMessageFormat("AVRO");
        
        String message = "{\"test\":\"value\"}";
        SchemaAwareMessageService.MessageParseResult result = service.parseMessage(message, clusterInfo, null);
        
        assertTrue(result.isSuccess());
        assertTrue(result.getProcessingInfo().contains("Schema Registry"));
        assertTrue(result.getProcessingInfo().contains("localhost:8081"));
    }
    
    @Test
    void testCanParseMessage() {
        ClusterInfo clusterInfo = new ClusterInfo("test", "localhost:9092");
        clusterInfo.setAvroSupportEnabled(true);
        clusterInfo.setSchemaRegistryEnabled(true);
        clusterInfo.setDefaultMessageFormat("AVRO");
        
        assertTrue(service.canParseMessage(clusterInfo, null));
        
        clusterInfo.setAvroSupportEnabled(false);
        assertFalse(service.canParseMessage(clusterInfo, null));
    }
    
    @Test
    void testCanParseWithInlineSchema() {
        ClusterInfo clusterInfo = new ClusterInfo("test", "localhost:9092");
        clusterInfo.setAvroSupportEnabled(true);
        clusterInfo.setDefaultMessageFormat("AVRO");
        
        TopicInfo topicInfo = new TopicInfo("test-topic", 1, (short) 1);
        topicInfo.setUseInlineSchema(true);
        topicInfo.setInlineAvroSchema("{ \"type\": \"string\" }");
        
        assertTrue(service.canParseMessage(clusterInfo, topicInfo));
    }
}