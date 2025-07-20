package com.kafkatool.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Model class representing security ACL information
 */
public class AclInfo {
    
    private final StringProperty resourceType = new SimpleStringProperty();
    private final StringProperty resourceName = new SimpleStringProperty();
    private final StringProperty patternType = new SimpleStringProperty();
    private final StringProperty principal = new SimpleStringProperty();
    private final StringProperty operation = new SimpleStringProperty();
    private final StringProperty permissionType = new SimpleStringProperty();
    private final StringProperty host = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    
    public AclInfo() {}
    
    public AclInfo(String resourceType, String resourceName, String principal, 
                   String operation, String permissionType) {
        setResourceType(resourceType);
        setResourceName(resourceName);
        setPrincipal(principal);
        setOperation(operation);
        setPermissionType(permissionType);
        setCreatedAt(LocalDateTime.now());
    }
    
    // Resource type property
    public String getResourceType() {
        return resourceType.get();
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType.set(resourceType);
    }
    
    public StringProperty resourceTypeProperty() {
        return resourceType;
    }
    
    // Resource name property
    public String getResourceName() {
        return resourceName.get();
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName.set(resourceName);
    }
    
    public StringProperty resourceNameProperty() {
        return resourceName;
    }
    
    // Pattern type property
    public String getPatternType() {
        return patternType.get();
    }
    
    public void setPatternType(String patternType) {
        this.patternType.set(patternType);
    }
    
    public StringProperty patternTypeProperty() {
        return patternType;
    }
    
    // Principal property
    public String getPrincipal() {
        return principal.get();
    }
    
    public void setPrincipal(String principal) {
        this.principal.set(principal);
    }
    
    public StringProperty principalProperty() {
        return principal;
    }
    
    // Operation property
    public String getOperation() {
        return operation.get();
    }
    
    public void setOperation(String operation) {
        this.operation.set(operation);
    }
    
    public StringProperty operationProperty() {
        return operation;
    }
    
    // Permission type property
    public String getPermissionType() {
        return permissionType.get();
    }
    
    public void setPermissionType(String permissionType) {
        this.permissionType.set(permissionType);
    }
    
    public StringProperty permissionTypeProperty() {
        return permissionType;
    }
    
    // Host property
    public String getHost() {
        return host.get();
    }
    
    public void setHost(String host) {
        this.host.set(host);
    }
    
    public StringProperty hostProperty() {
        return host;
    }
    
    // Created at property
    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }
    
    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s:%s [%s] %s", 
            getPrincipal(), getResourceType(), getResourceName(), 
            getOperation(), getPermissionType());
    }
}