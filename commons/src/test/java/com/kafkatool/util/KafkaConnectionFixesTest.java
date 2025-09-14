package com.kafkatool.util;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.model.ClusterInfo;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the Kafka authentication connection fixes
 */
public class KafkaConnectionFixesTest {
    
    @Test
    public void testSaslPlainConfiguration() {
        System.out.println("=== Testing SASL PLAIN Authentication ===");
        
        AuthenticationConfig authConfig = new AuthenticationConfig("testuser", "testpass");
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092", AuthenticationType.SASL_PLAIN, authConfig);
        
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        
        // Validate configuration before applying
        assertTrue(KafkaAuthenticationUtil.validateAuthenticationConfig(AuthenticationType.SASL_PLAIN, authConfig));
        
        KafkaAuthenticationUtil.configureAuthentication(props, cluster);
        
        assertEquals("SASL_PLAINTEXT", props.get("security.protocol"));
        assertEquals("PLAIN", props.get("sasl.mechanism"));
        assertTrue(props.get("sasl.jaas.config").toString().contains("PlainLoginModule"));
        assertTrue(props.get("sasl.jaas.config").toString().contains("testuser"));
        assertTrue(props.get("sasl.jaas.config").toString().contains("testpass"));
        
        System.out.println("✓ SASL PLAIN authentication configured correctly");
    }
    
    @Test
    public void testSaslSslWithoutTruststore() {
        System.out.println("=== Testing SASL SSL without Truststore ===");
        
        AuthenticationConfig authConfig = new AuthenticationConfig("testuser", "testpass");
        // Note: No truststore location set - this should work now
        
        // Validate configuration - should be valid without truststore
        assertTrue(KafkaAuthenticationUtil.validateAuthenticationConfig(AuthenticationType.SASL_SSL, authConfig));
        
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092", AuthenticationType.SASL_SSL, authConfig);
        
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        
        KafkaAuthenticationUtil.configureAuthentication(props, cluster);
        
        assertEquals("SASL_SSL", props.get("security.protocol"));
        assertEquals("PLAIN", props.get("sasl.mechanism"));
        assertTrue(props.get("sasl.jaas.config").toString().contains("PlainLoginModule"));
        assertTrue(props.get("sasl.jaas.config").toString().contains("testuser"));
        
        // Verify that SSL properties are not set when not provided
        assertNull(props.get("ssl.truststore.location"));
        
        System.out.println("✓ SASL SSL authentication without truststore configured correctly");
    }
    
    @Test
    public void testSaslSslWithCustomTruststore() {
        System.out.println("=== Testing SASL SSL with Custom Truststore ===");
        
        AuthenticationConfig authConfig = new AuthenticationConfig("testuser", "testpass");
        authConfig.setTruststoreLocation("/path/to/custom-truststore.jks");
        authConfig.setTruststorePassword("truststorepass");
        
        // Validate configuration
        assertTrue(KafkaAuthenticationUtil.validateAuthenticationConfig(AuthenticationType.SASL_SSL, authConfig));
        
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092", AuthenticationType.SASL_SSL, authConfig);
        
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        
        KafkaAuthenticationUtil.configureAuthentication(props, cluster);
        
        assertEquals("SASL_SSL", props.get("security.protocol"));
        assertEquals("PLAIN", props.get("sasl.mechanism"));
        assertEquals("/path/to/custom-truststore.jks", props.get("ssl.truststore.location"));
        assertEquals("truststorepass", props.get("ssl.truststore.password"));
        assertTrue(props.get("sasl.jaas.config").toString().contains("testuser"));
        
        System.out.println("✓ SASL SSL authentication with custom truststore configured correctly");
    }
    
    @Test
    public void testSaslSslValidationErrors() {
        System.out.println("=== Testing SASL SSL Validation Errors ===");
        
        // Test missing username
        AuthenticationConfig configMissingUser = new AuthenticationConfig();
        configMissingUser.setPassword("testpass");
        assertFalse(KafkaAuthenticationUtil.validateAuthenticationConfig(AuthenticationType.SASL_SSL, configMissingUser));
        
        // Test missing password
        AuthenticationConfig configMissingPass = new AuthenticationConfig();
        configMissingPass.setUsername("testuser");
        assertFalse(KafkaAuthenticationUtil.validateAuthenticationConfig(AuthenticationType.SASL_SSL, configMissingPass));
        
        // Test null configuration
        assertFalse(KafkaAuthenticationUtil.validateAuthenticationConfig(AuthenticationType.SASL_SSL, null));
        
        System.out.println("✓ SASL SSL validation correctly rejects invalid configurations");
    }
    
    @Test
    public void testSchemaRegistryConnector() {
        System.out.println("=== Testing Schema Registry Connector ===");
        
        // Test cluster with Schema Registry authentication
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092");
        cluster.setSchemaRegistryEnabled(true);
        cluster.setSchemaRegistryUrl("http://localhost:8081");
        cluster.setSchemaRegistryAuthType(AuthenticationType.SASL_PLAIN);
        
        AuthenticationConfig srAuthConfig = new AuthenticationConfig("sr-user", "sr-pass");
        cluster.setSchemaRegistryAuthConfig(srAuthConfig);
        
        // Verify Schema Registry authentication requirements
        assertTrue(cluster.requiresSchemaRegistryAuthentication());
        assertEquals(AuthenticationType.SASL_PLAIN, cluster.getSchemaRegistryAuthType());
        assertEquals("sr-user", cluster.getSchemaRegistryAuthConfig().getUsername());
        assertEquals("sr-pass", cluster.getSchemaRegistryAuthConfig().getPassword());
        
        System.out.println("✓ Schema Registry authentication configuration works correctly");
    }
    
    @Test
    public void testComprehensiveKafkaAuthentication() {
        System.out.println("=== Testing Comprehensive Kafka Authentication Scenarios ===");
        
        // Test 1: SASL PLAIN
        testAuthenticationScenario("SASL PLAIN", AuthenticationType.SASL_PLAIN, 
            new AuthenticationConfig("user1", "pass1"));
        
        // Test 2: SASL SSL without truststore
        testAuthenticationScenario("SASL SSL (no truststore)", AuthenticationType.SASL_SSL, 
            new AuthenticationConfig("user2", "pass2"));
        
        // Test 3: SASL SSL with truststore
        AuthenticationConfig sslWithTruststore = new AuthenticationConfig("user3", "pass3");
        sslWithTruststore.setTruststoreLocation("/path/to/truststore.jks");
        sslWithTruststore.setTruststorePassword("tspass");
        testAuthenticationScenario("SASL SSL (with truststore)", AuthenticationType.SASL_SSL, sslWithTruststore);
        
        // Test 4: SSL only
        AuthenticationConfig sslOnly = new AuthenticationConfig();
        sslOnly.setTruststoreLocation("/path/to/truststore.jks");
        testAuthenticationScenario("SSL Only", AuthenticationType.SSL, sslOnly);
        
        System.out.println("✓ All Kafka authentication scenarios work correctly");
    }
    
    private void testAuthenticationScenario(String scenarioName, AuthenticationType authType, AuthenticationConfig authConfig) {
        System.out.println("  Testing scenario: " + scenarioName);
        
        // Validate the configuration
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(authType, authConfig);
        
        assertTrue(result.isValid(), "Validation failed for " + scenarioName + ": " + result.getErrorMessage());
        
        // Create cluster and configure properties
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092", authType, authConfig);
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        
        // This should not throw any exceptions
        assertDoesNotThrow(() -> KafkaAuthenticationUtil.configureAuthentication(props, cluster));
        
        // Verify security protocol is set appropriately
        assertNotNull(props.get("security.protocol"), "Security protocol should be set for " + scenarioName);
        
        System.out.println("    ✓ " + scenarioName + " configuration successful");
    }
}