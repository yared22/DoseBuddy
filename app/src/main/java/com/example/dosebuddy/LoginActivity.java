package com.example.dosebuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dosebuddy.database.AppDatabase;
import com.example.dosebuddy.database.User;
import com.example.dosebuddy.database.UserDao;
import com.example.dosebuddy.utils.PasswordUtils;
import com.example.dosebuddy.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Login Activity for DoseBuddy
 * Handles user authentication
 */
public class LoginActivity extends AppCompatActivity {
    
    // UI Components
    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegisterLink;
    private ProgressBar progressBar;
    
    // Database
    private AppDatabase database;
    private UserDao userDao;
    private ExecutorService executorService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        initializeViews();
        initializeDatabase();
        setupClickListeners();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeViews() {
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        
        btnLogin = findViewById(R.id.btn_login);
        tvRegisterLink = findViewById(R.id.tv_register_link);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    /**
     * Initialize database components
     */
    private void initializeDatabase() {
        database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Setup click listeners for UI components
     */
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        tvRegisterLink.setOnClickListener(v -> navigateToRegister());
    }
    
    /**
     * Handle user login process
     */
    private void handleLogin() {
        if (!validateInputs()) {
            return;
        }
        
        showLoading(true);
        
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        
        // Perform login in background thread
        executorService.execute(() -> {
            try {
                // Get user from database
                User user = userDao.getUserByUsername(username);
                
                if (user != null && PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
                    // Login successful - save user session
                    saveUserSession(user.getId());

                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    });
                } else {
                    // Login failed
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    });
                }
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * Validate input fields
     * @return True if inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        boolean isValid = true;
        
        // Clear previous errors
        clearErrors();
        
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        
        // Validate username
        if (ValidationUtils.isEmpty(username)) {
            tilUsername.setError(getString(R.string.error_empty_field));
            isValid = false;
        }
        
        // Validate password
        if (ValidationUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_field));
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Clear all error messages
     */
    private void clearErrors() {
        tilUsername.setError(null);
        tilPassword.setError(null);
    }
    
    /**
     * Show/hide loading indicator
     * @param show True to show loading, false to hide
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }
    
    /**
     * Navigate to register activity
     */
    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
    
    /**
     * Check if user is already logged in
     */
    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("DoseBuddy", MODE_PRIVATE);
        return prefs.getBoolean("is_logged_in", false);
    }

    /**
     * Save user session to SharedPreferences
     */
    private void saveUserSession(int userId) {
        SharedPreferences prefs = getSharedPreferences("DoseBuddy", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("current_user_id", userId);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    /**
     * Navigate to main activity after successful login
     */
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
