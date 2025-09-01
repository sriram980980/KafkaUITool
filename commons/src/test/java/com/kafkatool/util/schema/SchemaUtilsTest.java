package com.kafkatool.util.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for schema utilities
 */
public class SchemaUtilsTest {
    
    private AvroSchemaValidator avroValidator;
    private ProtobufSchemaManager protobufManager;
    
    @BeforeEach
    void setUp() {
        avroValidator = new AvroSchemaValidator();
        protobufManager = new ProtobufSchemaManager();
    }
    
    @Test
    void testAvroSchemaValidation() {
        String validAvroSchema = """
            {
              "type": "record",
              "name": "User",
              "fields": [
                {"name": "id", "type": "string"},
                {"name": "name", "type": "string"},
                {"name": "age", "type": "int"}
              ]
            }
            """;
        
        AvroSchemaValidator.ValidationResult result = avroValidator.parseAvroSchema("user-schema", validAvroSchema);
        assertTrue(result.isSuccess());
        assertEquals("Avro schema parsed successfully", result.getMessage());
    }
    
    @Test
    void testInvalidAvroSchema() {
        String invalidAvroSchema = """
            {
              "type": "invalid",
              "name": "User"
            }
            """;
        
        AvroSchemaValidator.ValidationResult result = avroValidator.parseAvroSchema("invalid-schema", invalidAvroSchema);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Invalid Avro schema"));
    }
    
    @Test
    void testProtobufSchemaValidation() {
        String validProtoSchema = """
            syntax = "proto3";
            
            message User {
              string id = 1;
              string name = 2;
              int32 age = 3;
            }
            """;
        
        ProtobufSchemaManager.ValidationResult result = protobufManager.parseProtoSchema("user-proto", validProtoSchema);
        assertTrue(result.isSuccess());
        assertEquals("Schema parsed successfully", result.getMessage());
    }
    
    @Test
    void testProtobufMessageTypeExtraction() {
        String protoSchema = """
            syntax = "proto3";
            
            message User {
              string id = 1;
            }
            
            message Order {
              string order_id = 1;
            }
            """;
        
        protobufManager.parseProtoSchema("multi-message", protoSchema);
        String[] messageTypes = protobufManager.extractMessageTypes(protoSchema);
        
        assertEquals(2, messageTypes.length);
        assertTrue(java.util.Arrays.asList(messageTypes).contains("User"));
        assertTrue(java.util.Arrays.asList(messageTypes).contains("Order"));
    }
    
    @Test
    void testAvroSampleGeneration() {
        String avroSchema = """
            {
              "type": "record",
              "name": "User",
              "fields": [
                {"name": "id", "type": "string"},
                {"name": "age", "type": "int"}
              ]
            }
            """;
        
        String sample = avroValidator.generateSampleJson(avroSchema);
        assertTrue(sample.contains("\"id\""));
        assertTrue(sample.contains("\"age\""));
        assertTrue(sample.contains("sample_string"));
        assertTrue(sample.contains("42"));
    }
    
    @Test
    void testSchemaManagement() {
        String schema = "syntax = \"proto3\"; message Test { string id = 1; }";
        
        // Test add schema
        protobufManager.parseProtoSchema("test-schema", schema);
        assertEquals(schema, protobufManager.getSchema("test-schema"));
        
        // Test list schemas
        String[] schemas = protobufManager.getSchemaNames();
        assertTrue(java.util.Arrays.asList(schemas).contains("test-schema"));
        
        // Test delete schema
        assertTrue(protobufManager.deleteSchema("test-schema"));
        assertNull(protobufManager.getSchema("test-schema"));
    }
}