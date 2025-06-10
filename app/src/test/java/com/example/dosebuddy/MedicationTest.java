package com.example.dosebuddy;

import com.example.dosebuddy.database.Medication;
import com.example.dosebuddy.database.MedicationFrequency;
import com.example.dosebuddy.utils.DateTimeUtils;
import com.example.dosebuddy.utils.ValidationUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Medication-related functionality
 */
public class MedicationTest {

    @Test
    public void testMedicationCreation() {
        long startDate = DateTimeUtils.getCurrentDateTimestamp();
        Medication medication = new Medication(1, "Aspirin", "100mg", 
                MedicationFrequency.TWICE_DAILY, 2, startDate);
        
        assertNotNull(medication);
        assertEquals(1, medication.getUserId());
        assertEquals("Aspirin", medication.getName());
        assertEquals("100mg", medication.getDosage());
        assertEquals(MedicationFrequency.TWICE_DAILY.name(), medication.getFrequency());
        assertEquals(2, medication.getTimesPerDay());
        assertEquals(startDate, medication.getStartDate());
        assertTrue(medication.isActive());
        assertNull(medication.getEndDate());
    }

    @Test
    public void testMedicationFrequencyEnum() {
        // Test frequency enum conversion
        Medication medication = new Medication();
        medication.setFrequencyEnum(MedicationFrequency.THREE_TIMES_DAILY);
        
        assertEquals(MedicationFrequency.THREE_TIMES_DAILY, medication.getFrequencyEnum());
        assertEquals("THREE_TIMES_DAILY", medication.getFrequency());
    }

    @Test
    public void testMedicationFrequencyDefaults() {
        assertEquals(1, MedicationFrequency.ONCE_DAILY.getDefaultTimesPerDay());
        assertEquals(2, MedicationFrequency.TWICE_DAILY.getDefaultTimesPerDay());
        assertEquals(3, MedicationFrequency.THREE_TIMES_DAILY.getDefaultTimesPerDay());
        assertEquals(4, MedicationFrequency.FOUR_TIMES_DAILY.getDefaultTimesPerDay());
        assertEquals(1, MedicationFrequency.EVERY_OTHER_DAY.getDefaultTimesPerDay());
        assertEquals(1, MedicationFrequency.WEEKLY.getDefaultTimesPerDay());
        assertEquals(1, MedicationFrequency.AS_NEEDED.getDefaultTimesPerDay());
        assertEquals(1, MedicationFrequency.CUSTOM.getDefaultTimesPerDay());
    }

    @Test
    public void testMedicationNameValidation() {
        // Valid medication names
        assertTrue(ValidationUtils.isValidMedicationName("Aspirin"));
        assertTrue(ValidationUtils.isValidMedicationName("Tylenol Extra Strength"));
        assertTrue(ValidationUtils.isValidMedicationName("Vitamin D-3"));
        assertTrue(ValidationUtils.isValidMedicationName("Medication (Generic)"));
        
        // Invalid medication names
        assertFalse(ValidationUtils.isValidMedicationName("")); // Empty
        assertFalse(ValidationUtils.isValidMedicationName("A")); // Too short
        assertFalse(ValidationUtils.isValidMedicationName(null)); // Null
        
        // Test very long name
        String longName = "A".repeat(101);
        assertFalse(ValidationUtils.isValidMedicationName(longName)); // Too long
    }

    @Test
    public void testDosageValidation() {
        // Valid dosages
        assertTrue(ValidationUtils.isValidDosage("100mg"));
        assertTrue(ValidationUtils.isValidDosage("2 tablets"));
        assertTrue(ValidationUtils.isValidDosage("5ml"));
        assertTrue(ValidationUtils.isValidDosage("1/2 tablet"));
        assertTrue(ValidationUtils.isValidDosage("50%"));
        
        // Invalid dosages
        assertFalse(ValidationUtils.isValidDosage("")); // Empty
        assertFalse(ValidationUtils.isValidDosage(null)); // Null
        
        // Test very long dosage
        String longDosage = "A".repeat(51);
        assertFalse(ValidationUtils.isValidDosage(longDosage)); // Too long
    }

    @Test
    public void testTimesPerDayValidation() {
        // Valid times per day
        assertTrue(ValidationUtils.isValidTimesPerDay(1));
        assertTrue(ValidationUtils.isValidTimesPerDay(5));
        assertTrue(ValidationUtils.isValidTimesPerDay(10));
        
        // Invalid times per day
        assertFalse(ValidationUtils.isValidTimesPerDay(0)); // Too low
        assertFalse(ValidationUtils.isValidTimesPerDay(11)); // Too high
        assertFalse(ValidationUtils.isValidTimesPerDay(-1)); // Negative
    }

    @Test
    public void testDateRangeValidation() {
        long startDate = DateTimeUtils.getCurrentDateTimestamp();
        long endDate = DateTimeUtils.addDays(startDate, 30);
        long pastEndDate = DateTimeUtils.addDays(startDate, -10);
        
        // Valid date ranges
        assertTrue(ValidationUtils.isValidDateRange(startDate, endDate));
        assertTrue(ValidationUtils.isValidDateRange(startDate, null)); // No end date
        assertTrue(ValidationUtils.isValidDateRange(startDate, startDate)); // Same date
        
        // Invalid date range
        assertFalse(ValidationUtils.isValidDateRange(startDate, pastEndDate)); // End before start
    }

    @Test
    public void testMedicationTouch() {
        Medication medication = new Medication();
        long originalTime = medication.getUpdatedAt();
        
        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        medication.touch();
        assertTrue(medication.getUpdatedAt() > originalTime);
    }

    @Test
    public void testMedicationFrequencyFromString() {
        assertEquals(MedicationFrequency.ONCE_DAILY, 
                MedicationFrequency.fromString("ONCE_DAILY"));
        assertEquals(MedicationFrequency.TWICE_DAILY, 
                MedicationFrequency.fromString("twice_daily"));
        assertEquals(MedicationFrequency.ONCE_DAILY, 
                MedicationFrequency.fromString("INVALID")); // Default
        assertEquals(MedicationFrequency.ONCE_DAILY, 
                MedicationFrequency.fromString(null)); // Null
    }

    @Test
    public void testDateTimeUtils() {
        long timestamp = System.currentTimeMillis();
        
        // Test date formatting
        assertNotNull(DateTimeUtils.formatDate(timestamp));
        assertNotNull(DateTimeUtils.formatTime(timestamp));
        assertNotNull(DateTimeUtils.formatDateTime(timestamp));
        
        // Test date calculations
        assertTrue(DateTimeUtils.isToday(timestamp));
        assertFalse(DateTimeUtils.isPast(timestamp + 1000)); // Future
        assertFalse(DateTimeUtils.isFuture(timestamp - 1000)); // Past
        
        // Test day calculations
        long tomorrow = DateTimeUtils.addDays(timestamp, 1);
        assertEquals(1, DateTimeUtils.getDaysBetween(timestamp, tomorrow));
    }
}
