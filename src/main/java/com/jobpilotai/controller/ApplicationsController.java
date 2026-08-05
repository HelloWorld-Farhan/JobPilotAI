package com.jobpilotai.controller;

import com.jobpilotai.config.AppConfig;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.JobApplication;
import com.jobpilotai.service.ReportService;
import com.jobpilotai.viewmodel.ApplicationViewModel;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Applications table view with CRUD operations.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class ApplicationsController implements Initializable {

    @FXML private TextField                   searchField;
    @FXML private ComboBox<String>            statusFilterCombo;
    @FXML private TableView<JobApplication>   table;
    @FXML private TableColumn<JobApplication, Integer> colId;
    @FXML private TableColumn<JobApplication, String>  colCompany;
    @FXML private TableColumn<JobApplication, String>  colJobTitle;
    @FXML private TableColumn<JobApplication, String>  colWebsite;
    @FXML private TableColumn<JobApplication, String>  colStatus;
    @FXML private TableColumn<JobApplication, String>  colDate;
    @FXML private TableColumn<JobApplication, String>  colTime;
    @FXML private TableColumn<JobApplication, String>  colNotes;
    @FXML private TableColumn<JobApplication, Integer> colAttempts;
    @FXML private Label                       totalLabel;

    private final ApplicationViewModel viewModel = new ApplicationViewModel();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupFilters();
        viewModel.loadAll();
        bindTotal();
        AppLogger.info("Applications view initialised.");
    }

    // ── Table Setup ──────────────────────────────────────────────────────────

    private void setupColumns() {
        colId       .setCellValueFactory(new PropertyValueFactory<>("id"));
        colCompany  .setCellValueFactory(new PropertyValueFactory<>("company"));
        colJobTitle .setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        colWebsite  .setCellValueFactory(new PropertyValueFactory<>("website"));
        colDate     .setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime     .setCellValueFactory(new PropertyValueFactory<>("time"));
        colNotes    .setCellValueFactory(new PropertyValueFactory<>("notes"));
        colAttempts .setCellValueFactory(new PropertyValueFactory<>("attemptCount"));

        // Status column with coloured badge
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;" +
                        "-fx-text-fill: white; -fx-background-color: " + statusColor(status) + ";");
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        table.setItems(viewModel.getItems());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No applications found."));
    }

    private void setupFilters() {
        statusFilterCombo.getItems().addAll(
                "All", AppConfig.STATUS_SUCCESS, AppConfig.STATUS_ALREADY_APPLIED,
                AppConfig.STATUS_FAILED, AppConfig.STATUS_PENDING_OTP,
                AppConfig.STATUS_PENDING_CAPTCHA, AppConfig.STATUS_PENDING);
        statusFilterCombo.setValue("All");

        searchField.textProperty().addListener((o, ov, nv) -> {
            viewModel.searchQueryProperty().set(nv);
            viewModel.search();
        });
        statusFilterCombo.valueProperty().addListener((o, ov, nv) -> {
            viewModel.statusFilterProperty().set(nv);
            viewModel.applyFilter();
        });
    }

    private void bindTotal() {
        viewModel.getItems().addListener((javafx.collections.ListChangeListener<JobApplication>) c ->
                totalLabel.setText("Total: " + viewModel.getItems().size()));
        totalLabel.setText("Total: " + viewModel.getItems().size());
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    @FXML private void onAddApplication() {
        showAddDialog(null);
    }

    @FXML private void onEditSelected() {
        JobApplication selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Please select an application to edit."); return; }
        showAddDialog(selected);
    }

    @FXML private void onDeleteSelected() {
        JobApplication selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Please select an application to delete."); return; }
        if (confirm("Delete this application?\n" + selected.getCompany() + " – " + selected.getJobTitle())) {
            try { viewModel.delete(selected.getId()); }
            catch (Exception e) { showError("Delete failed: " + e.getMessage()); }
        }
    }

    @FXML private void onDeleteAll() {
        if (confirm("Delete ALL applications? This cannot be undone.")) {
            try { viewModel.deleteAll(); }
            catch (Exception e) { showError("Delete all failed: " + e.getMessage()); }
        }
    }

    @FXML private void onRefresh() {
        viewModel.loadAll();
    }

    @FXML private void onExportExcel() {
        try {
            ReportService.getInstance().generateManualReport();
            showInfo("Report exported successfully to the reports folder.");
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    // ── Add/Edit Dialog ──────────────────────────────────────────────────────

    private void showAddDialog(JobApplication existing) {
        boolean isEdit = existing != null;
        Dialog<JobApplication> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Application" : "Add Application");
        dialog.setHeaderText(null);

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));

        TextField tfCompany  = createField("Company Name *",  isEdit ? existing.getCompany()  : "");
        TextField tfTitle    = createField("Job Title *",      isEdit ? existing.getJobTitle() : "");
        TextField tfWebsite  = createField("Website",          isEdit ? existing.getWebsite()  : "");
        TextField tfJobUrl   = createField("Job URL",          isEdit ? existing.getJobUrl()   : "");
        TextField tfResume   = createField("Resume Used",      isEdit ? existing.getResumeUsed(): "");
        TextArea  taNotes    = new TextArea(isEdit ? existing.getNotes() : "");
        taNotes.setPrefRowCount(3);
        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll(AppConfig.STATUS_SUCCESS, AppConfig.STATUS_ALREADY_APPLIED,
                AppConfig.STATUS_FAILED, AppConfig.STATUS_PENDING_OTP,
                AppConfig.STATUS_PENDING_CAPTCHA, AppConfig.STATUS_PENDING);
        cbStatus.setValue(isEdit ? existing.getStatus() : AppConfig.STATUS_PENDING);

        grid.addRow(0, lbl("Company *:"),  tfCompany);
        grid.addRow(1, lbl("Job Title *:"), tfTitle);
        grid.addRow(2, lbl("Website:"),    tfWebsite);
        grid.addRow(3, lbl("Job URL:"),    tfJobUrl);
        grid.addRow(4, lbl("Status:"),     cbStatus);
        grid.addRow(5, lbl("Resume:"),     tfResume);
        grid.addRow(6, lbl("Notes:"),      taNotes);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                JobApplication app = isEdit ? existing : new JobApplication();
                app.setCompany    (tfCompany.getText().trim());
                app.setJobTitle   (tfTitle.getText().trim());
                app.setWebsite    (tfWebsite.getText().trim());
                app.setJobUrl     (tfJobUrl.getText().trim());
                app.setStatus     (cbStatus.getValue());
                app.setResumeUsed (tfResume.getText().trim());
                app.setNotes      (taNotes.getText().trim());
                if (!isEdit) {
                    app.setDate(LocalDate.now().toString());
                    app.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                }
                return app;
            }
            return null;
        });

        Optional<JobApplication> result = dialog.showAndWait();
        result.ifPresent(app -> {
            try {
                if (isEdit) viewModel.update(app);
                else        viewModel.add(app);
            } catch (Exception e) {
                showError("Save failed: " + e.getMessage());
            }
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String statusColor(String status) {
        return switch (status) {
            case "Success"         -> "#22C55E";
            case "Failed"          -> "#EF4444";
            case "Already Applied" -> "#3B82F6";
            case "Pending OTP",
                 "Pending CAPTCHA",
                 "Pending"         -> "#F59E0B";
            default                -> "#64748B";
        };
    }

    private TextField createField(String prompt, String value) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        tf.setPrefWidth(300);
        return tf;
    }

    private Label lbl(String text) { return new Label(text); }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        return a.showAndWait().filter(r -> r == ButtonType.YES).isPresent();
    }
}
