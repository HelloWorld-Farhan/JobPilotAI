package com.jobpilotai.automation.workflow;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Handles human verification loops (CAPTCHA, OTP, etc).
 */
public class VerificationHandler {
    private static VerificationHandler instance;

    private VerificationHandler() {}

    public static synchronized VerificationHandler getInstance() {
        if (instance == null) {
            instance = new VerificationHandler();
        }
        return instance;
    }

    public void requireVerification(String taskId, String reason, String screenshotPath) {
        AppLogger.warn("Human verification required: " + reason);
        
        // Log event to DB
        String sql = "INSERT INTO verification_events (task_id, reason, screenshot) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, taskId);
            pstmt.setString(2, reason);
            pstmt.setString(3, screenshotPath);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.error("Failed to log verification event", e);
        }

        // In a full implementation, this would trigger Windows Notification and Email dispatch.
        // For now, we pause the WorkflowEngine so the user can interact via the UI.
        WorkflowEngine.getInstance().pause();
        WorkflowEngine.getInstance().setState("Waiting for User Verification");
    }

    public void verificationCompleted() {
        AppLogger.info("Human verification completed by user. Resuming workflow.");
        WorkflowEngine.getInstance().resume();
    }
}
