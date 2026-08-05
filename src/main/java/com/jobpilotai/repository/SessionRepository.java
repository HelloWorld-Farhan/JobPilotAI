package com.jobpilotai.repository;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.SavedSession;

import java.sql.*;
import java.util.Optional;

/**
 * Data Access Object for {@link SavedSession} entities.
 * Only the most recent session is kept.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class SessionRepository {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    /** Saves a new session, deleting all previous ones (only 1 session kept). */
    public void save(SavedSession session) throws SQLException {
        try (Statement stmt = getConn().createStatement()) {
            stmt.execute("DELETE FROM saved_sessions");
        }
        try (PreparedStatement ps = getConn().prepareStatement(
                "INSERT INTO saved_sessions (data_json) VALUES (?)")) {
            ps.setString(1, session.getDataJson());
            ps.executeUpdate();
        }
    }

    /** Returns the most recent saved session if one exists. */
    public Optional<SavedSession> findLatest() {
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT * FROM saved_sessions ORDER BY id DESC LIMIT 1")) {
            if (rs.next()) {
                SavedSession s = new SavedSession();
                s.setId      (rs.getInt   ("id"));
                s.setDataJson(rs.getString("data_json"));
                s.setSavedAt (rs.getString("saved_at"));
                return Optional.of(s);
            }
        } catch (SQLException e) {
            AppLogger.error("SessionRepository.findLatest failed.", e);
        }
        return Optional.empty();
    }

    /** Clears all saved sessions. */
    public void clearAll() throws SQLException {
        try (Statement stmt = getConn().createStatement()) {
            stmt.execute("DELETE FROM saved_sessions");
        }
    }
}
