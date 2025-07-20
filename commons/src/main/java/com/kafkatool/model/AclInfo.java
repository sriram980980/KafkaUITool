package com.kafkatool.model;

/**
 * Model class representing ACL information
 */
public class AclInfo {
    private String resourceType;
    private String resourceName;
    private String operation;
    private String permissionType;
    private String principal;
    private String host;
    
    public AclInfo() {}
    
    public AclInfo(String resourceType, String resourceName, String operation) {
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.operation = operation;
    }
    
    // Getters and setters
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public String getPermissionType() { return permissionType; }
    public void setPermissionType(String permissionType) { this.permissionType = permissionType; }
    
    public String getPrincipal() { return principal; }
    public void setPrincipal(String principal) { this.principal = principal; }
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
}