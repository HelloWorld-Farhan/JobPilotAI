package com.jobpilotai.diagnostics;

import com.jobpilotai.database.DatabaseManager;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Service to aggregate system health and performance metrics.
 */
public class DiagnosticsService {

    public static class SystemHealth {
        public String javaVersion;
        public long maxMemoryMb;
        public long allocatedMemoryMb;
        public long freeMemoryMb;
        public long totalFreeMemoryMb;
        public long dbSizeKb;
        public boolean dbHealthy;
    }

    public static SystemHealth getHealth() {
        SystemHealth health = new SystemHealth();
        
        // Java & Memory
        health.javaVersion = System.getProperty("java.version");
        Runtime runtime = Runtime.getRuntime();
        health.maxMemoryMb = runtime.maxMemory() / (1024 * 1024);
        health.allocatedMemoryMb = runtime.totalMemory() / (1024 * 1024);
        health.freeMemoryMb = runtime.freeMemory() / (1024 * 1024);
        health.totalFreeMemoryMb = health.freeMemoryMb + (health.maxMemoryMb - health.allocatedMemoryMb);
        
        // Database
        File dbFile = new File("jobpilotai.db");
        if (dbFile.exists()) {
            health.dbSizeKb = dbFile.length() / 1024;
        } else {
            health.dbSizeKb = 0;
        }
        
        health.dbHealthy = checkDbHealth();
        
        return health;
    }
    
    private static boolean checkDbHealth() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
