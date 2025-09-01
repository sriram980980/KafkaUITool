package com.kafkatool.model;

/**
 * Enumeration of supported Kafka authentication types
 */
public enum AuthenticationType {
    /**
     * No authentication (default)
     */
    NONE("No Authentication"),
    
    /**
     * SASL Plain authentication with username/password
     */
    SASL_PLAIN("SASL Plain"),
    
    /**
     * SASL SCRAM-SHA-256 authentication
     */
    SASL_SCRAM_SHA_256("SASL SCRAM-SHA-256"),
    
    /**
     * SASL SCRAM-SHA-512 authentication
     */
    SASL_SCRAM_SHA_512("SASL SCRAM-SHA-512"),
    
    /**
     * SSL authentication with client certificates
     */
    SSL("SSL/TLS"),
    
    /**
     * SASL authentication over SSL
     */
    SASL_SSL("SASL over SSL"),
    
    /**
     * Kerberos authentication (SASL GSSAPI)
     */
    KERBEROS("Kerberos");
    
    private final String displayName;
    
    AuthenticationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}