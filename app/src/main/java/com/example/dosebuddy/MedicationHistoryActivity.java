package com.example.dosebuddy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dosebuddy.adapter.HistoryAdapter;
import com.example.dosebuddy.database.AppDatabase;
import com.example.dosebuddy.database.Medication;
import com.example.dosebuddy.database.MedicationDao;
import com.example.dosebuddy.database.MedicationHistory;
import com.example.dosebuddy.database.MedicationHistoryDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity to display medication history and adherence statistics
 */
public class MedicationHistoryActivity extends AppCompatActivity {
    
    // UI Components
    private Toolbar toolbar;
    private Spinner spinnerMedicationFilter;
    private RecyclerView rvHistory;
    private LinearLayout llEmptyState;
    private TextView tvEmptyMessage;
    private TextView tvAdherenceStats;
    
    // Data
    private HistoryAdapter historyAdapter;
    private AppDatabase database;
    private MedicationHistoryDao historyDao;
    private MedicationDao medicationDao;
    private ExecutorService executorService;
    private int currentUserId;
    private List<Medication> userMedications;
    private int selectedMedicationId = -1; // -1 means all medications
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_history);
        
        initializeViews();
        initializeDatabase();
        setupToolbar();
        setupRecyclerView();
        setupMedicationFilter();
        loadUserMedications();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerMedicationFilter = findViewById(R.id.spinner_medication_filter);
        rvHistory = findViewById(R.id.rv_history);
        llEmptyState = findViewById(R.id.ll_empty_state);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        tvAdherenceStats = findViewById(R.id.tv_adherence_stats);
    }
    
    /**
     * Initialize database components
     */
    private void initializeDatabase() {
        database = AppDatabase.getInstance(this);
        historyDao = database.medicationHistoryDao();
        medicationDao = database.medicationDao();
        executorService = Executors.newSingleThreadExecutor();
        currentUserId = getCurrentUserId();
    }
    
    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.medication_history_title));
        }
    }
    
    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }
    
    /**
     * Setup medication filter spinner
     */
    private void setupMedicationFilter() {
        spinnerMedicationFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedMedicationId = -1; // All medications
                } else {
                    selectedMedicationId = userMedications.get(position - 1).getId();
                }
                loadHistory();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    /**
     * Load user medications for filter
     */
    private void loadUserMedications() {
        executorService.execute(() -> {
            userMedications = medicationDao.getActiveMedicationsForUser(currentUserId);
            
            runOnUiThread(() -> {
                setupMedicationFilterAdapter();
                loadHistory();
            });
        });
    }
    
    /**
     * Setup medication filter adapter
     */
    private void setupMedicationFilterAdapter() {
        List<String> medicationNames = new ArrayList<>();
        medicationNames.add(getString(R.string.all_medications));
        
        for (Medication medication : userMedications) {
            medicationNames.add(medication.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, medicationNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMedicationFilter.setAdapter(adapter);
    }
    
    /**
     * Load medication history
     */
    private void loadHistory() {
        executorService.execute(() -> {
            List<MedicationHistory> history;
            
            if (selectedMedicationId == -1) {
                // Load all history for user
                history = historyDao.getHistoryForUser(currentUserId);
            } else {
                // Load history for specific medication
                history = historyDao.getHistoryForUserAndMedication(currentUserId, selectedMedicationId);
            }
            
            runOnUiThread(() -> {
                historyAdapter.setHistory(history);
                updateUI(history);
                calculateAndShowAdherenceStats(history);
            });
        });
    }
    
    /**
     * Update UI based on history data
     */
    private void updateUI(List<MedicationHistory> history) {
        if (history.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText(getString(R.string.no_history_yet));
            tvAdherenceStats.setVisibility(View.GONE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            tvAdherenceStats.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Calculate and show adherence statistics
     */
    private void calculateAndShowAdherenceStats(List<MedicationHistory> history) {
        if (history.isEmpty()) {
            return;
        }
        
        int totalDoses = history.size();
        int onTimeDoses = 0;
        
        for (MedicationHistory record : history) {
            if (record.isOnTime()) {
                onTimeDoses++;
            }
        }
        
        int adherencePercentage = totalDoses > 0 ? (onTimeDoses * 100) / totalDoses : 0;
        
        String statsText = getString(R.string.doses_taken, totalDoses, totalDoses) + "\n" +
                          getString(R.string.adherence_percentage, adherencePercentage);
        
        tvAdherenceStats.setText(statsText);
    }
    
    /**
     * Get current user ID
     */
    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("DoseBuddy", MODE_PRIVATE);
        return prefs.getInt("current_user_id", 1);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
