package com.jobpilotai.config;

/**
 * Centralised application-wide constants for JobPilotAI.
 * <p>
 * All magic strings and numbers used across the application should be defined
 * here so that future maintenance requires changes in exactly one place.
 * </p>
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public final class AppConfig {

    private AppConfig() { /* utility class */ }

    // ── Identity ────────────────────────────────────────────────────────────
    public static final String APP_NAME    = "JobPilotAI";
    public static final String APP_VERSION = "v1.0.0";
    public static final String APP_AUTHOR  = "JobPilotAI Team";

    // ── Window Dimensions ───────────────────────────────────────────────────
    public static final double MIN_WIDTH     = 1024;
    public static final double MIN_HEIGHT    = 700;
    public static final double DEFAULT_WIDTH = 1200;
    public static final double DEFAULT_HEIGHT = 800;

    // ── Database ─────────────────────────────────────────────────────────────
    public static final String DB_FILE_NAME = "jobpilotai.db";
    public static final int    DB_VERSION   = 4;

    // ── Settings keys ────────────────────────────────────────────────────────
    public static final String SETTING_THEME              = "theme";
    public static final String SETTING_DARK_MODE          = "dark_mode";
    public static final String SETTING_RESUME_PATH        = "resume_path";
    public static final String SETTING_DEFAULT_EMAIL      = "default_email";
    public static final String SETTING_GAS_URL            = "gas_url";
    public static final String SETTING_REPORT_FOLDER      = "report_folder";
    public static final String SETTING_LOG_FOLDER         = "log_folder";
    public static final String SETTING_ENABLE_NOTIF       = "enable_notifications";
    public static final String SETTING_ENABLE_EMAIL       = "enable_email";
    public static final String SETTING_AUTO_SAVE          = "auto_save";
    public static final String SETTING_AUTO_REPORTS       = "auto_reports";
    public static final String SETTING_REMEMBER_SIZE      = "remember_window_size";
    public static final String SETTING_REMEMBER_POS       = "remember_window_position";
    public static final String SETTING_WINDOW_X           = "window_x";
    public static final String SETTING_WINDOW_Y           = "window_y";
    public static final String SETTING_WINDOW_W           = "window_width";
    public static final String SETTING_WINDOW_H           = "window_height";
    public static final String SETTING_HEADLESS           = "headless_mode";
    public static final String SETTING_SCREENSHOT_ERROR   = "screenshot_on_error";
    public static final String SETTING_TIMEOUT            = "automation_timeout";
    public static final String SETTING_MAX_RETRIES        = "max_retries";
    public static final String SETTING_GEMINI_API_KEY     = "gemini_api_key";
    public static final String SETTING_AI_ENABLED         = "ai_enabled";

    // ── Defaults ─────────────────────────────────────────────────────────────
    public static final String DEFAULT_THEME         = "dark";
    public static final boolean DEFAULT_DARK_MODE    = true;
    public static final boolean DEFAULT_NOTIFICATIONS = true;
    public static final boolean DEFAULT_EMAIL        = false;
    public static final boolean DEFAULT_AUTO_SAVE    = true;
    public static final boolean DEFAULT_AUTO_REPORTS = false;
    public static final boolean DEFAULT_HEADLESS     = false;
    public static final boolean DEFAULT_SCREENSHOT_ERROR = true;
    public static final int     DEFAULT_TIMEOUT      = 30000;
    public static final int     DEFAULT_MAX_RETRIES  = 3;
    public static final boolean DEFAULT_AI_ENABLED   = false;

    // ── Report types ─────────────────────────────────────────────────────────
    public static final String REPORT_MANUAL  = "MANUAL";
    public static final String REPORT_HOURLY  = "HOURLY";
    public static final String REPORT_FINAL   = "FINAL";

    // ── Application statuses ─────────────────────────────────────────────────
    public static final String STATUS_SUCCESS        = "Success";
    public static final String STATUS_ALREADY_APPLIED = "Already Applied";
    public static final String STATUS_FAILED         = "Failed";
    public static final String STATUS_PENDING_OTP    = "Pending OTP";
    public static final String STATUS_PENDING_CAPTCHA = "Pending CAPTCHA";
    public static final String STATUS_PENDING        = "Pending";

    // ── Date / Time formats ──────────────────────────────────────────────────
    public static final String DATE_FORMAT       = "yyyy-MM-dd";
    public static final String TIME_FORMAT       = "HH:mm:ss";
    public static final String DATETIME_FORMAT   = "yyyy-MM-dd_HH-mm-ss";
    public static final String LOG_DATE_FORMAT   = "yyyy-MM-dd";
    public static final String DISPLAY_DATE_FMT  = "dd MMM yyyy";
    public static final String DISPLAY_TIME_FMT  = "HH:mm:ss";

    // ── Pagination ───────────────────────────────────────────────────────────
    public static final int PAGE_SIZE = 25;
}
