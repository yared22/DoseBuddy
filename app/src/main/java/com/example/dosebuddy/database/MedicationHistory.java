package com.example.dosebuddy.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * MedicationHistory entity for Room database
 * Tracks when medications are taken by users
 */
@Entity(tableName = "medication_history",
        foreignKeys = {
            @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
            ),
            @ForeignKey(
                entity = Medication.class,
                parentColumns = "id",
                childColumns = "medication_id",
                onDelete = ForeignKey.CASCADE
            )
        },
        indices = {
            @Index(value = "user_id"),
            @Index(value = "medication_id"),
            @Index(value = {"user_id", "medication_id", "taken_at"}),
            @Index(value = "taken_at")
        })
public class MedicationHistory {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "user_id")
    private int userId;
    
    @ColumnInfo(name = "medication_id")
    private int medicationId;
    
    @ColumnInfo(name = "medication_name")
    private String medicationName; // Store name for historical reference
    
    @ColumnInfo(name = "medication_dosage")
    private String medicationDosage; // Store dosage for historical reference
    
    @ColumnInfo(name = "scheduled_time")
    private Long scheduledTime; // When the dose was supposed to be taken
    
    @ColumnInfo(name = "taken_at")
    private long takenAt; // When the dose was actually taken
    
    @ColumnInfo(name = "taken_method")
    private String takenMethod; // How it was recorded (REMINDER, MANUAL, etc.)
    
    @ColumnInfo(name = "is_on_time")
    private boolean isOnTime; // Whether taken within acceptable time window
    
    @ColumnInfo(name = "notes")
    private String notes; // Optional notes about the dose
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    // Enum for taken method
    public enum TakenMethod {
        REMINDER("From Reminder"),
        MANUAL("Manual Entry"),
        NOTIFICATION("From Notification");
        
        private final String displayName;
        
        TakenMethod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static TakenMethod fromString(String value) {
            if (value == null) return MANUAL;
            try {
                return TakenMethod.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return MANUAL;
            }
        }
    }
    
    // Constructors
    public MedicationHistory() {
        this.createdAt = System.currentTimeMillis();
        this.takenAt = System.currentTimeMillis();
        this.isOnTime = true;
        this.takenMethod = TakenMethod.MANUAL.name();
    }
    
    @Ignore
    public MedicationHistory(int userId, int medicationId, String medicationName, 
                           String medicationDosage, long takenAt, TakenMethod takenMethod) {
        this();
        this.userId = userId;
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.medicationDosage = medicationDosage;
        this.takenAt = takenAt;
        this.takenMethod = takenMethod.name();
    }
    
    @Ignore
    public MedicationHistory(int userId, int medicationId, String medicationName, 
                           String medicationDosage, Long scheduledTime, long takenAt, 
                           TakenMethod takenMethod) {
        this(userId, medicationId, medicationName, medicationDosage, takenAt, takenMethod);
        this.scheduledTime = scheduledTime;
        this.isOnTime = calculateIsOnTime(scheduledTime, takenAt);
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getMedicationId() {
        return medicationId;
    }
    
    public void setMedicationId(int medicationId) {
        this.medicationId = medicationId;
    }
    
    public String getMedicationName() {
        return medicationName;
    }
    
    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }
    
    public String getMedicationDosage() {
        return medicationDosage;
    }
    
    public void setMedicationDosage(String medicationDosage) {
        this.medicationDosage = medicationDosage;
    }
    
    public Long getScheduledTime() {
        return scheduledTime;
    }
    
    public void setScheduledTime(Long scheduledTime) {
        this.scheduledTime = scheduledTime;
        if (scheduledTime != null) {
            this.isOnTime = calculateIsOnTime(scheduledTime, this.takenAt);
        }
    }
    
    public long getTakenAt() {
        return takenAt;
    }
    
    public void setTakenAt(long takenAt) {
        this.takenAt = takenAt;
        if (this.scheduledTime != null) {
            this.isOnTime = calculateIsOnTime(this.scheduledTime, takenAt);
        }
    }
    
    public String getTakenMethod() {
        return takenMethod;
    }
    
    public void setTakenMethod(String takenMethod) {
        this.takenMethod = takenMethod;
    }
    
    /**
     * Get taken method as enum
     */
    public TakenMethod getTakenMethodEnum() {
        return TakenMethod.fromString(takenMethod);
    }
    
    /**
     * Set taken method from enum
     */
    public void setTakenMethodEnum(TakenMethod takenMethod) {
        this.takenMethod = takenMethod.name();
    }
    
    public boolean isOnTime() {
        return isOnTime;
    }
    
    public void setOnTime(boolean onTime) {
        isOnTime = onTime;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Calculate if dose was taken on time
     * On time = within 30 minutes of scheduled time
     */
    private boolean calculateIsOnTime(Long scheduledTime, long takenAt) {
        if (scheduledTime == null) {
            return true; // No scheduled time, consider on time
        }
        
        long timeDifference = Math.abs(takenAt - scheduledTime);
        long thirtyMinutesInMillis = 30 * 60 * 1000; // 30 minutes
        
        return timeDifference <= thirtyMinutesInMillis;
    }
    
    /**
     * Get time difference from scheduled time in minutes
     */
    public long getTimeDifferenceMinutes() {
        if (scheduledTime == null) {
            return 0;
        }
        return (takenAt - scheduledTime) / (60 * 1000);
    }
    
    @Override
    public String toString() {
        return "MedicationHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", medicationId=" + medicationId +
                ", medicationName='" + medicationName + '\'' +
                ", takenAt=" + takenAt +
                ", takenMethod='" + takenMethod + '\'' +
                ", isOnTime=" + isOnTime +
                '}';
    }
}
