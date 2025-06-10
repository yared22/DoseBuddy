package com.example.dosebuddy.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class for password hashing and verification
 * Uses SHA-256 with salt for secure password storage
 */
public class PasswordUtils {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * Generate a random salt
     * @return Random salt as byte array
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }
    
    /**
     * Convert byte array to hexadecimal string
     * @param bytes Byte array to convert
     * @return Hexadecimal string representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Convert hexadecimal string to byte array
     * @param hex Hexadecimal string
     * @return Byte array
     */
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
    
    /**
     * Hash password with salt
     * @param password Plain text password
     * @param salt Salt bytes
     * @return Hashed password as hexadecimal string
     */
    private static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return bytesToHex(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Create password hash with salt for storage
     * @param password Plain text password
     * @return Salt + hash combined as hexadecimal string
     */
    public static String createPasswordHash(String password) {
        byte[] salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        return bytesToHex(salt) + hashedPassword;
    }
    
    /**
     * Verify password against stored hash
     * @param password Plain text password to verify
     * @param storedHash Stored hash (salt + hash)
     * @return True if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Extract salt from stored hash (first 32 characters = 16 bytes in hex)
            String saltHex = storedHash.substring(0, SALT_LENGTH * 2);
            String hashHex = storedHash.substring(SALT_LENGTH * 2);
            
            byte[] salt = hexToBytes(saltHex);
            String hashedPassword = hashPassword(password, salt);
            
            return hashedPassword.equals(hashHex);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if password meets minimum requirements
     * @param password Password to check
     * @return True if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
