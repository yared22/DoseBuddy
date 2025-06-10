package com.example.dosebuddy.utils;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.dosebuddy.database.Medication;
import com.example.dosebuddy.database.MedicationFrequency;
import com.example.dosebuddy.workers.MedicationReminderWorker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for scheduling and managing medication reminders
 */
public class ReminderScheduler {
    
    private static final String TAG = "ReminderScheduler";
    private static final String WORK_TAG_PREFIX = "medication_reminder_";
    private static final String SNOOZE_TAG_PREFIX = "medication_snooze_";
    
    /**
     * Schedule reminders for a medication
     */
    public static void scheduleMedicationReminders(Context context, Medication medication) {
        // Cancel existing reminders first
        cancelMedicationReminders(context, medication.getId());
        
        // Get reminder times for the medication
        List<Long> reminderTimes = getReminderTimes(medication);
        
        if (reminderTimes.isEmpty()) {
            Log.w(TAG, "No reminder times found for medication: " + medication.getName());
            return;
        }
        
        // Schedule reminders for each time
        for (Long reminderTime : reminderTimes) {
            scheduleReminderAtTime(context, medication, reminderTime);
        }
        
        Log.d(TAG, "Scheduled " + reminderTimes.size() + " reminders for: " + medication.getName());
    }
    
    /**
     * Cancel all reminders for a medication
     */
    public static void cancelMedicationReminders(Context context, int medicationId) {
        String workTag = getWorkTag(medicationId);
        WorkManager.getInstance(context).cancelAllWorkByTag(workTag);
        
        // Also cancel any snooze reminders
        String snoozeTag = getSnoozeWorkTag(medicationId);
        WorkManager.getInstance(context).cancelAllWorkByTag(snoozeTag);
        
        Log.d(TAG, "Cancelled reminders for medication ID: " + medicationId);
    }
    
    /**
     * Get reminder times for a medication based on frequency and specific times
     */
    private static List<Long> getReminderTimes(Medication medication) {
        List<Long> reminderTimes = new ArrayList<>();
        
        // Parse specific times from JSON if available
        String specificTimesJson = medication.getSpecificTimes();
        if (specificTimesJson != null && !specificTimesJson.isEmpty()) {
            try {
                String timesStr = specificTimesJson.replace("[", "").replace("]", "");
                if (!timesStr.trim().isEmpty()) {
                    String[] timeStrings = timesStr.split(",");
                    for (String timeStr : timeStrings) {
                        long timestamp = Long.parseLong(timeStr.trim());
                        reminderTimes.add(timestamp);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing specific times: " + e.getMessage());
            }
        }
        
        // If no specific times, generate default times based on frequency
        if (reminderTimes.isEmpty()) {
            reminderTimes = generateDefaultReminderTimes(medication.getFrequencyEnum(), medication.getTimesPerDay());
        }
        
        return reminderTimes;
    }
    
    /**
     * Generate default reminder times based on frequency
     */
    private static List<Long> generateDefaultReminderTimes(MedicationFrequency frequency, int timesPerDay) {
        List<Long> times = new ArrayList<>();
        
        if (frequency == MedicationFrequency.AS_NEEDED) {
            return times; // No scheduled reminders for as-needed medications
        }
        
        // Generate evenly spaced times throughout the day
        int actualTimes = Math.max(1, timesPerDay);
        int intervalHours = 24 / actualTimes;
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8); // Start at 8 AM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        for (int i = 0; i < actualTimes; i++) {
            times.add(calendar.getTimeInMillis());
            calendar.add(Calendar.HOUR_OF_DAY, intervalHours);
        }
        
        return times;
    }
    
    /**
     * Schedule a reminder at a specific time
     */
    private static void scheduleReminderAtTime(Context context, Medication medication, long reminderTime) {
        // Calculate delay until the reminder time
        long currentTime = System.currentTimeMillis();
        long delay = calculateNextReminderDelay(reminderTime, currentTime);
        
        if (delay <= 0) {
            Log.w(TAG, "Reminder time is in the past, skipping: " + DateTimeUtils.formatDateTime(reminderTime));
            return;
        }
        
        // Create work data
        Data inputData = new Data.Builder()
                .putInt(MedicationReminderWorker.MEDICATION_ID_KEY, medication.getId())
                .putString(MedicationReminderWorker.MEDICATION_NAME_KEY, medication.getName())
                .putString(MedicationReminderWorker.MEDICATION_DOSAGE_KEY, medication.getDosage())
                .putLong(MedicationReminderWorker.REMINDER_TIME_KEY, reminderTime)
                .build();
        
        // Create work request
        OneTimeWorkRequest reminderWorkRequest = new OneTimeWorkRequest.Builder(MedicationReminderWorker.class)
                .setInputData(inputData)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(getWorkTag(medication.getId()))
                .build();
        
        // Schedule the work
        WorkManager.getInstance(context).enqueue(reminderWorkRequest);
        
        Log.d(TAG, "Scheduled reminder for " + medication.getName() + " at " + 
              DateTimeUtils.formatDateTime(currentTime + delay));
    }
    
    /**
     * Calculate delay until next reminder time
     * Handles daily recurring reminders
     */
    private static long calculateNextReminderDelay(long reminderTime, long currentTime) {
        Calendar reminderCal = Calendar.getInstance();
        reminderCal.setTimeInMillis(reminderTime);
        
        Calendar currentCal = Calendar.getInstance();
        currentCal.setTimeInMillis(currentTime);
        
        // Set reminder to today
        reminderCal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
        reminderCal.set(Calendar.MONTH, currentCal.get(Calendar.MONTH));
        reminderCal.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH));
        
        long todayReminderTime = reminderCal.getTimeInMillis();
        
        // If reminder time for today has passed, schedule for tomorrow
        if (todayReminderTime <= currentTime) {
            reminderCal.add(Calendar.DAY_OF_MONTH, 1);
            todayReminderTime = reminderCal.getTimeInMillis();
        }
        
        return todayReminderTime - currentTime;
    }
    
    /**
     * Get work tag for a medication
     */
    public static String getWorkTag(int medicationId) {
        return WORK_TAG_PREFIX + medicationId;
    }
    
    /**
     * Get snooze work tag for a medication
     */
    public static String getSnoozeWorkTag(int medicationId) {
        return SNOOZE_TAG_PREFIX + medicationId;
    }
    
    /**
     * Reschedule all reminders for a medication (useful after editing)
     */
    public static void rescheduleMedicationReminders(Context context, Medication medication) {
        Log.d(TAG, "Rescheduling reminders for: " + medication.getName());
        scheduleMedicationReminders(context, medication);
    }
}
