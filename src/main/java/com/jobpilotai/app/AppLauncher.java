package com.jobpilotai.app;

/**
 * AppLauncher is the true entry point for the application.
 * <p>
 * This class exists as a workaround for the JavaFX module system requirement
 * that the main class must NOT extend {@link javafx.application.Application}
 * when packaging as a fat JAR (e.g. with maven-shade-plugin). Without this
 * indirection the JVM throws an error about missing JavaFX runtime components.
 * </p>
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class AppLauncher {

    /**
     * Application entry point.
     *
     * @param args command-line arguments forwarded to {@link MainApp}
     */
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
