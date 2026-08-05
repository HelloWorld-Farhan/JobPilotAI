package com.jobpilotai.controller;

import com.jobpilotai.coverletter.CoverLetterGenerator;
import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.UserProfile;
import com.jobpilotai.service.UserProfileService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;

public class CoverLetterController implements Initializable {

    @FXML private TextField tfJobTitle;
    @FXML private TextField tfCompany;
    @FXML private TextArea taJobDescription;
    @FXML private TextArea taCoverLetter;
    
    private String latestResumeJson = "";
    private UserProfile profile;
    private Preferences prefs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        profile = UserProfileService.getInstance().loadProfile();
        loadLatestResumeData();
        
        prefs = Preferences.userNodeForPackage(CoverLetterController.class);
        
        // Load saved values
        tfJobTitle.setText(prefs.get("cl_job_title", ""));
        tfCompany.setText(prefs.get("cl_company", ""));
        taJobDescription.setText(prefs.get("cl_job_desc", ""));
        taCoverLetter.setText(prefs.get("cl_output", ""));
        
        // Auto-save on change
        tfJobTitle.textProperty().addListener((obs, oldV, newV) -> prefs.put("cl_job_title", newV));
        tfCompany.textProperty().addListener((obs, oldV, newV) -> prefs.put("cl_company", newV));
        taJobDescription.textProperty().addListener((obs, oldV, newV) -> prefs.put("cl_job_desc", newV));
        taCoverLetter.textProperty().addListener((obs, oldV, newV) -> prefs.put("cl_output", newV));
    }

    private void loadLatestResumeData() {
        String sql = "SELECT raw_text, skills, experience FROM parsed_resumes ORDER BY parsed_at DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
             
            if (rs.next()) {
                latestResumeJson = "Skills: " + rs.getString("skills") + "\nExperience: " + rs.getString("experience");
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to load latest resume data", e);
        }
    }

    @FXML private void onGenerate() {
        if (taJobDescription.getText().trim().isEmpty()) {
            com.jobpilotai.utils.DialogUtils.showAlert("Warning", "Please enter a Job Description first.");
            return;
        }

        taCoverLetter.setText("AI is writing your cover letter... Please wait.");
        
        String profileData = "Name: " + profile.getFullName() + ", Email: " + profile.getEmail() + ", Phone: " + profile.getPhone();
        String jobData = "Title: " + tfJobTitle.getText() + ", Company: " + tfCompany.getText() + "\nDesc: " + taJobDescription.getText();

        CompletableFuture.runAsync(() -> {
            String result = CoverLetterGenerator.generate(profileData, latestResumeJson, jobData);
            
            Platform.runLater(() -> {
                taCoverLetter.setText(result);
                AppLogger.info("Cover letter generated.");
            });
        });
    }

    @FXML private void onExportPdf() {
        com.jobpilotai.utils.DialogUtils.showAlert("PDF Export", "PDF Export is available via external virtual printers. DOCX export is supported natively.");
    }

    @FXML private void onExportDocx() {
        String text = taCoverLetter.getText();
        if (text == null || text.trim().isEmpty() || text.startsWith("AI is writing")) {
            com.jobpilotai.utils.DialogUtils.showAlert("Warning", "No cover letter to export.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Save Cover Letter");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Document", "*.docx"));
        fc.setInitialFileName("CoverLetter_" + tfCompany.getText().replaceAll("\\s+", "") + ".docx");
        
        File file = fc.showSaveDialog(null);
        if (file != null) {
            boolean success = CoverLetterGenerator.exportToDocx(text, file);
            if (success) {
                com.jobpilotai.utils.DialogUtils.showAlert("Success", "Exported successfully!");
            } else {
                com.jobpilotai.utils.DialogUtils.showError("Export Failed", "Export failed. Check logs.");
            }
        }
    }
}
