package com.jobpilotai.service;

import com.jobpilotai.config.AppConfig;
import com.jobpilotai.config.PathConfig;
import com.jobpilotai.excel.ExcelReportGenerator;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.JobApplication;
import com.jobpilotai.model.Report;
import com.jobpilotai.repository.ReportRepository;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Business service for Excel report generation.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class ReportService {

    private static ReportService instance;
    private final ReportRepository   repo      = new ReportRepository();
    private final ApplicationService appService = ApplicationService.getInstance();

    private ReportService() {}

    public static synchronized ReportService getInstance() {
        if (instance == null) instance = new ReportService();
        return instance;
    }

    /**
     * Generates a manual report for all tracked applications.
     *
     * @return the created {@link Report} metadata
     * @throws Exception if generation or persistence fails
     */
    public Report generateManualReport() throws Exception {
        return generate(AppConfig.REPORT_MANUAL, appService.getAll());
    }

    /**
     * Generates a final/summary report.
     *
     * @return the created {@link Report} metadata
     * @throws Exception if generation or persistence fails
     */
    public Report generateFinalReport() throws Exception {
        return generate(AppConfig.REPORT_FINAL, appService.getAll());
    }

    /**
     * Generates a report for today's applications only.
     *
     * @return the created {@link Report} metadata
     * @throws Exception if generation or persistence fails
     */
    public Report generateHourlyReport() throws Exception {
        return generate(AppConfig.REPORT_HOURLY, appService.getToday());
    }

    private Report generate(String type, List<JobApplication> apps) throws Exception {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(AppConfig.DATETIME_FORMAT));
        Path filePath = PathConfig.getReportFile(type, timestamp);

        AppLogger.info("Generating " + type + " report: " + filePath.getFileName());

        ExcelReportGenerator.generate(apps, filePath.toFile(), type);

        Report report = new Report(filePath.getFileName().toString(),
                filePath.toAbsolutePath().toString(), type);
        Report saved = repo.save(report);

        AppLogger.info("Report saved: " + saved.getFilepath());
        return saved;
    }

    /** Returns all stored report records. */
    public List<Report> getAllReports() {
        try {
            return repo.findAll();
        } catch (SQLException e) {
            AppLogger.error("ReportService.getAllReports failed.", e);
            return Collections.emptyList();
        }
    }

    /** Deletes a report record from the database (does NOT delete the file). */
    public void deleteReport(int id) {
        try {
            repo.deleteById(id);
        } catch (SQLException e) {
            AppLogger.error("ReportService.deleteReport failed for id=" + id, e);
        }
    }
}
