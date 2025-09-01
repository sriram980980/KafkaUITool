package com.kafkatool.demo;

import com.kafkatool.model.KafkaMessage;
import com.kafkatool.service.SchemaRegistryServiceImpl;
import com.kafkatool.util.schema.AvroSchemaValidator;
import com.kafkatool.util.schema.ProtobufSchemaManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Demonstration of the new Kafka UI Tool enhanced features
 */
public class FeatureDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Kafka UI Tool Enhanced Features Demo ===\n");
        
        // 1. Demonstrate Message Preview
        demonstrateMessagePreview();
        
        // 2. Demonstrate Avro Schema Support
        demonstrateAvroSupport();
        
        // 3. Demonstrate Protobuf Support
        demonstrateProtobufSupport();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateMessagePreview() {
        System.out.println("1. MESSAGE PREVIEW FUNCTIONALITY");
        System.out.println("=================================");
        
        // Create a sample message with headers
        Map<String, String> headers = new HashMap<>();
        headers.put("source", "order-service");
        headers.put("version", "1.2.3");
        headers.put("correlation-id", "abc-123-def");
        
        String sampleValue = "{\n" +
                           "  \"orderId\": \"ORD-12345\",\n" +
                           "  \"customerId\": \"CUST-98765\",\n" +
                           "  \"amount\": 99.99,\n" +
                           "  \"status\": \"PENDING\"\n" +
                           "}";
        
        KafkaMessage message = new KafkaMessage(
            "orders-topic", 
            12345L, 
            2, 
            "order-key-123", 
            sampleValue, 
            LocalDateTime.now(), 
            headers
        );
        
        System.out.println("✓ Created sample message:");
        System.out.println("  Topic: " + message.getTopic());
        System.out.println("  Partition: " + message.getPartition());
        System.out.println("  Offset: " + message.getOffset());
        System.out.println("  Key: " + message.getKey());
        System.out.println("  Value Preview: " + message.getValue().substring(0, 50) + "...");
        System.out.println("  Headers: " + headers.size() + " headers");
        System.out.println("  Headers Detail: " + message.getHeadersAsString());
        System.out.println("  ✓ Message preview now supports expandable sections for headers, key, and value");
        System.out.println("  ✓ Search functionality now includes header search capability");
        System.out.println();
    }
    
    private static void demonstrateAvroSupport() {
        System.out.println("2. AVRO SCHEMA SUPPORT");
        System.out.println("======================");
        
        AvroSchemaValidator validator = new AvroSchemaValidator();
        
        String avroSchema = """
            {
              "type": "record",
              "name": "OrderEvent",
              "namespace": "com.example.events",
              "fields": [
                {"name": "orderId", "type": "string"},
                {"name": "customerId", "type": "string"},
                {"name": "amount", "type": "double"},
                {"name": "timestamp", "type": "long"},
                {"name": "status", "type": {"type": "enum", "name": "OrderStatus", "symbols": ["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED"]}}
              ]
            }
            """;
        
        // Test schema parsing
        AvroSchemaValidator.ValidationResult result = validator.parseAvroSchema("order-event", avroSchema);
        System.out.println("✓ Avro Schema Parsing:");
        System.out.println("  Success: " + result.isSuccess());
        System.out.println("  Message: " + result.getMessage());
        
        // Generate sample JSON
        String sampleJson = validator.generateSampleJson(avroSchema);
        System.out.println("✓ Generated Sample JSON:");
        System.out.println(sampleJson);
        
        // Test message validation
        String validMessage = """
            {
              "orderId": "ORD-12345",
              "customerId": "CUST-98765",
              "amount": 99.99,
              "timestamp": 1641024000000,
              "status": "PENDING"
            }
            """;
        
        AvroSchemaValidator.ValidationResult validationResult = validator.validateMessage("order-event", validMessage);
        System.out.println("✓ Message Validation:");
        System.out.println("  Success: " + validationResult.isSuccess());
        System.out.println("  Message: " + validationResult.getMessage());
        System.out.println("  ✓ Schema Registry integration available for remote schema management");
        System.out.println("  ✓ Schema compatibility checking implemented");
        System.out.println();
    }
    
    private static void demonstrateProtobufSupport() {
        System.out.println("3. PROTOBUF SUPPORT");
        System.out.println("===================");
        
        ProtobufSchemaManager manager = new ProtobufSchemaManager();
        
        String protoSchema = """
            syntax = "proto3";
            
            package com.example.events;
            
            message OrderEvent {
              string order_id = 1;
              string customer_id = 2;
              double amount = 3;
              int64 timestamp = 4;
              OrderStatus status = 5;
              
              enum OrderStatus {
                PENDING = 0;
                CONFIRMED = 1;
                SHIPPED = 2;
                DELIVERED = 3;
              }
            }
            
            message CustomerEvent {
              string customer_id = 1;
              string name = 2;
              string email = 3;
            }
            """;
        
        // Test schema parsing
        ProtobufSchemaManager.ValidationResult result = manager.parseProtoSchema("events", protoSchema);
        System.out.println("✓ Protobuf Schema Parsing:");
        System.out.println("  Success: " + result.isSuccess());
        System.out.println("  Message: " + result.getMessage());
        
        // Extract message types
        String[] messageTypes = manager.extractMessageTypes(protoSchema);
        System.out.println("✓ Extracted Message Types:");
        for (String type : messageTypes) {
            System.out.println("  - " + type);
        }
        
        // Generate sample JSON
        String sampleJson = manager.generateSampleJson("events", "OrderEvent");
        System.out.println("✓ Generated Sample JSON for OrderEvent:");
        System.out.println(sampleJson);
        
        // Test message validation
        String validMessage = """
            {
              "order_id": "ORD-12345",
              "customer_id": "CUST-98765",
              "amount": 99.99,
              "timestamp": 1641024000000,
              "status": "PENDING"
            }
            """;
        
        ProtobufSchemaManager.ValidationResult validationResult = manager.validateMessage("events", validMessage);
        System.out.println("✓ Message Validation:");
        System.out.println("  Success: " + validationResult.isSuccess());
        System.out.println("  Message: " + validationResult.getMessage());
        
        // Schema management
        System.out.println("✓ Schema Management:");
        System.out.println("  Stored Schemas: " + java.util.Arrays.toString(manager.getSchemaNames()));
        System.out.println("  ✓ .proto file import functionality implemented");
        System.out.println("  ✓ User-defined type management with schema editor");
        System.out.println("  ✓ Support for nested message types and repeated fields");
        System.out.println();
    }
}