package com.kafkatool.util.schema;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Utility for Avro schema validation and management
 */
public class AvroSchemaValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(AvroSchemaValidator.class);
    
    // Cache for compiled Avro schemas
    private final Map<String, Schema> schemaCache = new ConcurrentHashMap<>();
    
    /**
     * Parse and validate an Avro schema
     */
    public ValidationResult parseAvroSchema(String schemaName, String avroSchemaJson) {
        try {
            Schema schema = new Schema.Parser().parse(avroSchemaJson);
            schemaCache.put(schemaName, schema);
            return ValidationResult.success("Avro schema parsed successfully");
        } catch (Exception e) {
            logger.error("Failed to parse Avro schema: {}", e.getMessage());
            return ValidationResult.error("Invalid Avro schema: " + e.getMessage());
        }
    }
    
    /**
     * Validate a JSON message against an Avro schema
     */
    public ValidationResult validateMessage(String schemaName, String messageJson) {
        try {
            Schema schema = schemaCache.get(schemaName);
            if (schema == null) {
                return ValidationResult.error("Schema not found: " + schemaName);
            }
            
            DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
            JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, messageJson);
            GenericRecord record = reader.read(null, decoder);
            
            return ValidationResult.success("Message is valid according to Avro schema");
            
        } catch (Exception e) {
            logger.error("Failed to validate message against Avro schema: {}", e.getMessage());
            return ValidationResult.error("Validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Check if two schemas are compatible
     */
    public ValidationResult checkCompatibility(String schema1Json, String schema2Json) {
        try {
            Schema schema1 = new Schema.Parser().parse(schema1Json);
            Schema schema2 = new Schema.Parser().parse(schema2Json);
            
            // Basic compatibility check - in real implementation, we'd use Schema Registry's compatibility checker
            if (schema1.equals(schema2)) {
                return ValidationResult.success("Schemas are identical");
            }
            
            // For now, just check if types match
            if (schema1.getType() == schema2.getType()) {
                return ValidationResult.success("Schemas appear compatible (basic check)");
            } else {
                return ValidationResult.error("Schema types don't match");
            }
            
        } catch (Exception e) {
            logger.error("Failed to check schema compatibility: {}", e.getMessage());
            return ValidationResult.error("Compatibility check failed: " + e.getMessage());
        }
    }
    
    /**
     * Get cached schema
     */
    public Schema getSchema(String schemaName) {
        return schemaCache.get(schemaName);
    }
    
    /**
     * Remove schema from cache
     */
    public boolean removeSchema(String schemaName) {
        return schemaCache.remove(schemaName) != null;
    }
    
    /**
     * Clear all cached schemas
     */
    public void clearCache() {
        schemaCache.clear();
    }
    
    /**
     * Generate sample JSON for an Avro schema
     */
    public String generateSampleJson(String avroSchemaJson) {
        try {
            Schema schema = new Schema.Parser().parse(avroSchemaJson);
            return generateSampleForSchema(schema);
        } catch (Exception e) {
            logger.error("Failed to generate sample JSON: {}", e.getMessage());
            return "{}";
        }
    }
    
    private String generateSampleForSchema(Schema schema) {
        StringBuilder sb = new StringBuilder();
        
        switch (schema.getType()) {
            case RECORD:
                sb.append("{\n");
                boolean first = true;
                for (Schema.Field field : schema.getFields()) {
                    if (!first) sb.append(",\n");
                    sb.append("  \"").append(field.name()).append("\": ");
                    sb.append(generateSampleForSchema(field.schema()));
                    first = false;
                }
                sb.append("\n}");
                break;
            case STRING:
                sb.append("\"sample_string\"");
                break;
            case INT:
                sb.append("42");
                break;
            case LONG:
                sb.append("12345678901234");
                break;
            case FLOAT:
                sb.append("3.14");
                break;
            case DOUBLE:
                sb.append("3.141592653589793");
                break;
            case BOOLEAN:
                sb.append("true");
                break;
            case NULL:
                sb.append("null");
                break;
            case ARRAY:
                sb.append("[");
                sb.append(generateSampleForSchema(schema.getElementType()));
                sb.append("]");
                break;
            case MAP:
                sb.append("{\"key\": ");
                sb.append(generateSampleForSchema(schema.getValueType()));
                sb.append("}");
                break;
            case UNION:
                // Use the first non-null type
                for (Schema unionSchema : schema.getTypes()) {
                    if (unionSchema.getType() != Schema.Type.NULL) {
                        return generateSampleForSchema(unionSchema);
                    }
                }
                sb.append("null");
                break;
            default:
                sb.append("\"unknown_type\"");
        }
        
        return sb.toString();
    }
    
    /**
     * Result class for validation operations
     */
    public static class ValidationResult {
        private final boolean success;
        private final String message;
        
        private ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static ValidationResult success(String message) {
            return new ValidationResult(true, message);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}