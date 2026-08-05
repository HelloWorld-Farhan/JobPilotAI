package com.jobpilotai.controller;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.JobApplication;
import com.jobpilotai.service.ReportService;
import com.jobpilotai.viewmodel.ApplicationViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the History panel – read-only view with export and delete.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class HistoryController implements Initializable {

    @FXML private TextField                   searchField;
    @FXML private ComboBox<String>            statusFilterCombo;
    @FXML private TableView<JobApplication>   table;
    @FXML private TableColumn<JobApplication, Integer> colId;
    @FXML private TableColumn<JobApplication, String>  colCompany;
    @FXML private TableColumn<JobApplication, String>  colJobTitle;
    @FXML private TableColumn<JobApplication, String>  colStatus;
    @FXML private TableColumn<JobApplication, String>  colDate;
    @FXML private TableColumn<JobApplication, String>  colWebsite;
    @FXML private TableColumn<JobApplication, String>  colNotes;
    @FXML private Label                       totalLabel;

    private final ApplicationViewModel viewModel = new ApplicationViewModel();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId      .setCellValueFactory(new PropertyValueFactory<>("id"));
        colCompany .setCellValueFactory(new PropertyValueFactory<>("company"));
        colJobTitle.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        colDate    .setCellValueFactory(new PropertyValueFactory<>("date"));
        colWebsite .setCellValueFactory(new PropertyValueFactory<>("website"));
        colNotes   .setCellValueFactory(new PropertyValueFactory<>("notes"));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.setPadding(new Insets(2, 8, 2, 8));
                badge.setStyle("-fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: bold;" +
                        "-fx-text-fill: white; -fx-background-color: " + statusColor(status) + ";");
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        statusFilterCombo.getItems().addAll("All", "Success", "Already Applied", "Failed",
                "Pending OTP", "Pending CAPTCHA", "Pending");
        statusFilterCombo.setValue("All");

        searchField.textProperty().addListener((o, ov, nv) -> {
            viewModel.searchQueryProperty().set(nv);
            viewModel.search();
        });
        statusFilterCombo.valueProperty().addListener((o, ov, nv) -> {
            viewModel.statusFilterProperty().set(nv);
            viewModel.applyFilter();
        });

        table.setItems(viewModel.getItems());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No history found."));

        viewModel.loadAll();
        bindTotal();
    }

    @FXML private void onRefresh() { viewModel.loadAll(); }

    @FXML private void onExportExcel() {
        try {
            ReportService.getInstance().generateManualReport();
            com.jobpilotai.utils.DialogUtils.showAlert("Export Successful", "Exported to reports folder.");
        } catch (Exception e) {
            com.jobpilotai.utils.DialogUtils.showError("Export Failed", "Export failed: " + e.getMessage());
        }
    }

    @FXML private void onDeleteSelected() {
        JobApplication sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { com.jobpilotai.utils.DialogUtils.showAlert("Select Item", "Select an item to delete."); return; }
        if (com.jobpilotai.utils.DialogUtils.showConfirmation("Delete Entry", "Delete this entry?")) {
            try { viewModel.delete(sel.getId()); }
            catch (Exception e) { com.jobpilotai.utils.DialogUtils.showError("Delete Failed", e.getMessage()); }
        }
    }

    @FXML private void onDeleteAll() {
        if (com.jobpilotai.utils.DialogUtils.showConfirmation("Delete All History", "Delete ALL history? This cannot be undone.")) {
            try { viewModel.deleteAll(); }
            catch (Exception e) { com.jobpilotai.utils.DialogUtils.showError("Delete Failed", e.getMessage()); }
        }
    }

    private void bindTotal() {
        viewModel.getItems().addListener((javafx.collections.ListChangeListener<JobApplication>) c ->
                totalLabel.setText("Total: " + viewModel.getItems().size()));
        totalLabel.setText("Total: " + viewModel.getItems().size());
    }

    private String statusColor(String status) {
        return switch (status) {
            case "Success"         -> "#22C55E";
            case "Failed"          -> "#EF4444";
            case "Already Applied" -> "#3B82F6";
            default                -> "#F59E0B";
        };
    }
}
