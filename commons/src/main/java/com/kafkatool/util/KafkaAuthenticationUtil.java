package com.kafkatool.util;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.model.ClusterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Utility class for configuring Kafka client authentication properties
 */
public class KafkaAuthenticationUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaAuthenticationUtil.class);
    
    /**
     * Configure Kafka client properties with authentication settings
     */
    public static void configureAuthentication(Properties props, ClusterInfo clusterInfo) {
        if (clusterInfo == null || !clusterInfo.requiresAuthentication()) {
            return;
        }
        
        AuthenticationType authType = clusterInfo.getAuthenticationType();
        AuthenticationConfig authConfig = clusterInfo.getAuthenticationConfig();
        
        if (authConfig == null) {
            logger.warn("Authentication type {} specified but no configuration provided", authType);
            return;
        }
        
        // Decrypt credentials before use if security util is available
        try {
            authConfig = AuthenticationSecurityUtil.decryptCredentials(authConfig);
        } catch (Exception e) {
            logger.debug("Security util not available, using credentials as-is: {}", e.getMessage());
        }
        
        switch (authType) {
            case SASL_PLAIN:
                configureSaslPlain(props, authConfig);
                break;
            case SASL_SCRAM_SHA_256:
                configureSaslScram(props, authConfig, "SCRAM-SHA-256");
                break;
            case SASL_SCRAM_SHA_512:
                configureSaslScram(props, authConfig, "SCRAM-SHA-512");
                break;
            case SSL:
                configureSsl(props, authConfig);
                break;
            case SASL_SSL:
                configureSaslSsl(props, authConfig);
                break;
            case KERBEROS:
                configureKerberos(props, authConfig);
                break;
            default:
                // No authentication needed
                break;
        }
    }
    
    private static void configureSaslPlain(Properties props, AuthenticationConfig config) {
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "PLAIN");
        
        String jaasConfig = String.format(
            "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"%s\" password=\"%s\";",
            config.getUsername(),
            config.getPassword()
        );
        props.put("sasl.jaas.config", jaasConfig);
        
        logger.debug("Configured SASL Plain authentication for user: {}", config.getUsername());
    }
    
    private static void configureSaslScram(Properties props, AuthenticationConfig config, String mechanism) {
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", mechanism);
        
        String jaasConfig = String.format(
            "org.apache.kafka.common.security.scram.ScramLoginModule required " +
            "username=\"%s\" password=\"%s\";",
            config.getUsername(),
            config.getPassword()
        );
        props.put("sasl.jaas.config", jaasConfig);
        
        logger.debug("Configured {} authentication for user: {}", mechanism, config.getUsername());
    }
    
    private static void configureSsl(Properties props, AuthenticationConfig config) {
        props.put("security.protocol", "SSL");
        
        if (config.getKeystoreLocation() != null && !config.getKeystoreLocation().isEmpty()) {
            props.put("ssl.keystore.location", config.getKeystoreLocation());
        }
        if (config.getKeystorePassword() != null && !config.getKeystorePassword().isEmpty()) {
            props.put("ssl.keystore.password", config.getKeystorePassword());
        }
        if (config.getKeyPassword() != null && !config.getKeyPassword().isEmpty()) {
            props.put("ssl.key.password", config.getKeyPassword());
        }
        if (config.getTruststoreLocation() != null && !config.getTruststoreLocation().isEmpty()) {
            props.put("ssl.truststore.location", config.getTruststoreLocation());
        }
        if (config.getTruststorePassword() != null && !config.getTruststorePassword().isEmpty()) {
            props.put("ssl.truststore.password", config.getTruststorePassword());
        }
        
        logger.debug("Configured SSL authentication");
    }
    
    private static void configureSaslSsl(Properties props, AuthenticationConfig config) {
        // Set security protocol for SASL over SSL
        props.put("security.protocol", "SASL_SSL");
        
        // Configure SASL mechanism based on username/password presence
        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            props.put("sasl.mechanism", "PLAIN");
            String jaasConfig = String.format(
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                "username=\"%s\" password=\"%s\";",
                config.getUsername(),
                config.getPassword()
            );
            props.put("sasl.jaas.config", jaasConfig);
        }
        
        // Optionally configure SSL properties if truststore/keystore are provided
        if (config.getKeystoreLocation() != null && !config.getKeystoreLocation().isEmpty()) {
            props.put("ssl.keystore.location", config.getKeystoreLocation());
        }
        if (config.getKeystorePassword() != null && !config.getKeystorePassword().isEmpty()) {
            props.put("ssl.keystore.password", config.getKeystorePassword());
        }
        if (config.getKeyPassword() != null && !config.getKeyPassword().isEmpty()) {
            props.put("ssl.key.password", config.getKeyPassword());
        }
        if (config.getTruststoreLocation() != null && !config.getTruststoreLocation().isEmpty()) {
            props.put("ssl.truststore.location", config.getTruststoreLocation());
        }
        if (config.getTruststorePassword() != null && !config.getTruststorePassword().isEmpty()) {
            props.put("ssl.truststore.password", config.getTruststorePassword());
        }
        
        logger.debug("Configured SASL over SSL authentication");
    }
    
    private static void configureKerberos(Properties props, AuthenticationConfig config) {
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "GSSAPI");
        
        if (config.getKerberosServiceName() != null && !config.getKerberosServiceName().isEmpty()) {
            props.put("sasl.kerberos.service.name", config.getKerberosServiceName());
        }
        
        String jaasConfig;
        if (config.getKerberosKeytab() != null && !config.getKerberosKeytab().isEmpty()) {
            jaasConfig = String.format(
                "com.sun.security.auth.module.Krb5LoginModule required " +
                "useKeyTab=true storeKey=true keyTab=\"%s\" principal=\"%s\";",
                config.getKerberosKeytab(),
                config.getKerberosPrincipal()
            );
        } else {
            jaasConfig = "com.sun.security.auth.module.Krb5LoginModule required " +
                        "useTicketCache=true;";
        }
        props.put("sasl.jaas.config", jaasConfig);
        
        logger.debug("Configured Kerberos authentication");
    }
    
    /**
     * Validate authentication configuration for the given type
     */
    public static boolean validateAuthenticationConfig(AuthenticationType type, AuthenticationConfig config) {
        ValidationResult result = validateAuthenticationConfigDetailed(type, config);
        return result.isValid();
    }
    
    /**
     * Validate authentication configuration with detailed error reporting
     */
    public static ValidationResult validateAuthenticationConfigDetailed(AuthenticationType type, AuthenticationConfig config) {
        if (type == AuthenticationType.NONE) {
            return ValidationResult.valid();
        }
        
        if (config == null) {
            return ValidationResult.invalid("Authentication configuration is required for " + type.getDisplayName());
        }
        
        switch (type) {
            case SASL_PLAIN:
            case SASL_SCRAM_SHA_256:
            case SASL_SCRAM_SHA_512:
                if (config.getUsername() == null || config.getUsername().isEmpty()) {
                    return ValidationResult.invalid("Username is required for " + type.getDisplayName());
                }
                if (config.getPassword() == null || config.getPassword().isEmpty()) {
                    return ValidationResult.invalid("Password is required for " + type.getDisplayName());
                }
                return ValidationResult.valid();
            
            case SSL:
                // For SSL, only truststore is required. Keystore is optional (for client authentication)
                if (config.getTruststoreLocation() == null || config.getTruststoreLocation().isEmpty()) {
                    return ValidationResult.invalid("Truststore location is required for SSL authentication");
                }
                return ValidationResult.valid();
            
            case SASL_SSL:
                // For SASL_SSL, we need SASL credentials. Truststore is optional (can use default trust store)
                if (config.getUsername() == null || config.getUsername().isEmpty()) {
                    return ValidationResult.invalid("Username is required for SASL over SSL");
                }
                if (config.getPassword() == null || config.getPassword().isEmpty()) {
                    return ValidationResult.invalid("Password is required for SASL over SSL");
                }
                return ValidationResult.valid();
            
            case KERBEROS:
                if (config.getKerberosServiceName() == null || config.getKerberosServiceName().isEmpty()) {
                    return ValidationResult.invalid("Kerberos service name is required for Kerberos authentication");
                }
                return ValidationResult.valid();
            
            default:
                return ValidationResult.valid();
        }
    }
    
    /**
     * Inner class for validation results with detailed error messages
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}