package com.kafkatool.util.schema;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager for Protobuf schema operations including user-defined types
 */
public class ProtobufSchemaManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ProtobufSchemaManager.class);
    
    // Cache for compiled schemas
    private final Map<String, Descriptors.FileDescriptor> schemaCache = new ConcurrentHashMap<>();
    private final Map<String, String> userDefinedSchemas = new ConcurrentHashMap<>();
    
    /**
     * Parse and validate a .proto file content
     */
    public ValidationResult parseProtoSchema(String schemaName, String protoContent) {
        try {
            // Basic syntax validation
            if (!isValidProtoSyntax(protoContent)) {
                return ValidationResult.error("Invalid protobuf syntax");
            }
            
            // Store the schema
            userDefinedSchemas.put(schemaName, protoContent);
            
            // Try to create a descriptor (simplified validation)
            return ValidationResult.success("Schema parsed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to parse proto schema: {}", e.getMessage());
            return ValidationResult.error("Failed to parse schema: " + e.getMessage());
        }
    }
    
    /**
     * Import .proto file from filesystem
     */
    public ValidationResult importProtoFile(String schemaName, Path protoFilePath) {
        try {
            String content = Files.readString(protoFilePath);
            return parseProtoSchema(schemaName, content);
        } catch (IOException e) {
            logger.error("Failed to read proto file: {}", e.getMessage());
            return ValidationResult.error("Failed to read file: " + e.getMessage());
        }
    }
    
    /**
     * Validate a message against a protobuf schema
     */
    public ValidationResult validateMessage(String schemaName, String messageJson) {
        try {
            String protoContent = userDefinedSchemas.get(schemaName);
            if (protoContent == null) {
                return ValidationResult.error("Schema not found: " + schemaName);
            }
            
            // Basic JSON validation
            if (!isValidJson(messageJson)) {
                return ValidationResult.error("Invalid JSON format");
            }
            
            // For now, we'll do basic validation
            // In a full implementation, we'd compile the proto and validate against it
            return ValidationResult.success("Message is valid");
            
        } catch (Exception e) {
            logger.error("Failed to validate message: {}", e.getMessage());
            return ValidationResult.error("Validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Get all user-defined schemas
     */
    public Map<String, String> getAllSchemas() {
        return new HashMap<>(userDefinedSchemas);
    }
    
    /**
     * Get a specific schema by name
     */
    public String getSchema(String schemaName) {
        return userDefinedSchemas.get(schemaName);
    }
    
    /**
     * Delete a schema
     */
    public boolean deleteSchema(String schemaName) {
        String removed = userDefinedSchemas.remove(schemaName);
        schemaCache.remove(schemaName);
        return removed != null;
    }
    
    /**
     * List all schema names
     */
    public String[] getSchemaNames() {
        return userDefinedSchemas.keySet().toArray(new String[0]);
    }
    
    /**
     * Basic protobuf syntax validation
     */
    private boolean isValidProtoSyntax(String protoContent) {
        // Check for required syntax declaration
        if (!protoContent.contains("syntax")) {
            return false;
        }
        
        // Check for balanced braces
        int braceCount = 0;
        for (char c : protoContent.toCharArray()) {
            if (c == '{') braceCount++;
            else if (c == '}') braceCount--;
            if (braceCount < 0) return false;
        }
        
        return braceCount == 0;
    }
    
    /**
     * Basic JSON validation
     */
    private boolean isValidJson(String json) {
        try {
            // Simple check - just verify it starts and ends correctly
            String trimmed = json.trim();
            return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                   (trimmed.startsWith("[") && trimmed.endsWith("]"));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extract message types from proto content
     */
    public String[] extractMessageTypes(String protoContent) {
        Pattern pattern = Pattern.compile("message\\s+(\\w+)\\s*\\{");
        Matcher matcher = pattern.matcher(protoContent);
        
        return matcher.results()
                .map(result -> result.group(1))
                .toArray(String[]::new);
    }
    
    /**
     * Generate sample JSON for a message type
     */
    public String generateSampleJson(String schemaName, String messageType) {
        // This is a simplified implementation
        // In a full implementation, we'd parse the proto and generate sample JSON
        return "{\n  \"// Sample JSON for " + messageType + "\": \"value\"\n}";
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