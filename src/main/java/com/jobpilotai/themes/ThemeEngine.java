package com.jobpilotai.themes;

import javafx.scene.Scene;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the application-wide CSS themes.
 */
public class ThemeEngine {

    private static final Map<String, String> THEMES = new HashMap<>();

    static {
        THEMES.put("dark", "/css/dark-theme.css");
        THEMES.put("light", "/css/light-theme.css");
        THEMES.put("blue", "/css/blue-theme.css");
        THEMES.put("midnight", "/css/midnight-theme.css");
    }

    public static void applyTheme(Scene scene, String themeName) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(ThemeEngine.class.getResource("/css/base.css").toExternalForm());
        
        String themeFile = THEMES.getOrDefault(themeName.toLowerCase(), THEMES.get("dark"));
        scene.getStylesheets().add(ThemeEngine.class.getResource(themeFile).toExternalForm());
    }
    
    public static Map<String, String> getAvailableThemes() {
        return THEMES;
    }
}
