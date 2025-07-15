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
                    if (!(this instanceof AccountActivity)) {
                        startActivity(new Intent(this, AccountActivity.class));
                        finish();
                    }
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    if (!(this instanceof ProfileActivity)) {
                        startActivity(new Intent(this, ProfileActivity.class));
                        finish();
                    }
                    return true;
                } else if (itemId == R.id.navigation_bookings) {
                    if (!(this instanceof BookingsActivity)) {
                        startActivity(new Intent(this, BookingsActivity.class));
                        finish();
                    }
                    return true;
                }
                return false;
            });

            // Set the active menu item based on the current activity
            if (this instanceof MainActivity) {
                navView.setSelectedItemId(R.id.navigation_home);
            } else if (this instanceof ProfileActivity) {
                navView.setSelectedItemId(R.id.navigation_profile);
            } else if (this instanceof AccountActivity) {
                navView.setSelectedItemId(R.id.navigation_account);
            } else if (this instanceof BookingsActivity) {
                navView.setSelectedItemId(R.id.navigation_bookings);
            }
        }
    }

    @LayoutRes
    protected abstract int getLayoutResourceId();
} 