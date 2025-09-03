package com.kafkatool.util.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for dynamic schema compiler functionality
 */
public class DynamicSchemaCompilerTest {
    
    private DynamicSchemaCompiler compiler;
    
    @BeforeEach
    void setUp() {
        compiler = new DynamicSchemaCompiler();
    }
    
    @Test
    void testAvroMessageParsing() {
        String avroSchema = """
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
        
        String message = """
            {
              "id": "12345",
              "name": "John Doe",
              "age": 30
            }
            """;
        
        DynamicSchemaCompiler.ParseResult result = compiler.parseAvroMessage(avroSchema, message);
        assertTrue(result.isSuccess());
        assertEquals("AVRO", result.getSchemaType());
        assertNotNull(result.getFormattedMessage());
        assertTrue(result.getFormattedMessage().contains("User"));
    }
    
    @Test
    void testInvalidAvroSchema() {
        String invalidSchema = "{ invalid json }";
        String message = "{ \"test\": \"value\" }";
        
        DynamicSchemaCompiler.ParseResult result = compiler.parseAvroMessage(invalidSchema, message);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }
    
    @Test
    void testProtobufMessageParsing() {
        String protoSchema = """
            syntax = "proto3";
            
            message User {
              string id = 1;
              string name = 2;
              int32 age = 3;
            }
            """;
        
        String message = """
            {
              "id": "12345",
              "name": "John Doe",
              "age": 30
            }
            """;
        
        DynamicSchemaCompiler.ParseResult result = compiler.parseProtobufMessage(protoSchema, message);
        assertTrue(result.isSuccess());
        assertEquals("PROTOBUF", result.getSchemaType());
        assertNotNull(result.getFormattedMessage());
    }
    
    @Test
    void testClassCompilation() {
        String className = "TestParser";
        String schemaType = "AVRO";
        String schema = "test schema";
        
        Class<?> compiledClass = compiler.compileParserClass(className, schemaType, schema);
        assertNotNull(compiledClass);
        assertEquals(className, compiledClass.getSimpleName());
    }
    
    @Test
    void testCacheClearing() {
        // Parse a message to populate cache
        String schema = """
            {
              "type": "record",
              "name": "Test",
              "fields": [{"name": "value", "type": "string"}]
            }
            """;
        
        compiler.parseAvroMessage(schema, "{\"value\": \"test\"}");
        
        // Clear cache should not throw exception
        assertDoesNotThrow(() -> compiler.clearCaches());
    }
}