package com.jobpilotai.automation.sessionmanager;

import com.jobpilotai.automation.queue.QueueService;
import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles recovering automation state if the application was closed unexpectedly.
 */
public class SessionRecovery {

    private SessionRecovery() {}

    public static void checkAndRecover() {
        AppLogger.info("Checking for aborted automation sessions...");
        
        String sql = "SELECT current_task, state FROM automation_state WHERE id = 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                String state = rs.getString("state");
                String currentTask = rs.getString("current_task");
                
                if (("Running".equals(state) || "Paused".equals(state)) && currentTask != null) {
                    AppLogger.warn("Found interrupted task from previous session: " + currentTask);
                    
                    // Fail the task that was running when app crashed, or pause it to let user decide
                    QueueService.getInstance().updateStatus(currentTask, "Failed (App Crashed)");
                    QueueService.getInstance().incrementRetry(currentTask);
                    
                    // Reset the state to Idle for the fresh boot
                    resetState();
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to check automation state", e);
        }
    }

    private static void resetState() {
        String sql = "UPDATE automation_state SET state = 'Idle', current_task = NULL WHERE id = 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
            AppLogger.info("Automation state reset to Idle.");
        } catch (SQLException e) {
            AppLogger.error("Failed to reset automation state", e);
        }
    }
}
