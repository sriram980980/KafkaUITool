package com.kafkatool.service;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.AuthenticationType;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Schema Registry authentication functionality
 */
public class SchemaRegistryAuthenticationTest {
    
    private final SchemaRegistryService schemaRegistryService = new SchemaRegistryServiceImpl();
    
    @Test
    public void testSchemaRegistryConnectionWithoutAuth() throws Exception {
        // Test connection without authentication (should handle gracefully)
        CompletableFuture<Boolean> result = schemaRegistryService.testConnectionAsync("http://localhost:8081");
        
        // The test should not throw an exception even if connection fails
        // (since we're not running an actual Schema Registry)
        assertNotNull(result);
        
        // Wait for completion but expect false since no server is running
        Boolean connected = result.get();
        assertFalse(connected); // Expected to fail since no server running
    }
    
    @Test
    public void testSchemaRegistryConnectionWithSaslAuth() throws Exception {
        AuthenticationConfig authConfig = new AuthenticationConfig("testuser", "testpass");
        
        // Test connection with SASL authentication
        CompletableFuture<Boolean> result = schemaRegistryService.testConnectionAsync(
            "http://localhost:8081", AuthenticationType.SASL_PLAIN, authConfig);
        
        // The test should not throw an exception even if connection fails
        assertNotNull(result);
        
        // Wait for completion but expect false since no server is running
        Boolean connected = result.get();
        assertFalse(connected); // Expected to fail since no server running
    }
    
    @Test
    public void testSchemaRegistryConnectionWithNullAuth() throws Exception {
        // Test connection with null authentication (should work like no auth)
        CompletableFuture<Boolean> result = schemaRegistryService.testConnectionAsync(
            "http://localhost:8081", AuthenticationType.NONE, null);
        
        assertNotNull(result);
        
        // Wait for completion but expect false since no server is running
        Boolean connected = result.get();
        assertFalse(connected); // Expected to fail since no server running
    }
    
    @Test
    public void testSchemaRegistryConnectionWithEmptyCredentials() throws Exception {
        AuthenticationConfig authConfig = new AuthenticationConfig("", "");
        
        // Test connection with empty credentials (should handle gracefully)
        CompletableFuture<Boolean> result = schemaRegistryService.testConnectionAsync(
            "http://localhost:8081", AuthenticationType.SASL_PLAIN, authConfig);
        
        assertNotNull(result);
        
        // Wait for completion but expect false since no server is running
        Boolean connected = result.get();
        assertFalse(connected); // Expected to fail since no server running
    }
}