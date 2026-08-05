package com.jobpilotai.search;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Universal Search Service for JobPilotAI.
 * Searches across applications, companies, documents, and notes.
 */
public class SearchService {

    public static class SearchResult {
        public String type; // "Application", "Company", "Document", "Log"
        public String title;
        public String snippet;
        
        public SearchResult(String type, String title, String snippet) {
            this.type = type;
            this.title = title;
            this.snippet = snippet;
        }
    }

    public static List<SearchResult> searchAll(String query) {
        List<SearchResult> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return results;
        }
        
        String term = "%" + query.trim() + "%";

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // 1. Search Applications (Job Title or Company or Notes)
            String sqlApps = "SELECT company, job_title, status FROM applications WHERE company LIKE ? OR job_title LIKE ? OR notes LIKE ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlApps)) {
                pstmt.setString(1, term);
                pstmt.setString(2, term);
                pstmt.setString(3, term);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(new SearchResult("Application", 
                            rs.getString("company") + " - " + rs.getString("job_title"), 
                            "Status: " + rs.getString("status")));
                    }
                }
            }
            
            // 2. Search Companies
            String sqlComp = "SELECT name, industry FROM company_records WHERE name LIKE ? OR industry LIKE ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlComp)) {
                pstmt.setString(1, term);
                pstmt.setString(2, term);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(new SearchResult("Company", 
                            rs.getString("name"), 
                            "Industry: " + rs.getString("industry")));
                    }
                }
            }
            
            // 3. Search Documents
            String sqlDocs = "SELECT title, doc_type FROM documents WHERE title LIKE ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDocs)) {
                pstmt.setString(1, term);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(new SearchResult("Document", 
                            rs.getString("title"), 
                            "Type: " + rs.getString("doc_type")));
                    }
                }
            }

        } catch (SQLException e) {
            AppLogger.error("Failed to perform universal search", e);
        }
        
        return results;
    }
}
