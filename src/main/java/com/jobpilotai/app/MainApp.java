package com.jobpilotai.app;

import com.jobpilotai.config.AppConfig;
import com.jobpilotai.config.PathConfig;
import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.service.SessionService;
import com.jobpilotai.service.SettingsService;
import com.jobpilotai.workspace.ProfileManager;
import com.jobpilotai.plugins.PluginLoader;
import com.jobpilotai.themes.ThemeEngine;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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
        ProfileManager.getInstance().loadActiveProfile();

        // Load persisted settings
        SettingsService.getInstance().load();
        
        // Initialize Plugins
        PluginLoader.initialize();

        // Check for crashed sessions
        com.jobpilotai.automation.sessionmanager.SessionRecovery.checkAndRecover();

        AppLogger.info("Initialisation complete.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Configure stage before loading content
        stage.initStyle(StageStyle.UNDECORATED);
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

        Scene scene = new Scene(root);
        ThemeEngine.applyTheme(scene, SettingsService.getInstance().getTheme());
        
        // Global Keyboard Shortcuts
        KeyCombination newAppShortcut = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        KeyCombination searchShortcut = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (newAppShortcut.match(event)) {
                System.out.println("Global Shortcut: New Application triggered.");
                event.consume();
            } else if (searchShortcut.match(event)) {
                System.out.println("Global Shortcut: Search triggered.");
                event.consume();
            }
        });

        primaryStage.setScene(scene);

        // Restore window position/size if the user has saved it, else center it
        SettingsService.getInstance().restoreWindowState(stage);
        if (!SettingsService.getInstance().isRememberWindowPosition()) {
            stage.centerOnScreen();
        }

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

        // Shutdown Plugins
        PluginLoader.shutdown();

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
