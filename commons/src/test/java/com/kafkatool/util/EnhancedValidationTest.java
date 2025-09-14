package com.kafkatool.util;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.AuthenticationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for enhanced validation functionality with detailed error messages
 */
public class EnhancedValidationTest {
    
    @Test
    public void testValidSSLConfiguration() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setTruststoreLocation("/path/to/truststore.jks");
        config.setTruststorePassword("password");
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SSL, config);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    public void testSSLConfigurationWithoutTruststore() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setKeystoreLocation("/path/to/keystore.jks");
        config.setKeystorePassword("password");
        // Missing truststore - this should fail
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SSL, config);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Truststore location is required"));
    }
    
    @Test
    public void testSSLConfigurationWithOptionalKeystore() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setTruststoreLocation("/path/to/truststore.jks");
        config.setTruststorePassword("password");
        // Keystore fields are optional for SSL
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SSL, config);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    public void testSSLConfigurationWithOptionalKeystoreFields() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setTruststoreLocation("/path/to/truststore.jks");
        config.setTruststorePassword("password");
        // Add optional keystore fields
        config.setKeystoreLocation("/path/to/keystore.jks");
        config.setKeystorePassword("keystorepass");
        config.setKeyPassword("keypass");
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SSL, config);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    public void testSASLPlainValidConfiguration() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setUsername("testuser");
        config.setPassword("testpass");
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SASL_PLAIN, config);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    public void testSASLPlainMissingUsername() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setPassword("testpass");
        // Missing username
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SASL_PLAIN, config);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Username is required"));
    }
    
    @Test
    public void testSASLPlainMissingPassword() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setUsername("testuser");
        // Missing password
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SASL_PLAIN, config);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Password is required"));
    }
    
    @Test
    public void testSASLSSLValidConfiguration() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setUsername("testuser");
        config.setPassword("testpass");
        config.setTruststoreLocation("/path/to/truststore.jks");
        config.setTruststorePassword("truststorepass");
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SASL_SSL, config);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    public void testSASLSSLMissingTruststore() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setUsername("testuser");
        config.setPassword("testpass");
        // Missing truststore - should be valid now (truststore is optional for SASL_SSL)
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SASL_SSL, config);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    public void testSASLSSLMissingUsername() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setPassword("testpass");
        config.setTruststoreLocation("/path/to/truststore.jks");
        // Missing username - should fail
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SASL_SSL, config);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Username is required"));
    }
    
    @Test
    public void testSASLSSLMissingPassword() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setUsername("testuser");
        config.setTruststoreLocation("/path/to/truststore.jks");
        // Missing password - should fail
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SASL_SSL, config);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Password is required"));
    }
    
    @Test
    public void testKerberosValidConfiguration() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setKerberosServiceName("kafka");
        config.setKerberosRealm("EXAMPLE.COM");
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.KERBEROS, config);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    public void testKerberosMissingServiceName() {
        AuthenticationConfig config = new AuthenticationConfig();
        config.setKerberosRealm("EXAMPLE.COM");
        // Missing service name
        
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.KERBEROS, config);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Kerberos service name is required"));
    }
    
    @Test
    public void testNoAuthenticationValid() {
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.NONE, null);
        
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    public void testNullConfigurationForRequiredAuth() {
        KafkaAuthenticationUtil.ValidationResult result = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SASL_PLAIN, null);
        
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Authentication configuration is required"));
    }
}