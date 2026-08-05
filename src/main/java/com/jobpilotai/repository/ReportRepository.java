package com.jobpilotai.repository;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.model.Report;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link Report} entities.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class ReportRepository {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    public Report save(Report report) throws SQLException {
        String sql = "INSERT INTO reports (filename, filepath, type) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, report.getFilename());
            ps.setString(2, report.getFilepath());
            ps.setString(3, report.getType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) report.setId(keys.getInt(1));
            }
        }
        return report;
    }

    public List<Report> findAll() throws SQLException {
        List<Report> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM reports ORDER BY created_at DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void deleteById(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM reports WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteAll() throws SQLException {
        try (Statement stmt = getConn().createStatement()) {
            stmt.execute("DELETE FROM reports");
        }
    }

    private Report map(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setId       (rs.getInt   ("id"));
        r.setFilename (rs.getString("filename"));
        r.setFilepath (rs.getString("filepath"));
        r.setType     (rs.getString("type"));
        r.setCreatedAt(rs.getString("created_at"));
        return r;
    }
}
