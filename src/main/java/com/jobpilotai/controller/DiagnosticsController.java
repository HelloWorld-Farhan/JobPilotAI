package com.jobpilotai.controller;

import com.jobpilotai.diagnostics.DiagnosticsService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class DiagnosticsController implements Initializable {

    @FXML private Label lblJavaVersion;
    @FXML private Label lblMemoryUsed;
    @FXML private Label lblMemoryMax;
    @FXML private ProgressBar pbMemory;
    
    @FXML private Label lblDbSize;
    @FXML private Label lblDbStatus;

    private Timer timer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startPolling();
    }

    private void startPolling() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DiagnosticsService.SystemHealth health = DiagnosticsService.getHealth();
                Platform.runLater(() -> updateUi(health));
            }
        }, 0, 2000);
    }

    private void updateUi(DiagnosticsService.SystemHealth health) {
        lblJavaVersion.setText("Java " + health.javaVersion);
        lblMemoryUsed.setText(health.allocatedMemoryMb + " MB");
        lblMemoryMax.setText(health.maxMemoryMb + " MB");
        
        double ratio = (double) health.allocatedMemoryMb / health.maxMemoryMb;
        pbMemory.setProgress(ratio);
        
        if (ratio > 0.8) {
            pbMemory.setStyle("-fx-accent: red;");
        } else {
            pbMemory.setStyle("-fx-accent: #3498db;");
        }
        
        lblDbSize.setText(health.dbSizeKb + " KB");
        lblDbStatus.setText(health.dbHealthy ? "Healthy" : "Error");
        if (health.dbHealthy) {
            lblDbStatus.setStyle("-fx-text-fill: #2ecc71;");
        } else {
            lblDbStatus.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
    
    public void shutdown() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
