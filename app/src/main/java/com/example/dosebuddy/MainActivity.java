package com.example.dosebuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dosebuddy.adapter.MedicationAdapter;
import com.example.dosebuddy.database.AppDatabase;
import com.example.dosebuddy.database.Medication;
import com.example.dosebuddy.database.MedicationDao;
import com.example.dosebuddy.database.MedicationHistory;
import com.example.dosebuddy.utils.DateTimeUtils;
import com.example.dosebuddy.utils.MedicationHistoryManager;
import com.example.dosebuddy.utils.ReminderScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Activity for DoseBuddy
 * Medication dashboard showing user's medications with search and management features
 */
public class MainActivity extends AppCompatActivity implements MedicationAdapter.OnMedicationClickListener {

    // UI Components
    private TextView tvWelcome, tvMedicationCount;
    private TextInputEditText etSearch;
    private RecyclerView rvMedications;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabAddMedication, fabViewHistory;
    private MaterialButton btnGetStarted;

    // Data and Database
    private MedicationAdapter medicationAdapter;
    private AppDatabase database;
    private MedicationDao medicationDao;
    private ExecutorService executorService;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeDatabase();
        setupRecyclerView();
        setupClickListeners();
        setupSearch();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload medications when returning to this activity
        loadMedications();
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        tvMedicationCount = findViewById(R.id.tv_medication_count);
        etSearch = findViewById(R.id.et_search);
        rvMedications = findViewById(R.id.rv_medications);
        llEmptyState = findViewById(R.id.ll_empty_state);
        fabAddMedication = findViewById(R.id.fab_add_medication);
        fabViewHistory = findViewById(R.id.fab_view_history);
        btnGetStarted = findViewById(R.id.btn_get_started);
    }

    /**
     * Initialize database components
     */
    private void initializeDatabase() {
        database = AppDatabase.getInstance(this);
        medicationDao = database.medicationDao();
        executorService = Executors.newSingleThreadExecutor();
        currentUserId = getCurrentUserId();
    }

    /**
     * Setup RecyclerView and adapter
     */
    private void setupRecyclerView() {
        medicationAdapter = new MedicationAdapter(this);
        medicationAdapter.setOnMedicationClickListener(this);

        rvMedications.setLayoutManager(new LinearLayoutManager(this));
        rvMedications.setAdapter(medicationAdapter);
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        fabAddMedication.setOnClickListener(v -> navigateToAddMedication());
        fabViewHistory.setOnClickListener(v -> navigateToHistory());
        btnGetStarted.setOnClickListener(v -> navigateToAddMedication());
    }

    /**
     * Setup search functionality
     */
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                medicationAdapter.filterMedications(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    /**
     * Load medications from database
     */
    private void loadMedications() {
        executorService.execute(() -> {
            List<Medication> medications = medicationDao.getActiveMedicationsForUser(currentUserId);

            runOnUiThread(() -> {
                medicationAdapter.setMedications(medications);
                updateUI(medications);
            });
        });
    }

    /**
     * Update UI based on medication list
     */
    private void updateUI(List<Medication> medications) {
        if (medications.isEmpty()) {
            rvMedications.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
            tvMedicationCount.setText("0 medications");
        } else {
            rvMedications.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);

            String countText = medications.size() == 1 ?
                "1 medication" : medications.size() + " medications";
            tvMedicationCount.setText(countText);
        }
    }

    /**
     * Get current user ID from SharedPreferences
     */
    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("DoseBuddy", MODE_PRIVATE);
        return prefs.getInt("current_user_id", 1); // Default to 1 for now
    }

    /**
     * Navigate to Add Medication activity
     */
    private void navigateToAddMedication() {
        Intent intent = new Intent(this, AddMedicationActivity.class);
        startActivity(intent);
    }

    /**
     * Navigate to Medication History activity
     */
    private void navigateToHistory() {
        Intent intent = new Intent(this, MedicationHistoryActivity.class);
        startActivity(intent);
    }

    // MedicationAdapter.OnMedicationClickListener implementation

    @Override
    public void onMedicationClick(Medication medication) {
        // Navigate to edit medication
        navigateToEditMedication(medication.getId());
    }

    @Override
    public void onMedicationLongClick(Medication medication) {
        // Show quick actions menu
        showMedicationOptionsMenu(medication, null);
    }

    @Override
    public void onMoreOptionsClick(Medication medication, View view) {
        // Show popup menu with edit/delete options
        showMedicationOptionsMenu(medication, view);
    }

    @Override
    public void onMarkTakenClick(Medication medication) {
        showMarkTakenDialog(medication);
    }

    /**
     * Navigate to Edit Medication activity
     */
    private void navigateToEditMedication(int medicationId) {
        Intent intent = new Intent(this, EditMedicationActivity.class);
        intent.putExtra(EditMedicationActivity.EXTRA_MEDICATION_ID, medicationId);
        startActivity(intent);
    }

    /**
     * Show medication options menu (edit/delete)
     */
    private void showMedicationOptionsMenu(Medication medication, View anchorView) {
        if (anchorView != null) {
            // Show popup menu anchored to the view
            PopupMenu popupMenu = new PopupMenu(this, anchorView);
            popupMenu.getMenuInflater().inflate(R.menu.medication_options_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    navigateToEditMedication(medication.getId());
                    return true;
                } else if (itemId == R.id.action_delete) {
                    showDeleteConfirmationDialog(medication);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        } else {
            // Show alert dialog with options
            showMedicationOptionsDialog(medication);
        }
    }

    /**
     * Show medication options dialog
     */
    private void showMedicationOptionsDialog(Medication medication) {
        String[] options = {getString(R.string.edit), getString(R.string.delete_option)};

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.medication_options))
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit
                            navigateToEditMedication(medication.getId());
                            break;
                        case 1: // Delete
                            showDeleteConfirmationDialog(medication);
                            break;
                    }
                })
                .show();
    }

    /**
     * Show delete confirmation dialog
     */
    private void showDeleteConfirmationDialog(Medication medication) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_medication_title))
                .setMessage(getString(R.string.delete_medication_message) + "\n\n" +
                           medication.getName() + " (" + medication.getDosage() + ")")
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    deleteMedication(medication);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /**
     * Delete medication (soft delete)
     */
    private void deleteMedication(Medication medication) {
        executorService.execute(() -> {
            try {
                // Soft delete - set is_active to false
                int rowsUpdated = medicationDao.softDeleteMedication(
                        medication.getId(), System.currentTimeMillis());

                runOnUiThread(() -> {
                    if (rowsUpdated > 0) {
                        // Cancel reminders for the deleted medication
                        ReminderScheduler.cancelMedicationReminders(this, medication.getId());

                        // Show snackbar with undo option
                        Snackbar snackbar = Snackbar.make(
                                findViewById(R.id.main),
                                getString(R.string.medication_deleted),
                                Snackbar.LENGTH_LONG
                        );

                        snackbar.setAction(getString(R.string.undo), v -> {
                            restoreMedication(medication);
                        });

                        snackbar.show();

                        // Reload medications to update the list
                        loadMedications();
                    } else {
                        Toast.makeText(this, getString(R.string.medication_delete_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.medication_delete_failed),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Restore deleted medication
     */
    private void restoreMedication(Medication medication) {
        executorService.execute(() -> {
            try {
                int rowsUpdated = medicationDao.reactivateMedication(
                        medication.getId(), System.currentTimeMillis());

                runOnUiThread(() -> {
                    if (rowsUpdated > 0) {
                        // Reschedule reminders for the restored medication
                        ReminderScheduler.scheduleMedicationReminders(this, medication);

                        Toast.makeText(this, getString(R.string.medication_restored),
                                Toast.LENGTH_SHORT).show();
                        loadMedications();
                    }
                });

            } catch (Exception e) {
                // Ignore restore errors
            }
        });
    }

    /**
     * Show dialog to mark medication as taken
     */
    private void showMarkTakenDialog(Medication medication) {
        String[] options = {
            getString(R.string.just_now),
            getString(R.string.custom_time)
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.when_did_you_take))
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Just now
                            recordMedicationTaken(medication, System.currentTimeMillis());
                            break;
                        case 1: // Custom time
                            showCustomTimeDialog(medication);
                            break;
                    }
                })
                .show();
    }

    /**
     * Show dialog to select custom time for medication
     */
    private void showCustomTimeDialog(Medication medication) {
        // For simplicity, just use current time minus some hours
        // In a real app, you'd show a time picker
        String[] timeOptions = {
            "1 hour ago",
            "2 hours ago",
            "4 hours ago",
            "This morning"
        };

        new AlertDialog.Builder(this)
                .setTitle("When did you take " + medication.getName() + "?")
                .setItems(timeOptions, (dialog, which) -> {
                    long takenAt = System.currentTimeMillis();
                    switch (which) {
                        case 0: // 1 hour ago
                            takenAt -= 60 * 60 * 1000;
                            break;
                        case 1: // 2 hours ago
                            takenAt -= 2 * 60 * 60 * 1000;
                            break;
                        case 2: // 4 hours ago
                            takenAt -= 4 * 60 * 60 * 1000;
                            break;
                        case 3: // This morning (8 AM)
                            takenAt = DateTimeUtils.createTimeTimestamp(8, 0);
                            break;
                    }
                    recordMedicationTaken(medication, takenAt);
                })
                .show();
    }

    /**
     * Record medication as taken in history
     */
    private void recordMedicationTaken(Medication medication, long takenAt) {
        MedicationHistoryManager.recordMedicationTaken(
            this, currentUserId, medication, takenAt, MedicationHistory.TakenMethod.MANUAL);

        Toast.makeText(this, getString(R.string.dose_recorded), Toast.LENGTH_SHORT).show();

        // Refresh the medication list to update any status indicators
        loadMedications();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}