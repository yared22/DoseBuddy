package com.example.dosebuddy.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.dosebuddy.R;
import com.example.dosebuddy.database.AppDatabase;
import com.example.dosebuddy.database.Medication;
import com.example.dosebuddy.database.MedicationDao;
import com.example.dosebuddy.database.MedicationHistory;
import com.example.dosebuddy.utils.MedicationHistoryManager;
import com.example.dosebuddy.utils.ReminderScheduler;
import com.example.dosebuddy.workers.MedicationReminderWorker;

import java.util.concurrent.TimeUnit;

/**
 * BroadcastReceiver for handling medication notification actions
 */
public class MedicationActionReceiver extends BroadcastReceiver {
    
    public static final String ACTION_TAKE_NOW = "com.example.dosebuddy.ACTION_TAKE_NOW";
    public static final String ACTION_SNOOZE = "com.example.dosebuddy.ACTION_SNOOZE";
    public static final String ACTION_DISMISS = "com.example.dosebuddy.ACTION_DISMISS";
    
    public static final String EXTRA_MEDICATION_ID = "medication_id";
    public static final String EXTRA_MEDICATION_NAME = "medication_name";
    public static final String EXTRA_MEDICATION_DOSAGE = "medication_dosage";
    
    private static final int SNOOZE_MINUTES = 15; // Default snooze time
    private static final int NOTIFICATION_ID_BASE = 1000;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        
        int medicationId = intent.getIntExtra(EXTRA_MEDICATION_ID, -1);
        String medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME);
        String medicationDosage = intent.getStringExtra(EXTRA_MEDICATION_DOSAGE);
        
        if (medicationId == -1 || medicationName == null) return;
        
        // Dismiss the notification
        dismissNotification(context, medicationId);
        
        switch (action) {
            case ACTION_TAKE_NOW:
                handleTakeNow(context, medicationId, medicationName);
                break;
            case ACTION_SNOOZE:
                handleSnooze(context, medicationId, medicationName, medicationDosage);
                break;
            case ACTION_DISMISS:
                handleDismiss(context, medicationId, medicationName);
                break;
        }
    }
    
    /**
     * Handle "Take Now" action
     */
    private void handleTakeNow(Context context, int medicationId, String medicationName) {
        // Record medication as taken in history
        recordMedicationTaken(context, medicationId, MedicationHistory.TakenMethod.NOTIFICATION);

        String message = context.getString(R.string.medication_taken_notification, medicationName);
        showToast(context, message);
    }
    
    /**
     * Handle "Snooze" action
     */
    private void handleSnooze(Context context, int medicationId, String medicationName, String medicationDosage) {
        // Schedule a new reminder after snooze period
        scheduleSnoozeReminder(context, medicationId, medicationName, medicationDosage, SNOOZE_MINUTES);
        
        String message = context.getString(R.string.snoozed_for, SNOOZE_MINUTES + " minutes");
        showToast(context, message);
    }
    
    /**
     * Handle "Dismiss" action
     */
    private void handleDismiss(Context context, int medicationId, String medicationName) {
        // Just dismiss - notification is already dismissed above
        // Could log this action if needed
    }
    
    /**
     * Dismiss the notification
     */
    private void dismissNotification(Context context, int medicationId) {
        NotificationManager notificationManager = (NotificationManager) 
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            int notificationId = NOTIFICATION_ID_BASE + medicationId;
            notificationManager.cancel(notificationId);
        }
    }
    
    /**
     * Schedule a snooze reminder
     */
    private void scheduleSnoozeReminder(Context context, int medicationId, String medicationName, 
                                      String medicationDosage, int snoozeMinutes) {
        // Create work data
        Data inputData = new Data.Builder()
                .putInt(MedicationReminderWorker.MEDICATION_ID_KEY, medicationId)
                .putString(MedicationReminderWorker.MEDICATION_NAME_KEY, medicationName)
                .putString(MedicationReminderWorker.MEDICATION_DOSAGE_KEY, medicationDosage)
                .putLong(MedicationReminderWorker.REMINDER_TIME_KEY, System.currentTimeMillis() + (snoozeMinutes * 60 * 1000))
                .build();
        
        // Create work request
        OneTimeWorkRequest snoozeWorkRequest = new OneTimeWorkRequest.Builder(MedicationReminderWorker.class)
                .setInputData(inputData)
                .setInitialDelay(snoozeMinutes, TimeUnit.MINUTES)
                .addTag(ReminderScheduler.getSnoozeWorkTag(medicationId))
                .build();
        
        // Schedule the work
        WorkManager.getInstance(context).enqueue(snoozeWorkRequest);
    }
    
    /**
     * Record medication as taken in history
     */
    private void recordMedicationTaken(Context context, int medicationId,
                                     MedicationHistory.TakenMethod takenMethod) {
        // Get medication details and record in history
        new Thread(() -> {
            try {
                AppDatabase database = AppDatabase.getInstance(context);
                MedicationDao medicationDao = database.medicationDao();

                Medication medication = medicationDao.getMedicationById(medicationId);
                if (medication != null) {
                    // Get user ID from SharedPreferences
                    int userId = getCurrentUserId(context);

                    if (userId != -1) {
                        MedicationHistoryManager.recordMedicationTaken(
                            context, userId, medication, takenMethod);
                    }
                }
            } catch (Exception e) {
                // Log error but don't crash
            }
        }).start();
    }

    /**
     * Get current user ID from SharedPreferences
     */
    private int getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("DoseBuddy", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (!isLoggedIn) {
            return -1; // No user logged in
        }

        return prefs.getInt("current_user_id", -1);
    }

    /**
     * Show toast message
     */
    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
