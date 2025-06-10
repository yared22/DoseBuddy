package com.example.dosebuddy.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date and time operations
 */
public class DateTimeUtils {
    
    // Date format patterns
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String DATE_FORMAT_SHORT = "MM/dd/yyyy";
    public static final String TIME_FORMAT_12H = "hh:mm a";
    public static final String TIME_FORMAT_24H = "HH:mm";
    public static final String DATETIME_FORMAT = "MMM dd, yyyy hh:mm a";
    
    /**
     * Format timestamp to display date
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DISPLAY, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format timestamp to short date
     * @param timestamp Timestamp in milliseconds
     * @return Formatted short date string
     */
    public static String formatDateShort(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_SHORT, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format timestamp to time (12-hour format)
     * @param timestamp Timestamp in milliseconds
     * @return Formatted time string
     */
    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_12H, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format timestamp to time (24-hour format)
     * @param timestamp Timestamp in milliseconds
     * @return Formatted time string
     */
    public static String formatTime24H(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT_24H, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format timestamp to date and time
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date and time string
     */
    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Get start of day timestamp
     * @param timestamp Any timestamp within the day
     * @return Timestamp for start of day (00:00:00)
     */
    public static long getStartOfDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Get end of day timestamp
     * @param timestamp Any timestamp within the day
     * @return Timestamp for end of day (23:59:59.999)
     */
    public static long getEndOfDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Get current date timestamp (start of today)
     * @return Timestamp for start of current day
     */
    public static long getCurrentDateTimestamp() {
        return getStartOfDay(System.currentTimeMillis());
    }
    
    /**
     * Create timestamp from date components
     * @param year Year
     * @param month Month (0-based, January = 0)
     * @param day Day of month
     * @return Timestamp for the specified date
     */
    public static long createDateTimestamp(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Create timestamp from time components (today's date)
     * @param hourOfDay Hour (0-23)
     * @param minute Minute (0-59)
     * @return Timestamp for the specified time today
     */
    public static long createTimeTimestamp(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Check if a timestamp is today
     * @param timestamp Timestamp to check
     * @return True if timestamp is today, false otherwise
     */
    public static boolean isToday(long timestamp) {
        long todayStart = getCurrentDateTimestamp();
        long todayEnd = getEndOfDay(todayStart);
        return timestamp >= todayStart && timestamp <= todayEnd;
    }
    
    /**
     * Check if a timestamp is in the past
     * @param timestamp Timestamp to check
     * @return True if timestamp is in the past, false otherwise
     */
    public static boolean isPast(long timestamp) {
        return timestamp < System.currentTimeMillis();
    }
    
    /**
     * Check if a timestamp is in the future
     * @param timestamp Timestamp to check
     * @return True if timestamp is in the future, false otherwise
     */
    public static boolean isFuture(long timestamp) {
        return timestamp > System.currentTimeMillis();
    }
    
    /**
     * Get days between two timestamps
     * @param startTimestamp Start timestamp
     * @param endTimestamp End timestamp
     * @return Number of days between timestamps
     */
    public static int getDaysBetween(long startTimestamp, long endTimestamp) {
        long diffInMillis = Math.abs(endTimestamp - startTimestamp);
        return (int) (diffInMillis / (24 * 60 * 60 * 1000));
    }
    
    /**
     * Add days to a timestamp
     * @param timestamp Original timestamp
     * @param days Number of days to add (can be negative)
     * @return New timestamp with days added
     */
    public static long addDays(long timestamp, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Get hour from timestamp
     * @param timestamp Timestamp
     * @return Hour (0-23)
     */
    public static int getHour(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
    
    /**
     * Get minute from timestamp
     * @param timestamp Timestamp
     * @return Minute (0-59)
     */
    public static int getMinute(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.get(Calendar.MINUTE);
    }
}
