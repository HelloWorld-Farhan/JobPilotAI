package com.jobpilotai.controller;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.viewmodel.SettingsViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Settings panel.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class SettingsController implements Initializable {

    @FXML private TextField    tfResumePath;
    @FXML private TextField    tfDefaultEmail;
    @FXML private TextField    tfGasUrl;
    @FXML private TextField    tfReportFolder;
    @FXML private TextField    tfLogFolder;
    @FXML private ComboBox<String> cbTheme;
    @FXML private CheckBox     chkDarkMode;
    @FXML private CheckBox     chkNotifications;
    @FXML private CheckBox     chkEmail;
    @FXML private CheckBox     chkAutoSave;
    @FXML private CheckBox     chkAutoReports;
    @FXML private CheckBox     chkRememberSize;
    @FXML private CheckBox     chkRememberPosition;

    private final SettingsViewModel viewModel = new SettingsViewModel();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbTheme.getItems().addAll("dark", "light");
        viewModel.load();
        bindControls();
        AppLogger.info("Settings view initialised.");
    }

    private void bindControls() {
        tfResumePath       .textProperty().bindBidirectional(viewModel.resumePathProperty());
        tfDefaultEmail     .textProperty().bindBidirectional(viewModel.defaultEmailProperty());
        tfGasUrl           .textProperty().bindBidirectional(viewModel.gasUrlProperty());
        tfReportFolder     .textProperty().bindBidirectional(viewModel.reportFolderProperty());
        tfLogFolder        .textProperty().bindBidirectional(viewModel.logFolderProperty());
        cbTheme            .valueProperty().bindBidirectional(viewModel.themeProperty());
        chkDarkMode        .selectedProperty().bindBidirectional(viewModel.darkModeProperty());
        chkNotifications   .selectedProperty().bindBidirectional(viewModel.enableNotificationsProperty());
        chkEmail           .selectedProperty().bindBidirectional(viewModel.enableEmailProperty());
        chkAutoSave        .selectedProperty().bindBidirectional(viewModel.autoSaveProperty());
        chkAutoReports     .selectedProperty().bindBidirectional(viewModel.autoGenerateReportsProperty());
        chkRememberSize    .selectedProperty().bindBidirectional(viewModel.rememberWindowSizeProperty());
        chkRememberPosition.selectedProperty().bindBidirectional(viewModel.rememberWindowPositionProperty());
    }

    @FXML private void onBrowseResume() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Resume");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF / Word", "*.pdf", "*.docx", "*.doc"));
        File file = fc.showOpenDialog(null);
        if (file != null) viewModel.resumePathProperty().set(file.getAbsolutePath());
    }

    @FXML private void onBrowseReportFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Report Folder");
        File dir = dc.showDialog(null);
        if (dir != null) viewModel.reportFolderProperty().set(dir.getAbsolutePath());
    }

    @FXML private void onBrowseLogFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Log Folder");
        File dir = dc.showDialog(null);
        if (dir != null) viewModel.logFolderProperty().set(dir.getAbsolutePath());
    }

    @FXML private void onSave() {
        viewModel.save();
        new Alert(Alert.AlertType.INFORMATION, "Settings saved successfully.", ButtonType.OK).showAndWait();
        AppLogger.info("Settings saved by user.");
    }

    @FXML private void onReset() {
        if (new Alert(Alert.AlertType.CONFIRMATION,
                "Reset all settings to defaults?", ButtonType.YES, ButtonType.NO)
                .showAndWait().filter(r -> r == ButtonType.YES).isPresent()) {
            // Reload defaults from service defaults
            viewModel.themeProperty()            .set("dark");
            viewModel.darkModeProperty()         .set(true);
            viewModel.enableNotificationsProperty().set(true);
            viewModel.enableEmailProperty()      .set(false);
            viewModel.autoSaveProperty()         .set(true);
            viewModel.autoGenerateReportsProperty().set(false);
            viewModel.rememberWindowSizeProperty().set(true);
            viewModel.rememberWindowPositionProperty().set(true);
            viewModel.save();
        }
    }
}
