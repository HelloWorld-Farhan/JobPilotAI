package com.jobpilotai.workspace;

import com.jobpilotai.config.AppConfig;
import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Manages multiple user profiles.
 * Each profile has its own SQLite database.
 */
public class ProfileManager {
    private static ProfileManager instance;
    private String activeProfileName = "default";
    
    private final File profileConfigFile = new File("profiles.properties");

    private ProfileManager() {
        loadProfileConfig();
    }

    public static synchronized ProfileManager getInstance() {
        if (instance == null) {
            instance = new ProfileManager();
        }
        return instance;
    }

    private void loadProfileConfig() {
        if (profileConfigFile.exists()) {
            try (FileInputStream fis = new FileInputStream(profileConfigFile)) {
                Properties props = new Properties();
                props.load(fis);
                activeProfileName = props.getProperty("active_profile", "default");
            } catch (Exception e) {
                AppLogger.error("Failed to load profiles.properties", e);
            }
        }
    }

    public void saveProfileConfig() {
        try (FileOutputStream fos = new FileOutputStream(profileConfigFile)) {
            Properties props = new Properties();
            props.setProperty("active_profile", activeProfileName);
            props.store(fos, "JobPilotAI Profiles Config");
        } catch (Exception e) {
            AppLogger.error("Failed to save profiles.properties", e);
        }
    }

    /**
     * Initializes the database for the active profile.
     */
    public void loadActiveProfile() {
        try {
            String dbName = activeProfileName.equals("default") 
                ? AppConfig.DB_FILE_NAME 
                : "jobpilotai_" + activeProfileName + ".db";
                
            AppLogger.info("Loading profile: " + activeProfileName + " (DB: " + dbName + ")");
            
            // Close existing connection if switching
            DatabaseManager.getInstance().close();
            
            // Initialize new connection
            DatabaseManager.getInstance().initialize(dbName);
            
        } catch (Exception e) {
            AppLogger.error("Failed to initialize profile database", e);
        }
    }

    public void switchProfile(String newProfileName) {
        this.activeProfileName = newProfileName.replaceAll("[^a-zA-Z0-9_]", "_"); // Sanitize
        saveProfileConfig();
        loadActiveProfile();
    }

    public String getActiveProfileName() {
        return activeProfileName;
    }
}
