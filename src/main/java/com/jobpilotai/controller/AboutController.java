package com.jobpilotai.controller;

import com.jobpilotai.config.AppConfig;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the About panel.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class AboutController implements Initializable {

    @FXML private Label versionLabel;
    @FXML private Label authorLabel;
    @FXML private Hyperlink githubLink;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        versionLabel.setText(AppConfig.APP_NAME + " " + AppConfig.APP_VERSION);
        authorLabel .setText("Built by " + AppConfig.APP_AUTHOR);
        githubLink  .setText("https://github.com/HelloWorld-Farhan/JobPilotAI");
        githubLink  .setOnAction(e -> {
            try { Desktop.getDesktop().browse(new URI("https://github.com/HelloWorld-Farhan/JobPilotAI")); }
            catch (Exception ex) { /* ignore */ }
        });
    }
}
