package com.example.dosebuddy.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for User entity
 * Defines database operations for User table
 */
@Dao
public interface UserDao {
    
    /**
     * Insert a new user into the database
     * @param user User to insert
     * @return The row ID of the inserted user
     */
    @Insert
    long insertUser(User user);
    
    /**
     * Update an existing user
     * @param user User to update
     * @return Number of rows updated
     */
    @Update
    int updateUser(User user);
    
    /**
     * Delete a user from the database
     * @param user User to delete
     * @return Number of rows deleted
     */
    @Delete
    int deleteUser(User user);
    
    /**
     * Get a user by username
     * @param username Username to search for
     * @return User object or null if not found
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);
    
    /**
     * Get a user by email
     * @param email Email to search for
     * @return User object or null if not found
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);
    
    /**
     * Get a user by ID
     * @param id User ID to search for
     * @return User object or null if not found
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(int id);
    
    /**
     * Check if username exists
     * @param username Username to check
     * @return True if username exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE username = :username")
    boolean isUsernameExists(String username);
    
    /**
     * Check if email exists
     * @param email Email to check
     * @return True if email exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email")
    boolean isEmailExists(String email);
    
    /**
     * Get all active users
     * @return List of all active users
     */
    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY created_at DESC")
    List<User> getAllActiveUsers();
    
    /**
     * Get total number of users
     * @return Total count of users
     */
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
    
    /**
     * Authenticate user with username and password hash
     * @param username Username
     * @param passwordHash Password hash
     * @return User object if authentication successful, null otherwise
     */
    @Query("SELECT * FROM users WHERE username = :username AND password_hash = :passwordHash AND is_active = 1 LIMIT 1")
    User authenticateUser(String username, String passwordHash);
}
