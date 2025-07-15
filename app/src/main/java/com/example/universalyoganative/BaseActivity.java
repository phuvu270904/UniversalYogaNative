package com.example.universalyoganative;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {
    protected BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        
        setupBottomNavigation();
    }

    protected void setupBottomNavigation() {
        navView = findViewById(R.id.nav_view);
        if (navView != null) {
            navView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    if (!(this instanceof MainActivity)) {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                    return true;
                } else if (itemId == R.id.navigation_account) {
                    Toast.makeText(this, "Account - Coming soon!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    if (!(this instanceof ProfileActivity)) {
                        startActivity(new Intent(this, ProfileActivity.class));
                        finish();
                    }
                    return true;
                } else if (itemId == R.id.navigation_bookings) {
                    Toast.makeText(this, "Bookings - Coming soon!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            // Set the active menu item based on the current activity
            if (this instanceof MainActivity) {
                navView.setSelectedItemId(R.id.navigation_home);
            } else if (this instanceof ProfileActivity) {
                navView.setSelectedItemId(R.id.navigation_profile);
            }
        }
    }

    // Each activity must implement this to provide its layout
    @LayoutRes
    protected abstract int getLayoutResourceId();
} 