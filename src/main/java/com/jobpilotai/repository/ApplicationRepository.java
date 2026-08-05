package com.jobpilotai.repository;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.JobApplication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link JobApplication} entities.
 * <p>
 * All SQL operations use prepared statements to prevent SQL injection.
 * </p>
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class ApplicationRepository {

    private Connection getConn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * Inserts a new application and returns it with its generated ID.
     *
     * @param app the application to insert
     * @return the saved application with {@code id} populated
     * @throws SQLException on database error
     */
    public JobApplication save(JobApplication app) throws SQLException {
        String sql = """
            INSERT INTO applications
                (company, job_title, website, job_url, status, date, time,
                 resume_used, notes, attempt_count)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, app.getCompany());
            ps.setString(2, app.getJobTitle());
            ps.setString(3, app.getWebsite());
            ps.setString(4, app.getJobUrl());
            ps.setString(5, app.getStatus());
            ps.setString(6, app.getDate());
            ps.setString(7, app.getTime());
            ps.setString(8, app.getResumeUsed());
            ps.setString(9, app.getNotes());
            ps.setInt   (10, app.getAttemptCount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) app.setId(keys.getInt(1));
            }
        }
        AppLogger.debug("Saved application id=" + app.getId());
        return app;
    }

    /**
     * Updates an existing application.
     *
     * @param app the application to update (must have a valid id)
     * @throws SQLException on database error
     */
    public void update(JobApplication app) throws SQLException {
        String sql = """
            UPDATE applications SET
                company=?, job_title=?, website=?, job_url=?, status=?,
                date=?, time=?, resume_used=?, notes=?, attempt_count=?
            WHERE id=?
        """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1,  app.getCompany());
            ps.setString(2,  app.getJobTitle());
            ps.setString(3,  app.getWebsite());
            ps.setString(4,  app.getJobUrl());
            ps.setString(5,  app.getStatus());
            ps.setString(6,  app.getDate());
            ps.setString(7,  app.getTime());
            ps.setString(8,  app.getResumeUsed());
            ps.setString(9,  app.getNotes());
            ps.setInt   (10, app.getAttemptCount());
            ps.setInt   (11, app.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Deletes an application by ID.
     *
     * @param id the application ID
     * @throws SQLException on database error
     */
    public void deleteById(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement(
                "DELETE FROM applications WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Deletes all applications. */
    public void deleteAll() throws SQLException {
        try (Statement stmt = getConn().createStatement()) {
            stmt.execute("DELETE FROM applications");
        }
    }

    /**
     * Finds an application by its ID.
     *
     * @param id the application ID
     * @return an {@link Optional} containing the application if found
     * @throws SQLException on database error
     */
    public Optional<JobApplication> findById(int id) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT * FROM applications WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Returns all applications ordered by date descending.
     *
     * @return list of all applications
     * @throws SQLException on database error
     */
    public List<JobApplication> findAll() throws SQLException {
        List<JobApplication> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT * FROM applications ORDER BY created_at DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /**
     * Returns all applications for a given date (ISO format {@code yyyy-MM-dd}).
     *
     * @param date the date string
     * @return list of matching applications
     * @throws SQLException on database error
     */
    public List<JobApplication> findByDate(String date) throws SQLException {
        List<JobApplication> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT * FROM applications WHERE date=? ORDER BY time")) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Returns all applications matching a given status.
     *
     * @param status the status string
     * @return list of matching applications
     * @throws SQLException on database error
     */
    public List<JobApplication> findByStatus(String status) throws SQLException {
        List<JobApplication> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT * FROM applications WHERE status=? ORDER BY created_at DESC")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /**
     * Full-text search across company, jobTitle, notes.
     *
     * @param query search term
     * @return matching applications
     * @throws SQLException on database error
     */
    public List<JobApplication> search(String query) throws SQLException {
        List<JobApplication> list = new ArrayList<>();
        String like = "%" + query + "%";
        String sql = """
            SELECT * FROM applications
            WHERE company LIKE ? OR job_title LIKE ? OR notes LIKE ? OR website LIKE ?
            ORDER BY created_at DESC
        """;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Returns the total count of all applications. */
    public int countAll() throws SQLException {
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM applications")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Returns count of applications for today's date. */
    public int countToday() throws SQLException {
        String today = java.time.LocalDate.now().toString();
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT COUNT(*) FROM applications WHERE date=?")) {
            ps.setString(1, today);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Returns count of applications with a given status today. */
    public int countTodayByStatus(String status) throws SQLException {
        String today = java.time.LocalDate.now().toString();
        try (PreparedStatement ps = getConn().prepareStatement(
                "SELECT COUNT(*) FROM applications WHERE date=? AND status=?")) {
            ps.setString(1, today);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    private JobApplication map(ResultSet rs) throws SQLException {
        JobApplication a = new JobApplication();
        a.setId           (rs.getInt   ("id"));
        a.setCompany      (rs.getString("company"));
        a.setJobTitle     (rs.getString("job_title"));
        a.setWebsite      (rs.getString("website"));
        a.setJobUrl       (rs.getString("job_url"));
        a.setStatus       (rs.getString("status"));
        a.setDate         (rs.getString("date"));
        a.setTime         (rs.getString("time"));
        a.setResumeUsed   (rs.getString("resume_used"));
        a.setNotes        (rs.getString("notes"));
        a.setAttemptCount (rs.getInt   ("attempt_count"));
        a.setCreatedAt    (rs.getString("created_at"));
        return a;
    }
}
