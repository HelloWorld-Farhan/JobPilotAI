package com.jobpilotai.export;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CSVImporter {

    public static boolean importApplications(File csvFile) {
        String sql = "INSERT INTO applications (company, job_title, website, job_url, status, date, time, resume_used, notes, attempt_count, created_at, salary, location, recruiter) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
             
            String line = reader.readLine(); // Skip header
            
            int count = 0;
            while ((line = reader.readLine()) != null) {
                // Basic CSV split for non-quoted, for a real app consider Apache Commons CSV
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                if (parts.length >= 14) {
                    pstmt.setString(1, unescape(parts[1]));
                    pstmt.setString(2, unescape(parts[2]));
                    pstmt.setString(3, unescape(parts[3]));
                    pstmt.setString(4, unescape(parts[4]));
                    pstmt.setString(5, unescape(parts[5]));
                    pstmt.setString(6, unescape(parts[6]));
                    pstmt.setString(7, unescape(parts[7]));
                    pstmt.setString(8, unescape(parts[8]));
                    pstmt.setString(9, unescape(parts[9]));
                    
                    try {
                        pstmt.setInt(10, Integer.parseInt(unescape(parts[10])));
                    } catch (Exception e) {
                        pstmt.setInt(10, 1);
                    }
                    
                    pstmt.setString(11, unescape(parts[11]));
                    pstmt.setString(12, unescape(parts[12]));
                    pstmt.setString(13, unescape(parts[13]));
                    pstmt.setString(14, unescape(parts[14]));
                    
                    pstmt.addBatch();
                    count++;
                }
            }
            
            pstmt.executeBatch();
            AppLogger.info("Imported " + count + " applications from CSV.");
            return true;
            
        } catch (Exception e) {
            AppLogger.error("Failed to import CSV", e);
            return false;
        }
    }
    
    private static String unescape(String value) {
        if (value == null) return "";
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\"\"", "\"");
        }
        return value;
    }
}
