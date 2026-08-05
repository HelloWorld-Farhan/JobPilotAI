package com.jobpilotai.controller;

import com.jobpilotai.automation.queue.QueueService;
import com.jobpilotai.automation.workflow.WorkflowEngine;
import com.jobpilotai.model.QueueItem;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class AutomationController implements Initializable {

    @FXML private Label statusLabel;
    @FXML private Label currentTaskLabel;
    @FXML private Label queueSizeLabel;
    @FXML private javafx.scene.control.TextField tfJobUrl;

    @FXML private TableView<QueueItem> queueTable;
    @FXML private TableColumn<QueueItem, String> colCompany;
    @FXML private TableColumn<QueueItem, String> colJobTitle;
    @FXML private TableColumn<QueueItem, String> colStatus;
    @FXML private TableColumn<QueueItem, Number> colRetries;

    private ObservableList<QueueItem> queueItems = FXCollections.observableArrayList();
    private Timeline refreshTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        refreshData();

        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> refreshData()));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    private void setupTable() {
        colCompany.setCellValueFactory(cellData -> cellData.getValue().companyProperty());
        colJobTitle.setCellValueFactory(cellData -> cellData.getValue().jobTitleProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        colRetries.setCellValueFactory(cellData -> cellData.getValue().retryCountProperty());

        queueTable.setItems(queueItems);
    }

    private void refreshData() {
        queueItems.setAll(QueueService.getInstance().getQueue());
        statusLabel.setText("Status: " + WorkflowEngine.getInstance().getState());
        QueueItem current = WorkflowEngine.getInstance().getCurrentTask();
        if (current != null) {
            currentTaskLabel.setText(current.getCompany() + " - " + current.getJobTitle());
        } else {
            currentTaskLabel.setText("None");
        }
        queueSizeLabel.setText("Queue Size: " + queueItems.size());
    }

    @FXML private void onStart() {
        WorkflowEngine.getInstance().start();
        refreshData();
    }

    @FXML private void onPause() {
        WorkflowEngine.getInstance().pause();
        refreshData();
    }

    @FXML private void onResume() {
        WorkflowEngine.getInstance().resume();
        refreshData();
    }

    @FXML private void onStop() {
        WorkflowEngine.getInstance().stop();
        refreshData();
    }
    
    @FXML private void onClearQueue() {
        QueueService.getInstance().clearQueue();
        refreshData();
    }
    
    @FXML private void onAddJob() {
        String url = tfJobUrl.getText();
        if (url == null || url.trim().isEmpty()) {
            com.jobpilotai.utils.DialogUtils.showAlert("Error", "Please paste a job URL first.");
            return;
        }
        
        String website = url.contains("linkedin.com") ? "LinkedIn" : 
                        (url.contains("naukri.com") ? "Naukri" : 
                        (url.contains("indeed.com") ? "Indeed" : "Other"));
                        
        QueueService.getInstance().addJob(website, "Unknown Company", "Manual Task", url.trim());
        tfJobUrl.clear();
        refreshData();
    }
}
