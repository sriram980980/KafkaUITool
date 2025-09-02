package com.kafkatool.util;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.ClusterInfo;
import com.kafkatool.util.security.DataMaskingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;

/**
 * Utility for secure storage and retrieval of authentication credentials
 */
public class AuthenticationSecurityUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationSecurityUtil.class);
    
    // Store encryption key - in a real application, this should be loaded from a secure location
    private static SecretKey encryptionKey;
    
    static {
        try {
            // For demo purposes, generate a key. In production, load from secure storage
            encryptionKey = DataMaskingUtil.generateEncryptionKey();
        } catch (Exception e) {
            logger.error("Failed to initialize encryption key", e);
        }
    }
    
    /**
     * Encrypt sensitive fields in authentication configuration
     */
    public static AuthenticationConfig encryptCredentials(AuthenticationConfig config) {
        if (config == null || !config.hasSecureCredentials()) {
            return config;
        }
        
        try {
            AuthenticationConfig encrypted = new AuthenticationConfig();
            
            // Copy non-sensitive fields as-is
            encrypted.setUsername(config.getUsername());
            encrypted.setKeystoreLocation(config.getKeystoreLocation());
            encrypted.setTruststoreLocation(config.getTruststoreLocation());
            encrypted.setKerberosServiceName(config.getKerberosServiceName());
            encrypted.setKerberosRealm(config.getKerberosRealm());
            encrypted.setKerberosKeytab(config.getKerberosKeytab());
            encrypted.setKerberosPrincipal(config.getKerberosPrincipal());
            encrypted.setAdditionalProperties(config.getAdditionalProperties());
            
            // Encrypt sensitive fields
            if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                encrypted.setPassword(DataMaskingUtil.encrypt(config.getPassword(), encryptionKey));
            }
            if (config.getKeystorePassword() != null && !config.getKeystorePassword().isEmpty()) {
                encrypted.setKeystorePassword(DataMaskingUtil.encrypt(config.getKeystorePassword(), encryptionKey));
            }
            if (config.getKeyPassword() != null && !config.getKeyPassword().isEmpty()) {
                encrypted.setKeyPassword(DataMaskingUtil.encrypt(config.getKeyPassword(), encryptionKey));
            }
            if (config.getTruststorePassword() != null && !config.getTruststorePassword().isEmpty()) {
                encrypted.setTruststorePassword(DataMaskingUtil.encrypt(config.getTruststorePassword(), encryptionKey));
            }
            
            return encrypted;
        } catch (Exception e) {
            logger.error("Failed to encrypt authentication credentials", e);
            return config; // Return original if encryption fails
        }
    }
    
    /**
     * Decrypt sensitive fields in authentication configuration
     */
    public static AuthenticationConfig decryptCredentials(AuthenticationConfig config) {
        if (config == null) {
            return null;
        }
        
        try {
            AuthenticationConfig decrypted = new AuthenticationConfig();
            
            // Copy non-sensitive fields as-is
            decrypted.setUsername(config.getUsername());
            decrypted.setKeystoreLocation(config.getKeystoreLocation());
            decrypted.setTruststoreLocation(config.getTruststoreLocation());
            decrypted.setKerberosServiceName(config.getKerberosServiceName());
            decrypted.setKerberosRealm(config.getKerberosRealm());
            decrypted.setKerberosKeytab(config.getKerberosKeytab());
            decrypted.setKerberosPrincipal(config.getKerberosPrincipal());
            decrypted.setAdditionalProperties(config.getAdditionalProperties());
            
            // Decrypt sensitive fields
            if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                decrypted.setPassword(DataMaskingUtil.decrypt(config.getPassword(), encryptionKey));
            }
            if (config.getKeystorePassword() != null && !config.getKeystorePassword().isEmpty()) {
                decrypted.setKeystorePassword(DataMaskingUtil.decrypt(config.getKeystorePassword(), encryptionKey));
            }
            if (config.getKeyPassword() != null && !config.getKeyPassword().isEmpty()) {
                decrypted.setKeyPassword(DataMaskingUtil.decrypt(config.getKeyPassword(), encryptionKey));
            }
            if (config.getTruststorePassword() != null && !config.getTruststorePassword().isEmpty()) {
                decrypted.setTruststorePassword(DataMaskingUtil.decrypt(config.getTruststorePassword(), encryptionKey));
            }
            
            return decrypted;
        } catch (Exception e) {
            logger.error("Failed to decrypt authentication credentials", e);
            return config; // Return original if decryption fails
        }
    }
    
    /**
     * Encrypt cluster authentication credentials for storage
     */
    public static ClusterInfo encryptClusterCredentials(ClusterInfo cluster) {
        if (cluster == null) {
            return cluster;
        }
        
        // Use the full constructor to handle Schema Registry fields
        ClusterInfo encrypted = new ClusterInfo(
            cluster.getName(),
            cluster.getBrokerUrls(),
            cluster.isConnectByDefault(),
            cluster.getAuthenticationType(),
            cluster.requiresAuthentication() ? encryptCredentials(cluster.getAuthenticationConfig()) : cluster.getAuthenticationConfig(),
            cluster.isSchemaRegistryEnabled(),
            cluster.getSchemaRegistryUrl(),
            cluster.getSchemaRegistryAuthType(),
            cluster.requiresSchemaRegistryAuthentication() ? encryptCredentials(cluster.getSchemaRegistryAuthConfig()) : cluster.getSchemaRegistryAuthConfig()
        );
        
        encrypted.setStatus(cluster.getStatus());
        encrypted.setKafkaVersion(cluster.getKafkaVersion());
        
        return encrypted;
    }
    
    /**
     * Decrypt cluster authentication credentials for use
     */
    public static ClusterInfo decryptClusterCredentials(ClusterInfo cluster) {
        if (cluster == null) {
            return cluster;
        }
        
        // Use the full constructor to handle Schema Registry fields
        ClusterInfo decrypted = new ClusterInfo(
            cluster.getName(),
            cluster.getBrokerUrls(),
            cluster.isConnectByDefault(),
            cluster.getAuthenticationType(),
            cluster.requiresAuthentication() ? decryptCredentials(cluster.getAuthenticationConfig()) : cluster.getAuthenticationConfig(),
            cluster.isSchemaRegistryEnabled(),
            cluster.getSchemaRegistryUrl(),
            cluster.getSchemaRegistryAuthType(),
            cluster.requiresSchemaRegistryAuthentication() ? decryptCredentials(cluster.getSchemaRegistryAuthConfig()) : cluster.getSchemaRegistryAuthConfig()
        );
        
        decrypted.setStatus(cluster.getStatus());
        decrypted.setKafkaVersion(cluster.getKafkaVersion());
        
        return decrypted;
    }
    
    /**
     * Check if a value appears to be encrypted
     */
    public static boolean isEncrypted(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        // Simple check - encrypted values typically are longer and contain base64 characters
        return value.length() > 20 && value.matches("^[A-Za-z0-9+/=]+$");
    }
    
    /**
     * Get the encryption key as string for storage/configuration
     */
    public static String getEncryptionKeyAsString() {
        return DataMaskingUtil.keyToString(encryptionKey);
    }
    
    /**
     * Set encryption key from string (for loading from configuration)
     */
    public static void setEncryptionKeyFromString(String keyString) {
        try {
            encryptionKey = DataMaskingUtil.createKeyFromString(keyString);
        } catch (Exception e) {
            logger.error("Failed to set encryption key from string", e);
        }
    }
}