package com.kafkatool.service;

import com.kafkatool.model.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for Schema Registry operations
 */
public interface SchemaRegistryService {
    
    /**
     * Test connection to Schema Registry
     */
    CompletableFuture<Boolean> testConnectionAsync(String schemaRegistryUrl);
    
    /**
     * Test connection to Schema Registry with authentication
     */
    CompletableFuture<Boolean> testConnectionAsync(String schemaRegistryUrl, AuthenticationType authType, AuthenticationConfig authConfig);
    
    /**
     * Get all subjects in the Schema Registry
     */
    CompletableFuture<List<String>> getSubjectsAsync(String schemaRegistryUrl);
    
    /**
     * Get all versions for a subject
     */
    CompletableFuture<List<Integer>> getSubjectVersionsAsync(String schemaRegistryUrl, String subject);
    
    /**
     * Get schema information by subject and version
     */
    CompletableFuture<SchemaInfo> getSchemaAsync(String schemaRegistryUrl, String subject, int version);
    
    /**
     * Get latest schema for a subject
     */
    CompletableFuture<SchemaInfo> getLatestSchemaAsync(String schemaRegistryUrl, String subject);
    
    /**
     * Register a new schema
     */
    CompletableFuture<Integer> registerSchemaAsync(String schemaRegistryUrl, String subject, 
                                                   String schemaType, String schema);
    
    /**
     * Delete a subject (and all its versions)
     */
    CompletableFuture<List<Integer>> deleteSubjectAsync(String schemaRegistryUrl, String subject);
    
    /**
     * Delete a specific version of a subject
     */
    CompletableFuture<Integer> deleteSchemaVersionAsync(String schemaRegistryUrl, String subject, int version);
    
    /**
     * Get compatibility level for a subject
     */
    CompletableFuture<String> getCompatibilityAsync(String schemaRegistryUrl, String subject);
    
    /**
     * Set compatibility level for a subject
     */
    CompletableFuture<String> setCompatibilityAsync(String schemaRegistryUrl, String subject, String compatibility);
    
    /**
     * Check if a schema is compatible with the subject
     */
    CompletableFuture<Boolean> testCompatibilityAsync(String schemaRegistryUrl, String subject, 
                                                      String schemaType, String schema);
    
    /**
     * Get global compatibility level
     */
    CompletableFuture<String> getGlobalCompatibilityAsync(String schemaRegistryUrl);
    
    /**
     * Set global compatibility level
     */
    CompletableFuture<String> setGlobalCompatibilityAsync(String schemaRegistryUrl, String compatibility);
}