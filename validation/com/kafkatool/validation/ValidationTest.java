package com.kafkatool.validation;

import com.kafkatool.model.ClusterInfo;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.model.AuthenticationConfig;

/**
 * Quick validation test for Schema Registry functionality
 */
public class ValidationTest {
    
    public static void main(String[] args) {
        System.out.println("=== Schema Registry Validation Test ===\n");
        
        boolean allTestsPassed = true;
        
        // Test 1: Default values
        allTestsPassed &= testDefaultValues();
        
        // Test 2: Setting values
        allTestsPassed &= testSettingValues();
        
        // Test 3: Authentication requirements
        allTestsPassed &= testAuthenticationRequirements();
        
        // Test 4: Masked copy functionality
        allTestsPassed &= testMaskedCopy();
        
        // Test 5: Constructor variations
        allTestsPassed &= testConstructors();
        
        System.out.println("\n=== VALIDATION RESULT ===");
        if (allTestsPassed) {
            System.out.println("✅ ALL TESTS PASSED - Implementation is robust and ready!");
        } else {
            System.out.println("❌ Some tests failed - Implementation needs review");
        }
    }
    
    private static boolean testDefaultValues() {
        System.out.println("Test 1: Default Values");
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092");
        
        boolean passed = !cluster.isSchemaRegistryEnabled() &&
                        cluster.getSchemaRegistryUrl() == null &&
                        cluster.getSchemaRegistryAuthType() == AuthenticationType.NONE &&
                        cluster.getSchemaRegistryAuthConfig() == null &&
                        !cluster.requiresSchemaRegistryAuthentication();
        
        System.out.println("   Result: " + (passed ? "✅ PASS" : "❌ FAIL"));
        return passed;
    }
    
    private static boolean testSettingValues() {
        System.out.println("Test 2: Setting Values");
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092");
        
        cluster.setSchemaRegistryEnabled(true);
        cluster.setSchemaRegistryUrl("http://localhost:8081");
        cluster.setSchemaRegistryAuthType(AuthenticationType.SASL_PLAIN);
        AuthenticationConfig config = new AuthenticationConfig("user", "pass");
        cluster.setSchemaRegistryAuthConfig(config);
        
        boolean passed = cluster.isSchemaRegistryEnabled() &&
                        "http://localhost:8081".equals(cluster.getSchemaRegistryUrl()) &&
                        cluster.getSchemaRegistryAuthType() == AuthenticationType.SASL_PLAIN &&
                        cluster.getSchemaRegistryAuthConfig() == config &&
                        cluster.requiresSchemaRegistryAuthentication();
        
        System.out.println("   Result: " + (passed ? "✅ PASS" : "❌ FAIL"));
        return passed;
    }
    
    private static boolean testAuthenticationRequirements() {
        System.out.println("Test 3: Authentication Requirements");
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092");
        
        // SR disabled - no auth required
        boolean test1 = !cluster.requiresSchemaRegistryAuthentication();
        
        // SR enabled but no auth type - no auth required
        cluster.setSchemaRegistryEnabled(true);
        cluster.setSchemaRegistryUrl("http://localhost:8081");
        boolean test2 = !cluster.requiresSchemaRegistryAuthentication();
        
        // SR enabled with auth type - auth required
        cluster.setSchemaRegistryAuthType(AuthenticationType.SASL_PLAIN);
        boolean test3 = cluster.requiresSchemaRegistryAuthentication();
        
        boolean passed = test1 && test2 && test3;
        System.out.println("   Result: " + (passed ? "✅ PASS" : "❌ FAIL"));
        return passed;
    }
    
    private static boolean testMaskedCopy() {
        System.out.println("Test 4: Masked Copy");
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092");
        cluster.setSchemaRegistryEnabled(true);
        cluster.setSchemaRegistryUrl("http://localhost:8081");
        cluster.setSchemaRegistryAuthType(AuthenticationType.SASL_PLAIN);
        
        AuthenticationConfig config = new AuthenticationConfig("user", "secret-password");
        cluster.setSchemaRegistryAuthConfig(config);
        
        ClusterInfo masked = cluster.createMaskedCopy();
        
        boolean passed = masked.isSchemaRegistryEnabled() &&
                        "http://localhost:8081".equals(masked.getSchemaRegistryUrl()) &&
                        masked.getSchemaRegistryAuthType() == AuthenticationType.SASL_PLAIN &&
                        masked.getSchemaRegistryAuthConfig() != null &&
                        "****".equals(masked.getSchemaRegistryAuthConfig().getPassword());
        
        System.out.println("   Result: " + (passed ? "✅ PASS" : "❌ FAIL"));
        return passed;
    }
    
    private static boolean testConstructors() {
        System.out.println("Test 5: Constructor Variations");
        
        // Test full constructor
        AuthenticationConfig kafkaAuth = new AuthenticationConfig("kafka-user", "kafka-pass");
        AuthenticationConfig schemaAuth = new AuthenticationConfig("schema-user", "schema-pass");
        
        ClusterInfo cluster = new ClusterInfo(
            "full-cluster", "localhost:9092", true,
            AuthenticationType.SASL_PLAIN, kafkaAuth,
            true, "http://localhost:8081",
            AuthenticationType.SASL_SCRAM_SHA_256, schemaAuth
        );
        
        boolean passed = "full-cluster".equals(cluster.getName()) &&
                        "localhost:9092".equals(cluster.getBrokerUrls()) &&
                        cluster.isConnectByDefault() &&
                        cluster.getAuthenticationType() == AuthenticationType.SASL_PLAIN &&
                        cluster.getAuthenticationConfig() == kafkaAuth &&
                        cluster.isSchemaRegistryEnabled() &&
                        "http://localhost:8081".equals(cluster.getSchemaRegistryUrl()) &&
                        cluster.getSchemaRegistryAuthType() == AuthenticationType.SASL_SCRAM_SHA_256 &&
                        cluster.getSchemaRegistryAuthConfig() == schemaAuth;
        
        System.out.println("   Result: " + (passed ? "✅ PASS" : "❌ FAIL"));
        return passed;
    }
}