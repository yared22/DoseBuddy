package com.example.dosebuddy.database;

/**
 * Enum representing different medication frequency options
 */
public enum MedicationFrequency {
    ONCE_DAILY("Once Daily", 1),
    TWICE_DAILY("Twice Daily", 2),
    THREE_TIMES_DAILY("Three Times Daily", 3),
    FOUR_TIMES_DAILY("Four Times Daily", 4),
    EVERY_OTHER_DAY("Every Other Day", 0.5),
    WEEKLY("Weekly", 0.14),
    AS_NEEDED("As Needed", 0),
    CUSTOM("Custom", -1);
    
    private final String displayName;
    private final double timesPerDay;
    
    MedicationFrequency(String displayName, double timesPerDay) {
        this.displayName = displayName;
        this.timesPerDay = timesPerDay;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getTimesPerDay() {
        return timesPerDay;
    }
    
    /**
     * Get default times per day as integer
     * @return Default number of times per day
     */
    public int getDefaultTimesPerDay() {
        if (timesPerDay < 0) return 1; // Custom defaults to 1
        if (timesPerDay < 1) return 1; // Weekly, every other day default to 1
        return (int) Math.round(timesPerDay);
    }
    
    /**
     * Get frequency from string value
     * @param value String value
     * @return MedicationFrequency enum
     */
    public static MedicationFrequency fromString(String value) {
        if (value == null) return ONCE_DAILY;
        
        try {
            return MedicationFrequency.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ONCE_DAILY;
        }
    }
    
    /**
     * Get all frequency options as array
     * @return Array of all frequency options
     */
    public static MedicationFrequency[] getAllFrequencies() {
        return values();
    }
    
    /**
     * Get display names for spinner
     * @return Array of display names
     */
    public static String[] getDisplayNames() {
        MedicationFrequency[] frequencies = values();
        String[] names = new String[frequencies.length];
        for (int i = 0; i < frequencies.length; i++) {
            names[i] = frequencies[i].getDisplayName();
        }
        return names;
    }
}
