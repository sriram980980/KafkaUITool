package com.kafkatool.plugin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Plugin manager for handling plugin lifecycle
 */
public interface PluginManager {
    
    /**
     * Load all plugins from the plugins directory
     */
    void loadPlugins();
    
    /**
     * Register a plugin
     */
    void registerPlugin(Plugin plugin);
    
    /**
     * Unregister a plugin
     */
    void unregisterPlugin(String pluginName);
    
    /**
     * Get all registered plugins
     */
    List<Plugin> getAllPlugins();
    
    /**
     * Get enabled plugins
     */
    List<Plugin> getEnabledPlugins();
    
    /**
     * Get plugin by name
     */
    Optional<Plugin> getPlugin(String name);
    
    /**
     * Start all enabled plugins
     */
    void startPlugins();
    
    /**
     * Stop all plugins
     */
    void stopPlugins();
    
    /**
     * Enable a plugin
     */
    void enablePlugin(String pluginName);
    
    /**
     * Disable a plugin
     */
    void disablePlugin(String pluginName);
    
    /**
     * Configure a plugin
     */
    void configurePlugin(String pluginName, Map<String, Object> config);
    
    /**
     * Get plugin configuration
     */
    Map<String, Object> getPluginConfig(String pluginName);
    
    /**
     * Reload a plugin
     */
    void reloadPlugin(String pluginName);
    
    /**
     * Get plugin status information
     */
    Map<String, Object> getPluginStatus(String pluginName);
    
    /**
     * Get all plugin statuses
     */
    Map<String, Map<String, Object>> getAllPluginStatuses();
}