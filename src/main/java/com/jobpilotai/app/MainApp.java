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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;

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

        // Dynamically scale down if the user's screen is smaller than the defaults
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double idealWidth = Math.min(AppConfig.DEFAULT_WIDTH, bounds.getWidth() * 0.9);
        double idealHeight = Math.min(AppConfig.DEFAULT_HEIGHT, bounds.getHeight() * 0.9);
        double minWidth = Math.min(AppConfig.MIN_WIDTH, bounds.getWidth() * 0.8);
        double minHeight = Math.min(AppConfig.MIN_HEIGHT, bounds.getHeight() * 0.8);

        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.setWidth(idealWidth);
        stage.setHeight(idealHeight);

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
        KeyCombination globalSearchShortcut = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        KeyCombination openShortcut = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
        KeyCombination saveShortcut = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        KeyCombination refreshShortcut = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
        KeyCombination backupShortcut = new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN);
        KeyCombination pluginsShortcut = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
        KeyCombination helpShortcut = new KeyCodeCombination(KeyCode.F1);
        KeyCombination refreshF5Shortcut = new KeyCodeCombination(KeyCode.F5);
        KeyCombination fullscreenShortcut = new KeyCodeCombination(KeyCode.F11);
        
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (newAppShortcut.match(event)) {
                System.out.println("Global Shortcut: New Application triggered.");
                event.consume();
            } else if (globalSearchShortcut.match(event)) {
                System.out.println("Global Shortcut: Global Search triggered.");
                event.consume();
            } else if (searchShortcut.match(event)) {
                System.out.println("Global Shortcut: Search triggered.");
                event.consume();
            } else if (openShortcut.match(event)) {
                System.out.println("Global Shortcut: Open Workspace triggered.");
                event.consume();
            } else if (saveShortcut.match(event)) {
                System.out.println("Global Shortcut: Save Workspace triggered.");
                event.consume();
            } else if (refreshShortcut.match(event) || refreshF5Shortcut.match(event)) {
                System.out.println("Global Shortcut: Refresh triggered.");
                event.consume();
            } else if (backupShortcut.match(event)) {
                System.out.println("Global Shortcut: Backup triggered.");
                event.consume();
            } else if (pluginsShortcut.match(event)) {
                System.out.println("Global Shortcut: Plugins triggered.");
                event.consume();
            } else if (helpShortcut.match(event)) {
                System.out.println("Global Shortcut: Help triggered.");
                event.consume();
            } else if (fullscreenShortcut.match(event)) {
                stage.setFullScreen(!stage.isFullScreen());
                event.consume();
            }
        });

        primaryStage.setScene(scene);

        // Restore window position/size if the user has saved it, else center it
        SettingsService.getInstance().restoreWindowState(stage);
        if (!SettingsService.getInstance().isRememberWindowPosition() || Double.isNaN(stage.getX())) {
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
