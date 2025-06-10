package com.example.dosebuddy;

import android.content.Intent;
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
 * Registration Activity for DoseBuddy
 * Handles user registration with validation and database storage
 */
public class RegisterActivity extends AppCompatActivity {
    
    // UI Components
    private TextInputLayout tilFullName, tilUsername, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etUsername, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;
    
    // Database
    private AppDatabase database;
    private UserDao userDao;
    private ExecutorService executorService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        initializeViews();
        initializeDatabase();
        setupClickListeners();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeViews() {
        tilFullName = findViewById(R.id.til_full_name);
        tilUsername = findViewById(R.id.til_username);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        
        etFullName = findViewById(R.id.et_full_name);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);
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
        btnRegister.setOnClickListener(v -> handleRegistration());
        tvLoginLink.setOnClickListener(v -> navigateToLogin());
    }
    
    /**
     * Handle user registration process
     */
    private void handleRegistration() {
        if (!validateInputs()) {
            return;
        }
        
        showLoading(true);
        
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        
        // Perform registration in background thread
        executorService.execute(() -> {
            try {
                // Check if username already exists
                if (userDao.isUsernameExists(username)) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        tilUsername.setError(getString(R.string.error_username_exists));
                    });
                    return;
                }
                
                // Check if email already exists
                if (userDao.isEmailExists(email)) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        tilEmail.setError(getString(R.string.error_email_exists));
                    });
                    return;
                }
                
                // Create password hash
                String passwordHash = PasswordUtils.createPasswordHash(password);
                
                // Create new user
                User newUser = new User(username, email, fullName, passwordHash);
                
                // Insert user into database
                long userId = userDao.insertUser(newUser);
                
                runOnUiThread(() -> {
                    showLoading(false);
                    if (userId > 0) {
                        Toast.makeText(this, getString(R.string.registration_successful), 
                                Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        Toast.makeText(this, getString(R.string.registration_failed), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, getString(R.string.registration_failed), 
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
        
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        
        // Validate full name
        String fullNameError = ValidationUtils.getFullNameError(fullName);
        if (fullNameError != null) {
            tilFullName.setError(fullNameError);
            isValid = false;
        }
        
        // Validate username
        String usernameError = ValidationUtils.getUsernameError(username);
        if (usernameError != null) {
            tilUsername.setError(usernameError);
            isValid = false;
        }
        
        // Validate email
        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            tilEmail.setError(emailError);
            isValid = false;
        }
        
        // Validate password
        String passwordError = ValidationUtils.getPasswordError(password);
        if (passwordError != null) {
            tilPassword.setError(passwordError);
            isValid = false;
        }
        
        // Validate password confirmation
        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Clear all error messages
     */
    private void clearErrors() {
        tilFullName.setError(null);
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }
    
    /**
     * Show/hide loading indicator
     * @param show True to show loading, false to hide
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
    
    /**
     * Navigate to login activity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
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
