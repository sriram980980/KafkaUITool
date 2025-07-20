package com.kafkatool.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kafkatool.model.ClusterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing application settings and persistence
 */
public class SettingsManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SettingsManager.class);
    private static final String SETTINGS_DIR = System.getProperty("user.home") + File.separator + ".kafka-ui-tool";
    private static final String CLUSTERS_FILE = "clusters.json";
    private static final String SETTINGS_FILE = "settings.json";
    
    private final ObjectMapper objectMapper;
    private final File settingsDirectory;
    private final File clustersFile;
    private final File settingsFile;
    
    public SettingsManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        this.settingsDirectory = new File(SETTINGS_DIR);
        this.clustersFile = new File(settingsDirectory, CLUSTERS_FILE);
        this.settingsFile = new File(settingsDirectory, SETTINGS_FILE);
        
        createSettingsDirectory();
    }
    
    private void createSettingsDirectory() {
        if (!settingsDirectory.exists()) {
            boolean created = settingsDirectory.mkdirs();
            if (created) {
                logger.info("Created settings directory: {}", settingsDirectory.getAbsolutePath());
            } else {
                logger.warn("Failed to create settings directory: {}", settingsDirectory.getAbsolutePath());
            }
        }
    }
    
    /**
     * Save clusters to persistent storage
     */
    public void saveClusters(List<ClusterInfo> clusters) throws IOException {
        try {
            objectMapper.writeValue(clustersFile, clusters);
            logger.info("Saved {} clusters to {}", clusters.size(), clustersFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save clusters", e);
            throw e;
        }
    }
    
    /**
     * Load clusters from persistent storage
     */
    public List<ClusterInfo> loadClusters() throws IOException {
        if (!clustersFile.exists()) {
            logger.info("Clusters file does not exist, returning empty list");
            return new ArrayList<>();
        }
        
        try {
            List<ClusterInfo> clusters = objectMapper.readValue(clustersFile, 
                new TypeReference<List<ClusterInfo>>() {});
            logger.info("Loaded {} clusters from {}", clusters.size(), clustersFile.getAbsolutePath());
            return clusters;
        } catch (IOException e) {
            logger.error("Failed to load clusters", e);
            throw e;
        }
    }
    
    /**
     * Save application settings
     */
    public void saveSettings(ApplicationSettings settings) throws IOException {
        try {
            objectMapper.writeValue(settingsFile, settings);
            logger.info("Saved application settings to {}", settingsFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save settings", e);
            throw e;
        }
    }
    
    /**
     * Load application settings
     */
    public ApplicationSettings loadSettings() throws IOException {
        if (!settingsFile.exists()) {
            logger.info("Settings file does not exist, returning default settings");
            return new ApplicationSettings();
        }
        
        try {
            ApplicationSettings settings = objectMapper.readValue(settingsFile, ApplicationSettings.class);
            logger.info("Loaded application settings from {}", settingsFile.getAbsolutePath());
            return settings;
        } catch (IOException e) {
            logger.error("Failed to load settings", e);
            throw e;
        }
    }
    
    /**
     * Check if clusters file exists
     */
    public boolean clustersFileExists() {
        return clustersFile.exists();
    }
    
    /**
     * Check if settings file exists
     */
    public boolean settingsFileExists() {
        return settingsFile.exists();
    }
    
    /**
     * Get the settings directory path
     */
    public String getSettingsDirectoryPath() {
        return settingsDirectory.getAbsolutePath();
    }
    
    /**
     * Application settings class
     */
    public static class ApplicationSettings {
        private String theme = "dark";
        private boolean autoConnect = true;
        private int defaultMessageCount = 100;
        private int searchTimeout = 30;
        private boolean enableAutoRefresh = false;
        private int autoRefreshInterval = 30;
        
        // Getters and setters
        public String getTheme() {
            return theme;
        }
        
        public void setTheme(String theme) {
            this.theme = theme;
        }
        
        public boolean isAutoConnect() {
            return autoConnect;
        }
        
        public void setAutoConnect(boolean autoConnect) {
            this.autoConnect = autoConnect;
        }
        
        public int getDefaultMessageCount() {
            return defaultMessageCount;
        }
        
        public void setDefaultMessageCount(int defaultMessageCount) {
            this.defaultMessageCount = defaultMessageCount;
        }
        
        public int getSearchTimeout() {
            return searchTimeout;
        }
        
        public void setSearchTimeout(int searchTimeout) {
            this.searchTimeout = searchTimeout;
        }
        
        public boolean isEnableAutoRefresh() {
            return enableAutoRefresh;
        }
        
        public void setEnableAutoRefresh(boolean enableAutoRefresh) {
            this.enableAutoRefresh = enableAutoRefresh;
        }
        
        public int getAutoRefreshInterval() {
            return autoRefreshInterval;
        }
        
        public void setAutoRefreshInterval(int autoRefreshInterval) {
            this.autoRefreshInterval = autoRefreshInterval;
        }
    }
}