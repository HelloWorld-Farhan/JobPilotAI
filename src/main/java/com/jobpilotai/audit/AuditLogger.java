package com.jobpilotai.audit;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Creates immutable audit records for sensitive actions.
 */
public class AuditLogger {

    /**
     * Logs an audit event to the database.
     *
     * @param action  The action performed (e.g., "SETTINGS_CHANGED", "BACKUP_CREATED")
     * @param details Additional context or details about the action.
     */
    public static void log(String action, String details) {
        // Run asynchronously so we don't block the calling thread (e.g., UI or I/O)
        Thread auditThread = new Thread(() -> {
            String sql = "INSERT INTO audit_logs (action, details, user_profile) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                 
                pstmt.setString(1, action);
                pstmt.setString(2, details);
                // In Phase 2 this will resolve the active profile name dynamically
                pstmt.setString(3, "Admin");
                
                pstmt.executeUpdate();
                
                // Fallback to standard logging as well
                AppLogger.info("AUDIT: [" + action + "] - " + details);
                
            } catch (SQLException e) {
                AppLogger.error("Failed to write to audit log", e);
            }
        });
        auditThread.setDaemon(true);
        auditThread.start();
    }
}
