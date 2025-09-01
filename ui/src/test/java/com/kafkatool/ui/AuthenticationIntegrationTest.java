package com.kafkatool.ui;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.model.ClusterInfo;
import com.kafkatool.util.KafkaAuthenticationUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for authentication integration in UI components
 */
public class AuthenticationIntegrationTest {
    
    @Test
    public void testClusterInfoWithAuthentication() {
        // Test creating cluster with SASL Plain authentication
        AuthenticationConfig authConfig = new AuthenticationConfig("testuser", "testpass");
        ClusterInfo cluster = new ClusterInfo("Test Cluster", "localhost:9092", 
            AuthenticationType.SASL_PLAIN, authConfig);
        
        assertNotNull(cluster);
        assertEquals("Test Cluster", cluster.getName());
        assertEquals("localhost:9092", cluster.getBrokerUrls());
        assertEquals(AuthenticationType.SASL_PLAIN, cluster.getAuthenticationType());
        assertTrue(cluster.requiresAuthentication());
        
        AuthenticationConfig retrievedConfig = cluster.getAuthenticationConfig();
        assertNotNull(retrievedConfig);
        assertEquals("testuser", retrievedConfig.getUsername());
        assertEquals("testpass", retrievedConfig.getPassword());
    }
    
    @Test
    public void testBackwardCompatibility() {
        // Test that existing cluster creation still works
        ClusterInfo cluster = new ClusterInfo("Old Cluster", "localhost:9092");
        
        assertNotNull(cluster);
        assertEquals("Old Cluster", cluster.getName());
        assertEquals("localhost:9092", cluster.getBrokerUrls());
        assertEquals(AuthenticationType.NONE, cluster.getAuthenticationType());
        assertFalse(cluster.requiresAuthentication());
        assertNull(cluster.getAuthenticationConfig());
    }
    
    @Test
    public void testMaskedCopy() {
        AuthenticationConfig authConfig = new AuthenticationConfig("testuser", "secretpass");
        authConfig.setKeystorePassword("keystorepass");
        
        ClusterInfo cluster = new ClusterInfo("Test Cluster", "localhost:9092", 
            AuthenticationType.SASL_PLAIN, authConfig);
        
        ClusterInfo masked = cluster.createMaskedCopy();
        
        assertNotNull(masked);
        assertEquals("Test Cluster", masked.getName());
        assertEquals("testuser", masked.getAuthenticationConfig().getUsername());
        assertEquals("****", masked.getAuthenticationConfig().getPassword());
        assertEquals("****", masked.getAuthenticationConfig().getKeystorePassword());
    }
    
    @Test
    public void testValidationIntegration() {
        // Test valid SASL configuration
        AuthenticationConfig validConfig = new AuthenticationConfig("user", "pass");
        assertTrue(KafkaAuthenticationUtil.validateAuthenticationConfig(
            AuthenticationType.SASL_PLAIN, validConfig));
        
        // Test invalid SASL configuration
        AuthenticationConfig invalidConfig = new AuthenticationConfig("", "");
        assertFalse(KafkaAuthenticationUtil.validateAuthenticationConfig(
            AuthenticationType.SASL_PLAIN, invalidConfig));
        
        // Test valid SSL configuration
        AuthenticationConfig sslConfig = new AuthenticationConfig();
        sslConfig.setTruststoreLocation("/path/to/truststore.jks");
        assertTrue(KafkaAuthenticationUtil.validateAuthenticationConfig(
            AuthenticationType.SSL, sslConfig));
    }
    
    @Test
    public void testAllAuthenticationTypes() {
        for (AuthenticationType type : AuthenticationType.values()) {
            assertNotNull(type.getDisplayName());
            assertNotNull(type.toString());
            
            // Ensure each type can be used to create a cluster
            ClusterInfo cluster = new ClusterInfo("Test", "localhost:9092", type, null);
            assertEquals(type, cluster.getAuthenticationType());
        }
    }
}