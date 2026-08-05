package com.jobpilotai.restore;

import com.jobpilotai.audit.AuditLogger;
import com.jobpilotai.logs.AppLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handles extracting and restoring data from backups.
 */
public class RestoreManager {

    /**
     * Extracts a backup ZIP file and overwrites current data.
     * WARNING: This should only be called after user confirmation!
     */
    public static boolean restoreBackup(File backupZip) {
        if (!backupZip.exists() || !backupZip.getName().endsWith(".zip")) {
            AppLogger.error("Invalid backup file: " + backupZip.getAbsolutePath(), null);
            return false;
        }
        
        File destDir = new File("."); // Extract to root
        byte[] buffer = new byte[1024];
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backupZip))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            
            AppLogger.info("Restore completed successfully from " + backupZip.getName());
            AuditLogger.log("BACKUP_RESTORED", "Restored from " + backupZip.getName());
            return true;
            
        } catch (IOException e) {
            AppLogger.error("Failed to restore backup", e);
            return false;
        }
    }
    
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}
