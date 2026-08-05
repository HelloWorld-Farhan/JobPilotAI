package com.jobpilotai.service;

import com.jobpilotai.config.AppConfig;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.repository.SettingsRepository;
import javafx.stage.Stage;

/**
 * Singleton service that manages all user-configurable application settings.
 * <p>
 * Settings are persisted in the SQLite {@code settings} table and cached in
 * memory for fast read access.
 * </p>
 *
 * @author JobPilotAI Team
 * @version 1.0.0
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

    private SettingsService() {}

    public static synchronized SettingsService getInstance() {
        if (instance == null) instance = new SettingsService();
        return instance;
    }

    /** Loads all settings from the database into the in-memory cache. */
    public void load() {
        AppLogger.info("Loading settings…");
        theme                = repo.get(AppConfig.SETTING_THEME,         AppConfig.DEFAULT_THEME);
        darkMode             = bool(repo.get(AppConfig.SETTING_DARK_MODE,   "true"));
        resumePath           = repo.get(AppConfig.SETTING_RESUME_PATH,    "");
        defaultEmail         = repo.get(AppConfig.SETTING_DEFAULT_EMAIL,  "");
        gasUrl               = repo.get(AppConfig.SETTING_GAS_URL,        "");
        reportFolder         = repo.get(AppConfig.SETTING_REPORT_FOLDER,  "");
        logFolder            = repo.get(AppConfig.SETTING_LOG_FOLDER,     "");
        enableNotifications  = bool(repo.get(AppConfig.SETTING_ENABLE_NOTIF,  "true"));
        enableEmail          = bool(repo.get(AppConfig.SETTING_ENABLE_EMAIL,  "false"));
        autoSave             = bool(repo.get(AppConfig.SETTING_AUTO_SAVE,     "true"));
        autoGenerateReports  = bool(repo.get(AppConfig.SETTING_AUTO_REPORTS,  "false"));
        rememberWindowSize   = bool(repo.get(AppConfig.SETTING_REMEMBER_SIZE, "true"));
        rememberWindowPosition = bool(repo.get(AppConfig.SETTING_REMEMBER_POS,"true"));
        AppLogger.info("Settings loaded. Theme=" + theme);
    }

    /** Saves all in-memory settings back to the database. */
    public void save() {
        repo.set(AppConfig.SETTING_THEME,         theme);
        repo.set(AppConfig.SETTING_DARK_MODE,      String.valueOf(darkMode));
        repo.set(AppConfig.SETTING_RESUME_PATH,    resumePath);
        repo.set(AppConfig.SETTING_DEFAULT_EMAIL,  defaultEmail);
        repo.set(AppConfig.SETTING_GAS_URL,        gasUrl);
        repo.set(AppConfig.SETTING_REPORT_FOLDER,  reportFolder);
        repo.set(AppConfig.SETTING_LOG_FOLDER,     logFolder);
        repo.set(AppConfig.SETTING_ENABLE_NOTIF,   String.valueOf(enableNotifications));
        repo.set(AppConfig.SETTING_ENABLE_EMAIL,   String.valueOf(enableEmail));
        repo.set(AppConfig.SETTING_AUTO_SAVE,      String.valueOf(autoSave));
        repo.set(AppConfig.SETTING_AUTO_REPORTS,   String.valueOf(autoGenerateReports));
        repo.set(AppConfig.SETTING_REMEMBER_SIZE,  String.valueOf(rememberWindowSize));
        repo.set(AppConfig.SETTING_REMEMBER_POS,   String.valueOf(rememberWindowPosition));
        AppLogger.info("Settings saved.");
    }

    /** Persists the current window size and position. */
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

    /** Restores window size and position from the database. */
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

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean bool(String v) {
        return "true".equalsIgnoreCase(v);
    }

    private double doubleSetting(String key, double def) {
        String v = repo.get(key, null);
        if (v == null) return def;
        try { return Double.parseDouble(v); } catch (NumberFormatException e) { return def; }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────

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

    public boolean isEnableNotifications()          { return enableNotifications; }
    public void    setEnableNotifications(boolean v){ this.enableNotifications = v; }

    public boolean isEnableEmail()           { return enableEmail; }
    public void    setEnableEmail(boolean v) { this.enableEmail = v; }

    public boolean isAutoSave()              { return autoSave; }
    public void    setAutoSave(boolean v)    { this.autoSave = v; }

    public boolean isAutoGenerateReports()          { return autoGenerateReports; }
    public void    setAutoGenerateReports(boolean v){ this.autoGenerateReports = v; }

    public boolean isRememberWindowSize()           { return rememberWindowSize; }
    public void    setRememberWindowSize(boolean v) { this.rememberWindowSize = v; }

    public boolean isRememberWindowPosition()           { return rememberWindowPosition; }
    public void    setRememberWindowPosition(boolean v) { this.rememberWindowPosition = v; }
}
