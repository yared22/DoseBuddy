package com.example.dosebuddy.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Medication entity for Room database
 * Represents a medication in the DoseBuddy application
 */
@Entity(tableName = "medications",
        foreignKeys = @ForeignKey(
            entity = User.class,
            parentColumns = "id",
            childColumns = "user_id",
            onDelete = ForeignKey.CASCADE
        ),
        indices = {
            @Index(value = "user_id"),
            @Index(value = {"user_id", "name"})
        })
public class Medication {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "user_id")
    private int userId;
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "dosage")
    private String dosage;
    
    @ColumnInfo(name = "frequency")
    private String frequency; // Store as string to allow for enum serialization
    
    @ColumnInfo(name = "times_per_day")
    private int timesPerDay;
    
    @ColumnInfo(name = "specific_times")
    private String specificTimes; // JSON string of time arrays
    
    @ColumnInfo(name = "start_date")
    private long startDate; // Timestamp
    
    @ColumnInfo(name = "end_date")
    private Long endDate; // Nullable timestamp
    
    @ColumnInfo(name = "notes")
    private String notes;
    
    @ColumnInfo(name = "is_active")
    private boolean isActive;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "updated_at")
    private long updatedAt;
    
    // Constructors
    public Medication() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isActive = true;
    }
    
    @Ignore
    public Medication(int userId, String name, String dosage, MedicationFrequency frequency, 
                     int timesPerDay, long startDate) {
        this();
        this.userId = userId;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency.name();
        this.timesPerDay = timesPerDay;
        this.startDate = startDate;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDosage() {
        return dosage;
    }
    
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
    
    public String getFrequency() {
        return frequency;
    }
    
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    
    /**
     * Get frequency as enum
     * @return MedicationFrequency enum
     */
    public MedicationFrequency getFrequencyEnum() {
        return MedicationFrequency.fromString(frequency);
    }
    
    /**
     * Set frequency from enum
     * @param frequency MedicationFrequency enum
     */
    public void setFrequencyEnum(MedicationFrequency frequency) {
        this.frequency = frequency.name();
    }
    
    public int getTimesPerDay() {
        return timesPerDay;
    }
    
    public void setTimesPerDay(int timesPerDay) {
        this.timesPerDay = timesPerDay;
    }
    
    public String getSpecificTimes() {
        return specificTimes;
    }
    
    public void setSpecificTimes(String specificTimes) {
        this.specificTimes = specificTimes;
    }
    
    public long getStartDate() {
        return startDate;
    }
    
    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }
    
    public Long getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Update the updated_at timestamp
     */
    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return "Medication{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", timesPerDay=" + timesPerDay +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isActive=" + isActive +
                '}';
    }
}
