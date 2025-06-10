package com.example.dosebuddy.utils;

import android.content.Context;
import android.util.Log;

import com.example.dosebuddy.database.AppDatabase;
import com.example.dosebuddy.database.Medication;
import com.example.dosebuddy.database.MedicationHistory;
import com.example.dosebuddy.database.MedicationHistoryDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for managing medication history and adherence tracking
 */
public class MedicationHistoryManager {
    
    private static final String TAG = "MedicationHistoryManager";
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    /**
     * Record that a medication was taken
     */
    public static void recordMedicationTaken(Context context, int userId, Medication medication, 
                                           MedicationHistory.TakenMethod takenMethod) {
        recordMedicationTaken(context, userId, medication, System.currentTimeMillis(), 
                            takenMethod, null, null);
    }
    
    /**
     * Record that a medication was taken at a specific time
     */
    public static void recordMedicationTaken(Context context, int userId, Medication medication, 
                                           long takenAt, MedicationHistory.TakenMethod takenMethod) {
        recordMedicationTaken(context, userId, medication, takenAt, takenMethod, null, null);
    }
    
    /**
     * Record that a medication was taken with full details
     */
    public static void recordMedicationTaken(Context context, int userId, Medication medication, 
                                           long takenAt, MedicationHistory.TakenMethod takenMethod,
                                           Long scheduledTime, String notes) {
        executorService.execute(() -> {
            try {
                AppDatabase database = AppDatabase.getInstance(context);
                MedicationHistoryDao historyDao = database.medicationHistoryDao();
                
                MedicationHistory history = new MedicationHistory(
                    userId,
                    medication.getId(),
                    medication.getName(),
                    medication.getDosage(),
                    scheduledTime,
                    takenAt,
                    takenMethod
                );
                
                if (notes != null && !notes.trim().isEmpty()) {
                    history.setNotes(notes.trim());
                }
                
                long historyId = historyDao.insertHistory(history);
                
                if (historyId > 0) {
                    Log.d(TAG, "Recorded medication taken: " + medication.getName() + 
                          " at " + DateTimeUtils.formatDateTime(takenAt));
                } else {
                    Log.e(TAG, "Failed to record medication taken: " + medication.getName());
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error recording medication taken", e);
            }
        });
    }
    
    /**
     * Get adherence statistics for a medication
     */
    public static void getAdherenceStats(Context context, int medicationId, long startTime, 
                                       long endTime, AdherenceCallback callback) {
        executorService.execute(() -> {
            try {
                AppDatabase database = AppDatabase.getInstance(context);
                MedicationHistoryDao historyDao = database.medicationHistoryDao();
                
                int totalDoses = historyDao.getDosesTakenInDateRange(medicationId, startTime, endTime);
                int onTimeDoses = historyDao.getOnTimeDosesInDateRange(medicationId, startTime, endTime);
                int adherenceRate = historyDao.getAdherenceRate(medicationId, startTime, endTime);
                
                AdherenceStats stats = new AdherenceStats(totalDoses, onTimeDoses, adherenceRate);
                
                if (callback != null) {
                    callback.onAdherenceStatsReady(stats);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting adherence stats", e);
                if (callback != null) {
                    callback.onAdherenceStatsReady(new AdherenceStats(0, 0, 0));
                }
            }
        });
    }
    
    /**
     * Get recent history for a medication
     */
    public static void getRecentHistory(Context context, int medicationId, int days, 
                                      HistoryCallback callback) {
        executorService.execute(() -> {
            try {
                AppDatabase database = AppDatabase.getInstance(context);
                MedicationHistoryDao historyDao = database.medicationHistoryDao();
                
                long endTime = System.currentTimeMillis();
                long startTime = endTime - (days * 24 * 60 * 60 * 1000L);
                
                List<MedicationHistory> history = historyDao.getMedicationHistoryInDateRange(
                    medicationId, startTime, endTime);
                
                if (callback != null) {
                    callback.onHistoryReady(history);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting recent history", e);
                if (callback != null) {
                    callback.onHistoryReady(null);
                }
            }
        });
    }
    
    /**
     * Check if medication was taken today
     */
    public static void wasTakenToday(Context context, int medicationId, TakenTodayCallback callback) {
        executorService.execute(() -> {
            try {
                AppDatabase database = AppDatabase.getInstance(context);
                MedicationHistoryDao historyDao = database.medicationHistoryDao();
                
                long todayStart = DateTimeUtils.getStartOfDay(System.currentTimeMillis());
                long todayEnd = DateTimeUtils.getEndOfDay(System.currentTimeMillis());
                
                boolean wasTaken = historyDao.wasTakenToday(medicationId, todayStart, todayEnd);
                
                if (callback != null) {
                    callback.onResult(wasTaken);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error checking if taken today", e);
                if (callback != null) {
                    callback.onResult(false);
                }
            }
        });
    }
    
    /**
     * Get last taken time for a medication
     */
    public static void getLastTaken(Context context, int medicationId, LastTakenCallback callback) {
        executorService.execute(() -> {
            try {
                AppDatabase database = AppDatabase.getInstance(context);
                MedicationHistoryDao historyDao = database.medicationHistoryDao();
                
                MedicationHistory lastTaken = historyDao.getLastTakenForMedication(medicationId);
                
                if (callback != null) {
                    callback.onLastTakenReady(lastTaken);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting last taken", e);
                if (callback != null) {
                    callback.onLastTakenReady(null);
                }
            }
        });
    }
    
    /**
     * Calculate adherence level based on percentage
     */
    public static AdherenceLevel getAdherenceLevel(int adherencePercentage) {
        if (adherencePercentage >= 90) {
            return AdherenceLevel.EXCELLENT;
        } else if (adherencePercentage >= 75) {
            return AdherenceLevel.GOOD;
        } else if (adherencePercentage >= 50) {
            return AdherenceLevel.FAIR;
        } else {
            return AdherenceLevel.POOR;
        }
    }
    
    // Data classes and interfaces
    public static class AdherenceStats {
        public final int totalDoses;
        public final int onTimeDoses;
        public final int adherencePercentage;
        
        public AdherenceStats(int totalDoses, int onTimeDoses, int adherencePercentage) {
            this.totalDoses = totalDoses;
            this.onTimeDoses = onTimeDoses;
            this.adherencePercentage = adherencePercentage;
        }
    }
    
    public enum AdherenceLevel {
        EXCELLENT("Excellent"),
        GOOD("Good"),
        FAIR("Fair"),
        POOR("Needs Improvement");
        
        private final String displayName;
        
        AdherenceLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Callback interfaces
    public interface AdherenceCallback {
        void onAdherenceStatsReady(AdherenceStats stats);
    }
    
    public interface HistoryCallback {
        void onHistoryReady(List<MedicationHistory> history);
    }
    
    public interface TakenTodayCallback {
        void onResult(boolean wasTaken);
    }
    
    public interface LastTakenCallback {
        void onLastTakenReady(MedicationHistory lastTaken);
    }
}
