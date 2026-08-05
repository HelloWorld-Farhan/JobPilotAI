package com.jobpilotai.config;

import com.jobpilotai.logs.AppLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages all filesystem paths used by JobPilotAI.
 * <p>
 * All directories are created relative to the user's home directory under
 * {@code ~/JobPilotAI/} so the application is self-contained without requiring
 * administrative rights.
 * </p>
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public final class PathConfig {

    private PathConfig() { /* utility class */ }

    /** Root application data directory. */
    public static final Path APP_HOME = Paths.get(
            System.getProperty("user.home"), "JobPilotAI");

    public static final Path DATABASE_DIR  = APP_HOME.resolve("database");
    public static final Path REPORTS_DIR   = APP_HOME.resolve("reports");
    public static final Path LOGS_DIR      = APP_HOME.resolve("logs");
    public static final Path RESUME_DIR    = APP_HOME.resolve("resume");
    public static final Path SETTINGS_DIR  = APP_HOME.resolve("settings");
    public static final Path SCREENSHOTS_DIR = APP_HOME.resolve("screenshots");
    public static final Path TEMP_DIR      = APP_HOME.resolve("temp");
    public static final Path BACKUP_DIR    = APP_HOME.resolve("backup");

    /** Full path to the SQLite database file. */
    public static final Path DATABASE_FILE = DATABASE_DIR.resolve(AppConfig.DB_FILE_NAME);

    /**
     * Creates all required application directories if they do not already
     * exist. This method is safe to call multiple times.
     */
    public static void initializeDirectories() {
        Path[] dirs = {
            APP_HOME, DATABASE_DIR, REPORTS_DIR, LOGS_DIR,
            RESUME_DIR, SETTINGS_DIR, SCREENSHOTS_DIR, TEMP_DIR, BACKUP_DIR
        };

        for (Path dir : dirs) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                // Cannot use AppLogger here (it may not be initialised yet)
                System.err.println("[PathConfig] Failed to create directory: " + dir + " – " + e.getMessage());
            }
        }
    }

    /**
     * Returns the path to today's daily log file.
     *
     * @param date ISO date string (yyyy-MM-dd)
     * @return path to the log file
     */
    public static Path getDailyLogFile(String date) {
        return LOGS_DIR.resolve("jobpilotai-" + date + ".log");
    }

    /**
     * Returns a unique report file path that will never overwrite an existing
     * report by appending a timestamp.
     *
     * @param type     report type label (e.g. MANUAL, HOURLY, FINAL)
     * @param timestamp timestamp suffix
     * @return path to the new report file
     */
    public static Path getReportFile(String type, String timestamp) {
        String filename = "report-" + type.toLowerCase() + "-" + timestamp + ".xlsx";
        return REPORTS_DIR.resolve(filename);
    }
}
