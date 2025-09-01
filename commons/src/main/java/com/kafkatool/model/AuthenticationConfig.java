package com.kafkatool.model;

/**
 * Model class for storing authentication configuration details
 */
public class AuthenticationConfig {
    
    // SASL username/password
    private String username;
    private String password;
    
    // SSL configuration
    private String keystoreLocation;
    private String keystorePassword;
    private String keyPassword;
    private String truststoreLocation;
    private String truststorePassword;
    
    // Kerberos configuration
    private String kerberosServiceName;
    private String kerberosRealm;
    private String kerberosKeytab;
    private String kerberosPrincipal;
    
    // Additional properties for advanced configurations
    private String additionalProperties;
    
    public AuthenticationConfig() {}
    
    // SASL constructor
    public AuthenticationConfig(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // SSL constructor
    public AuthenticationConfig(String keystoreLocation, String keystorePassword, 
                               String truststoreLocation, String truststorePassword) {
        this.keystoreLocation = keystoreLocation;
        this.keystorePassword = keystorePassword;
        this.truststoreLocation = truststoreLocation;
        this.truststorePassword = truststorePassword;
    }
    
    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getKeystoreLocation() {
        return keystoreLocation;
    }
    
    public void setKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
    }
    
    public String getKeystorePassword() {
        return keystorePassword;
    }
    
    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }
    
    public String getKeyPassword() {
        return keyPassword;
    }
    
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }
    
    public String getTruststoreLocation() {
        return truststoreLocation;
    }
    
    public void setTruststoreLocation(String truststoreLocation) {
        this.truststoreLocation = truststoreLocation;
    }
    
    public String getTruststorePassword() {
        return truststorePassword;
    }
    
    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }
    
    public String getKerberosServiceName() {
        return kerberosServiceName;
    }
    
    public void setKerberosServiceName(String kerberosServiceName) {
        this.kerberosServiceName = kerberosServiceName;
    }
    
    public String getKerberosRealm() {
        return kerberosRealm;
    }
    
    public void setKerberosRealm(String kerberosRealm) {
        this.kerberosRealm = kerberosRealm;
    }
    
    public String getKerberosKeytab() {
        return kerberosKeytab;
    }
    
    public void setKerberosKeytab(String kerberosKeytab) {
        this.kerberosKeytab = kerberosKeytab;
    }
    
    public String getKerberosPrincipal() {
        return kerberosPrincipal;
    }
    
    public void setKerberosPrincipal(String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
    }
    
    public String getAdditionalProperties() {
        return additionalProperties;
    }
    
    public void setAdditionalProperties(String additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
    
    /**
     * Check if this configuration has any credentials that need security
     */
    public boolean hasSecureCredentials() {
        return (password != null && !password.isEmpty()) ||
               (keystorePassword != null && !keystorePassword.isEmpty()) ||
               (keyPassword != null && !keyPassword.isEmpty()) ||
               (truststorePassword != null && !truststorePassword.isEmpty());
    }
    
    /**
     * Create a copy of this config with masked sensitive data for display
     */
    public AuthenticationConfig createMaskedCopy() {
        AuthenticationConfig masked = new AuthenticationConfig();
        masked.username = this.username;
        masked.password = maskSensitiveValue(this.password);
        masked.keystoreLocation = this.keystoreLocation;
        masked.keystorePassword = maskSensitiveValue(this.keystorePassword);
        masked.keyPassword = maskSensitiveValue(this.keyPassword);
        masked.truststoreLocation = this.truststoreLocation;
        masked.truststorePassword = maskSensitiveValue(this.truststorePassword);
        masked.kerberosServiceName = this.kerberosServiceName;
        masked.kerberosRealm = this.kerberosRealm;
        masked.kerberosKeytab = this.kerberosKeytab;
        masked.kerberosPrincipal = this.kerberosPrincipal;
        masked.additionalProperties = this.additionalProperties;
        return masked;
    }
    
    private String maskSensitiveValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return "****";
    }
}