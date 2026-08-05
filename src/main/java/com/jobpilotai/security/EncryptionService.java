package com.jobpilotai.security;

import com.jobpilotai.logs.AppLogger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Handles encryption and decryption of sensitive local configuration data.
 */
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String INTERNAL_SECRET = "JobPilotAI_V4_Enterprise_Key_!@#";
    private static SecretKeySpec secretKey;

    static {
        try {
            byte[] key = INTERNAL_SECRET.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            AppLogger.error("Failed to initialize EncryptionService", e);
        }
    }

    /**
     * Encrypts a plain text string using AES.
     */
    public static String encrypt(String strToEncrypt) {
        if (strToEncrypt == null || strToEncrypt.trim().isEmpty()) {
            return strToEncrypt;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            AppLogger.error("Error while encrypting: " + e.toString(), e);
        }
        return null;
    }

    /**
     * Decrypts an AES-encrypted base64 string.
     */
    public static String decrypt(String strToDecrypt) {
        if (strToDecrypt == null || strToDecrypt.trim().isEmpty()) {
            return strToDecrypt;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            // If decryption fails, it might be legacy unencrypted data, return it as-is.
            return strToDecrypt;
        }
    }
}
