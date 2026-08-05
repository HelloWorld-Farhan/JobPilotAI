package com.jobpilotai.controller;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AnalyticsController implements Initializable {

    @FXML private BarChart<String, Number> appsByMonthChart;
    @FXML private PieChart statusPieChart;
    
    @FXML private Label lblInterviewRate;
    @FXML private Label lblOfferRate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMonthlyData();
        loadStatusData();
        loadRates();
    }

    private void loadMonthlyData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Applications");
        
        String sql = """
            SELECT strftime('%Y-%m', created_at) as month, COUNT(id) as count 
            FROM applications 
            GROUP BY month 
            ORDER BY month ASC 
            LIMIT 12
        """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
             
            while (rs.next()) {
                String month = rs.getString("month");
                int count = rs.getInt("count");
                if (month != null) {
                    series.getData().add(new XYChart.Data<>(month, count));
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to load monthly analytics", e);
        }
        
        appsByMonthChart.getData().add(series);
    }

    private void loadStatusData() {
        String sql = "SELECT status, COUNT(id) as count FROM applications GROUP BY status";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
             
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                statusPieChart.getData().add(new PieChart.Data(status + " (" + count + ")", count));
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to load status analytics", e);
        }
    }
    
    private void loadRates() {
        String sql = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN status LIKE '%Interview%' THEN 1 ELSE 0 END) as interviews,
                SUM(CASE WHEN status LIKE '%Offer%' THEN 1 ELSE 0 END) as offers
            FROM applications
        """;
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
             
            if (rs.next()) {
                int total = rs.getInt("total");
                int interviews = rs.getInt("interviews");
                int offers = rs.getInt("offers");
                
                if (total > 0) {
                    int iRate = (int) (((double) interviews / total) * 100);
                    int oRate = (int) (((double) offers / total) * 100);
                    lblInterviewRate.setText(iRate + "%");
                    lblOfferRate.setText(oRate + "%");
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Failed to load rates", e);
        }
    }
}
