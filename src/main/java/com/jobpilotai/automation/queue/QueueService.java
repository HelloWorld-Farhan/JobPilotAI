package com.jobpilotai.automation.queue;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.QueueItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages the automation queue in SQLite.
 */
public class QueueService {
    private static QueueService instance;

    private QueueService() {}

    public static synchronized QueueService getInstance() {
        if (instance == null) {
            instance = new QueueService();
        }
        return instance;
    }

    public void addJob(String website, String company, String jobTitle, String jobUrl) {
        String sql = """
            INSERT INTO automation_queue (id, website, company, job_title, job_url, queue_position)
            VALUES (?, ?, ?, ?, ?, (SELECT IFNULL(MAX(queue_position), 0) + 1 FROM automation_queue))
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, website);
            pstmt.setString(3, company);
            pstmt.setString(4, jobTitle);
            pstmt.setString(5, jobUrl);
            pstmt.executeUpdate();
            AppLogger.info("Added job to queue: " + company + " - " + jobTitle);
        } catch (SQLException e) {
            AppLogger.error("Failed to add job to queue", e);
        }
    }

    public List<QueueItem> getQueue() {
        List<QueueItem> list = new ArrayList<>();
        String sql = "SELECT * FROM automation_queue ORDER BY queue_position ASC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(new QueueItem(
                    rs.getString("id"),
                    rs.getString("website"),
                    rs.getString("company"),
                    rs.getString("job_title"),
                    rs.getString("job_url"),
                    rs.getInt("queue_position"),
                    rs.getString("status"),
                    rs.getInt("retry_count"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
                ));
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to load queue", e);
        }
        return list;
    }

    public QueueItem getNextJob() {
        String sql = "SELECT * FROM automation_queue WHERE status = 'Queued' OR (status = 'Failed' AND retry_count < 3) ORDER BY queue_position ASC LIMIT 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return new QueueItem(
                    rs.getString("id"),
                    rs.getString("website"),
                    rs.getString("company"),
                    rs.getString("job_title"),
                    rs.getString("job_url"),
                    rs.getInt("queue_position"),
                    rs.getString("status"),
                    rs.getInt("retry_count"),
                    rs.getString("created_at"),
                    rs.getString("updated_at")
                );
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to get next job", e);
        }
        return null;
    }

    public void updateStatus(String id, String status) {
        String sql = "UPDATE automation_queue SET status = ?, updated_at = datetime('now') WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.error("Failed to update job status", e);
        }
    }
    
    public void incrementRetry(String id) {
        String sql = "UPDATE automation_queue SET retry_count = retry_count + 1, updated_at = datetime('now') WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.error("Failed to increment retry", e);
        }
    }

    public void removeJob(String id) {
        String sql = "DELETE FROM automation_queue WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.error("Failed to remove job from queue", e);
        }
    }

    public void clearQueue() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM automation_queue")) {
            pstmt.executeUpdate();
            AppLogger.info("Queue cleared.");
        } catch (SQLException e) {
            AppLogger.error("Failed to clear queue", e);
        }
    }
}
