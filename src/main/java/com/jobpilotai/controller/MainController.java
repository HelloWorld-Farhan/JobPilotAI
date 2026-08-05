package com.jobpilotai.controller;

import com.jobpilotai.config.AppConfig;
import com.jobpilotai.logs.AppLogger;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Root controller managing the sidebar navigation and content area.
 * <p>
 * Each sidebar button loads the corresponding FXML panel into the main
 * content area using a fade transition.
 * </p>
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class MainController implements Initializable {

    @FXML private BorderPane  rootPane;
    @FXML private VBox        sidebar;
    @FXML private StackPane   contentArea;
    @FXML private Label       clockLabel;
    @FXML private Label       versionLabel;
    @FXML private Button      sidebarToggleBtn;

    @FXML private Button navDashboard;
    @FXML private Button navApplications;
    @FXML private Button navHistory;
    @FXML private Button navReports;
    @FXML private Button navSettings;
    @FXML private Button navLogs;
    @FXML private Button navAbout;

    private boolean sidebarCollapsed = false;
    private Timeline clockTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        versionLabel.setText(AppConfig.APP_VERSION);
        startClock();
        loadView("dashboard");
        setActiveButton(navDashboard);
        AppLogger.info("Main controller initialised.");
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @FXML private void onDashboard()     { loadView("dashboard");     setActiveButton(navDashboard); }
    @FXML private void onApplications()  { loadView("applications");  setActiveButton(navApplications); }
    @FXML private void onHistory()       { loadView("history");        setActiveButton(navHistory); }
    @FXML private void onReports()       { loadView("reports");        setActiveButton(navReports); }
    @FXML private void onSettings()      { loadView("settings");       setActiveButton(navSettings); }
    @FXML private void onLogs()          { loadView("logs");           setActiveButton(navLogs); }
    @FXML private void onAbout()         { loadView("about");          setActiveButton(navAbout); }

    @FXML private void onToggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        double targetWidth = sidebarCollapsed ? 60 : 220;
        String btnText     = sidebarCollapsed ? "→" : "←";

        KeyValue kv = new KeyValue(sidebar.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH);
        new Timeline(new KeyFrame(Duration.millis(250), kv)).play();

        sidebarToggleBtn.setText(btnText);

        // Show/hide labels in nav buttons
        for (Node node : sidebar.getChildren()) {
            if (node instanceof Button btn && btn != sidebarToggleBtn) {
                String text = btn.getText();
                if (sidebarCollapsed) {
                    btn.setUserData(text);
                    btn.setText(text.length() > 0 ? String.valueOf(text.charAt(0)) : "");
                } else if (btn.getUserData() != null) {
                    btn.setText(btn.getUserData().toString());
                }
            }
        }
    }

    // ── View Loading ─────────────────────────────────────────────────────────

    private void loadView(String name) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/" + name + ".fxml");
            if (fxmlUrl == null) {
                AppLogger.error("FXML not found: " + name);
                return;
            }
            Node view = FXMLLoader.load(fxmlUrl);

            // Fade transition
            FadeTransition fade = new FadeTransition(Duration.millis(200), view);
            fade.setFromValue(0);
            fade.setToValue(1);

            contentArea.getChildren().setAll(view);
            fade.play();

            AppLogger.debug("Loaded view: " + name);
        } catch (IOException e) {
            AppLogger.error("Failed to load view: " + name, e);
        }
    }

    private void setActiveButton(Button active) {
        Button[] allButtons = {navDashboard, navApplications, navHistory,
                navReports, navSettings, navLogs, navAbout};
        for (Button btn : allButtons) {
            btn.getStyleClass().remove("nav-btn-active");
            btn.getStyleClass().add("nav-btn");
        }
        active.getStyleClass().add("nav-btn-active");
    }

    // ── Clock ─────────────────────────────────────────────────────────────────

    private void startClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(AppConfig.DISPLAY_TIME_FMT);
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e ->
            clockLabel.setText(LocalDateTime.now().format(fmt))));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
        clockLabel.setText(LocalDateTime.now().format(fmt));
    }
}
