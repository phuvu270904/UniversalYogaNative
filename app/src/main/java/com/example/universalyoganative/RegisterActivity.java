package com.example.universalyoganative;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database helper and session manager
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());
        
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate input
        if (!validateInput(name, email, password, confirmPassword)) {
            return;
        }

        // Show loading
        showLoading(true);

        // Register user in background thread
        new Thread(() -> {
            try {
                // Check if email already exists
                if (databaseHelper.emailExists(email)) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Email already exists. Please use a different email.", 
                                     Toast.LENGTH_SHORT).show();
                        etEmail.setError("Email already exists");
                        etEmail.requestFocus();
                    });
                    return;
                }

                // Register user with admin role
                long userId = databaseHelper.registerUser(name, email, password, "admin");
                
                runOnUiThread(() -> {
                    showLoading(false);
                    
                    if (userId > 0) {
                        // Registration successful
                        Toast.makeText(this, "Account created successfully! Please login.", 
                                     Toast.LENGTH_SHORT).show();
                        
                        // Navigate to login activity
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("registered_email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        // Registration failed
                        Toast.makeText(this, "Registration failed. Please try again.", 
                                     Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        // Validate name
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        if (name.length() < 2) {
            etName.setError("Name must be at least 2 characters");
            etName.requestFocus();
            return false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            btnRegister.setText("Creating Account...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            btnRegister.setText("Create Account");
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Allow going back to login activity
        super.onBackPressed();
    }
} 