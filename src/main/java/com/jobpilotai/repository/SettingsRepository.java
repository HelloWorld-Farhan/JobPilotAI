package com.jobpilotai.repository;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for key/value application settings.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class SettingsRepository {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    /**
     * Returns the value for the given key, or {@code defaultValue} if not found.
     *
     * @param key          settings key
     * @param defaultValue fallback value
     * @return the stored value or defaultValue
     */
    public String get(String key, String defaultValue) {
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT value FROM settings WHERE key=?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("value") : defaultValue;
            }
        } catch (SQLException e) {
            AppLogger.error("SettingsRepository.get failed for key=" + key + ": " + e.getMessage(), e);
            return defaultValue;
        }
    }

    /**
     * Inserts or replaces a setting.
     *
     * @param key   settings key
     * @param value settings value
     */
    public void set(String key, String value) {
        try (PreparedStatement ps = getConn().prepareStatement(
                "INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?)")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            AppLogger.error("SettingsRepository.set failed for key=" + key + ": " + e.getMessage(), e);
        }
    }

    /** Loads all settings as a map. */
    public Map<String, String> loadAll() {
        Map<String, String> map = new HashMap<>();
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT key, value FROM settings")) {
            while (rs.next()) {
                map.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            AppLogger.error("SettingsRepository.loadAll failed: " + e.getMessage(), e);
        }
        return map;
    }

    /** Deletes a single setting entry. */
    public void delete(String key) {
        try (PreparedStatement ps = getConn().prepareStatement(
                "DELETE FROM settings WHERE key=?")) {
            ps.setString(1, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            AppLogger.error("SettingsRepository.delete failed for key=" + key, e);
        }
    }
}
