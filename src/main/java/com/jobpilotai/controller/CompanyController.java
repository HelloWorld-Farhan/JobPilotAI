package com.jobpilotai.controller;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CompanyController implements Initializable {

    public static class CompanyInsight {
        public StringProperty name = new SimpleStringProperty();
        public IntegerProperty apps = new SimpleIntegerProperty();
        public IntegerProperty interviews = new SimpleIntegerProperty();
        public IntegerProperty offers = new SimpleIntegerProperty();
        
        public CompanyInsight(String name, int apps, int interviews, int offers) {
            this.name.set(name);
            this.apps.set(apps);
            this.interviews.set(interviews);
            this.offers.set(offers);
        }
    }

    @FXML private TableView<CompanyInsight> table;
    @FXML private TableColumn<CompanyInsight, String> colName;
    @FXML private TableColumn<CompanyInsight, Number> colApps;
    @FXML private TableColumn<CompanyInsight, Number> colInterviews;
    @FXML private TableColumn<CompanyInsight, Number> colOffers;

    private final ObservableList<CompanyInsight> insightsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colName.setCellValueFactory(cellData -> cellData.getValue().name);
        colApps.setCellValueFactory(cellData -> cellData.getValue().apps);
        colInterviews.setCellValueFactory(cellData -> cellData.getValue().interviews);
        colOffers.setCellValueFactory(cellData -> cellData.getValue().offers);
        
        table.setItems(insightsList);
        loadInsights();
    }

    private void loadInsights() {
        insightsList.clear();
        String sql = """
            SELECT company, 
                   COUNT(id) as total_apps, 
                   SUM(CASE WHEN status LIKE '%Interview%' THEN 1 ELSE 0 END) as interviews,
                   SUM(CASE WHEN status LIKE '%Offer%' THEN 1 ELSE 0 END) as offers
            FROM applications 
            GROUP BY company 
            ORDER BY total_apps DESC
        """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
             
            while (rs.next()) {
                insightsList.add(new CompanyInsight(
                    rs.getString("company"),
                    rs.getInt("total_apps"),
                    rs.getInt("interviews"),
                    rs.getInt("offers")
                ));
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to load company insights", e);
        }
    }
}
