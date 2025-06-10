package com.example.dosebuddy.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for MedicationHistory entity
 * Defines database operations for medication history tracking
 */
@Dao
public interface MedicationHistoryDao {
    
    /**
     * Insert a new medication history record
     * @param history History record to insert
     * @return The row ID of the inserted record
     */
    @Insert
    long insertHistory(MedicationHistory history);
    
    /**
     * Update an existing history record
     * @param history History record to update
     * @return Number of rows updated
     */
    @Update
    int updateHistory(MedicationHistory history);
    
    /**
     * Delete a history record
     * @param history History record to delete
     * @return Number of rows deleted
     */
    @Delete
    int deleteHistory(MedicationHistory history);
    
    /**
     * Get history record by ID
     * @param id History record ID
     * @return History record or null if not found
     */
    @Query("SELECT * FROM medication_history WHERE id = :id LIMIT 1")
    MedicationHistory getHistoryById(int id);
    
    /**
     * Get all history for a specific user
     * @param userId User ID
     * @return List of history records ordered by taken_at descending
     */
    @Query("SELECT * FROM medication_history WHERE user_id = :userId ORDER BY taken_at DESC")
    List<MedicationHistory> getHistoryForUser(int userId);
    
    /**
     * Get history for a specific medication
     * @param medicationId Medication ID
     * @return List of history records ordered by taken_at descending
     */
    @Query("SELECT * FROM medication_history WHERE medication_id = :medicationId ORDER BY taken_at DESC")
    List<MedicationHistory> getHistoryForMedication(int medicationId);
    
    /**
     * Get history for a specific user and medication
     * @param userId User ID
     * @param medicationId Medication ID
     * @return List of history records ordered by taken_at descending
     */
    @Query("SELECT * FROM medication_history WHERE user_id = :userId AND medication_id = :medicationId ORDER BY taken_at DESC")
    List<MedicationHistory> getHistoryForUserAndMedication(int userId, int medicationId);
    
    /**
     * Get history within a date range
     * @param userId User ID
     * @param startTime Start time (inclusive)
     * @param endTime End time (inclusive)
     * @return List of history records in the date range
     */
    @Query("SELECT * FROM medication_history WHERE user_id = :userId AND taken_at >= :startTime AND taken_at <= :endTime ORDER BY taken_at DESC")
    List<MedicationHistory> getHistoryInDateRange(int userId, long startTime, long endTime);
    
    /**
     * Get history for a medication within a date range
     * @param medicationId Medication ID
     * @param startTime Start time (inclusive)
     * @param endTime End time (inclusive)
     * @return List of history records in the date range
     */
    @Query("SELECT * FROM medication_history WHERE medication_id = :medicationId AND taken_at >= :startTime AND taken_at <= :endTime ORDER BY taken_at DESC")
    List<MedicationHistory> getMedicationHistoryInDateRange(int medicationId, long startTime, long endTime);
    
    /**
     * Get the last taken record for a medication
     * @param medicationId Medication ID
     * @return Most recent history record or null
     */
    @Query("SELECT * FROM medication_history WHERE medication_id = :medicationId ORDER BY taken_at DESC LIMIT 1")
    MedicationHistory getLastTakenForMedication(int medicationId);
    
    /**
     * Get total doses taken for a medication
     * @param medicationId Medication ID
     * @return Total number of doses taken
     */
    @Query("SELECT COUNT(*) FROM medication_history WHERE medication_id = :medicationId")
    int getTotalDosesTaken(int medicationId);
    
    /**
     * Get doses taken for a medication within a date range
     * @param medicationId Medication ID
     * @param startTime Start time (inclusive)
     * @param endTime End time (inclusive)
     * @return Number of doses taken in the date range
     */
    @Query("SELECT COUNT(*) FROM medication_history WHERE medication_id = :medicationId AND taken_at >= :startTime AND taken_at <= :endTime")
    int getDosesTakenInDateRange(int medicationId, long startTime, long endTime);
    
    /**
     * Get on-time doses for a medication within a date range
     * @param medicationId Medication ID
     * @param startTime Start time (inclusive)
     * @param endTime End time (inclusive)
     * @return Number of on-time doses in the date range
     */
    @Query("SELECT COUNT(*) FROM medication_history WHERE medication_id = :medicationId AND taken_at >= :startTime AND taken_at <= :endTime AND is_on_time = 1")
    int getOnTimeDosesInDateRange(int medicationId, long startTime, long endTime);
    
    /**
     * Get adherence rate for a medication (percentage of on-time doses)
     * @param medicationId Medication ID
     * @param startTime Start time (inclusive)
     * @param endTime End time (inclusive)
     * @return Adherence rate as percentage (0-100)
     */
    @Query("SELECT CASE WHEN COUNT(*) = 0 THEN 0 ELSE (COUNT(CASE WHEN is_on_time = 1 THEN 1 END) * 100 / COUNT(*)) END FROM medication_history WHERE medication_id = :medicationId AND taken_at >= :startTime AND taken_at <= :endTime")
    int getAdherenceRate(int medicationId, long startTime, long endTime);
    
    /**
     * Get history records taken today for a user
     * @param userId User ID
     * @param todayStart Start of today (timestamp)
     * @param todayEnd End of today (timestamp)
     * @return List of today's history records
     */
    @Query("SELECT * FROM medication_history WHERE user_id = :userId AND taken_at >= :todayStart AND taken_at <= :todayEnd ORDER BY taken_at DESC")
    List<MedicationHistory> getTodayHistory(int userId, long todayStart, long todayEnd);
    
    /**
     * Get recent history for a user (last 7 days)
     * @param userId User ID
     * @param sevenDaysAgo Timestamp for 7 days ago
     * @return List of recent history records
     */
    @Query("SELECT * FROM medication_history WHERE user_id = :userId AND taken_at >= :sevenDaysAgo ORDER BY taken_at DESC")
    List<MedicationHistory> getRecentHistory(int userId, long sevenDaysAgo);
    
    /**
     * Check if medication was taken today
     * @param medicationId Medication ID
     * @param todayStart Start of today (timestamp)
     * @param todayEnd End of today (timestamp)
     * @return True if medication was taken today
     */
    @Query("SELECT COUNT(*) > 0 FROM medication_history WHERE medication_id = :medicationId AND taken_at >= :todayStart AND taken_at <= :todayEnd")
    boolean wasTakenToday(int medicationId, long todayStart, long todayEnd);
    
    /**
     * Get history by taken method
     * @param userId User ID
     * @param takenMethod Method how medication was taken
     * @return List of history records with the specified method
     */
    @Query("SELECT * FROM medication_history WHERE user_id = :userId AND taken_method = :takenMethod ORDER BY taken_at DESC")
    List<MedicationHistory> getHistoryByMethod(int userId, String takenMethod);
    
    /**
     * Delete all history for a medication (when medication is permanently deleted)
     * @param medicationId Medication ID
     * @return Number of rows deleted
     */
    @Query("DELETE FROM medication_history WHERE medication_id = :medicationId")
    int deleteHistoryForMedication(int medicationId);
    
    /**
     * Get medication names with history for a user (for statistics)
     * @param userId User ID
     * @return List of distinct medication names that have history
     */
    @Query("SELECT DISTINCT medication_name FROM medication_history WHERE user_id = :userId ORDER BY medication_name")
    List<String> getMedicationNamesWithHistory(int userId);
    
    /**
     * Get overall adherence rate for a user across all medications
     * @param userId User ID
     * @param startTime Start time (inclusive)
     * @param endTime End time (inclusive)
     * @return Overall adherence rate as percentage (0-100)
     */
    @Query("SELECT CASE WHEN COUNT(*) = 0 THEN 0 ELSE (COUNT(CASE WHEN is_on_time = 1 THEN 1 END) * 100 / COUNT(*)) END FROM medication_history WHERE user_id = :userId AND taken_at >= :startTime AND taken_at <= :endTime")
    int getOverallAdherenceRate(int userId, long startTime, long endTime);
}
