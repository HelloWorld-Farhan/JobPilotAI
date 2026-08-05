package com.jobpilotai.automation.workflow;

import com.jobpilotai.automation.browser.BrowserManager;
import com.jobpilotai.automation.queue.QueueService;
import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.QueueItem;
import com.jobpilotai.automation.strategy.JobApplyStrategy;
import com.jobpilotai.automation.strategy.LinkedInEasyApplyStrategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executes queued jobs in a background thread.
 */
public class WorkflowEngine {
    private static WorkflowEngine instance;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private Thread engineThread;
    
    private String currentState = "Idle";
    private QueueItem currentTask;

    private WorkflowEngine() {}

    public static synchronized WorkflowEngine getInstance() {
        if (instance == null) {
            instance = new WorkflowEngine();
        }
        return instance;
    }

    public synchronized void start() {
        if (isRunning.get()) return;
        
        isRunning.set(true);
        isPaused.set(false);
        setState("Running");
        
        engineThread = new Thread(this::runLoop);
        engineThread.setDaemon(true);
        engineThread.start();
        AppLogger.info("Workflow Engine started.");
    }

    public synchronized void pause() {
        isPaused.set(true);
        setState("Paused");
        AppLogger.info("Workflow Engine paused.");
    }

    public synchronized void resume() {
        isPaused.set(false);
        setState("Running");
        AppLogger.info("Workflow Engine resumed.");
    }

    public synchronized void stop() {
        isRunning.set(false);
        setState("Idle");
        if (engineThread != null) {
            engineThread.interrupt();
        }
        BrowserManager.getInstance().stop();
        AppLogger.info("Workflow Engine stopped.");
    }

    public String getState() {
        return currentState;
    }

    void setState(String state) {
        this.currentState = state;
        updateStateInDb();
    }

    public QueueItem getCurrentTask() {
        return currentTask;
    }

    private void runLoop() {
        while (isRunning.get()) {
            try {
                if (isPaused.get()) {
                    Thread.sleep(1000);
                    continue;
                }

                currentTask = QueueService.getInstance().getNextJob();
                
                if (currentTask == null) {
                    setState("Idle");
                    Thread.sleep(2000); // Wait for new jobs
                    continue;
                }

                setState("Running");
                executeTask(currentTask);

            } catch (InterruptedException e) {
                AppLogger.warn("Workflow Engine thread interrupted.");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                AppLogger.error("Unexpected error in Workflow Engine loop.", e);
            }
        }
    }

    private void executeTask(QueueItem task) {
        AppLogger.info("Executing task: " + task.getCompany() + " - " + task.getJobTitle());
        QueueService.getInstance().updateStatus(task.getId(), "In Progress");
        updateStateInDb();

        try {
            boolean navigated = BrowserManager.getInstance().navigate(task.getJobUrl());
            if (!navigated) {
                QueueService.getInstance().updateStatus(task.getId(), "Failed");
                logHistory(task, "Failed", "Navigation failed");
                return;
            }

            boolean success = false;
            
            if (task.getWebsite() != null && task.getWebsite().toLowerCase().contains("linkedin")) {
                if (task.getJobUrl().contains("/jobs/search/") || task.getJobUrl().contains("/search/results/")) {
                    JobApplyStrategy strategy = new com.jobpilotai.automation.strategy.LinkedInSearchStrategy();
                    success = strategy.apply(BrowserManager.getInstance().getPage(), task.getJobUrl());
                } else {
                    JobApplyStrategy strategy = new LinkedInEasyApplyStrategy();
                    success = strategy.apply(BrowserManager.getInstance().getPage(), task.getJobUrl());
                }
            } else {
                AppLogger.warn("No automation strategy found for website: " + task.getWebsite());
                // Fallback for unsupported sites (wait briefly, then fail)
                Thread.sleep(3000);
            }

            if (success) {
                QueueService.getInstance().updateStatus(task.getId(), "Completed");
                logHistory(task, "Completed", "Applied successfully via AI Bot");
            } else {
                QueueService.getInstance().updateStatus(task.getId(), "Failed");
                logHistory(task, "Failed", "Automation strategy returned false");
            }
        } catch (Exception e) {
            QueueService.getInstance().updateStatus(task.getId(), "Failed");
            QueueService.getInstance().incrementRetry(task.getId());
            AppLogger.error("Task failed execution: " + task.getId(), e);
            BrowserManager.getInstance().takeScreenshot("error_" + task.getId());
        } finally {
            currentTask = null;
        }
    }

    private void logHistory(QueueItem task, String status, String reason) {
        String sql = "INSERT INTO workflow_history (task_id, website, company, job_title, status, reason) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getId());
            pstmt.setString(2, task.getWebsite());
            pstmt.setString(3, task.getCompany());
            pstmt.setString(4, task.getJobTitle());
            pstmt.setString(5, status);
            pstmt.setString(6, reason);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.error("Failed to log workflow history", e);
        }
    }

    private void updateStateInDb() {
        String taskId = currentTask != null ? currentTask.getId() : null;
        String sql = "UPDATE automation_state SET state = ?, current_task = ?, updated_at = datetime('now') WHERE id = 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentState);
            pstmt.setString(2, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.error("Failed to persist automation state", e);
        }
    }
}
