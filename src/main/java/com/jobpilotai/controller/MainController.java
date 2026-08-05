package com.jobpilotai.controller;

import com.jobpilotai.app.MainApp;
import com.jobpilotai.config.AppConfig;
import com.jobpilotai.logs.AppLogger;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Root controller – manages the animated sidebar, fade page transitions,
 * live clock, active-nav-button state, and custom window dragging.
 *
 * @author  JobPilotAI Team
 * @version 2.0.0
 */
public class MainController implements Initializable {

    // ── FXML fields ──────────────────────────────────────────────────────────
    @FXML private BorderPane rootPane;
    @FXML private HBox       titleBar;
    @FXML private VBox       sidebar;
    @FXML private StackPane  contentArea;
    @FXML private Label      clockLabel;
    @FXML private Label      versionLabel;
    @FXML private Button     sidebarToggleBtn;

    @FXML private Button navDashboard;
    @FXML private Button navAutomation;
    @FXML private Button navApplications;
    @FXML private Button navHistory;
    @FXML private Button navReports;
    @FXML private Button navSettings;
    @FXML private Button navLogs;
    @FXML private Button navAbout;
    @FXML private Button navAi;
    @FXML private Button navCoverLetter;
    @FXML private Button navCompany;
    @FXML private Button navAnalytics;
    @FXML private Button navDiagnostics;
    @FXML private Button navPlugins;

    // ── State ─────────────────────────────────────────────────────────────────
    private static final double SIDEBAR_EXPANDED  = 230;
    private static final double SIDEBAR_COLLAPSED = 64;

    private boolean  sidebarCollapsed = false;
    private Timeline clockTimeline;

    private double xOffset = 0;
    private double yOffset = 0;

    // Labels stored for collapse/expand toggle
    private static final String[] NAV_FULL_TEXT = {
        "📊   Dashboard", "🤖   Automation", "📋   Applications", "📁   History",
        "📈   Reports",   "⚙   Settings",     "📜   Logs",
        "ℹ   About"
    };
    private static final String[] NAV_ICON_TEXT = {
        "📊", "🤖", "📋", "📁", "📈", "⚙", "📜", "ℹ"
    };

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        versionLabel.setText(AppConfig.APP_VERSION);
        startClock();
        navigateTo("dashboard");
        setActiveButton(navDashboard);
        AppLogger.info("Main controller initialised.");
    }

    // ── Window Controls ───────────────────────────────────────────────────────

    @FXML private void onTitleBarPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML private void onTitleBarDragged(MouseEvent event) {
        Stage stage = MainApp.getPrimaryStage();
        if (stage != null && !stage.isMaximized()) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }

    @FXML private void onMinimize() {
        Stage stage = MainApp.getPrimaryStage();
        if (stage != null) stage.setIconified(true);
    }

    @FXML private void onMaximize() {
        Stage stage = MainApp.getPrimaryStage();
        if (stage != null) {
            stage.setMaximized(!stage.isMaximized());
        }
    }

    @FXML private void onClose() {
        AppLogger.info("Close button clicked.");
        Platform.exit();
    }

    // ── Navigation handlers ───────────────────────────────────────────────────

    @FXML private void onDashboard()    { navigateTo("dashboard");    setActiveButton(navDashboard); }
    @FXML private void onAutomation()   { navigateTo("automation");   setActiveButton(navAutomation); }
    @FXML private void onApplications() { navigateTo("applications"); setActiveButton(navApplications); }
    @FXML private void onHistory()      { navigateTo("history");      setActiveButton(navHistory); }
    @FXML private void onReports()      { navigateTo("reports");      setActiveButton(navReports); }
    @FXML private void onSettings()     { navigateTo("settings");     setActiveButton(navSettings); }
    @FXML private void onLogs()         { navigateTo("logs");         setActiveButton(navLogs); }
    @FXML private void onAbout()        { navigateTo("about");        setActiveButton(navAbout); }
    @FXML private void onAi()           { navigateTo("ai_analyzer");  setActiveButton(navAi); }
    @FXML private void onCoverLetter()  { navigateTo("cover_letter"); setActiveButton(navCoverLetter); }
    @FXML private void onCompany()      { navigateTo("company");      setActiveButton(navCompany); }
    @FXML private void onAnalytics()    { navigateTo("analytics");    setActiveButton(navAnalytics); }
    @FXML private void onDiagnostics()  { navigateTo("diagnostics");  setActiveButton(navDiagnostics); }
    @FXML private void onPlugins()      { navigateTo("plugins");      setActiveButton(navPlugins); }

    @FXML private void onToggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        double targetW = sidebarCollapsed ? SIDEBAR_COLLAPSED : SIDEBAR_EXPANDED;

        // Animate width
        KeyValue kv = new KeyValue(
                sidebar.prefWidthProperty(), targetW, Interpolator.EASE_BOTH);
        new Timeline(new KeyFrame(Duration.millis(280), kv)).play();

        // Swap button text labels
        Button[] navBtns = {navDashboard, navAutomation, navApplications, navHistory,
                navReports, navSettings, navLogs, navAbout};
        String[] texts = sidebarCollapsed ? NAV_ICON_TEXT : NAV_FULL_TEXT;
        for (int i = 0; i < navBtns.length; i++) {
            navBtns[i].setText(texts[i]);
            navBtns[i].setAlignment(sidebarCollapsed
                    ? javafx.geometry.Pos.CENTER
                    : javafx.geometry.Pos.CENTER_LEFT);
        }
        sidebarToggleBtn.setText(sidebarCollapsed ? "→" : "← Collapse");
    }

    // ── View loading ──────────────────────────────────────────────────────────

    private void navigateTo(String viewName) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/" + viewName + ".fxml");
            if (fxmlUrl == null) {
                AppLogger.error("FXML not found: " + viewName);
                return;
            }
            Node view = FXMLLoader.load(fxmlUrl);

            // Fade out → swap → fade in
            if (!contentArea.getChildren().isEmpty()) {
                Node current = contentArea.getChildren().get(0);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(100), current);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> {
                    contentArea.getChildren().setAll(view);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(220), view);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();
                });
                fadeOut.play();
            } else {
                contentArea.getChildren().setAll(view);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(220), view);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            }

            AppLogger.debug("Navigated to: " + viewName);
        } catch (IOException e) {
            AppLogger.error("Failed to load view: " + viewName, e);
        }
    }

    // ── Active button ─────────────────────────────────────────────────────────

    private void setActiveButton(Button active) {
        Button[] all = {navDashboard, navAutomation, navApplications, navHistory,
                navReports, navSettings, navLogs, navAbout};
        for (Button btn : all) {
            btn.getStyleClass().removeAll("nav-btn-active");
            if (!btn.getStyleClass().contains("nav-btn")) {
                btn.getStyleClass().add("nav-btn");
            }
        }
        if (!active.getStyleClass().contains("nav-btn-active")) {
            active.getStyleClass().add("nav-btn-active");
        }
    }

    // ── Clock ─────────────────────────────────────────────────────────────────

    private void startClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        clockLabel.setText(LocalDateTime.now().format(fmt));
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                clockLabel.setText(LocalDateTime.now().format(fmt))));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }
}
