package com.kafkatool.demo;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.util.KafkaAuthenticationUtil;

/**
 * Demonstration of enhanced validation functionality
 */
public class ValidationDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Kafka UI Tool - Enhanced Validation Demo ===\n");
        
        // Test 1: SSL Configuration with optional keystore
        System.out.println("Test 1: SSL Configuration with optional keystore fields");
        AuthenticationConfig sslConfig = new AuthenticationConfig();
        sslConfig.setTruststoreLocation("/path/to/truststore.jks");
        sslConfig.setTruststorePassword("trustpass");
        // Keystore is optional for SSL
        
        KafkaAuthenticationUtil.ValidationResult result1 = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SSL, sslConfig);
        
        System.out.println("   SSL with truststore only: " + (result1.isValid() ? "✅ VALID" : "❌ INVALID"));
        if (!result1.isValid()) {
            System.out.println("   Error: " + result1.getErrorMessage());
        }
        
        // Test 2: SSL Configuration with missing truststore (should fail)
        System.out.println("\nTest 2: SSL Configuration with missing truststore");
        AuthenticationConfig badSslConfig = new AuthenticationConfig();
        badSslConfig.setKeystoreLocation("/path/to/keystore.jks");
        badSslConfig.setKeystorePassword("keypass");
        // Missing truststore - this should fail
        
        KafkaAuthenticationUtil.ValidationResult result2 = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SSL, badSslConfig);
        
        System.out.println("   SSL without truststore: " + (result2.isValid() ? "✅ VALID" : "❌ INVALID"));
        if (!result2.isValid()) {
            System.out.println("   Error: " + result2.getErrorMessage());
        }
        
        // Test 3: SSL Configuration with both keystore and truststore (should pass)
        System.out.println("\nTest 3: SSL Configuration with both keystore and truststore");
        AuthenticationConfig fullSslConfig = new AuthenticationConfig();
        fullSslConfig.setTruststoreLocation("/path/to/truststore.jks");
        fullSslConfig.setTruststorePassword("trustpass");
        fullSslConfig.setKeystoreLocation("/path/to/keystore.jks");
        fullSslConfig.setKeystorePassword("keypass");
        fullSslConfig.setKeyPassword("clientkeypass");
        
        KafkaAuthenticationUtil.ValidationResult result3 = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SSL, fullSslConfig);
        
        System.out.println("   SSL with both stores: " + (result3.isValid() ? "✅ VALID" : "❌ INVALID"));
        if (!result3.isValid()) {
            System.out.println("   Error: " + result3.getErrorMessage());
        }
        
        // Test 4: SASL Plain with missing password
        System.out.println("\nTest 4: SASL Plain with missing password");
        AuthenticationConfig saslConfig = new AuthenticationConfig();
        saslConfig.setUsername("testuser");
        // Missing password
        
        KafkaAuthenticationUtil.ValidationResult result4 = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SASL_PLAIN, saslConfig);
        
        System.out.println("   SASL Plain missing password: " + (result4.isValid() ? "✅ VALID" : "❌ INVALID"));
        if (!result4.isValid()) {
            System.out.println("   Error: " + result4.getErrorMessage());
        }
        
        // Test 5: Valid SASL Plain configuration
        System.out.println("\nTest 5: Valid SASL Plain configuration");
        AuthenticationConfig validSaslConfig = new AuthenticationConfig();
        validSaslConfig.setUsername("testuser");
        validSaslConfig.setPassword("testpass");
        
        KafkaAuthenticationUtil.ValidationResult result5 = 
            KafkaAuthenticationUtil.validateAuthenticationConfigDetailed(AuthenticationType.SASL_PLAIN, validSaslConfig);
        
        System.out.println("   Valid SASL Plain: " + (result5.isValid() ? "✅ VALID" : "❌ INVALID"));
        if (!result5.isValid()) {
            System.out.println("   Error: " + result5.getErrorMessage());
        }
        
        System.out.println("\n=== Key Improvements ===");
        System.out.println("✓ Enhanced validation with specific error messages");
        System.out.println("✓ Proper handling of optional SSL fields (keystore is optional, truststore required)");
        System.out.println("✓ Clear distinction between required and optional authentication parameters");
        System.out.println("✓ Backward compatibility maintained with existing validation");
        System.out.println("\n=== Timestamp Search Features ===");
        System.out.println("✓ searchMessagesByTimestampAsync - search within time range");
        System.out.println("✓ searchMessagesByPatternAndTimestampAsync - combine pattern + timestamp filtering");
        System.out.println("✓ Enhanced UI with date/time picker for timestamp selection");
        System.out.println("✓ Efficient implementation using Kafka's offsetsForTimes API");
    }
}