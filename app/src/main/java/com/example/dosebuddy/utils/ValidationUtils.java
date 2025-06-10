package com.example.dosebuddy.utils;

import android.util.Patterns;

/**
 * Utility class for input validation
 * Contains methods to validate user input fields
 */
public class ValidationUtils {
    
    /**
     * Check if a string is null or empty
     * @param text String to check
     * @return True if string is null or empty, false otherwise
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
    
    /**
     * Validate email address format
     * @param email Email address to validate
     * @return True if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return !isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Validate username format
     * Username should be 3-20 characters, alphanumeric and underscore only
     * @param username Username to validate
     * @return True if username is valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (isEmpty(username)) {
            return false;
        }
        
        // Check length (3-20 characters)
        if (username.length() < 3 || username.length() > 20) {
            return false;
        }
        
        // Check format (alphanumeric and underscore only)
        return username.matches("^[a-zA-Z0-9_]+$");
    }
    
    /**
     * Validate full name
     * Should contain only letters, spaces, and common punctuation
     * @param fullName Full name to validate
     * @return True if full name is valid, false otherwise
     */
    public static boolean isValidFullName(String fullName) {
        if (isEmpty(fullName)) {
            return false;
        }
        
        // Check length (2-50 characters)
        if (fullName.length() < 2 || fullName.length() > 50) {
            return false;
        }
        
        // Check format (letters, spaces, apostrophes, hyphens, and dots)
        return fullName.matches("^[a-zA-Z\\s'.-]+$");
    }
    
    /**
     * Validate password strength
     * @param password Password to validate
     * @return True if password meets requirements, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return PasswordUtils.isValidPassword(password);
    }
    
    /**
     * Check if two passwords match
     * @param password First password
     * @param confirmPassword Second password
     * @return True if passwords match, false otherwise
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
    
    /**
     * Get validation error message for username
     * @param username Username to validate
     * @return Error message or null if valid
     */
    public static String getUsernameError(String username) {
        if (isEmpty(username)) {
            return "Username cannot be empty";
        }
        if (username.length() < 3) {
            return "Username must be at least 3 characters";
        }
        if (username.length() > 20) {
            return "Username must be less than 20 characters";
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Username can only contain letters, numbers, and underscores";
        }
        return null;
    }
    
    /**
     * Get validation error message for email
     * @param email Email to validate
     * @return Error message or null if valid
     */
    public static String getEmailError(String email) {
        if (isEmpty(email)) {
            return "Email cannot be empty";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address";
        }
        return null;
    }
    
    /**
     * Get validation error message for full name
     * @param fullName Full name to validate
     * @return Error message or null if valid
     */
    public static String getFullNameError(String fullName) {
        if (isEmpty(fullName)) {
            return "Full name cannot be empty";
        }
        if (fullName.length() < 2) {
            return "Full name must be at least 2 characters";
        }
        if (fullName.length() > 50) {
            return "Full name must be less than 50 characters";
        }
        if (!fullName.matches("^[a-zA-Z\\s'.-]+$")) {
            return "Full name can only contain letters, spaces, and common punctuation";
        }
        return null;
    }
    
    /**
     * Get validation error message for password
     * @param password Password to validate
     * @return Error message or null if valid
     */
    public static String getPasswordError(String password) {
        if (isEmpty(password)) {
            return "Password cannot be empty";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        return null;
    }

    // Medication Validation Methods

    /**
     * Validate medication name
     * @param medicationName Medication name to validate
     * @return True if valid, false otherwise
     */
    public static boolean isValidMedicationName(String medicationName) {
        if (isEmpty(medicationName)) {
            return false;
        }

        // Check length (2-100 characters)
        if (medicationName.length() < 2 || medicationName.length() > 100) {
            return false;
        }

        // Allow letters, numbers, spaces, hyphens, and common punctuation
        return medicationName.matches("^[a-zA-Z0-9\\s\\-.,()]+$");
    }

    /**
     * Validate dosage
     * @param dosage Dosage to validate
     * @return True if valid, false otherwise
     */
    public static boolean isValidDosage(String dosage) {
        if (isEmpty(dosage)) {
            return false;
        }

        // Check length (1-50 characters)
        if (dosage.length() < 1 || dosage.length() > 50) {
            return false;
        }

        // Allow letters, numbers, spaces, and common dosage symbols
        return dosage.matches("^[a-zA-Z0-9\\s\\-.,/()%]+$");
    }

    /**
     * Validate times per day
     * @param timesPerDay Times per day value
     * @return True if valid, false otherwise
     */
    public static boolean isValidTimesPerDay(int timesPerDay) {
        return timesPerDay >= 1 && timesPerDay <= 10;
    }

    /**
     * Validate date range (end date must be after start date)
     * @param startDate Start date timestamp
     * @param endDate End date timestamp (can be null)
     * @return True if valid, false otherwise
     */
    public static boolean isValidDateRange(long startDate, Long endDate) {
        if (endDate == null) {
            return true; // End date is optional
        }
        return endDate >= startDate;
    }

    /**
     * Get validation error message for medication name
     * @param medicationName Medication name to validate
     * @return Error message or null if valid
     */
    public static String getMedicationNameError(String medicationName) {
        if (isEmpty(medicationName)) {
            return "Medication name cannot be empty";
        }
        if (medicationName.length() < 2) {
            return "Medication name must be at least 2 characters";
        }
        if (medicationName.length() > 100) {
            return "Medication name must be less than 100 characters";
        }
        if (!medicationName.matches("^[a-zA-Z0-9\\s\\-.,()]+$")) {
            return "Medication name contains invalid characters";
        }
        return null;
    }

    /**
     * Get validation error message for dosage
     * @param dosage Dosage to validate
     * @return Error message or null if valid
     */
    public static String getDosageError(String dosage) {
        if (isEmpty(dosage)) {
            return "Dosage cannot be empty";
        }
        if (dosage.length() < 1) {
            return "Dosage must be specified";
        }
        if (dosage.length() > 50) {
            return "Dosage must be less than 50 characters";
        }
        if (!dosage.matches("^[a-zA-Z0-9\\s\\-.,/()%]+$")) {
            return "Dosage contains invalid characters";
        }
        return null;
    }

    /**
     * Get validation error message for times per day
     * @param timesPerDay Times per day value
     * @return Error message or null if valid
     */
    public static String getTimesPerDayError(int timesPerDay) {
        if (timesPerDay < 1) {
            return "Must take medication at least once per day";
        }
        if (timesPerDay > 10) {
            return "Cannot take medication more than 10 times per day";
        }
        return null;
    }

    /**
     * Get validation error message for date range
     * @param startDate Start date timestamp
     * @param endDate End date timestamp (can be null)
     * @return Error message or null if valid
     */
    public static String getDateRangeError(long startDate, Long endDate) {
        if (endDate != null && endDate < startDate) {
            return "End date cannot be before start date";
        }
        return null;
    }
}
