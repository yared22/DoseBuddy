package com.example.dosebuddy.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for Medication entity
 * Defines database operations for Medication table
 */
@Dao
public interface MedicationDao {
    
    /**
     * Insert a new medication into the database
     * @param medication Medication to insert
     * @return The row ID of the inserted medication
     */
    @Insert
    long insertMedication(Medication medication);
    
    /**
     * Update an existing medication
     * @param medication Medication to update
     * @return Number of rows updated
     */
    @Update
    int updateMedication(Medication medication);
    
    /**
     * Delete a medication from the database
     * @param medication Medication to delete
     * @return Number of rows deleted
     */
    @Delete
    int deleteMedication(Medication medication);
    
    /**
     * Get a medication by ID
     * @param id Medication ID
     * @return Medication object or null if not found
     */
    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    Medication getMedicationById(int id);
    
    /**
     * Get all medications for a specific user
     * @param userId User ID
     * @return List of medications for the user
     */
    @Query("SELECT * FROM medications WHERE user_id = :userId AND is_active = 1 ORDER BY created_at DESC")
    List<Medication> getMedicationsForUser(int userId);
    
    /**
     * Get all active medications for a specific user
     * @param userId User ID
     * @return List of active medications for the user
     */
    @Query("SELECT * FROM medications WHERE user_id = :userId AND is_active = 1 ORDER BY name ASC")
    List<Medication> getActiveMedicationsForUser(int userId);
    
    /**
     * Get medications by name for a specific user
     * @param userId User ID
     * @param name Medication name (case-insensitive)
     * @return List of medications matching the name
     */
    @Query("SELECT * FROM medications WHERE user_id = :userId AND LOWER(name) LIKE LOWER(:name) AND is_active = 1")
    List<Medication> getMedicationsByName(int userId, String name);
    
    /**
     * Get medications due today for a specific user
     * @param userId User ID
     * @param todayStart Start of today (timestamp)
     * @param todayEnd End of today (timestamp)
     * @return List of medications due today
     */
    @Query("SELECT * FROM medications WHERE user_id = :userId AND is_active = 1 " +
           "AND start_date <= :todayEnd " +
           "AND (end_date IS NULL OR end_date >= :todayStart) " +
           "ORDER BY name ASC")
    List<Medication> getMedicationsDueToday(int userId, long todayStart, long todayEnd);
    
    /**
     * Get medications by frequency for a specific user
     * @param userId User ID
     * @param frequency Frequency string
     * @return List of medications with the specified frequency
     */
    @Query("SELECT * FROM medications WHERE user_id = :userId AND frequency = :frequency AND is_active = 1")
    List<Medication> getMedicationsByFrequency(int userId, String frequency);
    
    /**
     * Check if a medication name already exists for a user
     * @param userId User ID
     * @param name Medication name
     * @return True if medication name exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM medications WHERE user_id = :userId AND LOWER(name) = LOWER(:name) AND is_active = 1")
    boolean isMedicationNameExists(int userId, String name);
    
    /**
     * Get total count of medications for a user
     * @param userId User ID
     * @return Total count of active medications
     */
    @Query("SELECT COUNT(*) FROM medications WHERE user_id = :userId AND is_active = 1")
    int getMedicationCount(int userId);
    
    /**
     * Soft delete a medication (set is_active to false)
     * @param medicationId Medication ID
     * @return Number of rows updated
     */
    @Query("UPDATE medications SET is_active = 0, updated_at = :timestamp WHERE id = :medicationId")
    int softDeleteMedication(int medicationId, long timestamp);
    
    /**
     * Reactivate a medication (set is_active to true)
     * @param medicationId Medication ID
     * @return Number of rows updated
     */
    @Query("UPDATE medications SET is_active = 1, updated_at = :timestamp WHERE id = :medicationId")
    int reactivateMedication(int medicationId, long timestamp);
    
    /**
     * Get all medications (including inactive) for a user
     * @param userId User ID
     * @return List of all medications for the user
     */
    @Query("SELECT * FROM medications WHERE user_id = :userId ORDER BY is_active DESC, updated_at DESC")
    List<Medication> getAllMedicationsForUser(int userId);
    
    /**
     * Update medication times
     * @param medicationId Medication ID
     * @param specificTimes JSON string of specific times
     * @param timestamp Update timestamp
     * @return Number of rows updated
     */
    @Query("UPDATE medications SET specific_times = :specificTimes, updated_at = :timestamp WHERE id = :medicationId")
    int updateMedicationTimes(int medicationId, String specificTimes, long timestamp);
    
    /**
     * Search medications by name or dosage
     * @param userId User ID
     * @param searchQuery Search query
     * @return List of medications matching the search
     */
    @Query("SELECT * FROM medications WHERE user_id = :userId AND is_active = 1 " +
           "AND (LOWER(name) LIKE LOWER(:searchQuery) OR LOWER(dosage) LIKE LOWER(:searchQuery)) " +
           "ORDER BY name ASC")
    List<Medication> searchMedications(int userId, String searchQuery);
}
