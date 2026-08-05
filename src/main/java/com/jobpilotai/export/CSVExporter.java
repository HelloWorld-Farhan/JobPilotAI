package com.jobpilotai.export;

import com.jobpilotai.database.DatabaseManager;
import com.jobpilotai.logs.AppLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CSVExporter {

    public static boolean exportApplications(File csvFile) {
        String sql = "SELECT * FROM applications";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
             
            // Header
            writer.write("ID,Company,Job Title,Website,Job URL,Status,Date,Time,Resume Used,Notes,Attempt Count,Created At,Salary,Location,Recruiter\n");
            
            while (rs.next()) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%s,%s,%s,%s\n",
                    rs.getInt("id"),
                    escape(rs.getString("company")),
                    escape(rs.getString("job_title")),
                    escape(rs.getString("website")),
                    escape(rs.getString("job_url")),
                    escape(rs.getString("status")),
                    escape(rs.getString("date")),
                    escape(rs.getString("time")),
                    escape(rs.getString("resume_used")),
                    escape(rs.getString("notes")),
                    rs.getInt("attempt_count"),
                    escape(rs.getString("created_at")),
                    escape(rs.getString("salary")),
                    escape(rs.getString("location")),
                    escape(rs.getString("recruiter"))
                ));
            }
            
            AppLogger.info("Exported applications to CSV: " + csvFile.getAbsolutePath());
            return true;
            
        } catch (Exception e) {
            AppLogger.error("Failed to export to CSV", e);
            return false;
        }
    }
    
    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
