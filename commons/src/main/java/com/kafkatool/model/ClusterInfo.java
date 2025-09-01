package com.kafkatool.model;

/**
 * Model class representing a Kafka cluster configuration
 * Plain POJO version for use in commons module
 */
public class ClusterInfo {
    
    private String name;
    private String brokerUrls;
    private String status = "Disconnected";
    private boolean connectByDefault = false;
    private String kafkaVersion = "";
    
    // Authentication fields
    private AuthenticationType authenticationType = AuthenticationType.NONE;
    private AuthenticationConfig authenticationConfig;
    
    public ClusterInfo() {}
    
    public ClusterInfo(String name, String brokerUrls) {
        this.name = name;
        this.brokerUrls = brokerUrls;
    }
    
    public ClusterInfo(String name, String brokerUrls, boolean connectByDefault) {
        this(name, brokerUrls);
        this.connectByDefault = connectByDefault;
    }
    
    public ClusterInfo(String name, String brokerUrls, AuthenticationType authenticationType, 
                      AuthenticationConfig authenticationConfig) {
        this(name, brokerUrls);
        this.authenticationType = authenticationType != null ? authenticationType : AuthenticationType.NONE;
        this.authenticationConfig = authenticationConfig;
    }
    
    public ClusterInfo(String name, String brokerUrls, boolean connectByDefault,
                      AuthenticationType authenticationType, AuthenticationConfig authenticationConfig) {
        this(name, brokerUrls, authenticationType, authenticationConfig);
        this.connectByDefault = connectByDefault;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getBrokerUrls() {
        return brokerUrls;
    }
    
    public void setBrokerUrls(String brokerUrls) {
        this.brokerUrls = brokerUrls;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public boolean isConnectByDefault() {
        return connectByDefault;
    }
    
    public void setConnectByDefault(boolean connectByDefault) {
        this.connectByDefault = connectByDefault;
    }
    
    public String getKafkaVersion() {
        return kafkaVersion;
    }
    
    public void setKafkaVersion(String kafkaVersion) {
        this.kafkaVersion = kafkaVersion;
    }
    
    public AuthenticationType getAuthenticationType() {
        return authenticationType != null ? authenticationType : AuthenticationType.NONE;
    }
    
    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType != null ? authenticationType : AuthenticationType.NONE;
    }
    
    public AuthenticationConfig getAuthenticationConfig() {
        return authenticationConfig;
    }
    
    public void setAuthenticationConfig(AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }
    
    /**
     * Check if this cluster requires authentication
     */
    public boolean requiresAuthentication() {
        return authenticationType != null && authenticationType != AuthenticationType.NONE;
    }
    
    /**
     * Get a copy of this cluster info with masked authentication data for display
     */
    public ClusterInfo createMaskedCopy() {
        ClusterInfo masked = new ClusterInfo(this.name, this.brokerUrls, this.connectByDefault);
        masked.status = this.status;
        masked.kafkaVersion = this.kafkaVersion;
        masked.authenticationType = this.authenticationType;
        if (this.authenticationConfig != null) {
            masked.authenticationConfig = this.authenticationConfig.createMaskedCopy();
        }
        return masked;
    }
    
    @Override
    public String toString() {
        if (name != null && brokerUrls != null) {
            return String.format("%s (%s)", name, brokerUrls);
        }
        return name != null ? name : "Unnamed Cluster";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClusterInfo that = (ClusterInfo) obj;
        return java.util.Objects.equals(name, that.name) && 
               java.util.Objects.equals(brokerUrls, that.brokerUrls);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, brokerUrls);
    }
}