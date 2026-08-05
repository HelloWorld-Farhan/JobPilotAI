package com.jobpilotai.service;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.JobApplication;
import com.jobpilotai.repository.ApplicationRepository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Business service for job application management.
 * <p>
 * Orchestrates validation, repository access, and logging for all
 * application CRUD operations.
 * </p>
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class ApplicationService {

    private static ApplicationService instance;
    private final ApplicationRepository repo = new ApplicationRepository();

    private ApplicationService() {}

    public static synchronized ApplicationService getInstance() {
        if (instance == null) instance = new ApplicationService();
        return instance;
    }

    /**
     * Saves a new job application after validating required fields.
     *
     * @param app the application to save
     * @return the saved application with generated ID
     * @throws IllegalArgumentException if required fields are missing
     * @throws SQLException on database error
     */
    public JobApplication add(JobApplication app) throws SQLException {
        validate(app);
        JobApplication saved = repo.save(app);
        AppLogger.info("Application added: " + saved.getCompany() + " – " + saved.getJobTitle());
        return saved;
    }

    /**
     * Updates an existing application.
     *
     * @param app the application to update
     * @throws SQLException on database error
     */
    public void update(JobApplication app) throws SQLException {
        validate(app);
        repo.update(app);
        AppLogger.info("Application updated id=" + app.getId());
    }

    /**
     * Deletes a single application by ID.
     *
     * @param id the application ID
     * @throws SQLException on database error
     */
    public void delete(int id) throws SQLException {
        repo.deleteById(id);
        AppLogger.info("Application deleted id=" + id);
    }

    /** Deletes all applications. */
    public void deleteAll() throws SQLException {
        repo.deleteAll();
        AppLogger.warn("All applications deleted.");
    }

    /** Returns all applications. */
    public List<JobApplication> getAll() {
        try {
            return repo.findAll();
        } catch (SQLException e) {
            AppLogger.error("ApplicationService.getAll failed.", e);
            return Collections.emptyList();
        }
    }

    /** Returns a single application by ID. */
    public Optional<JobApplication> getById(int id) {
        try {
            return repo.findById(id);
        } catch (SQLException e) {
            AppLogger.error("ApplicationService.getById failed for id=" + id, e);
            return Optional.empty();
        }
    }

    /** Returns all applications for today. */
    public List<JobApplication> getToday() {
        try {
            return repo.findByDate(java.time.LocalDate.now().toString());
        } catch (SQLException e) {
            AppLogger.error("ApplicationService.getToday failed.", e);
            return Collections.emptyList();
        }
    }

    /** Full-text search. */
    public List<JobApplication> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        try {
            return repo.search(query);
        } catch (SQLException e) {
            AppLogger.error("ApplicationService.search failed.", e);
            return Collections.emptyList();
        }
    }

    /** Returns applications filtered by status. */
    public List<JobApplication> getByStatus(String status) {
        try {
            return repo.findByStatus(status);
        } catch (SQLException e) {
            AppLogger.error("ApplicationService.getByStatus failed.", e);
            return Collections.emptyList();
        }
    }

    // ── Statistics ──────────────────────────────────────────────────────────

    public int countToday()                  { try { return repo.countToday(); } catch (SQLException e) { return 0; } }
    public int countTodaySuccess()           { try { return repo.countTodayByStatus("Success"); } catch (SQLException e) { return 0; } }
    public int countTodayAlreadyApplied()    { try { return repo.countTodayByStatus("Already Applied"); } catch (SQLException e) { return 0; } }
    public int countTodayFailed()            { try { return repo.countTodayByStatus("Failed"); } catch (SQLException e) { return 0; } }
    public int countTodayPendingOtp()        { try { return repo.countTodayByStatus("Pending OTP"); } catch (SQLException e) { return 0; } }
    public int countTodayPendingCaptcha()    { try { return repo.countTodayByStatus("Pending CAPTCHA"); } catch (SQLException e) { return 0; } }
    public int countAll()                    { try { return repo.countAll(); } catch (SQLException e) { return 0; } }

    // ── Validation ──────────────────────────────────────────────────────────

    private void validate(JobApplication app) {
        if (app == null)                                 throw new IllegalArgumentException("Application cannot be null.");
        if (app.getCompany()  == null || app.getCompany().isBlank())  throw new IllegalArgumentException("Company name is required.");
        if (app.getJobTitle() == null || app.getJobTitle().isBlank()) throw new IllegalArgumentException("Job title is required.");
    }
}
