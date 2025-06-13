package com.example.dosebuddy;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.dosebuddy.api.DrugInfo;
import com.example.dosebuddy.api.DrugInfoService;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Activity to display detailed drug information from FDA API
 */
public class DrugInfoActivity extends AppCompatActivity {

    public static final String EXTRA_DRUG_NAME = "drug_name";

    // UI Components
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;
    private TextView tvNoDrugInfo;

    // Data
    private DrugInfoService drugInfoService;
    private String drugName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_drug_info);

            // Get drug name from intent
            drugName = getIntent().getStringExtra(EXTRA_DRUG_NAME);
            if (drugName == null || drugName.trim().isEmpty()) {
                Toast.makeText(this, "No drug name provided", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            initializeViews();
            setupToolbar();
            initializeService();
            loadDrugInfo();

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        contentLayout = findViewById(R.id.content_layout);
        tvNoDrugInfo = findViewById(R.id.tv_no_drug_info);
    }

    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        try {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Drug Information");
                getSupportActionBar().setSubtitle(drugName);
            }
        } catch (Exception e) {
            // If toolbar setup fails, continue without it
            Toast.makeText(this, "Toolbar setup failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initialize drug info service
     */
    private void initializeService() {
        drugInfoService = new DrugInfoService();
    }

    /**
     * Load drug information from API
     */
    private void loadDrugInfo() {
        showLoading(true);

        drugInfoService.searchDrugInfo(drugName, new DrugInfoService.DrugInfoCallback() {
            @Override
            public void onSuccess(List<DrugInfo> drugInfoList) {
                runOnUiThread(() -> {
                    try {
                        showLoading(false);
                        if (drugInfoList != null && !drugInfoList.isEmpty()) {
                            displayDrugInfo(drugInfoList.get(0)); // Show first result
                        } else {
                            // Show sample data for common medications
                            showNoDrugInfo();
                        }
                    } catch (Exception e) {
                        showError("Error displaying results: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError(errorMessage);
                });
            }
        });
    }

    /**
     * Display drug information in the UI
     */
    private void displayDrugInfo(DrugInfo drugInfo) {
        try {
            contentLayout.removeAllViews();
            contentLayout.setVisibility(View.VISIBLE);
            tvNoDrugInfo.setVisibility(View.GONE);

            // Basic Information Card
            addInfoCard("Basic Information", createBasicInfoContent(drugInfo));

            // Purpose Card
            if (drugInfo.getPurpose() != null && !drugInfo.getPurpose().trim().isEmpty()) {
                addInfoCard("Purpose", drugInfo.getPurpose());
            }

            // Dosage Information Card
            if (drugInfo.getDosageAndAdministration() != null && !drugInfo.getDosageAndAdministration().trim().isEmpty()) {
                addInfoCard("Dosage & Administration", drugInfo.getDosageAndAdministration());
            }

            // Warnings Card
            if (drugInfo.getWarnings() != null && !drugInfo.getWarnings().isEmpty()) {
                StringBuilder warningsText = new StringBuilder();
                for (String warning : drugInfo.getWarnings()) {
                    if (warningsText.length() > 0) warningsText.append("\n\n");
                    warningsText.append("• ").append(warning);
                }
                addInfoCard("⚠️ Warnings", warningsText.toString());
            }

            // Side Effects Card
            if (drugInfo.getSideEffects() != null && !drugInfo.getSideEffects().isEmpty()) {
                StringBuilder sideEffectsText = new StringBuilder();
                for (String sideEffect : drugInfo.getSideEffects()) {
                    if (sideEffectsText.length() > 0) sideEffectsText.append("\n\n");
                    sideEffectsText.append("• ").append(sideEffect);
                }
                addInfoCard("Side Effects", sideEffectsText.toString());
            }

            // Manufacturer Card
            if (drugInfo.getManufacturer() != null && !drugInfo.getManufacturer().trim().isEmpty()) {
                addInfoCard("Manufacturer", drugInfo.getManufacturer());
            }

            // Description Card
            if (drugInfo.getDescription() != null && !drugInfo.getDescription().trim().isEmpty()) {
                addInfoCard("Description", drugInfo.getDescription());
            }

        } catch (Exception e) {
            showError("Error displaying drug information: " + e.getMessage());
        }
    }

    /**
     * Create basic information content
     */
    private String createBasicInfoContent(DrugInfo drugInfo) {
        StringBuilder content = new StringBuilder();

        if (drugInfo.getBrandName() != null && !drugInfo.getBrandName().trim().isEmpty()) {
            content.append("Brand Name: ").append(drugInfo.getBrandName()).append("\n");
        }

        if (drugInfo.getGenericName() != null && !drugInfo.getGenericName().trim().isEmpty()) {
            content.append("Generic Name: ").append(drugInfo.getGenericName()).append("\n");
        }

        if (drugInfo.getActiveIngredient() != null && !drugInfo.getActiveIngredient().trim().isEmpty()) {
            content.append("Active Ingredient: ").append(drugInfo.getActiveIngredient());
        }

        return content.toString().trim();
    }

    /**
     * Add an information card to the layout
     */
    private void addInfoCard(String title, String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        try {
            // Create card view
            MaterialCardView cardView = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 32);
            cardView.setLayoutParams(cardParams);
            cardView.setCardElevation(4);
            cardView.setRadius(12);

            // Create content layout
            LinearLayout cardContent = new LinearLayout(this);
            cardContent.setOrientation(LinearLayout.VERTICAL);
            cardContent.setPadding(32, 24, 32, 24);

            // Title
            TextView titleView = new TextView(this);
            titleView.setText(title);
            titleView.setTextSize(18);
            titleView.setTextColor(getResources().getColor(android.R.color.black, null));
            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
            titleView.setPadding(0, 0, 0, 16);

            // Content
            TextView contentView = new TextView(this);
            contentView.setText(content);
            contentView.setTextSize(14);
            contentView.setTextColor(getResources().getColor(android.R.color.black, null));
            contentView.setLineSpacing(4, 1.2f);

            cardContent.addView(titleView);
            cardContent.addView(contentView);
            cardView.addView(cardContent);

            contentLayout.addView(cardView);

        } catch (Exception e) {
            // If card creation fails, add simple text
            TextView simpleText = new TextView(this);
            simpleText.setText(title + "\n" + content + "\n");
            simpleText.setPadding(16, 16, 16, 16);
            contentLayout.addView(simpleText);
        }
    }

    /**
     * Show loading indicator
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        tvNoDrugInfo.setVisibility(View.GONE);
    }

    /**
     * Show no drug info message
     */
    private void showNoDrugInfo() {
        // For testing purposes, show sample data for common medications
        if (drugName.toLowerCase().contains("paracetamol") || drugName.toLowerCase().contains("acetaminophen")) {
            showSampleDrugInfo("Acetaminophen (Paracetamol)");
        } else if (drugName.toLowerCase().contains("aspirin")) {
            showSampleDrugInfo("Aspirin");
        } else if (drugName.toLowerCase().contains("ibuprofen")) {
            showSampleDrugInfo("Ibuprofen");
        } else {
            contentLayout.setVisibility(View.GONE);
            tvNoDrugInfo.setVisibility(View.VISIBLE);
            tvNoDrugInfo.setText("No information found for '" + drugName + "'.\n\nThis might be because:\n• The drug name is misspelled\n• It's not in the FDA database\n• It's a very new medication\n\nNote: This is a demo version showing sample data for common medications.");
        }
    }

    /**
     * Show sample drug information for testing
     */
    private void showSampleDrugInfo(String drugType) {
        try {
            contentLayout.removeAllViews();
            contentLayout.setVisibility(View.VISIBLE);
            tvNoDrugInfo.setVisibility(View.GONE);

            switch (drugType) {
                case "Acetaminophen (Paracetamol)":
                    addInfoCard("Basic Information", "Brand Name: Tylenol\nGeneric Name: Acetaminophen\nActive Ingredient: Acetaminophen");
                    addInfoCard("Purpose", "Pain reliever and fever reducer");
                    addInfoCard("Dosage & Administration", "Adults: 325-650 mg every 4-6 hours as needed. Do not exceed 3000 mg in 24 hours.");
                    addInfoCard("⚠️ Warnings", "• Do not exceed recommended dose\n• Severe liver damage may occur if you take more than directed\n• Do not use with other products containing acetaminophen");
                    addInfoCard("Side Effects", "• Rare at recommended doses\n• Liver damage with overdose\n• Allergic reactions (rare)");
                    break;

                case "Aspirin":
                    addInfoCard("Basic Information", "Brand Name: Bayer Aspirin\nGeneric Name: Aspirin\nActive Ingredient: Acetylsalicylic Acid");
                    addInfoCard("Purpose", "Pain reliever, fever reducer, and anti-inflammatory");
                    addInfoCard("Dosage & Administration", "Adults: 325-650 mg every 4 hours as needed for pain/fever. For heart protection: 81 mg daily as directed by doctor.");
                    addInfoCard("⚠️ Warnings", "• May cause stomach bleeding\n• Do not give to children under 12\n• Consult doctor if taking blood thinners\n• Stop use if ringing in ears occurs");
                    addInfoCard("Side Effects", "• Stomach upset or bleeding\n• Heartburn\n• Nausea\n• Ringing in ears (with high doses)");
                    break;

                case "Ibuprofen":
                    addInfoCard("Basic Information", "Brand Name: Advil, Motrin\nGeneric Name: Ibuprofen\nActive Ingredient: Ibuprofen");
                    addInfoCard("Purpose", "Pain reliever, fever reducer, and anti-inflammatory");
                    addInfoCard("Dosage & Administration", "Adults: 200-400 mg every 4-6 hours as needed. Do not exceed 1200 mg in 24 hours unless directed by doctor.");
                    addInfoCard("⚠️ Warnings", "• May cause stomach bleeding\n• Increased risk of heart attack or stroke\n• Do not use if allergic to aspirin\n• Consult doctor if taking blood pressure medications");
                    addInfoCard("Side Effects", "• Stomach upset\n• Heartburn\n• Dizziness\n• Headache\n• Fluid retention");
                    break;
            }

            // Add note about sample data
            TextView noteView = new TextView(this);
            noteView.setText("\n📝 Note: This is sample information for demonstration purposes. Always consult your healthcare provider or pharmacist for accurate medication information.");
            noteView.setTextSize(12);
            noteView.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            noteView.setPadding(16, 16, 16, 16);
            noteView.setBackgroundColor(0xFFF5F5F5);
            contentLayout.addView(noteView);

        } catch (Exception e) {
            showError("Error displaying sample information: " + e.getMessage());
        }
    }

    /**
     * Show error message
     */
    private void showError(String errorMessage) {
        contentLayout.setVisibility(View.GONE);
        tvNoDrugInfo.setVisibility(View.VISIBLE);
        tvNoDrugInfo.setText("Error loading drug information:\n\n" + errorMessage + "\n\nPlease check your internet connection and try again.");

        Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (drugInfoService != null) {
            drugInfoService.shutdown();
        }
    }
}
