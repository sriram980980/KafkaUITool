package com.kafkatool.demo;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.model.ClusterInfo;
import com.kafkatool.util.KafkaAuthenticationUtil;

import java.util.Properties;

/**
 * Demo class showing how to use the new authentication features
 */
public class AuthenticationDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Kafka UI Tool Authentication Demo ===\n");
        
        // Demo 1: Backward compatibility - no authentication
        demonstrateNoAuthentication();
        
        // Demo 2: SASL Plain authentication  
        demonstrateSaslPlain();
        
        // Demo 3: SSL authentication
        demonstrateSsl();
        
        // Demo 4: SASL over SSL authentication
        demonstrateSaslSsl();
        
        // Demo 5: Credential masking
        demonstrateCredentialMasking();
        
        // Demo 6: Validation
        demonstrateValidation();
    }
    
    private static void demonstrateNoAuthentication() {
        System.out.println("1. No Authentication (Backward Compatible):");
        
        // Old way still works
        ClusterInfo cluster = new ClusterInfo("Local Kafka", "localhost:9092");
        
        System.out.println("   Cluster: " + cluster.getName());
        System.out.println("   Brokers: " + cluster.getBrokerUrls());
        System.out.println("   Auth Type: " + cluster.getAuthenticationType());
        System.out.println("   Requires Auth: " + cluster.requiresAuthentication());
        System.out.println();
    }
    
    private static void demonstrateSaslPlain() {
        System.out.println("2. SASL Plain Authentication:");
        
        AuthenticationConfig authConfig = new AuthenticationConfig("kafkauser", "kafkapass");
        ClusterInfo cluster = new ClusterInfo("Production Kafka", "prod-kafka:9092", 
            AuthenticationType.SASL_PLAIN, authConfig);
        
        System.out.println("   Cluster: " + cluster.getName());
        System.out.println("   Auth Type: " + cluster.getAuthenticationType().getDisplayName());
        System.out.println("   Username: " + authConfig.getUsername());
        System.out.println("   Password: [PROTECTED]");
        
        // Show how it configures Kafka properties
        Properties props = new Properties();
        props.put("bootstrap.servers", cluster.getBrokerUrls());
        KafkaAuthenticationUtil.configureAuthentication(props, cluster);
        
        System.out.println("   Generated Properties:");
        System.out.println("     security.protocol = " + props.get("security.protocol"));
        System.out.println("     sasl.mechanism = " + props.get("sasl.mechanism"));
        System.out.println("     sasl.jaas.config = [CONFIGURED]");
        System.out.println();
    }
    
    private static void demonstrateSsl() {
        System.out.println("3. SSL Authentication:");
        
        AuthenticationConfig authConfig = new AuthenticationConfig();
        authConfig.setTruststoreLocation("/etc/kafka/ssl/kafka.client.truststore.jks");
        authConfig.setTruststorePassword("truststorepass");
        authConfig.setKeystoreLocation("/etc/kafka/ssl/kafka.client.keystore.jks");
        authConfig.setKeystorePassword("keystorepass");
        
        ClusterInfo cluster = new ClusterInfo("Secure Kafka", "secure-kafka:9093", 
            AuthenticationType.SSL, authConfig);
        
        System.out.println("   Cluster: " + cluster.getName());
        System.out.println("   Auth Type: " + cluster.getAuthenticationType().getDisplayName());
        System.out.println("   Truststore: " + authConfig.getTruststoreLocation());
        System.out.println("   Keystore: " + authConfig.getKeystoreLocation());
        System.out.println("   Passwords: [PROTECTED]");
        System.out.println();
    }
    
    private static void demonstrateSaslSsl() {
        System.out.println("4. SASL over SSL Authentication:");
        
        AuthenticationConfig authConfig = new AuthenticationConfig("secureuser", "securepass");
        authConfig.setTruststoreLocation("/etc/kafka/ssl/kafka.client.truststore.jks");
        authConfig.setTruststorePassword("truststorepass");
        
        ClusterInfo cluster = new ClusterInfo("Enterprise Kafka", "enterprise-kafka:9094", 
            AuthenticationType.SASL_SSL, authConfig);
        
        System.out.println("   Cluster: " + cluster.getName());
        System.out.println("   Auth Type: " + cluster.getAuthenticationType().getDisplayName());
        System.out.println("   Username: " + authConfig.getUsername());
        System.out.println("   Truststore: " + authConfig.getTruststoreLocation());
        System.out.println("   All Passwords: [PROTECTED]");
        System.out.println();
    }
    
    private static void demonstrateCredentialMasking() {
        System.out.println("5. Credential Masking for UI Display:");
        
        AuthenticationConfig authConfig = new AuthenticationConfig("admin", "supersecretpassword");
        authConfig.setKeystorePassword("anothersecret");
        
        ClusterInfo cluster = new ClusterInfo("Demo Cluster", "demo:9092", 
            AuthenticationType.SASL_PLAIN, authConfig);
        
        System.out.println("   Original:");
        System.out.println("     Password: " + authConfig.getPassword());
        System.out.println("     Keystore Password: " + authConfig.getKeystorePassword());
        
        ClusterInfo maskedCluster = cluster.createMaskedCopy();
        AuthenticationConfig maskedConfig = maskedCluster.getAuthenticationConfig();
        
        System.out.println("   Masked for UI:");
        System.out.println("     Password: " + maskedConfig.getPassword());
        System.out.println("     Keystore Password: " + maskedConfig.getKeystorePassword());
        System.out.println();
    }
    
    private static void demonstrateValidation() {
        System.out.println("6. Authentication Validation:");
        
        // Valid SASL configuration
        AuthenticationConfig validSasl = new AuthenticationConfig("user", "pass");
        boolean valid = KafkaAuthenticationUtil.validateAuthenticationConfig(
            AuthenticationType.SASL_PLAIN, validSasl);
        System.out.println("   Valid SASL Config: " + valid);
        
        // Invalid SASL configuration (missing password)
        AuthenticationConfig invalidSasl = new AuthenticationConfig("user", "");
        boolean invalid = KafkaAuthenticationUtil.validateAuthenticationConfig(
            AuthenticationType.SASL_PLAIN, invalidSasl);
        System.out.println("   Invalid SASL Config: " + invalid);
        
        // Valid SSL configuration
        AuthenticationConfig validSsl = new AuthenticationConfig();
        validSsl.setTruststoreLocation("/path/to/truststore.jks");
        boolean validSslConfig = KafkaAuthenticationUtil.validateAuthenticationConfig(
            AuthenticationType.SSL, validSsl);
        System.out.println("   Valid SSL Config: " + validSslConfig);
        
        System.out.println();
        System.out.println("=== Demo Complete ===");
        System.out.println("The Kafka UI Tool now supports comprehensive authentication!");
    }
}