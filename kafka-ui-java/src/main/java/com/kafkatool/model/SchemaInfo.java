package com.kafkatool.model;

import javafx.beans.property.*;

/**
 * Model class representing a Schema Registry schema
 */
public class SchemaInfo {
    
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty subject = new SimpleStringProperty();
    private final IntegerProperty version = new SimpleIntegerProperty();
    private final StringProperty schemaType = new SimpleStringProperty();
    private final StringProperty schema = new SimpleStringProperty();
    private final BooleanProperty isLatest = new SimpleBooleanProperty();
    private final StringProperty compatibility = new SimpleStringProperty();
    
    public SchemaInfo() {}
    
    public SchemaInfo(int id, String subject, int version, String schemaType, String schema) {
        setId(id);
        setSubject(subject);
        setVersion(version);
        setSchemaType(schemaType);
        setSchema(schema);
    }
    
    // ID property
    public int getId() {
        return id.get();
    }
    
    public void setId(int id) {
        this.id.set(id);
    }
    
    public IntegerProperty idProperty() {
        return id;
    }
    
    // Subject property
    public String getSubject() {
        return subject.get();
    }
    
    public void setSubject(String subject) {
        this.subject.set(subject);
    }
    
    public StringProperty subjectProperty() {
        return subject;
    }
    
    // Version property
    public int getVersion() {
        return version.get();
    }
    
    public void setVersion(int version) {
        this.version.set(version);
    }
    
    public IntegerProperty versionProperty() {
        return version;
    }
    
    // Schema type property
    public String getSchemaType() {
        return schemaType.get();
    }
    
    public void setSchemaType(String schemaType) {
        this.schemaType.set(schemaType);
    }
    
    public StringProperty schemaTypeProperty() {
        return schemaType;
    }
    
    // Schema property
    public String getSchema() {
        return schema.get();
    }
    
    public void setSchema(String schema) {
        this.schema.set(schema);
    }
    
    public StringProperty schemaProperty() {
        return schema;
    }
    
    // Is latest property
    public boolean isLatest() {
        return isLatest.get();
    }
    
    public void setLatest(boolean latest) {
        this.isLatest.set(latest);
    }
    
    public BooleanProperty latestProperty() {
        return isLatest;
    }
    
    // Compatibility property
    public String getCompatibility() {
        return compatibility.get();
    }
    
    public void setCompatibility(String compatibility) {
        this.compatibility.set(compatibility);
    }
    
    public StringProperty compatibilityProperty() {
        return compatibility;
    }
    
    @Override
    public String toString() {
        return String.format("%s (v%d, %s)", getSubject(), getVersion(), getSchemaType());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SchemaInfo that = (SchemaInfo) obj;
        return getId() == that.getId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}