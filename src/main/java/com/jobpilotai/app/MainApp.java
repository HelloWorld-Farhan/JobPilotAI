package com.jobpilotai.app;

import com.jobpilotai.config.AppConfig;
import com.jobpilotai.config.PathConfig;
import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.service.SessionService;
import com.jobpilotai.service.SettingsService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;
import java.util.Objects;

/**
 * Main JavaFX Application class for JobPilotAI.
 * <p>
 * Responsible for:
 * <ul>
 *   <li>Bootstrapping application directories</li>
 *   <li>Initialising the SQLite database</li>
 *   <li>Loading user settings and session state</li>
 *   <li>Launching the primary JavaFX stage</li>
 * </ul>
 * </p>
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    /**
     * Returns the primary {@link Stage} so other parts of the application can
     * open dialogs relative to it.
     *
     * @return the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void init() throws Exception {
        // Ensure all application directories exist before the UI starts
        PathConfig.initializeDirectories();

        // Boot the logger first so everything below is recorded
        AppLogger.initialize();
        AppLogger.info("JobPilotAI " + AppConfig.APP_VERSION + " starting…");

        // Initialise SQLite database & run migrations
        DatabaseManager.getInstance().initialize();

        // Load persisted settings
        SettingsService.getInstance().load();

        AppLogger.info("Initialisation complete.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Configure stage before loading content
        stage.setTitle(AppConfig.APP_NAME + " – " + AppConfig.APP_VERSION);
        stage.setMinWidth(AppConfig.MIN_WIDTH);
        stage.setMinHeight(AppConfig.MIN_HEIGHT);
        stage.setWidth(AppConfig.DEFAULT_WIDTH);
        stage.setHeight(AppConfig.DEFAULT_HEIGHT);

        // Load application icon
        try {
            InputStream iconStream = getClass().getResourceAsStream("/images/icon.png");
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            AppLogger.warn("Application icon not found, using default.");
        }

        // Load main FXML layout
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/fxml/main.fxml")));
        Parent root = loader.load();

        // Apply the theme from saved settings
        String theme = SettingsService.getInstance().getTheme();
        String cssPath = "dark".equalsIgnoreCase(theme)
                ? "/css/dark-theme.css"
                : "/css/light-theme.css";

        Scene scene = new Scene(root, AppConfig.DEFAULT_WIDTH, AppConfig.DEFAULT_HEIGHT);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/css/base.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource(cssPath)).toExternalForm());

        stage.setScene(scene);

        // Restore window position/size if the user has saved it
        SettingsService.getInstance().restoreWindowState(stage);

        stage.show();

        AppLogger.info("Primary stage displayed.");

        // Check for a previous session and prompt the user
        SessionService.getInstance().checkAndPromptSession(stage);
    }

    @Override
    public void stop() throws Exception {
        AppLogger.info("JobPilotAI shutting down.");

        // Persist window size/position
        if (primaryStage != null) {
            SettingsService.getInstance().saveWindowState(primaryStage);
        }

        // Close database connection
        DatabaseManager.getInstance().close();

        AppLogger.info("Shutdown complete.");
        AppLogger.close();
    }

    /**
     * Delegate main so that {@link AppLauncher} can call it without the JVM
     * complaining about a missing JavaFX runtime.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
