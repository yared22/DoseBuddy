package com.example.dosebuddy.workers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.dosebuddy.MainActivity;
import com.example.dosebuddy.R;
import com.example.dosebuddy.database.AppDatabase;
import com.example.dosebuddy.database.Medication;
import com.example.dosebuddy.database.MedicationDao;
import com.example.dosebuddy.receivers.MedicationActionReceiver;

/**
 * WorkManager worker for sending medication reminder notifications
 */
public class MedicationReminderWorker extends Worker {
    
    public static final String MEDICATION_ID_KEY = "medication_id";
    public static final String MEDICATION_NAME_KEY = "medication_name";
    public static final String MEDICATION_DOSAGE_KEY = "medication_dosage";
    public static final String REMINDER_TIME_KEY = "reminder_time";
    
    private static final String CHANNEL_ID = "medication_reminders";
    private static final int NOTIFICATION_ID_BASE = 1000;
    
    public MedicationReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        // Get medication details from input data
        int medicationId = getInputData().getInt(MEDICATION_ID_KEY, -1);
        String medicationName = getInputData().getString(MEDICATION_NAME_KEY);
        String medicationDosage = getInputData().getString(MEDICATION_DOSAGE_KEY);
        long reminderTime = getInputData().getLong(REMINDER_TIME_KEY, 0);
        
        if (medicationId == -1 || medicationName == null || medicationDosage == null) {
            return Result.failure();
        }
        
        // Verify medication still exists and is active
        if (!isMedicationActive(medicationId)) {
            return Result.success(); // Medication was deleted or deactivated
        }
        
        // Create and show notification
        createNotificationChannel();
        showMedicationNotification(medicationId, medicationName, medicationDosage, reminderTime);
        
        return Result.success();
    }
    
    /**
     * Check if medication is still active
     */
    private boolean isMedicationActive(int medicationId) {
        try {
            AppDatabase database = AppDatabase.getInstance(getApplicationContext());
            MedicationDao medicationDao = database.medicationDao();
            Medication medication = medicationDao.getMedicationById(medicationId);
            return medication != null && medication.isActive();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Create notification channel for Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getApplicationContext().getString(R.string.medication_reminder_channel),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(getApplicationContext().getString(R.string.medication_reminder_channel_description));

            // Enhanced vibration pattern
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500, 200, 500}); // Custom vibration pattern

            // Set notification sound
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            // Additional settings
            channel.setShowBadge(true);
            channel.enableLights(true);
            channel.setLightColor(getApplicationContext().getColor(R.color.primary));
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Show medication reminder notification
     */
    private void showMedicationNotification(int medicationId, String medicationName, 
                                          String medicationDosage, long reminderTime) {
        Context context = getApplicationContext();
        
        // Create intent to open app when notification is tapped
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(
                context, medicationId, openAppIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Create "Take Now" action intent
        Intent takeNowIntent = new Intent(context, MedicationActionReceiver.class);
        takeNowIntent.setAction(MedicationActionReceiver.ACTION_TAKE_NOW);
        takeNowIntent.putExtra(MedicationActionReceiver.EXTRA_MEDICATION_ID, medicationId);
        takeNowIntent.putExtra(MedicationActionReceiver.EXTRA_MEDICATION_NAME, medicationName);
        PendingIntent takeNowPendingIntent = PendingIntent.getBroadcast(
                context, medicationId * 10 + 1, takeNowIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Create "Snooze" action intent
        Intent snoozeIntent = new Intent(context, MedicationActionReceiver.class);
        snoozeIntent.setAction(MedicationActionReceiver.ACTION_SNOOZE);
        snoozeIntent.putExtra(MedicationActionReceiver.EXTRA_MEDICATION_ID, medicationId);
        snoozeIntent.putExtra(MedicationActionReceiver.EXTRA_MEDICATION_NAME, medicationName);
        snoozeIntent.putExtra(MedicationActionReceiver.EXTRA_MEDICATION_DOSAGE, medicationDosage);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context, medicationId * 10 + 2, snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Build notification
        String title = context.getString(R.string.medication_reminder);
        String content = context.getString(R.string.take_medication_now, medicationName);
        String bigText = context.getString(R.string.dosage_reminder, medicationName, medicationDosage);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medication_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(openAppPendingIntent)
                .addAction(R.drawable.ic_check, context.getString(R.string.take_now), takeNowPendingIntent)
                .addAction(R.drawable.ic_snooze, context.getString(R.string.snooze), snoozePendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS)
                .setVibrate(new long[]{0, 500, 200, 500, 200, 500}) // Custom vibration pattern
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setFullScreenIntent(openAppPendingIntent, false) // Show heads-up notification
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(false)
                .setTimeoutAfter(30 * 60 * 1000); // Auto-dismiss after 30 minutes
        
        // Show notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            int notificationId = NOTIFICATION_ID_BASE + medicationId;
            notificationManager.notify(notificationId, builder.build());

            // Trigger additional vibration for older Android versions
            triggerVibration(context);
        }
    }

    /**
     * Trigger vibration for medication reminder
     */
    private void triggerVibration(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Android 8.0+ use VibrationEffect
                VibrationEffect effect = VibrationEffect.createWaveform(
                        new long[]{0, 500, 200, 500, 200, 500}, // Pattern: wait, vibrate, wait, vibrate...
                        -1 // Don't repeat
                );
                vibrator.vibrate(effect);
            } else {
                // For older versions use deprecated method
                vibrator.vibrate(new long[]{0, 500, 200, 500, 200, 500}, -1);
            }
        }
    }
}
