package com.kafkatool.service;

import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.TopicInfo;
import com.kafkatool.util.schema.DynamicSchemaCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for parsing and displaying messages based on schema configuration
 */
public class SchemaAwareMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchemaAwareMessageService.class);
    
    private final DynamicSchemaCompiler schemaCompiler;
    
    public SchemaAwareMessageService() {
        this.schemaCompiler = new DynamicSchemaCompiler();
    }
    
    /**
     * Parse and format message based on cluster and topic schema configuration
     */
    public MessageParseResult parseMessage(String messageValue, ClusterInfo clusterInfo, TopicInfo topicInfo) {
        try {
            // Determine the effective schema format
            String effectiveFormat = determineEffectiveFormat(clusterInfo, topicInfo);
            
            // If no schema format is specified, return as-is
            if (effectiveFormat == null || "STRING".equals(effectiveFormat)) {
                return MessageParseResult.success(messageValue, "STRING", "No schema processing");
            }
            
            // Check if topic has inline schema override
            if (topicInfo != null && topicInfo.isUseInlineSchema()) {
                return parseWithInlineSchema(messageValue, effectiveFormat, topicInfo);
            }
            
            // Use cluster-level schema registry if available
            if (clusterInfo.isSchemaRegistryEnabled() && ("AVRO".equals(effectiveFormat) || "PROTOBUF".equals(effectiveFormat))) {
                return parseWithSchemaRegistry(messageValue, effectiveFormat, clusterInfo);
            }
            
            // Default to JSON formatting for structured formats
            if ("JSON".equals(effectiveFormat)) {
                return MessageParseResult.success(formatAsJson(messageValue), "JSON", "JSON formatted");
            }
            
            return MessageParseResult.success(messageValue, effectiveFormat, "No specific schema processing available");
            
        } catch (Exception e) {
            logger.error("Failed to parse message: {}", e.getMessage());
            return MessageParseResult.error("Message parsing failed: " + e.getMessage());
        }
    }
    
    private String determineEffectiveFormat(ClusterInfo clusterInfo, TopicInfo topicInfo) {
        // Topic-level override takes precedence
        if (topicInfo != null && topicInfo.getSchemaFormat() != null) {
            return topicInfo.getSchemaFormat();
        }
        
        // Fall back to cluster default
        return clusterInfo.getDefaultMessageFormat();
    }
    
    private MessageParseResult parseWithInlineSchema(String messageValue, String format, TopicInfo topicInfo) {
        try {
            if ("AVRO".equals(format) && topicInfo.getInlineAvroSchema() != null) {
                DynamicSchemaCompiler.ParseResult result = schemaCompiler.parseAvroMessage(
                    topicInfo.getInlineAvroSchema(), messageValue);
                if (result.isSuccess()) {
                    return MessageParseResult.success(result.getFormattedMessage(), "AVRO", 
                        "Parsed with inline Avro schema");
                } else {
                    return MessageParseResult.error("Avro parsing failed: " + result.getErrorMessage());
                }
            } else if ("PROTOBUF".equals(format) && topicInfo.getInlineProtobufSchema() != null) {
                DynamicSchemaCompiler.ParseResult result = schemaCompiler.parseProtobufMessage(
                    topicInfo.getInlineProtobufSchema(), messageValue);
                if (result.isSuccess()) {
                    return MessageParseResult.success(result.getFormattedMessage(), "PROTOBUF", 
                        "Parsed with inline Protobuf schema");
                } else {
                    return MessageParseResult.error("Protobuf parsing failed: " + result.getErrorMessage());
                }
            }
            
            return MessageParseResult.success(messageValue, format, "No inline schema available");
        } catch (Exception e) {
            logger.error("Failed to parse with inline schema: {}", e.getMessage());
            return MessageParseResult.error("Inline schema parsing failed: " + e.getMessage());
        }
    }
    
    private MessageParseResult parseWithSchemaRegistry(String messageValue, String format, ClusterInfo clusterInfo) {
        // For now, return formatted indication that schema registry would be used
        // Full implementation would integrate with SchemaRegistryService
        return MessageParseResult.success(messageValue, format, 
            "Would use Schema Registry at: " + clusterInfo.getSchemaRegistryUrl());
    }
    
    private String formatAsJson(String value) {
        try {
            // Simple JSON formatting
            if (value.trim().startsWith("{") || value.trim().startsWith("[")) {
                return value.replaceAll(",", ",\n  ").replaceAll("\\{", "{\n  ").replaceAll("\\}", "\n}");
            }
            return value;
        } catch (Exception e) {
            return value;
        }
    }
    
    /**
     * Check if message can be parsed with the given schema configuration
     */
    public boolean canParseMessage(ClusterInfo clusterInfo, TopicInfo topicInfo) {
        String effectiveFormat = determineEffectiveFormat(clusterInfo, topicInfo);
        
        if ("STRING".equals(effectiveFormat) || "JSON".equals(effectiveFormat)) {
            return true;
        }
        
        if ("AVRO".equals(effectiveFormat)) {
            return clusterInfo.isAvroSupportEnabled() && 
                   (clusterInfo.isSchemaRegistryEnabled() || 
                    (topicInfo != null && topicInfo.isUseInlineSchema() && topicInfo.getInlineAvroSchema() != null));
        }
        
        if ("PROTOBUF".equals(effectiveFormat)) {
            return clusterInfo.isProtobufSupportEnabled() && 
                   (clusterInfo.isSchemaRegistryEnabled() || 
                    (topicInfo != null && topicInfo.isUseInlineSchema() && topicInfo.getInlineProtobufSchema() != null));
        }
        
        return false;
    }
    
    /**
     * Result class for message parsing operations
     */
    public static class MessageParseResult {
        private final boolean success;
        private final String formattedMessage;
        private final String schemaType;
        private final String processingInfo;
        private final String errorMessage;
        
        private MessageParseResult(boolean success, String formattedMessage, String schemaType, 
                                 String processingInfo, String errorMessage) {
            this.success = success;
            this.formattedMessage = formattedMessage;
            this.schemaType = schemaType;
            this.processingInfo = processingInfo;
            this.errorMessage = errorMessage;
        }
        
        public static MessageParseResult success(String formattedMessage, String schemaType, String processingInfo) {
            return new MessageParseResult(true, formattedMessage, schemaType, processingInfo, null);
        }
        
        public static MessageParseResult error(String errorMessage) {
            return new MessageParseResult(false, null, null, null, errorMessage);
        }
        
        public boolean isSuccess() { return success; }
        public String getFormattedMessage() { return formattedMessage; }
        public String getSchemaType() { return schemaType; }
        public String getProcessingInfo() { return processingInfo; }
        public String getErrorMessage() { return errorMessage; }
    }
}