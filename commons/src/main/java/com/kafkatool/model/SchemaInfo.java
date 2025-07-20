package com.kafkatool.model;

/**
 * Model class representing schema information
 */
public class SchemaInfo {
    private int id;
    private String subject;
    private int version;
    private String schemaType;
    private String schema;
    private boolean isDeleted;
    private String references;
    
    public SchemaInfo() {}
    
    public SchemaInfo(int id, String subject, int version) {
        this.id = id;
        this.subject = subject;
        this.version = version;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    
    public String getSchemaType() { return schemaType; }
    public void setSchemaType(String schemaType) { this.schemaType = schemaType; }
    
    public String getSchema() { return schema; }
    public void setSchema(String schema) { this.schema = schema; }
    
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
    
    public String getReferences() { return references; }
    public void setReferences(String references) { this.references = references; }
}