package com.jobpilotai.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobpilotai.ai.AiClient;
import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.resume.DocumentParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;

public class AiController implements Initializable {

    // Resume Parsing
    @FXML private TextField tfSelectedResume;
    @FXML private TextArea taResumeOutput;

    // Job Description Parsing
    @FXML private TextField tfJobTitle;
    @FXML private TextField tfCompany;
    @FXML private TextArea taJobInput;
    @FXML private TextArea taJobOutput;

    private File currentResume;
    private final ObjectMapper mapper = new ObjectMapper();
    private Preferences prefs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppLogger.info("AI Analyzer view initialised.");
        
        prefs = Preferences.userNodeForPackage(AiController.class);
        
        // Load saved values
        tfSelectedResume.setText(prefs.get("ai_resume_path", ""));
        tfJobTitle.setText(prefs.get("ai_job_title", ""));
        tfCompany.setText(prefs.get("ai_company", ""));
        taJobInput.setText(prefs.get("ai_job_desc", ""));
        
        // Auto-save on change
        tfSelectedResume.textProperty().addListener((obs, oldV, newV) -> prefs.put("ai_resume_path", newV));
        tfJobTitle.textProperty().addListener((obs, oldV, newV) -> prefs.put("ai_job_title", newV));
        tfCompany.textProperty().addListener((obs, oldV, newV) -> prefs.put("ai_company", newV));
        taJobInput.textProperty().addListener((obs, oldV, newV) -> prefs.put("ai_job_desc", newV));
        
        // Load globally persisted parsed JSON
        String savedJson = com.jobpilotai.service.SettingsService.getInstance().getParsedResumeJson();
        if (savedJson != null && !savedJson.trim().isEmpty()) {
            taResumeOutput.setText(formatJson(savedJson));
        }
    }

    @FXML private void onBrowseResume() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Resume");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.txt", "*.tex")
        );
        File file = fc.showOpenDialog(null);
        if (file != null) {
            currentResume = file;
            tfSelectedResume.setText(file.getAbsolutePath());
            
            // Save to global settings
            com.jobpilotai.service.SettingsService settings = com.jobpilotai.service.SettingsService.getInstance();
            settings.setResumePath(file.getAbsolutePath());
            settings.save();
        }
    }

    @FXML private void onAnalyzeResume() {
        if (currentResume == null) {
            com.jobpilotai.utils.DialogUtils.showAlert("Warning", "Please select a resume file first.");
            return;
        }

        taResumeOutput.setText("Extracting text and calling AI...");
        CompletableFuture.runAsync(() -> {
            try {
                String rawText = DocumentParser.extractText(currentResume);
                if (rawText.isEmpty()) {
                    Platform.runLater(() -> taResumeOutput.setText("Failed to extract text."));
                    return;
                }

                String jsonResult = AiClient.getInstance().extractResumeData(rawText);
                
                // Save to DB
                saveParsedResumeToDb(currentResume.getName(), currentResume.getAbsolutePath(), jsonResult, rawText);
                
                // Save to Global Settings for persistence
                com.jobpilotai.service.SettingsService.getInstance().setParsedResumeJson(jsonResult);
                com.jobpilotai.service.SettingsService.getInstance().save();

                Platform.runLater(() -> {
                    taResumeOutput.setText(formatJson(jsonResult));
                    AppLogger.info("Resume successfully analyzed by AI.");
                });
            } catch (Exception e) {
                AppLogger.error("Error analyzing resume", e);
                Platform.runLater(() -> taResumeOutput.setText("Error analyzing resume: " + e.getMessage()));
            }
        });
    }

    @FXML private void onAnalyzeJob() {
        String jobText = taJobInput.getText();
        String title = tfJobTitle.getText();
        String company = tfCompany.getText();

        if (jobText.trim().isEmpty() || title.trim().isEmpty()) {
            com.jobpilotai.utils.DialogUtils.showAlert("Warning", "Job Title and Description are required.");
            return;
        }

        taJobOutput.setText("Analyzing job description with AI...");
        CompletableFuture.runAsync(() -> {
            try {
                String jsonResult = AiClient.getInstance().extractJobDescription(jobText);
                
                // Save to DB
                saveJobDescriptionToDb(title, company, jsonResult, jobText);

                Platform.runLater(() -> {
                    taJobOutput.setText(formatJson(jsonResult));
                    AppLogger.info("Job Description successfully analyzed by AI.");
                });
            } catch (Exception e) {
                AppLogger.error("Error analyzing job", e);
                Platform.runLater(() -> taJobOutput.setText("Error analyzing job: " + e.getMessage()));
            }
        });
    }

    private void saveParsedResumeToDb(String filename, String filepath, String json, String rawText) {
        String name = "", email = "", skills = "", edu = "", exp = "", proj = "", cert = "";
        try {
            JsonNode root = mapper.readTree(json);
            name = root.path("name").asText("");
            email = root.path("email").asText("");
            skills = root.path("skills").asText("");
            edu = root.path("education").asText("");
            exp = root.path("experience").asText("");
            proj = root.path("projects").asText("");
            cert = root.path("certifications").asText("");
        } catch (Exception ignored) { }

        String sql = """
            INSERT INTO parsed_resumes (filename, filepath, name, email, skills, education, experience, projects, certifications, raw_text)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filename);
            pstmt.setString(2, filepath);
            pstmt.setString(3, name);
            pstmt.setString(4, email);
            pstmt.setString(5, skills);
            pstmt.setString(6, edu);
            pstmt.setString(7, exp);
            pstmt.setString(8, proj);
            pstmt.setString(9, cert);
            pstmt.setString(10, rawText);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.error("Failed to save parsed resume", e);
        }
    }

    private void saveJobDescriptionToDb(String title, String company, String json, String rawText) {
        String req = "", pref = "", edu = "", exp = "";
        try {
            JsonNode root = mapper.readTree(json);
            req = root.path("requiredSkills").asText("");
            pref = root.path("preferredSkills").asText("");
            edu = root.path("education").asText("");
            exp = root.path("experience").asText("");
        } catch (Exception ignored) { }

        String sql = """
            INSERT INTO job_descriptions (job_title, company, req_skills, pref_skills, education, experience, raw_text)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, company);
            pstmt.setString(3, req);
            pstmt.setString(4, pref);
            pstmt.setString(5, edu);
            pstmt.setString(6, exp);
            pstmt.setString(7, rawText);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            AppLogger.error("Failed to save job description", e);
        }
    }

    private String formatJson(String raw) {
        try {
            Object json = mapper.readValue(raw, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return raw;
        }
    }
}
