package com.example.universalyoganative;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class BookingsActivity extends AppCompatActivity implements BookingAdapter.OnBookingClickListener {
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView tvNoBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewBookings);
        tvNoBookings = findViewById(R.id.tvNoBookings);

        // Set up toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Set up bottom navigation
        setupBottomNavigation();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Load bookings
        loadBookings();
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
                    startActivity(new Intent(this, ProfileActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_bookings) {
                    // Already on bookings activity
                    return true;
                }
                return false;
            });

            // Set the active menu item for bookings
            navView.setSelectedItemId(R.id.navigation_bookings);
        }
    }

    private void loadBookings() {
        // Load all bookings from all users
        List<Booking> bookings = dbHelper.getAllBookings();
        
        if (bookings.isEmpty()) {
            tvNoBookings.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoBookings.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            
            adapter = new BookingAdapter(this, bookings, this);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onBookingClick(Booking booking) {
        // Handle booking click if needed
        // For example, show booking details or cancellation dialog
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 