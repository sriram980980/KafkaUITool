package com.kafkatool.model;

import javafx.beans.property.*;

/**
 * Model class representing cluster configuration properties
 */
public class ClusterConfig {
    
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty value = new SimpleStringProperty();
    private final StringProperty source = new SimpleStringProperty();
    private final BooleanProperty readOnly = new SimpleBooleanProperty(false);
    private final BooleanProperty sensitive = new SimpleBooleanProperty(false);
    private final StringProperty documentation = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty defaultValue = new SimpleStringProperty();
    
    public ClusterConfig() {}
    
    public ClusterConfig(String name, String value, String source) {
        setName(name);
        setValue(value);
        setSource(source);
    }
    
    // Name property
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    // Value property
    public String getValue() {
        return value.get();
    }
    
    public void setValue(String value) {
        this.value.set(value);
    }
    
    public StringProperty valueProperty() {
        return value;
    }
    
    // Source property
    public String getSource() {
        return source.get();
    }
    
    public void setSource(String source) {
        this.source.set(source);
    }
    
    public StringProperty sourceProperty() {
        return source;
    }
    
    // Read only property
    public boolean isReadOnly() {
        return readOnly.get();
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly.set(readOnly);
    }
    
    public BooleanProperty readOnlyProperty() {
        return readOnly;
    }
    
    // Sensitive property
    public boolean isSensitive() {
        return sensitive.get();
    }
    
    public void setSensitive(boolean sensitive) {
        this.sensitive.set(sensitive);
    }
    
    public BooleanProperty sensitiveProperty() {
        return sensitive;
    }
    
    // Documentation property
    public String getDocumentation() {
        return documentation.get();
    }
    
    public void setDocumentation(String documentation) {
        this.documentation.set(documentation);
    }
    
    public StringProperty documentationProperty() {
        return documentation;
    }
    
    // Description property (alias for documentation)
    public String getDescription() {
        return getDocumentation();
    }
    
    public void setDescription(String description) {
        setDocumentation(description);
    }
    
    public StringProperty descriptionProperty() {
        return documentation;
    }
    
    // Type property
    public String getType() {
        return type.get();
    }
    
    public void setType(String type) {
        this.type.set(type);
    }
    
    public StringProperty typeProperty() {
        return type;
    }
    
    // Default value property
    public String getDefaultValue() {
        return defaultValue.get();
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue.set(defaultValue);
    }
    
    public StringProperty defaultValueProperty() {
        return defaultValue;
    }
    
    @Override
    public String toString() {
        return String.format("%s = %s", getName(), getValue());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClusterConfig that = (ClusterConfig) obj;
        return getName().equals(that.getName());
    }
    
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}