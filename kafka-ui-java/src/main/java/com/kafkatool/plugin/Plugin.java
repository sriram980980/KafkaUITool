package com.kafkatool.plugin;

import java.util.Map;

/**
 * Interface for Kafka UI Tool plugins
 */
public interface Plugin {
    
    /**
     * Get the plugin name
     */
    String getName();
    
    /**
     * Get the plugin version
     */
    String getVersion();
    
    /**
     * Get the plugin description
     */
    String getDescription();
    
    /**
     * Get the plugin author
     */
    String getAuthor();
    
    /**
     * Initialize the plugin with configuration
     */
    void initialize(Map<String, Object> config);
    
    /**
     * Start the plugin
     */
    void start();
    
    /**
     * Stop the plugin
     */
    void stop();
    
    /**
     * Check if the plugin is enabled
     */
    boolean isEnabled();
    
    /**
     * Enable or disable the plugin
     */
    void setEnabled(boolean enabled);
    
    /**
     * Get plugin configuration schema
     */
    Map<String, Object> getConfigSchema();
    
    /**
     * Validate plugin configuration
     */
    boolean validateConfig(Map<String, Object> config);
    
    /**
     * Get plugin status information
     */
    Map<String, Object> getStatus();
}