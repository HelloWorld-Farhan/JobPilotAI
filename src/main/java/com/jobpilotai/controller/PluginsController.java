package com.jobpilotai.controller;

import com.jobpilotai.plugins.IJobPilotPlugin;
import com.jobpilotai.plugins.PluginLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;

import java.net.URL;
import java.util.ResourceBundle;

public class PluginsController implements Initializable {

    @FXML private ListView<IJobPilotPlugin> lvPlugins;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<IJobPilotPlugin> items = FXCollections.observableArrayList(PluginLoader.getActivePlugins());
        lvPlugins.setItems(items);
        
        lvPlugins.setCellFactory(param -> new ListCell<IJobPilotPlugin>() {
            @Override
            protected void updateItem(IJobPilotPlugin item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("🧩 " + item.getName() + " (v" + item.getVersion() + ")\n   " + item.getDescription());
                    setStyle("-fx-text-fill: white; -fx-padding: 10px;");
                }
            }
        });
    }
}
