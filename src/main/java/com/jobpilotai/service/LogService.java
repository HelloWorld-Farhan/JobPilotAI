package com.jobpilotai.service;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.LogEntry;
import com.jobpilotai.repository.LogRepository;

import java.util.Collections;
import java.util.List;

/**
 * Service for writing and reading log entries in the database.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class LogService {

    private static LogService instance;
    private final LogRepository repo = new LogRepository();

    private LogService() {}

    public static synchronized LogService getInstance() {
        if (instance == null) instance = new LogService();
        return instance;
    }

    public void log(String level, String message) {
        try {
            repo.save(new LogEntry(level, message));
        } catch (Exception e) {
            AppLogger.error("LogService.log failed.", e);
        }
    }

    public void info(String message)  { log("INFO",  message); }
    public void warn(String message)  { log("WARN",  message); }
    public void error(String message) { log("ERROR", message); }

    public List<LogEntry> getAll() {
        try { return repo.findAll(); }
        catch (Exception e) { AppLogger.error("LogService.getAll failed.", e); return Collections.emptyList(); }
    }

    public List<LogEntry> search(String query) {
        try { return repo.search(query); }
        catch (Exception e) { AppLogger.error("LogService.search failed.", e); return Collections.emptyList(); }
    }

    public List<LogEntry> getByLevel(String level) {
        try { return repo.findByLevel(level); }
        catch (Exception e) { AppLogger.error("LogService.getByLevel failed.", e); return Collections.emptyList(); }
    }

    public void clearAll() {
        try { repo.deleteAll(); AppLogger.warn("All logs cleared."); }
        catch (Exception e) { AppLogger.error("LogService.clearAll failed.", e); }
    }
}
