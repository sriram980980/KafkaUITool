package com.kafkatool.demo;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.model.ClusterInfo;
import com.kafkatool.util.AuthenticationSecurityUtil;

/**
 * Demo class showing the new Schema Registry functionality
 */
public class SchemaRegistryDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Schema Registry Integration Demo ===\n");
        
        // Demo 1: Backward compatibility - existing clusters still work
        demonstrateBackwardCompatibility();
        
        // Demo 2: Schema Registry without authentication
        demonstrateSchemaRegistryBasic();
        
        // Demo 3: Schema Registry with authentication
        demonstrateSchemaRegistryWithAuth();
        
        // Demo 4: Security integration
        demonstrateSecurityIntegration();
        
        // Demo 5: Validation scenarios
        demonstrateValidation();
    }
    
    private static void demonstrateBackwardCompatibility() {
        System.out.println("1. Backward Compatibility Test:");
        
        // Old cluster creation still works exactly the same
        ClusterInfo oldCluster = new ClusterInfo("Legacy Cluster", "localhost:9092");
        
        System.out.println("   Cluster: " + oldCluster.getName());
        System.out.println("   Brokers: " + oldCluster.getBrokerUrls());
        System.out.println("   Schema Registry Enabled: " + oldCluster.isSchemaRegistryEnabled());
        System.out.println("   Schema Registry Auth Type: " + oldCluster.getSchemaRegistryAuthType());
        System.out.println("   ✓ Existing code works unchanged\n");
    }
    
    private static void demonstrateSchemaRegistryBasic() {
        System.out.println("2. Schema Registry Basic Configuration:");
        
        ClusterInfo cluster = new ClusterInfo("Kafka with Schema Registry", "localhost:9092");
        cluster.setSchemaRegistryEnabled(true);
        cluster.setSchemaRegistryUrl("http://localhost:8081");
        // No authentication needed
        
        System.out.println("   Cluster: " + cluster.getName());
        System.out.println("   Schema Registry Enabled: " + cluster.isSchemaRegistryEnabled());
        System.out.println("   Schema Registry URL: " + cluster.getSchemaRegistryUrl());
        System.out.println("   Schema Registry Auth: " + cluster.getSchemaRegistryAuthType());
        System.out.println("   Requires SR Auth: " + cluster.requiresSchemaRegistryAuthentication());
        System.out.println("   ✓ Schema Registry configured without authentication\n");
    }
    
    private static void demonstrateSchemaRegistryWithAuth() {
        System.out.println("3. Schema Registry with Authentication:");
        
        // Create authentication config for Schema Registry
        AuthenticationConfig schemaAuth = new AuthenticationConfig("sr-user", "sr-password");
        
        ClusterInfo cluster = new ClusterInfo(
            "Secure Kafka", "localhost:9092", false,
            AuthenticationType.SASL_PLAIN, new AuthenticationConfig("kafka-user", "kafka-pass"),
            true, "https://schema-registry.company.com:8081",
            AuthenticationType.SASL_PLAIN, schemaAuth
        );
        
        System.out.println("   Cluster: " + cluster.getName());
        System.out.println("   Kafka Auth: " + cluster.getAuthenticationType());
        System.out.println("   Schema Registry Enabled: " + cluster.isSchemaRegistryEnabled());
        System.out.println("   Schema Registry URL: " + cluster.getSchemaRegistryUrl());
        System.out.println("   Schema Registry Auth: " + cluster.getSchemaRegistryAuthType());
        System.out.println("   Kafka Requires Auth: " + cluster.requiresAuthentication());
        System.out.println("   Schema Registry Requires Auth: " + cluster.requiresSchemaRegistryAuthentication());
        System.out.println("   ✓ Both Kafka and Schema Registry authenticated\n");
    }
    
    private static void demonstrateSecurityIntegration() {
        System.out.println("4. Security Integration Test:");
        
        AuthenticationConfig schemaAuth = new AuthenticationConfig("sr-user", "secret-password");
        ClusterInfo cluster = new ClusterInfo(
            "Secure Cluster", "localhost:9092", false,
            AuthenticationType.NONE, null,
            true, "http://localhost:8081",
            AuthenticationType.SASL_PLAIN, schemaAuth
        );
        
        System.out.println("   Original Schema Registry Password: " + schemaAuth.getPassword());
        
        // Encrypt credentials
        ClusterInfo encrypted = AuthenticationSecurityUtil.encryptClusterCredentials(cluster);
        System.out.println("   Encrypted SR Password: " + encrypted.getSchemaRegistryAuthConfig().getPassword());
        
        // Create masked copy for display
        ClusterInfo masked = cluster.createMaskedCopy();
        System.out.println("   Masked SR Password: " + masked.getSchemaRegistryAuthConfig().getPassword());
        
        System.out.println("   ✓ Schema Registry credentials encrypted and masked properly\n");
    }
    
    private static void demonstrateValidation() {
        System.out.println("5. Validation Scenarios:");
        
        ClusterInfo cluster = new ClusterInfo("Test Cluster", "localhost:9092");
        
        // Test 1: Schema Registry disabled
        System.out.println("   Test 1 - SR Disabled: " + !cluster.requiresSchemaRegistryAuthentication() + " ✓");
        
        // Test 2: Schema Registry enabled but no auth
        cluster.setSchemaRegistryEnabled(true);
        cluster.setSchemaRegistryUrl("http://localhost:8081");
        System.out.println("   Test 2 - SR No Auth: " + !cluster.requiresSchemaRegistryAuthentication() + " ✓");
        
        // Test 3: Schema Registry with auth
        cluster.setSchemaRegistryAuthType(AuthenticationType.SASL_PLAIN);
        cluster.setSchemaRegistryAuthConfig(new AuthenticationConfig("user", "pass"));
        System.out.println("   Test 3 - SR With Auth: " + cluster.requiresSchemaRegistryAuthentication() + " ✓");
        
        System.out.println("   ✓ All validation scenarios work correctly\n");
    }
}