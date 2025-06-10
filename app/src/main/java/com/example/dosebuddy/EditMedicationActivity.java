package com.example.dosebuddy;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dosebuddy.database.AppDatabase;
import com.example.dosebuddy.database.Medication;
import com.example.dosebuddy.database.MedicationDao;
import com.example.dosebuddy.database.MedicationFrequency;
import com.example.dosebuddy.utils.DateTimeUtils;
import com.example.dosebuddy.utils.ReminderScheduler;
import com.example.dosebuddy.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for editing existing medications
 * Pre-populates form with existing medication data
 */
public class EditMedicationActivity extends AppCompatActivity {
    
    public static final String EXTRA_MEDICATION_ID = "medication_id";
    
    // UI Components
    private TextView tvHeader;
    private TextInputLayout tilMedicationName, tilDosage, tilTimesPerDay, tilNotes;
    private TextInputEditText etMedicationName, etDosage, etTimesPerDay, etNotes;
    private Spinner spinnerFrequency;
    private MaterialButton btnStartDate, btnEndDate, btnAddTime, btnUpdateMedication;
    private LinearLayout llTimesContainer;
    private ProgressBar progressBar;
    
    // Data
    private AppDatabase database;
    private MedicationDao medicationDao;
    private ExecutorService executorService;
    private int medicationId;
    private Medication currentMedication;
    
    // Date and time tracking
    private long selectedStartDate;
    private Long selectedEndDate;
    private List<Long> selectedTimes;
    private MedicationFrequency selectedFrequency;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);
        
        // Get medication ID from intent
        medicationId = getIntent().getIntExtra(EXTRA_MEDICATION_ID, -1);
        if (medicationId == -1) {
            Toast.makeText(this, "Error: Invalid medication", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        initializeDatabase();
        initializeData();
        setupFrequencySpinner();
        setupClickListeners();
        loadMedicationData();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeViews() {
        tvHeader = findViewById(R.id.tv_header);
        tilMedicationName = findViewById(R.id.til_medication_name);
        tilDosage = findViewById(R.id.til_dosage);
        tilTimesPerDay = findViewById(R.id.til_times_per_day);
        tilNotes = findViewById(R.id.til_notes);
        
        etMedicationName = findViewById(R.id.et_medication_name);
        etDosage = findViewById(R.id.et_dosage);
        etTimesPerDay = findViewById(R.id.et_times_per_day);
        etNotes = findViewById(R.id.et_notes);
        
        spinnerFrequency = findViewById(R.id.spinner_frequency);
        
        btnStartDate = findViewById(R.id.btn_start_date);
        btnEndDate = findViewById(R.id.btn_end_date);
        btnAddTime = findViewById(R.id.btn_add_time);
        btnUpdateMedication = findViewById(R.id.btn_save_medication);
        
        llTimesContainer = findViewById(R.id.ll_times_container);
        progressBar = findViewById(R.id.progress_bar);
        
        // Update UI for edit mode
        tvHeader.setText(getString(R.string.edit_medication_title));
        btnUpdateMedication.setText(getString(R.string.update_medication));
    }
    
    /**
     * Initialize database components
     */
    private void initializeDatabase() {
        database = AppDatabase.getInstance(this);
        medicationDao = database.medicationDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Initialize data structures
     */
    private void initializeData() {
        selectedTimes = new ArrayList<>();
        selectedFrequency = MedicationFrequency.ONCE_DAILY;
    }
    
    /**
     * Setup frequency spinner
     */
    private void setupFrequencySpinner() {
        String[] frequencyNames = MedicationFrequency.getDisplayNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, frequencyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);
        
        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFrequency = MedicationFrequency.getAllFrequencies()[position];
                updateTimesPerDayVisibility();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        btnStartDate.setOnClickListener(v -> showStartDatePicker());
        btnEndDate.setOnClickListener(v -> showEndDatePicker());
        btnAddTime.setOnClickListener(v -> showTimePicker());
        btnUpdateMedication.setOnClickListener(v -> updateMedication());
    }
    
    /**
     * Load existing medication data from database
     */
    private void loadMedicationData() {
        showLoading(true);
        
        executorService.execute(() -> {
            currentMedication = medicationDao.getMedicationById(medicationId);
            
            runOnUiThread(() -> {
                showLoading(false);
                if (currentMedication != null) {
                    populateForm();
                } else {
                    Toast.makeText(this, "Medication not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }
    
    /**
     * Populate form with existing medication data
     */
    private void populateForm() {
        // Set basic information
        etMedicationName.setText(currentMedication.getName());
        etDosage.setText(currentMedication.getDosage());
        
        if (currentMedication.getNotes() != null) {
            etNotes.setText(currentMedication.getNotes());
        }
        
        // Set frequency
        selectedFrequency = currentMedication.getFrequencyEnum();
        int frequencyPosition = getFrequencyPosition(selectedFrequency);
        spinnerFrequency.setSelection(frequencyPosition);
        
        // Set times per day
        etTimesPerDay.setText(String.valueOf(currentMedication.getTimesPerDay()));
        updateTimesPerDayVisibility();
        
        // Set dates
        selectedStartDate = currentMedication.getStartDate();
        selectedEndDate = currentMedication.getEndDate();
        updateStartDateButton();
        updateEndDateButton();
        
        // Parse and set specific times
        parseSpecificTimes();
        updateTimesDisplay();
    }
    
    /**
     * Get position of frequency in spinner
     */
    private int getFrequencyPosition(MedicationFrequency frequency) {
        MedicationFrequency[] frequencies = MedicationFrequency.getAllFrequencies();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] == frequency) {
                return i;
            }
        }
        return 0; // Default to first position
    }
    
    /**
     * Parse specific times from JSON string
     */
    private void parseSpecificTimes() {
        selectedTimes.clear();

        String specificTimesJson = currentMedication.getSpecificTimes();
        if (specificTimesJson != null && !specificTimesJson.isEmpty()) {
            try {
                // Simple JSON parsing for array of timestamps
                String timesStr = specificTimesJson.replace("[", "").replace("]", "");
                if (!timesStr.trim().isEmpty()) {
                    String[] timeStrings = timesStr.split(",");
                    for (String timeStr : timeStrings) {
                        long timestamp = Long.parseLong(timeStr.trim());
                        selectedTimes.add(timestamp);
                    }
                }
            } catch (Exception e) {
                // If parsing fails, ignore and use empty times
                selectedTimes.clear();
            }
        }
    }

    /**
     * Update times per day field visibility based on frequency
     */
    private void updateTimesPerDayVisibility() {
        if (selectedFrequency == MedicationFrequency.CUSTOM) {
            tilTimesPerDay.setVisibility(View.VISIBLE);
        } else {
            tilTimesPerDay.setVisibility(View.GONE);
        }
    }

    /**
     * Show start date picker dialog
     */
    private void showStartDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedStartDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedStartDate = DateTimeUtils.createDateTimestamp(year, month, dayOfMonth);
                    updateStartDateButton();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    /**
     * Show end date picker dialog
     */
    private void showEndDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedEndDate != null) {
            calendar.setTimeInMillis(selectedEndDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedEndDate = DateTimeUtils.createDateTimestamp(year, month, dayOfMonth);
                    updateEndDateButton();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set minimum date to start date
        datePickerDialog.getDatePicker().setMinDate(selectedStartDate);
        datePickerDialog.show();
    }

    /**
     * Show time picker dialog
     */
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    long timeTimestamp = DateTimeUtils.createTimeTimestamp(hourOfDay, minute);
                    if (!selectedTimes.contains(timeTimestamp)) {
                        selectedTimes.add(timeTimestamp);
                        updateTimesDisplay();
                    } else {
                        Toast.makeText(this, "Time already selected", Toast.LENGTH_SHORT).show();
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false // Use 12-hour format
        );

        timePickerDialog.show();
    }

    /**
     * Update start date button text
     */
    private void updateStartDateButton() {
        String dateText = DateTimeUtils.formatDate(selectedStartDate);
        btnStartDate.setText(dateText);
    }

    /**
     * Update end date button text
     */
    private void updateEndDateButton() {
        if (selectedEndDate != null) {
            String dateText = DateTimeUtils.formatDate(selectedEndDate);
            btnEndDate.setText(dateText);
        } else {
            btnEndDate.setText("Select End Date (Optional)");
        }
    }

    /**
     * Update times display in the container
     */
    private void updateTimesDisplay() {
        llTimesContainer.removeAllViews();

        for (int i = 0; i < selectedTimes.size(); i++) {
            final int index = i;
            final long timeTimestamp = selectedTimes.get(i);

            // Create time item layout
            LinearLayout timeItem = new LinearLayout(this);
            timeItem.setOrientation(LinearLayout.HORIZONTAL);
            timeItem.setPadding(16, 8, 16, 8);

            // Time text
            TextView timeText = new TextView(this);
            timeText.setText(DateTimeUtils.formatTime(timeTimestamp));
            timeText.setTextSize(16);
            timeText.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            // Remove button
            MaterialButton removeButton = new MaterialButton(this);
            removeButton.setText("Remove");
            removeButton.setOnClickListener(v -> {
                selectedTimes.remove(index);
                updateTimesDisplay();
            });

            timeItem.addView(timeText);
            timeItem.addView(removeButton);
            llTimesContainer.addView(timeItem);
        }
    }

    /**
     * Update medication in database
     */
    private void updateMedication() {
        if (!validateInputs()) {
            return;
        }

        showLoading(true);

        // Get input values
        String medicationName = etMedicationName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        int timesPerDay;
        if (selectedFrequency == MedicationFrequency.CUSTOM) {
            timesPerDay = Integer.parseInt(etTimesPerDay.getText().toString().trim());
        } else {
            timesPerDay = selectedFrequency.getDefaultTimesPerDay();
        }

        // Update medication object
        currentMedication.setName(medicationName);
        currentMedication.setDosage(dosage);
        currentMedication.setFrequencyEnum(selectedFrequency);
        currentMedication.setTimesPerDay(timesPerDay);
        currentMedication.setStartDate(selectedStartDate);
        currentMedication.setEndDate(selectedEndDate);
        currentMedication.setNotes(notes.isEmpty() ? null : notes);
        currentMedication.touch(); // Update timestamp

        // Convert selected times to JSON string
        if (!selectedTimes.isEmpty()) {
            StringBuilder timesJson = new StringBuilder("[");
            for (int i = 0; i < selectedTimes.size(); i++) {
                if (i > 0) timesJson.append(",");
                timesJson.append(selectedTimes.get(i));
            }
            timesJson.append("]");
            currentMedication.setSpecificTimes(timesJson.toString());
        } else {
            currentMedication.setSpecificTimes(null);
        }

        // Update in database in background thread
        executorService.execute(() -> {
            try {
                int rowsUpdated = medicationDao.updateMedication(currentMedication);

                runOnUiThread(() -> {
                    showLoading(false);
                    if (rowsUpdated > 0) {
                        // Reschedule reminders for the updated medication
                        ReminderScheduler.rescheduleMedicationReminders(this, currentMedication);

                        Toast.makeText(this, getString(R.string.medication_updated_successfully),
                                Toast.LENGTH_SHORT).show();
                        finish(); // Return to previous activity
                    } else {
                        Toast.makeText(this, getString(R.string.medication_update_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, getString(R.string.medication_update_failed),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Validate all input fields
     * @return True if all inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        boolean isValid = true;

        // Clear previous errors
        clearErrors();

        String medicationName = etMedicationName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();

        // Validate medication name
        String nameError = ValidationUtils.getMedicationNameError(medicationName);
        if (nameError != null) {
            tilMedicationName.setError(nameError);
            isValid = false;
        }

        // Validate dosage
        String dosageError = ValidationUtils.getDosageError(dosage);
        if (dosageError != null) {
            tilDosage.setError(dosageError);
            isValid = false;
        }

        // Validate times per day for custom frequency
        if (selectedFrequency == MedicationFrequency.CUSTOM) {
            String timesPerDayStr = etTimesPerDay.getText().toString().trim();
            if (timesPerDayStr.isEmpty()) {
                tilTimesPerDay.setError("Times per day cannot be empty");
                isValid = false;
            } else {
                try {
                    int timesPerDay = Integer.parseInt(timesPerDayStr);
                    String timesError = ValidationUtils.getTimesPerDayError(timesPerDay);
                    if (timesError != null) {
                        tilTimesPerDay.setError(timesError);
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    tilTimesPerDay.setError("Please enter a valid number");
                    isValid = false;
                }
            }
        }

        // Validate date range
        String dateRangeError = ValidationUtils.getDateRangeError(selectedStartDate, selectedEndDate);
        if (dateRangeError != null) {
            Toast.makeText(this, dateRangeError, Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate that at least one time is selected (except for as-needed)
        if (selectedFrequency != MedicationFrequency.AS_NEEDED && selectedTimes.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_no_times_selected), Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    /**
     * Clear all error messages
     */
    private void clearErrors() {
        tilMedicationName.setError(null);
        tilDosage.setError(null);
        tilTimesPerDay.setError(null);
        tilNotes.setError(null);
    }

    /**
     * Show/hide loading indicator
     * @param show True to show loading, false to hide
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdateMedication.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
