package com.kafkatool.demo;

import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.TopicInfo;
import com.kafkatool.service.SchemaAwareMessageService;
import com.kafkatool.util.schema.DynamicSchemaCompiler;

/**
 * Demo application to verify schema integration functionality
 */
public class SchemaIntegrationDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Kafka UI Tool Schema Integration Demo ===\n");
        
        demonstrateConnectionLevelSchemaSupport();
        demonstrateTopicLevelSchemaOverrides();
        demonstrateSchemaAwareParsing();
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static void demonstrateConnectionLevelSchemaSupport() {
        System.out.println("1. CONNECTION-LEVEL SCHEMA SUPPORT");
        System.out.println("==================================");
        
        // Create cluster with schema support
        ClusterInfo cluster = new ClusterInfo("demo-cluster", "localhost:9092");
        cluster.setAvroSupportEnabled(true);
        cluster.setProtobufSupportEnabled(true);
        cluster.setDefaultMessageFormat("AVRO");
        cluster.setSchemaRegistryEnabled(true);
        cluster.setSchemaRegistryUrl("http://localhost:8081");
        
        System.out.println("✓ Created cluster with schema support:");
        System.out.println("  - Avro Support: " + cluster.isAvroSupportEnabled());
        System.out.println("  - Protobuf Support: " + cluster.isProtobufSupportEnabled());
        System.out.println("  - Default Format: " + cluster.getDefaultMessageFormat());
        System.out.println("  - Schema Registry: " + cluster.getSchemaRegistryUrl());
        System.out.println();
    }
    
    private static void demonstrateTopicLevelSchemaOverrides() {
        System.out.println("2. TOPIC-LEVEL SCHEMA OVERRIDES");
        System.out.println("===============================");
        
        // Create topic with inline schema
        TopicInfo topic = new TopicInfo("user-events", 3, (short) 1);
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
                {"name": "metadata", "type": ["null", "string"], "default": null}
              ]
            }
            """);
        
        System.out.println("✓ Created topic with inline Avro schema:");
        System.out.println("  - Topic: " + topic.getName());
        System.out.println("  - Schema Format: " + topic.getSchemaFormat());
        System.out.println("  - Uses Inline Schema: " + topic.isUseInlineSchema());
        System.out.println("  - Schema Type: UserEvent record");
        System.out.println();
        
        // Create topic with Protobuf schema
        TopicInfo protoTopic = new TopicInfo("order-events", 5, (short) 2);
        protoTopic.setSchemaFormat("PROTOBUF");
        protoTopic.setUseInlineSchema(true);
        protoTopic.setInlineProtobufSchema("""
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
            """);
        
        System.out.println("✓ Created topic with inline Protobuf schema:");
        System.out.println("  - Topic: " + protoTopic.getName());
        System.out.println("  - Schema Format: " + protoTopic.getSchemaFormat());
        System.out.println("  - Uses Inline Schema: " + protoTopic.isUseInlineSchema());
        System.out.println("  - Schema Type: OrderEvent message");
        System.out.println();
    }
    
    private static void demonstrateSchemaAwareParsing() {
        System.out.println("3. SCHEMA-AWARE MESSAGE PARSING");
        System.out.println("===============================");
        
        SchemaAwareMessageService messageService = new SchemaAwareMessageService();
        
        // Setup cluster and topic
        ClusterInfo cluster = new ClusterInfo("demo-cluster", "localhost:9092");
        cluster.setAvroSupportEnabled(true);
        cluster.setDefaultMessageFormat("AVRO");
        
        TopicInfo topic = new TopicInfo("user-events", 3, (short) 1);
        topic.setSchemaFormat("AVRO");
        topic.setUseInlineSchema(true);
        topic.setInlineAvroSchema("""
            {
              "type": "record",
              "name": "User",
              "fields": [
                {"name": "id", "type": "string"},
                {"name": "name", "type": "string"},
                {"name": "age", "type": "int"}
              ]
            }
            """);
        
        // Test message parsing
        String jsonMessage = """
            {
              "id": "user123",
              "name": "John Doe",
              "age": 30
            }
            """;
        
        SchemaAwareMessageService.MessageParseResult result = 
            messageService.parseMessage(jsonMessage, cluster, topic);
        
        System.out.println("✓ Parsed message with schema-aware service:");
        System.out.println("  - Success: " + result.isSuccess());
        System.out.println("  - Schema Type: " + result.getSchemaType());
        System.out.println("  - Processing Info: " + result.getProcessingInfo());
        System.out.println("  - Formatted Output:");
        System.out.println(indent(result.getFormattedMessage(), "    "));
        System.out.println();
        
        // Test dynamic compilation
        System.out.println("✓ Testing dynamic schema compilation:");
        DynamicSchemaCompiler compiler = new DynamicSchemaCompiler();
        
        DynamicSchemaCompiler.ParseResult parseResult = 
            compiler.parseAvroMessage(topic.getInlineAvroSchema(), jsonMessage);
        
        System.out.println("  - Compilation Success: " + parseResult.isSuccess());
        System.out.println("  - Schema Type: " + parseResult.getSchemaType());
        if (parseResult.isSuccess()) {
            System.out.println("  - Parsed Output:");
            System.out.println(indent(parseResult.getFormattedMessage(), "    "));
        } else {
            System.out.println("  - Error: " + parseResult.getErrorMessage());
        }
        System.out.println();
        
        // Test capability checking
        boolean canParse = messageService.canParseMessage(cluster, topic);
        System.out.println("✓ Message parsing capability check: " + canParse);
        System.out.println();
    }
    
    private static String indent(String text, String indentation) {
        if (text == null) return "null";
        return text.lines()
                  .map(line -> indentation + line)
                  .reduce((a, b) -> a + "\n" + b)
                  .orElse("");
    }
}