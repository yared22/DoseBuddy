package com.example.dosebuddy;

import com.example.dosebuddy.utils.PasswordUtils;
import com.example.dosebuddy.utils.ValidationUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for PasswordUtils and ValidationUtils
 */
public class PasswordUtilsTest {

    @Test
    public void testPasswordHashing() {
        String password = "testPassword123";
        String hash = PasswordUtils.createPasswordHash(password);
        
        // Hash should not be null or empty
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        
        // Hash should be different from original password
        assertNotEquals(password, hash);
        
        // Hash should be consistent length (32 chars salt + 64 chars hash = 96 chars)
        assertEquals(96, hash.length());
    }

    @Test
    public void testPasswordVerification() {
        String password = "mySecurePassword";
        String hash = PasswordUtils.createPasswordHash(password);
        
        // Correct password should verify
        assertTrue(PasswordUtils.verifyPassword(password, hash));
        
        // Wrong password should not verify
        assertFalse(PasswordUtils.verifyPassword("wrongPassword", hash));
        
        // Empty password should not verify
        assertFalse(PasswordUtils.verifyPassword("", hash));
        
        // Null password should not verify
        assertFalse(PasswordUtils.verifyPassword(null, hash));
    }

    @Test
    public void testPasswordValidation() {
        // Valid passwords
        assertTrue(PasswordUtils.isValidPassword("password123"));
        assertTrue(PasswordUtils.isValidPassword("123456"));
        assertTrue(PasswordUtils.isValidPassword("verylongpassword"));
        
        // Invalid passwords
        assertFalse(PasswordUtils.isValidPassword("12345")); // Too short
        assertFalse(PasswordUtils.isValidPassword("")); // Empty
        assertFalse(PasswordUtils.isValidPassword(null)); // Null
    }

    // Note: Email validation test skipped because Android Patterns class is not available in unit tests
    // Email validation will be tested in instrumented tests

    @Test
    public void testUsernameValidation() {
        // Valid usernames
        assertTrue(ValidationUtils.isValidUsername("user123"));
        assertTrue(ValidationUtils.isValidUsername("test_user"));
        assertTrue(ValidationUtils.isValidUsername("username"));
        
        // Invalid usernames
        assertFalse(ValidationUtils.isValidUsername("us")); // Too short
        assertFalse(ValidationUtils.isValidUsername("user@name")); // Invalid character
        assertFalse(ValidationUtils.isValidUsername("user name")); // Space
        assertFalse(ValidationUtils.isValidUsername("")); // Empty
        assertFalse(ValidationUtils.isValidUsername(null)); // Null
    }

    @Test
    public void testFullNameValidation() {
        // Valid names
        assertTrue(ValidationUtils.isValidFullName("John Doe"));
        assertTrue(ValidationUtils.isValidFullName("Mary Jane"));
        assertTrue(ValidationUtils.isValidFullName("O'Connor"));
        assertTrue(ValidationUtils.isValidFullName("Jean-Pierre"));
        
        // Invalid names
        assertFalse(ValidationUtils.isValidFullName("A")); // Too short
        assertFalse(ValidationUtils.isValidFullName("John123")); // Numbers
        assertFalse(ValidationUtils.isValidFullName("")); // Empty
        assertFalse(ValidationUtils.isValidFullName(null)); // Null
    }

    @Test
    public void testPasswordMatching() {
        String password1 = "password123";
        String password2 = "password123";
        String password3 = "differentPassword";
        
        // Same passwords should match
        assertTrue(ValidationUtils.doPasswordsMatch(password1, password2));
        
        // Different passwords should not match
        assertFalse(ValidationUtils.doPasswordsMatch(password1, password3));
        
        // Null handling
        assertFalse(ValidationUtils.doPasswordsMatch(null, password1));
        assertFalse(ValidationUtils.doPasswordsMatch(password1, null));
        assertFalse(ValidationUtils.doPasswordsMatch(null, null));
    }
}
