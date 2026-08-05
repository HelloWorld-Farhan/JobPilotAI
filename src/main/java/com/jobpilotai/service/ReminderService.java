package com.jobpilotai.service;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderService {
    private static ReminderService instance;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private ReminderService() {
        startPolling();
    }

    public static synchronized ReminderService getInstance() {
        if (instance == null) {
            instance = new ReminderService();
        }
        return instance;
    }

    private void startPolling() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkReminders();
            } catch (Exception e) {
                AppLogger.error("Error in reminder polling", e);
            }
        }, 1, 15, TimeUnit.MINUTES);
    }

    private void checkReminders() {
        String sql = "SELECT id, title, description FROM reminders WHERE due_date <= ? AND is_completed = 0";
        String now = LocalDateTime.now().toString();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, now);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    String desc = rs.getString("description");
                    int id = rs.getInt("id");
                    
                    AppLogger.info("REMINDER: " + title + " - " + desc);
                    
                    // TODO: Trigger actual Windows Notification here using System tray
                    markCompleted(id);
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to check reminders", e);
        }
    }

    private void markCompleted(int id) {
        String sql = "UPDATE reminders SET is_completed = 1 WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.error("Failed to mark reminder completed", e);
        }
    }
}
