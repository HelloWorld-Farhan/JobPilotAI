package com.jobpilotai.cache;

import com.jobpilotai.config.PathConfig;
import com.jobpilotai.logs.AppLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

/**
 * Persists cached JSON/String data to the disk to survive application restarts.
 */
public class DiskCache {

    private final Path cacheDir;

    public DiskCache(String cacheFolderName) {
        this.cacheDir = Paths.get(PathConfig.getAppDir(), cacheFolderName);
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            AppLogger.error("Failed to create DiskCache directory", e);
        }
    }

    public void put(String key, String data) {
        try {
            Path file = cacheDir.resolve(hashKey(key));
            Files.writeString(file, data);
        } catch (Exception e) {
            AppLogger.warn("Failed to write to DiskCache", e);
        }
    }

    public String get(String key) {
        try {
            Path file = cacheDir.resolve(hashKey(key));
            if (Files.exists(file)) {
                return Files.readString(file);
            }
        } catch (Exception e) {
            AppLogger.warn("Failed to read from DiskCache", e);
        }
        return null;
    }
    
    public void clear() {
        try {
            File[] files = cacheDir.toFile().listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        } catch (Exception e) {
            AppLogger.warn("Failed to clear DiskCache", e);
        }
    }

    private String hashKey(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(key.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return String.valueOf(key.hashCode());
        }
    }
}
