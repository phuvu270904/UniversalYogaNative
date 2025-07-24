package com.example.universalyoganative;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private TextView tvName, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
        }

        setupBottomNavigation();
        initializeViews();
        displayUserInfo();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            
            v.setPadding(systemBars.left, statusBarHeight, systemBars.right, 0);
            return insets;
        });
    }

    protected void setupBottomNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        if (navView != null) {
            navView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_account) {
                    startActivity(new Intent(this, AccountActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    // Already on profile activity
                    return true;
                } else if (itemId == R.id.navigation_bookings) {
                    startActivity(new Intent(this, BookingsActivity.class));
                    finish();
                    return true;
                }
                return false;
            });

            // Set the active menu item for profile
            navView.setSelectedItemId(R.id.navigation_profile);
        }
    }

    private void initializeViews() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
    }

    private void displayUserInfo() {
        User user = sessionManager.getLoggedInUser();
        if (user != null) {
            tvName.setText(user.getName());
            tvEmail.setText(user.getEmail());
        }
    }
} 