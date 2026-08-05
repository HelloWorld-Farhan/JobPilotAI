package com.jobpilotai.controller;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.LogEntry;
import com.jobpilotai.viewmodel.LogViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Logs viewer.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class LogsController implements Initializable {

    @FXML private TextField             searchField;
    @FXML private ComboBox<String>      levelCombo;
    @FXML private TableView<LogEntry>   table;
    @FXML private TableColumn<LogEntry, Integer> colId;
    @FXML private TableColumn<LogEntry, String>  colTimestamp;
    @FXML private TableColumn<LogEntry, String>  colLevel;
    @FXML private TableColumn<LogEntry, String>  colMessage;
    @FXML private Label                 totalLabel;

    private final LogViewModel viewModel = new LogViewModel();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId       .setCellValueFactory(new PropertyValueFactory<>("id"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colMessage  .setCellValueFactory(new PropertyValueFactory<>("message"));

        colLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        colLevel.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String level, boolean empty) {
                super.updateItem(level, empty);
                if (empty || level == null) { setGraphic(null); return; }
                Label badge = new Label(level);
                badge.setPadding(new Insets(2, 8, 2, 8));
                badge.setStyle("-fx-background-radius: 8; -fx-font-size: 10px; -fx-font-weight: bold;" +
                        "-fx-text-fill: white; -fx-background-color: " + levelColor(level) + ";");
                setGraphic(badge);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        levelCombo.getItems().addAll("All", "INFO", "WARN", "ERROR", "DEBUG");
        levelCombo.setValue("All");

        searchField.textProperty().addListener((o, ov, nv) -> {
            viewModel.searchQueryProperty().set(nv);
            viewModel.search();
        });
        levelCombo.valueProperty().addListener((o, ov, nv) -> {
            viewModel.levelFilterProperty().set(nv);
            viewModel.applyFilter();
        });

        table.setItems(viewModel.getItems());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No log entries found."));

        viewModel.loadAll();
        bindTotal();
    }

    @FXML private void onRefresh()   { viewModel.loadAll(); }

    @FXML private void onCopySelected() {
        LogEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Clipboard cb = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(selected.toString());
        cb.setContent(content);
    }

    @FXML private void onClearLogs() {
        if (confirm("Clear all log entries from the database?")) {
            viewModel.clearAll();
        }
    }

    private void bindTotal() {
        viewModel.getItems().addListener((javafx.collections.ListChangeListener<LogEntry>) c ->
                totalLabel.setText("Entries: " + viewModel.getItems().size()));
        totalLabel.setText("Entries: " + viewModel.getItems().size());
    }

    private String levelColor(String level) {
        return switch (level) {
            case "INFO"  -> "#22C55E";
            case "WARN"  -> "#F59E0B";
            case "ERROR" -> "#EF4444";
            case "DEBUG" -> "#3B82F6";
            default      -> "#64748B";
        };
    }

    private boolean confirm(String msg) {
        return new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO)
                .showAndWait().filter(r -> r == ButtonType.YES).isPresent();
    }
}
