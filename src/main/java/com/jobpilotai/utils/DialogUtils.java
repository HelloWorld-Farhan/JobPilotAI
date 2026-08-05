package com.jobpilotai.utils;

import com.jobpilotai.service.SettingsService;
import com.jobpilotai.themes.ThemeEngine;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;
import java.util.Optional;

public class DialogUtils {

    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        styleDialog(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        styleDialog(alert);
        alert.showAndWait();
    }

    private static void styleDialog(Alert alert) {
        alert.initStyle(StageStyle.UNDECORATED);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().clear();
        dialogPane.getStylesheets().add(ThemeEngine.class.getResource("/css/base.css").toExternalForm());
        
        String themeFile = ThemeEngine.getAvailableThemes().getOrDefault(
                SettingsService.getInstance().getTheme().toLowerCase(), 
                ThemeEngine.getAvailableThemes().get("dark")
        );
        dialogPane.getStylesheets().add(ThemeEngine.class.getResource(themeFile).toExternalForm());
        
        dialogPane.setStyle("-fx-border-color: #3B82F6; -fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px;");
    }
}
