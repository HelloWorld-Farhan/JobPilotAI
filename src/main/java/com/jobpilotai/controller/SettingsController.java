package com.jobpilotai.controller;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.UserProfile;
import com.jobpilotai.service.UserProfileService;
import com.jobpilotai.viewmodel.SettingsViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    // User Profile
    @FXML private TextField tfFullName;
    @FXML private TextField tfPhone;
    @FXML private TextField tfCity;
    @FXML private TextField tfCountry;
    @FXML private TextField tfLinkedinUrl;
    @FXML private TextField tfGithubUrl;
    
    // File Paths & Account
    @FXML private TextField    tfResumePath;
    @FXML private TextField    tfDefaultEmail;
    @FXML private TextField    tfGasUrl;
    @FXML private TextField    tfReportFolder;
    @FXML private TextField    tfLogFolder;
    
    // Appearance & Behaviour
    @FXML private ComboBox<String> cbTheme;
    @FXML private CheckBox     chkDarkMode;
    @FXML private CheckBox     chkNotifications;
    @FXML private CheckBox     chkEmail;
    @FXML private CheckBox     chkAutoSave;
    @FXML private CheckBox     chkAutoReports;
    @FXML private CheckBox     chkRememberSize;
    @FXML private CheckBox     chkRememberPosition;
    
    // Automation
    @FXML private CheckBox     chkHeadless;
    @FXML private CheckBox     chkScreenshotError;
    @FXML private TextField    tfTimeout;
    @FXML private TextField    tfRetries;
    
    // AI
    @FXML private CheckBox     chkAiEnabled;
    @FXML private TextField    tfGeminiApiKey;

    private final SettingsViewModel viewModel = new SettingsViewModel();
    private UserProfile currentProfile;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbTheme.getItems().addAll("dark", "light");
        
        viewModel.load();
        bindControls();
        
        loadUserProfile();
        
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
        
        chkHeadless        .selectedProperty().bindBidirectional(viewModel.headlessModeProperty());
        chkScreenshotError .selectedProperty().bindBidirectional(viewModel.screenshotOnErrorProperty());
        tfTimeout          .textProperty().bindBidirectional(viewModel.timeoutMsProperty());
        tfRetries          .textProperty().bindBidirectional(viewModel.maxRetriesProperty());
        
        chkAiEnabled       .selectedProperty().bindBidirectional(viewModel.aiEnabledProperty());
        tfGeminiApiKey     .textProperty().bindBidirectional(viewModel.geminiApiKeyProperty());
    }

    private void loadUserProfile() {
        currentProfile = UserProfileService.getInstance().loadProfile();
        tfFullName.setText(currentProfile.getFullName());
        tfPhone.setText(currentProfile.getPhone());
        tfCity.setText(currentProfile.getCity());
        tfCountry.setText(currentProfile.getCountry());
        tfLinkedinUrl.setText(currentProfile.getLinkedinUrl());
        tfGithubUrl.setText(currentProfile.getGithubUrl());
    }
    
    private void saveUserProfile() {
        if (currentProfile == null) currentProfile = new UserProfile();
        currentProfile.setFullName(tfFullName.getText());
        currentProfile.setPhone(tfPhone.getText());
        currentProfile.setCity(tfCity.getText());
        currentProfile.setCountry(tfCountry.getText());
        currentProfile.setLinkedinUrl(tfLinkedinUrl.getText());
        currentProfile.setGithubUrl(tfGithubUrl.getText());
        
        UserProfileService.getInstance().saveProfile(currentProfile);
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
        // If Dark Mode checkbox is checked, force theme to "dark" (and vice versa for "light") to keep them in sync
        if (chkDarkMode.isSelected()) {
            cbTheme.setValue("dark");
            viewModel.themeProperty().set("dark");
        } else {
            cbTheme.setValue("light");
            viewModel.themeProperty().set("light");
        }

        viewModel.save();
        saveUserProfile();
        
        // Apply the theme live immediately
        if (cbTheme.getScene() != null) {
            com.jobpilotai.themes.ThemeEngine.applyTheme(cbTheme.getScene(), viewModel.themeProperty().get());
        }
        
        new Alert(Alert.AlertType.INFORMATION, "Settings & Profile saved successfully.", ButtonType.OK).showAndWait();
        AppLogger.info("Settings & Profile saved by user.");
    }

    @FXML private void onReset() {
        if (new Alert(Alert.AlertType.CONFIRMATION,
                "Reset all settings to defaults?", ButtonType.YES, ButtonType.NO)
                .showAndWait().filter(r -> r == ButtonType.YES).isPresent()) {
            
            viewModel.themeProperty()            .set("dark");
            viewModel.darkModeProperty()         .set(true);
            viewModel.enableNotificationsProperty().set(true);
            viewModel.enableEmailProperty()      .set(false);
            viewModel.autoSaveProperty()         .set(true);
            viewModel.autoGenerateReportsProperty().set(false);
            viewModel.rememberWindowSizeProperty().set(true);
            viewModel.rememberWindowPositionProperty().set(true);
            viewModel.headlessModeProperty()     .set(false);
            viewModel.screenshotOnErrorProperty().set(true);
            viewModel.timeoutMsProperty()        .set("30000");
            viewModel.maxRetriesProperty()       .set("3");
            
            viewModel.aiEnabledProperty()        .set(false);
            viewModel.geminiApiKeyProperty()     .set("");
            
            viewModel.save();
        }
    }
}
