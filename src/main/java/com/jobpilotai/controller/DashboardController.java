package com.jobpilotai.controller;

import com.jobpilotai.config.AppConfig;
import com.jobpilotai.viewmodel.DashboardViewModel;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the Dashboard panel showing live statistics and status.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class DashboardController implements Initializable {

    @FXML private Label statToday;
    @FXML private Label statSuccess;
    @FXML private Label statAlreadyApplied;
    @FXML private Label statFailed;
    @FXML private Label statPendingOtp;
    @FXML private Label statPendingCaptcha;
    @FXML private Label runningTaskLabel;
    @FXML private Label currentWebsiteLabel;
    @FXML private Label currentCompanyLabel;
    @FXML private Label currentJobLabel;
    @FXML private Label systemStatusLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label appVersionLabel;
    @FXML private ProgressBar progressBar;

    private final DashboardViewModel viewModel = new DashboardViewModel();
    private Timeline refreshTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bindProperties();
        viewModel.refresh();
        startAutoRefresh();
        appVersionLabel.setText(AppConfig.APP_NAME + " " + AppConfig.APP_VERSION);
        updateDateTime();
    }

    private void bindProperties() {
        statToday          .textProperty().bind(viewModel.applicationsTodayProperty().asString());
        statSuccess        .textProperty().bind(viewModel.successfulProperty().asString());
        statAlreadyApplied .textProperty().bind(viewModel.alreadyAppliedProperty().asString());
        statFailed         .textProperty().bind(viewModel.failedProperty().asString());
        statPendingOtp     .textProperty().bind(viewModel.pendingOtpProperty().asString());
        statPendingCaptcha .textProperty().bind(viewModel.pendingCaptchaProperty().asString());
        runningTaskLabel   .textProperty().bind(viewModel.runningTaskProperty());
        currentWebsiteLabel.textProperty().bind(viewModel.currentWebsiteProperty());
        currentCompanyLabel.textProperty().bind(viewModel.currentCompanyProperty());
        currentJobLabel    .textProperty().bind(viewModel.currentJobProperty());
        systemStatusLabel  .textProperty().bind(viewModel.systemStatusProperty());
        progressBar        .progressProperty().bind(viewModel.progressProperty());
    }

    private void startAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            viewModel.refresh();
            updateDateTime();
        }));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    private void updateDateTime() {
        String dt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  •  HH:mm:ss"));
        dateTimeLabel.setText(dt);
    }
}
