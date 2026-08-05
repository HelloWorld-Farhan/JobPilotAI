package com.jobpilotai.controller;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.Report;
import com.jobpilotai.viewmodel.ReportViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Reports panel.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class ReportsController implements Initializable {

    @FXML private TableView<Report>               table;
    @FXML private TableColumn<Report, Integer>    colId;
    @FXML private TableColumn<Report, String>     colFilename;
    @FXML private TableColumn<Report, String>     colType;
    @FXML private TableColumn<Report, String>     colCreatedAt;
    @FXML private Label                           totalLabel;

    private final ReportViewModel viewModel = new ReportViewModel();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId       .setCellValueFactory(new PropertyValueFactory<>("id"));
        colFilename .setCellValueFactory(new PropertyValueFactory<>("filename"));
        colType     .setCellValueFactory(new PropertyValueFactory<>("type"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        table.setItems(viewModel.getItems());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No reports generated yet."));
        viewModel.loadAll();
        bindTotal();
    }

    @FXML private void onGenerateManual() {
        try {
            Report r = viewModel.generateManual();
            showInfo("Manual report created:\n" + r.getFilename());
        } catch (Exception e) { showError("Failed: " + e.getMessage()); }
    }

    @FXML private void onGenerateHourly() {
        try {
            Report r = viewModel.generateHourly();
            showInfo("Hourly report created:\n" + r.getFilename());
        } catch (Exception e) { showError("Failed: " + e.getMessage()); }
    }

    @FXML private void onGenerateFinal() {
        try {
            Report r = viewModel.generateFinal();
            showInfo("Final report created:\n" + r.getFilename());
        } catch (Exception e) { showError("Failed: " + e.getMessage()); }
    }

    @FXML private void onOpenSelected() {
        Report selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Select a report to open."); return; }
        try {
            File f = new File(selected.getFilepath());
            if (f.exists()) {
                ((javafx.stage.Stage) table.getScene().getWindow()).setIconified(true);
                Desktop.getDesktop().open(f);
            }
            else showError("File not found: " + selected.getFilepath());
        } catch (Exception e) { showError("Cannot open file: " + e.getMessage()); }
    }

    @FXML private void onDeleteSelected() {
        Report selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Select a report to delete."); return; }
        if (confirm("Delete report record?\n" + selected.getFilename())) {
            viewModel.delete(selected.getId());
        }
    }

    @FXML private void onRefresh() { viewModel.loadAll(); }

    private void bindTotal() {
        viewModel.getItems().addListener((javafx.collections.ListChangeListener<Report>) c ->
                totalLabel.setText("Reports: " + viewModel.getItems().size()));
        totalLabel.setText("Reports: " + viewModel.getItems().size());
    }

    private void showInfo(String msg) {
        com.jobpilotai.utils.DialogUtils.showAlert("Information", msg);
    }
    private void showError(String msg) {
        com.jobpilotai.utils.DialogUtils.showError("Error", msg);
    }
    private boolean confirm(String msg) {
        return com.jobpilotai.utils.DialogUtils.showConfirmation("Confirm Action", msg);
    }
}
