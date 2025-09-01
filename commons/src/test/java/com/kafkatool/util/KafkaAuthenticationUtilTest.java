package com.kafkatool.util;

import com.kafkatool.model.AuthenticationConfig;
import com.kafkatool.model.AuthenticationType;
import com.kafkatool.model.ClusterInfo;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for KafkaAuthenticationUtil
 */
public class KafkaAuthenticationUtilTest {
    
    @Test
    public void testNoAuthenticationDoesNotModifyProperties() {
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092");
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        
        KafkaAuthenticationUtil.configureAuthentication(props, cluster);
        
        // Should only have the original property
        assertEquals(1, props.size());
        assertEquals("localhost:9092", props.get("bootstrap.servers"));
    }
    
    @Test
    public void testSaslPlainConfiguration() {
        AuthenticationConfig authConfig = new AuthenticationConfig("testuser", "testpass");
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092", AuthenticationType.SASL_PLAIN, authConfig);
        
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        
        KafkaAuthenticationUtil.configureAuthentication(props, cluster);
        
        assertEquals("SASL_PLAINTEXT", props.get("security.protocol"));
        assertEquals("PLAIN", props.get("sasl.mechanism"));
        assertTrue(props.get("sasl.jaas.config").toString().contains("testuser"));
        assertTrue(props.get("sasl.jaas.config").toString().contains("testpass"));
    }
    
    @Test
    public void testSaslScramSha256Configuration() {
        AuthenticationConfig authConfig = new AuthenticationConfig("testuser", "testpass");
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092", AuthenticationType.SASL_SCRAM_SHA_256, authConfig);
        
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        
        KafkaAuthenticationUtil.configureAuthentication(props, cluster);
        
        assertEquals("SASL_PLAINTEXT", props.get("security.protocol"));
        assertEquals("SCRAM-SHA-256", props.get("sasl.mechanism"));
        assertTrue(props.get("sasl.jaas.config").toString().contains("ScramLoginModule"));
    }
    
    @Test
    public void testSslConfiguration() {
        AuthenticationConfig authConfig = new AuthenticationConfig();
        authConfig.setKeystoreLocation("/path/to/keystore.jks");
        authConfig.setKeystorePassword("keystorepass");
        authConfig.setTruststoreLocation("/path/to/truststore.jks");
        authConfig.setTruststorePassword("truststorepass");
        
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092", AuthenticationType.SSL, authConfig);
        
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        
        KafkaAuthenticationUtil.configureAuthentication(props, cluster);
        
        assertEquals("SSL", props.get("security.protocol"));
        assertEquals("/path/to/keystore.jks", props.get("ssl.keystore.location"));
        assertEquals("keystorepass", props.get("ssl.keystore.password"));
        assertEquals("/path/to/truststore.jks", props.get("ssl.truststore.location"));
        assertEquals("truststorepass", props.get("ssl.truststore.password"));
    }
    
    @Test
    public void testSaslSslConfiguration() {
        AuthenticationConfig authConfig = new AuthenticationConfig("testuser", "testpass");
        authConfig.setTruststoreLocation("/path/to/truststore.jks");
        authConfig.setTruststorePassword("truststorepass");
        
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092", AuthenticationType.SASL_SSL, authConfig);
        
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        
        KafkaAuthenticationUtil.configureAuthentication(props, cluster);
        
        assertEquals("SASL_SSL", props.get("security.protocol"));
        assertEquals("PLAIN", props.get("sasl.mechanism"));
        assertEquals("/path/to/truststore.jks", props.get("ssl.truststore.location"));
        assertTrue(props.get("sasl.jaas.config").toString().contains("testuser"));
    }
    
    @Test
    public void testKerberosConfiguration() {
        AuthenticationConfig authConfig = new AuthenticationConfig();
        authConfig.setKerberosServiceName("kafka");
        authConfig.setKerberosKeytab("/path/to/kafka.keytab");
        authConfig.setKerberosPrincipal("kafka/localhost@EXAMPLE.COM");
        
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092", AuthenticationType.KERBEROS, authConfig);
        
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        
        KafkaAuthenticationUtil.configureAuthentication(props, cluster);
        
        assertEquals("SASL_PLAINTEXT", props.get("security.protocol"));
        assertEquals("GSSAPI", props.get("sasl.mechanism"));
        assertEquals("kafka", props.get("sasl.kerberos.service.name"));
        assertTrue(props.get("sasl.jaas.config").toString().contains("Krb5LoginModule"));
    }
    
    @Test
    public void testValidateAuthenticationConfig() {
        // Test NONE authentication
        assertTrue(KafkaAuthenticationUtil.validateAuthenticationConfig(AuthenticationType.NONE, null));
        
        // Test SASL Plain with valid config
        AuthenticationConfig saslConfig = new AuthenticationConfig("user", "pass");
        assertTrue(KafkaAuthenticationUtil.validateAuthenticationConfig(AuthenticationType.SASL_PLAIN, saslConfig));
        
        // Test SASL Plain with invalid config
        AuthenticationConfig invalidSaslConfig = new AuthenticationConfig("", "");
        assertFalse(KafkaAuthenticationUtil.validateAuthenticationConfig(AuthenticationType.SASL_PLAIN, invalidSaslConfig));
        
        // Test SSL with valid config
        AuthenticationConfig sslConfig = new AuthenticationConfig();
        sslConfig.setTruststoreLocation("/path/to/truststore.jks");
        assertTrue(KafkaAuthenticationUtil.validateAuthenticationConfig(AuthenticationType.SSL, sslConfig));
        
        // Test SSL with invalid config
        AuthenticationConfig invalidSslConfig = new AuthenticationConfig();
        assertFalse(KafkaAuthenticationUtil.validateAuthenticationConfig(AuthenticationType.SSL, invalidSslConfig));
    }
    
    @Test
    public void testAuthenticationConfigMasking() {
        AuthenticationConfig config = new AuthenticationConfig("testuser", "secretpassword");
        config.setKeystorePassword("keystorepass");
        
        AuthenticationConfig masked = config.createMaskedCopy();
        
        assertEquals("testuser", masked.getUsername());
        assertEquals("****", masked.getPassword());
        assertEquals("****", masked.getKeystorePassword());
    }
    
    @Test
    public void testClusterInfoMasking() {
        AuthenticationConfig authConfig = new AuthenticationConfig("testuser", "secretpassword");
        ClusterInfo cluster = new ClusterInfo("test", "localhost:9092", AuthenticationType.SASL_PLAIN, authConfig);
        
        ClusterInfo masked = cluster.createMaskedCopy();
        
        assertEquals("test", masked.getName());
        assertEquals("localhost:9092", masked.getBrokerUrls());
        assertEquals(AuthenticationType.SASL_PLAIN, masked.getAuthenticationType());
        assertEquals("testuser", masked.getAuthenticationConfig().getUsername());
        assertEquals("****", masked.getAuthenticationConfig().getPassword());
    }
}