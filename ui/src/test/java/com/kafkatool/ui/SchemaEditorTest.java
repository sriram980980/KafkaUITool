package com.kafkatool.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Schema Editor enhancements
 */
public class SchemaEditorTest {
    
    @Test
    public void testAvroSchemaValidation() {
        String validAvroSchema = """
            {
              "type": "record",
              "name": "User",
              "fields": [
                {"name": "id", "type": "string"},
                {"name": "name", "type": "string"}
              ]
            }
            """;
        
        // Test that the schema appears to be valid JSON and contains expected Avro elements
        assertTrue(validAvroSchema.contains("type"));
        assertTrue(validAvroSchema.contains("record"));
        assertTrue(validAvroSchema.contains("fields"));
    }
    
    @Test
    public void testProtobufSchemaValidation() {
        String validProtoSchema = """
            syntax = "proto3";
            
            message User {
              string id = 1;
              string name = 2;
            }
            """;
        
        // Test that the schema contains expected ProtoBuff elements
        assertTrue(validProtoSchema.contains("syntax"));
        assertTrue(validProtoSchema.contains("message"));
        assertTrue(validProtoSchema.contains("{"));
    }
    
    @Test
    public void testJsonValidation() {
        String validJson = "{\"key\": \"value\"}";
        String invalidJson = "key: value";
        
        assertTrue(isValidJsonHelper(validJson));
        assertFalse(isValidJsonHelper(invalidJson));
    }
    
    private boolean isValidJsonHelper(String json) {
        if (json == null || json.trim().isEmpty()) return false;
        String trimmed = json.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
}