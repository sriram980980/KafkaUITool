package com.kafkatool.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Utility class for JSON formatting and validation
 */
public class JsonFormatter {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * Format JSON string with proper indentation
     * @param jsonString Raw JSON string
     * @return Formatted JSON string or null if not valid JSON
     */
    public static String formatJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            return null; // Not valid JSON
        }
    }
    
    /**
     * Check if a string is valid JSON
     * @param jsonString String to check
     * @return true if valid JSON, false otherwise
     */
    public static boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }
        
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Minify JSON string (remove formatting)
     * @param jsonString Formatted JSON string
     * @return Minified JSON string or original if not valid JSON
     */
    public static String minifyJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return jsonString;
        }
        
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            return jsonString; // Return original if not valid JSON
        }
    }
}