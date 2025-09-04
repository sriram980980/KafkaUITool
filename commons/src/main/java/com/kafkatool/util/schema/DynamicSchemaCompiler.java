package com.kafkatool.util.schema;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.codehaus.janino.SimpleCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dynamic schema compiler using Janino for runtime compilation and message parsing
 */
public class DynamicSchemaCompiler {
    
    private static final Logger logger = LoggerFactory.getLogger(DynamicSchemaCompiler.class);
    
    // Cache for compiled schemas and classes
    private final Map<String, Schema> avroSchemaCache = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> compiledClassCache = new ConcurrentHashMap<>();
    
    /**
     * Parse and display Avro message using inline schema
     */
    public ParseResult parseAvroMessage(String schemaJson, String messageJson) {
        try {
            Schema schema = avroSchemaCache.computeIfAbsent(schemaJson, this::parseAvroSchema);
            if (schema == null) {
                return ParseResult.error("Failed to parse Avro schema");
            }
            
            // For JSON input, we need to parse it first
            if (messageJson.trim().startsWith("{")) {
                // This is a simplified JSON-to-Avro conversion
                // In a full implementation, we'd use proper Avro JSON parsing
                return ParseResult.success(formatJsonAsAvro(messageJson, schema), "AVRO");
            } else {
                // Assume it's already in Avro format
                return ParseResult.success(messageJson, "AVRO");
            }
        } catch (Exception e) {
            logger.error("Failed to parse Avro message: {}", e.getMessage());
            return ParseResult.error("Avro parsing failed: " + e.getMessage());
        }
    }
    
    /**
     * Parse Protobuf message using inline schema (simplified implementation)
     */
    public ParseResult parseProtobufMessage(String schemaProto, String messageJson) {
        try {
            // For now, return formatted JSON with protobuf indication
            // Full protobuf parsing would require compiling the .proto file
            return ParseResult.success(formatJson(messageJson), "PROTOBUF");
        } catch (Exception e) {
            logger.error("Failed to parse Protobuf message: {}", e.getMessage());
            return ParseResult.error("Protobuf parsing failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate dynamic parser class using Janino (example implementation)
     */
    public Class<?> compileParserClass(String className, String schemaType, String schema) {
        try {
            String classCode = generateParserClassCode(className, schemaType, schema);
            SimpleCompiler compiler = new SimpleCompiler();
            compiler.cook(classCode);
            
            Class<?> clazz = compiler.getClassLoader().loadClass(className);
            compiledClassCache.put(className, clazz);
            return clazz;
        } catch (Exception e) {
            logger.error("Failed to compile parser class: {}", e.getMessage());
            return null;
        }
    }
    
    private Schema parseAvroSchema(String schemaJson) {
        try {
            return new Schema.Parser().parse(schemaJson);
        } catch (Exception e) {
            logger.error("Failed to parse Avro schema: {}", e.getMessage());
            return null;
        }
    }
    
    private String formatAvroRecord(GenericRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append("Avro Record:\n");
        record.getSchema().getFields().forEach(field -> {
            Object value = record.get(field.name());
            sb.append("  ").append(field.name()).append(": ").append(value).append("\n");
        });
        return sb.toString();
    }
    
    private String formatJsonAsAvro(String jsonMessage, Schema schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("Avro-formatted (").append(schema.getName()).append("):\n");
        sb.append("Schema: ").append(schema.getType()).append("\n");
        sb.append("Message: ").append(jsonMessage).append("\n");
        return sb.toString();
    }
    
    private String formatJson(String json) {
        // Simple JSON formatting - could be enhanced
        try {
            return json.replaceAll(",", ",\n  ").replaceAll("\\{", "{\n  ").replaceAll("\\}", "\n}");
        } catch (Exception e) {
            return json;
        }
    }
    
    private String generateParserClassCode(String className, String schemaType, String schema) {
        return String.format("""
            public class %s {
                public String parse(String message) {
                    return "Parsed with %s: " + message;
                }
            }
            """, className, schemaType);
    }
    
    /**
     * Clear all caches
     */
    public void clearCaches() {
        avroSchemaCache.clear();
        compiledClassCache.clear();
    }
    
    /**
     * Result class for parse operations
     */
    public static class ParseResult {
        private final boolean success;
        private final String formattedMessage;
        private final String schemaType;
        private final String errorMessage;
        
        private ParseResult(boolean success, String formattedMessage, String schemaType, String errorMessage) {
            this.success = success;
            this.formattedMessage = formattedMessage;
            this.schemaType = schemaType;
            this.errorMessage = errorMessage;
        }
        
        public static ParseResult success(String formattedMessage, String schemaType) {
            return new ParseResult(true, formattedMessage, schemaType, null);
        }
        
        public static ParseResult error(String errorMessage) {
            return new ParseResult(false, null, null, errorMessage);
        }
        
        public boolean isSuccess() { return success; }
        public String getFormattedMessage() { return formattedMessage; }
        public String getSchemaType() { return schemaType; }
        public String getErrorMessage() { return errorMessage; }
    }
}