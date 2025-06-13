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
                            // No results found from API
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

                    // Provide more specific error messages
                    String specificError = errorMessage;
                    if (errorMessage != null) {
                        if (errorMessage.contains("UnknownHostException") || errorMessage.contains("ConnectException")) {
                            specificError = "No internet connection available. Please check your network connection and try again.";
                        } else if (errorMessage.contains("SocketTimeoutException")) {
                            specificError = "Request timed out. The FDA API service may be slow or unavailable. Please try again later.";
                        } else if (errorMessage.contains("HTTP 404")) {
                            specificError = "Drug information not found in the FDA database for '" + drugName + "'.";
                        } else if (errorMessage.contains("HTTP 500")) {
                            specificError = "FDA API service is currently experiencing issues. Please try again later.";
                        }
                    }

                    showError(specificError);
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
        contentLayout.setVisibility(View.GONE);
        tvNoDrugInfo.setVisibility(View.VISIBLE);
        tvNoDrugInfo.setText("No information found for '" + drugName + "'.\n\nThis might be because:\n• The drug name is misspelled\n• It's not in the FDA database\n• It's a very new medication\n• The API service is currently unavailable\n\nPlease check the spelling and try again, or consult your healthcare provider for accurate medication information.");
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
