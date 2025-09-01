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
        // First configure SSL
        configureSsl(props, config);
        
        // Then override security protocol for SASL over SSL
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
        if (type == AuthenticationType.NONE) {
            return true;
        }
        
        if (config == null) {
            return false;
        }
        
        switch (type) {
            case SASL_PLAIN:
            case SASL_SCRAM_SHA_256:
            case SASL_SCRAM_SHA_512:
                return config.getUsername() != null && !config.getUsername().isEmpty() &&
                       config.getPassword() != null && !config.getPassword().isEmpty();
            
            case SSL:
                return config.getTruststoreLocation() != null && !config.getTruststoreLocation().isEmpty();
            
            case SASL_SSL:
                return (config.getTruststoreLocation() != null && !config.getTruststoreLocation().isEmpty()) &&
                       (config.getUsername() != null && !config.getUsername().isEmpty() &&
                        config.getPassword() != null && !config.getPassword().isEmpty());
            
            case KERBEROS:
                return config.getKerberosServiceName() != null && !config.getKerberosServiceName().isEmpty();
            
            default:
                return true;
        }
    }
}