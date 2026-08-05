package com.jobpilotai.service;

import com.jobpilotai.config.AppConfig;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.repository.SettingsRepository;
import com.jobpilotai.security.EncryptionService;
import javafx.stage.Stage;

/**
 * Singleton service that manages all user-configurable application settings.
 */
public class SettingsService {

    private static SettingsService instance;
    private final SettingsRepository repo = new SettingsRepository();

    // In-memory cache
    private String theme;
    private boolean darkMode;
    private String resumePath;
    private String defaultEmail;
    private String gasUrl;
    private String reportFolder;
    private String logFolder;
    private boolean enableNotifications;
    private boolean enableEmail;
    private boolean autoSave;
    private boolean autoGenerateReports;
    private boolean rememberWindowSize;
    private boolean rememberWindowPosition;
    
    // AI Settings
    private String geminiApiKey;
    private boolean aiEnabled;
    
    // Automation
    private boolean headlessMode;
    private boolean screenshotOnError;
    private int timeoutMs;
    private int maxRetries;

    private SettingsService() {}

    public static synchronized SettingsService getInstance() {
        if (instance == null) instance = new SettingsService();
        return instance;
    }

    public void load() {
        AppLogger.info("Loading settings…");
        theme                = repo.get(AppConfig.SETTING_THEME,         AppConfig.DEFAULT_THEME);
        darkMode             = bool(repo.get(AppConfig.SETTING_DARK_MODE,   String.valueOf(AppConfig.DEFAULT_DARK_MODE)));
        resumePath           = repo.get(AppConfig.SETTING_RESUME_PATH,    "");
        defaultEmail         = repo.get(AppConfig.SETTING_DEFAULT_EMAIL,  "");
        gasUrl               = EncryptionService.decrypt(repo.get(AppConfig.SETTING_GAS_URL, ""));
        reportFolder         = repo.get(AppConfig.SETTING_REPORT_FOLDER,  "");
        logFolder            = repo.get(AppConfig.SETTING_LOG_FOLDER,     "");
        enableNotifications  = bool(repo.get(AppConfig.SETTING_ENABLE_NOTIF,  String.valueOf(AppConfig.DEFAULT_NOTIFICATIONS)));
        enableEmail          = bool(repo.get(AppConfig.SETTING_ENABLE_EMAIL,  String.valueOf(AppConfig.DEFAULT_EMAIL)));
        autoSave             = bool(repo.get(AppConfig.SETTING_AUTO_SAVE,     String.valueOf(AppConfig.DEFAULT_AUTO_SAVE)));
        autoGenerateReports  = bool(repo.get(AppConfig.SETTING_AUTO_REPORTS,  String.valueOf(AppConfig.DEFAULT_AUTO_REPORTS)));
        rememberWindowSize   = bool(repo.get(AppConfig.SETTING_REMEMBER_SIZE, "true"));
        rememberWindowPosition = bool(repo.get(AppConfig.SETTING_REMEMBER_POS,"true"));
        
        headlessMode         = bool(repo.get(AppConfig.SETTING_HEADLESS, String.valueOf(AppConfig.DEFAULT_HEADLESS)));
        screenshotOnError    = bool(repo.get(AppConfig.SETTING_SCREENSHOT_ERROR, String.valueOf(AppConfig.DEFAULT_SCREENSHOT_ERROR)));
        timeoutMs            = intSetting(AppConfig.SETTING_TIMEOUT, AppConfig.DEFAULT_TIMEOUT);
        maxRetries           = intSetting(AppConfig.SETTING_MAX_RETRIES, AppConfig.DEFAULT_MAX_RETRIES);
        
        geminiApiKey         = EncryptionService.decrypt(repo.get(AppConfig.SETTING_GEMINI_API_KEY, ""));
        aiEnabled            = bool(repo.get(AppConfig.SETTING_AI_ENABLED, String.valueOf(AppConfig.DEFAULT_AI_ENABLED)));

        AppLogger.info("Settings loaded. Theme=" + theme);
    }

    public void save() {
        repo.set(AppConfig.SETTING_THEME,         theme);
        repo.set(AppConfig.SETTING_DARK_MODE,      String.valueOf(darkMode));
        repo.set(AppConfig.SETTING_RESUME_PATH,    resumePath);
        repo.set(AppConfig.SETTING_DEFAULT_EMAIL,  defaultEmail);
        repo.set(AppConfig.SETTING_GAS_URL,        EncryptionService.encrypt(gasUrl));
        repo.set(AppConfig.SETTING_REPORT_FOLDER,  reportFolder);
        repo.set(AppConfig.SETTING_LOG_FOLDER,     logFolder);
        repo.set(AppConfig.SETTING_ENABLE_NOTIF,   String.valueOf(enableNotifications));
        repo.set(AppConfig.SETTING_ENABLE_EMAIL,   String.valueOf(enableEmail));
        repo.set(AppConfig.SETTING_AUTO_SAVE,      String.valueOf(autoSave));
        repo.set(AppConfig.SETTING_AUTO_REPORTS,   String.valueOf(autoGenerateReports));
        repo.set(AppConfig.SETTING_REMEMBER_SIZE,  String.valueOf(rememberWindowSize));
        repo.set(AppConfig.SETTING_REMEMBER_POS,   String.valueOf(rememberWindowPosition));
        
        repo.set(AppConfig.SETTING_HEADLESS,       String.valueOf(headlessMode));
        repo.set(AppConfig.SETTING_SCREENSHOT_ERROR, String.valueOf(screenshotOnError));
        repo.set(AppConfig.SETTING_TIMEOUT,        String.valueOf(timeoutMs));
        repo.set(AppConfig.SETTING_MAX_RETRIES,    String.valueOf(maxRetries));
        
        repo.set(AppConfig.SETTING_GEMINI_API_KEY, EncryptionService.encrypt(geminiApiKey));
        repo.set(AppConfig.SETTING_AI_ENABLED,     String.valueOf(aiEnabled));

        AppLogger.info("Settings saved.");
    }

    public void saveWindowState(Stage stage) {
        if (rememberWindowSize) {
            repo.set(AppConfig.SETTING_WINDOW_W, String.valueOf(stage.getWidth()));
            repo.set(AppConfig.SETTING_WINDOW_H, String.valueOf(stage.getHeight()));
        }
        if (rememberWindowPosition) {
            repo.set(AppConfig.SETTING_WINDOW_X, String.valueOf(stage.getX()));
            repo.set(AppConfig.SETTING_WINDOW_Y, String.valueOf(stage.getY()));
        }
    }

    public void restoreWindowState(Stage stage) {
        if (rememberWindowSize) {
            double w = doubleSetting(AppConfig.SETTING_WINDOW_W, AppConfig.DEFAULT_WIDTH);
            double h = doubleSetting(AppConfig.SETTING_WINDOW_H, AppConfig.DEFAULT_HEIGHT);
            stage.setWidth(Math.max(w, AppConfig.MIN_WIDTH));
            stage.setHeight(Math.max(h, AppConfig.MIN_HEIGHT));
        }
        if (rememberWindowPosition) {
            String xStr = repo.get(AppConfig.SETTING_WINDOW_X, null);
            String yStr = repo.get(AppConfig.SETTING_WINDOW_Y, null);
            if (xStr != null && yStr != null) {
                try {
                    stage.setX(Double.parseDouble(xStr));
                    stage.setY(Double.parseDouble(yStr));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private boolean bool(String v) { return "true".equalsIgnoreCase(v); }
    private double doubleSetting(String key, double def) {
        String v = repo.get(key, null);
        if (v == null) return def;
        try { return Double.parseDouble(v); } catch (NumberFormatException e) { return def; }
    }
    private int intSetting(String key, int def) {
        String v = repo.get(key, null);
        if (v == null) return def;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return def; }
    }

    // Getters and Setters
    public String  getTheme()                { return theme; }
    public void    setTheme(String v)        { this.theme = v; }
    public boolean isDarkMode()              { return darkMode; }
    public void    setDarkMode(boolean v)    { this.darkMode = v; }
    public String  getResumePath()           { return resumePath; }
    public void    setResumePath(String v)   { this.resumePath = v; }
    public String  getDefaultEmail()         { return defaultEmail; }
    public void    setDefaultEmail(String v) { this.defaultEmail = v; }
    public String  getGasUrl()               { return gasUrl; }
    public void    setGasUrl(String v)       { this.gasUrl = v; }
    public String  getReportFolder()         { return reportFolder; }
    public void    setReportFolder(String v) { this.reportFolder = v; }
    public String  getLogFolder()            { return logFolder; }
    public void    setLogFolder(String v)    { this.logFolder = v; }
    public boolean isEnableNotifications()   { return enableNotifications; }
    public void    setEnableNotifications(boolean v){ this.enableNotifications = v; }
    public boolean isEnableEmail()           { return enableEmail; }
    public void    setEnableEmail(boolean v) { this.enableEmail = v; }
    public boolean isAutoSave()              { return autoSave; }
    public void    setAutoSave(boolean v)    { this.autoSave = v; }
    public boolean isAutoGenerateReports()   { return autoGenerateReports; }
    public void    setAutoGenerateReports(boolean v){ this.autoGenerateReports = v; }
    public boolean isRememberWindowSize()    { return rememberWindowSize; }
    public void    setRememberWindowSize(boolean v) { this.rememberWindowSize = v; }
    public boolean isRememberWindowPosition(){ return rememberWindowPosition; }
    public void    setRememberWindowPosition(boolean v) { this.rememberWindowPosition = v; }

    public boolean isHeadlessMode() { return headlessMode; }
    public void setHeadlessMode(boolean headlessMode) { this.headlessMode = headlessMode; }
    public boolean isScreenshotOnError() { return screenshotOnError; }
    public void setScreenshotOnError(boolean screenshotOnError) { this.screenshotOnError = screenshotOnError; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    
    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String key) { this.geminiApiKey = key; }
    public boolean isAiEnabled() { return aiEnabled; }
    public void setAiEnabled(boolean enabled) { this.aiEnabled = enabled; }
}
