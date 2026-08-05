package com.jobpilotai.controller;

import com.jobpilotai.service.SettingsService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class CoverLetterController implements Initializable {

    @FXML private TextArea taCoverLetter;
    @FXML private Label lblStatus;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String saved = SettingsService.getInstance().getUniversalCoverLetter();
        if (saved != null) {
            taCoverLetter.setText(saved);
        }
    }

    @FXML private void onSave() {
        String text = taCoverLetter.getText();
        SettingsService.getInstance().setUniversalCoverLetter(text);
        SettingsService.getInstance().save();
        
        lblStatus.setText("Saved successfully!");
        PauseTransition pt = new PauseTransition(Duration.seconds(3));
        pt.setOnFinished(e -> lblStatus.setText(""));
        pt.play();
    }
}
