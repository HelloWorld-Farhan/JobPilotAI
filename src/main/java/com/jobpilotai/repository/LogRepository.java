package com.jobpilotai.repository;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.model.LogEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link LogEntry} entities.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class LogRepository {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    public void save(LogEntry entry) throws SQLException {
        String sql = "INSERT INTO logs (level, message) VALUES (?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, entry.getLevel());
            ps.setString(2, entry.getMessage());
            ps.executeUpdate();
        }
    }

    public List<LogEntry> findAll() throws SQLException {
        List<LogEntry> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM logs ORDER BY id DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<LogEntry> findByLevel(String level) throws SQLException {
        List<LogEntry> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT * FROM logs WHERE level=? ORDER BY id DESC")) {
            ps.setString(1, level);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<LogEntry> search(String query) throws SQLException {
        List<LogEntry> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT * FROM logs WHERE message LIKE ? ORDER BY id DESC")) {
            ps.setString(1, "%" + query + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void deleteAll() throws SQLException {
        try (Statement stmt = getConn().createStatement()) {
            stmt.execute("DELETE FROM logs");
        }
    }

    private LogEntry map(ResultSet rs) throws SQLException {
        LogEntry e = new LogEntry();
        e.setId       (rs.getInt   ("id"));
        e.setLevel    (rs.getString("level"));
        e.setMessage  (rs.getString("message"));
        e.setTimestamp(rs.getString("timestamp"));
        return e;
    }
}
