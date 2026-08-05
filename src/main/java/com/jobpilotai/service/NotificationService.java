package com.jobpilotai.service;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.service.SettingsService;

import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Notification service supporting Windows system tray notifications and
 * Google Apps Script email notifications.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class NotificationService {

    private static NotificationService instance;
    private TrayIcon trayIcon;

    private NotificationService() {
        initTray();
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) instance = new NotificationService();
        return instance;
    }

    /**
     * Initialises the system tray icon for Windows notifications.
     */
    private void initTray() {
        if (!SystemTray.isSupported()) {
            AppLogger.warn("System tray not supported on this platform.");
            return;
        }
        try {
            SystemTray tray = SystemTray.getSystemTray();
            // Use default AWT image as placeholder (replace with actual icon)
            Image image = Toolkit.getDefaultToolkit()
                    .createImage(getClass().getResource("/images/icon.png"));
            trayIcon = new TrayIcon(image, "JobPilotAI");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        } catch (Exception e) {
            AppLogger.warn("Could not initialise system tray: " + e.getMessage());
        }
    }

    /**
     * Displays a Windows system tray notification.
     *
     * @param title   notification title
     * @param message notification body
     */
    public void showWindowsNotification(String title, String message) {
        if (!SettingsService.getInstance().isEnableNotifications()) return;

        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            AppLogger.info("Notification shown: " + title);
        } else {
            AppLogger.warn("TrayIcon not available. Cannot show notification.");
        }
    }

    /**
     * Sends an email notification via a Google Apps Script Web App URL.
     *
     * @param subject email subject
     * @param body    email body text
     */
    public void sendEmailNotification(String subject, String body) {
        if (!SettingsService.getInstance().isEnableEmail()) return;

        String gasUrl = SettingsService.getInstance().getGasUrl();
        if (gasUrl == null || gasUrl.isBlank()) {
            AppLogger.warn("Google Apps Script URL not configured. Email skipped.");
            return;
        }

        // Fire-and-forget on a background thread
        Thread.ofVirtual().start(() -> {
            try {
                String defaultEmail = SettingsService.getInstance().getDefaultEmail();
                if (defaultEmail == null || defaultEmail.isBlank()) {
                    AppLogger.warn("Default Email not set in settings. Email skipped.");
                    return;
                }
                
                String payload = "{\"type\":\"alert\",\"to_email\":\"" + escape(defaultEmail) + 
                        "\",\"subject\":\"" + escape(subject) +
                        "\",\"body\":\"" + escape(body) + "\"}";

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(gasUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .timeout(Duration.ofSeconds(15))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                AppLogger.info("GAS email notification sent. Response: " + response.statusCode());
            } catch (Exception e) {
                AppLogger.error("Email notification failed.", e);
            }
        });
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    /** Convenience: show both notification types. */
    public void notify(String title, String message) {
        showWindowsNotification(title, message);
        sendEmailNotification(title, message);
    }
}
