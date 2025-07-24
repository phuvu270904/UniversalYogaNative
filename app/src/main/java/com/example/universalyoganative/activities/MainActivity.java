package com.example.universalyoganative;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {
    public static DatabaseHelper helper;
    private SessionManager sessionManager;
    private BottomNavigationView bottomNavigation;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize session manager before calling super (which sets content view)
        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        super.onCreate(savedInstanceState);

        helper = new DatabaseHelper(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set toolbar title with welcome message
        User currentUser = sessionManager.getLoggedInUser();
        if (currentUser != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Welcome, " + currentUser.getName());
        }

        // Load default fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "Home");
        }
        
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

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.nav_view);
    }

    @Override
    protected void setupBottomNavigation() {
        // Initialize views first to ensure bottomNavigation is not null
        initializeViews();
        
        super.setupBottomNavigation();
        
        // Override the parent's navigation behavior for fragment-based navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";
            
            if (item.getItemId() == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
                title = "Home";
            } else if (item.getItemId() == R.id.navigation_account) {
                selectedFragment = new AccountFragment();
                title = "Account Management";
            } else if (item.getItemId() == R.id.navigation_bookings) {
                selectedFragment = new BookingsFragment();
                title = "Booking Management";
            } else if (item.getItemId() == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
                title = "Profile";
            }
            
            if (selectedFragment != null) {
                loadFragment(selectedFragment, title);
                return true;
            }
            
            return false;
        });
    }

    private void loadFragment(Fragment fragment, String title) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Check if this is the same type of fragment to avoid unnecessary replacements
        if (currentFragment == null || !currentFragment.getClass().equals(fragment.getClass())) {
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
            currentFragment = fragment;
        }
        
        // Update toolbar title
        if (getSupportActionBar() != null) {
            if (title.equals("Home")) {
                // Show welcome message for home
                User currentUser = sessionManager.getLoggedInUser();
                if (currentUser != null) {
                    getSupportActionBar().setTitle("Welcome, " + currentUser.getName());
                } else {
                    getSupportActionBar().setTitle("Home");
                }
            } else {
                getSupportActionBar().setTitle(title);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            showLogoutDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.logoutUser();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // If not on home fragment, go to home fragment first
        if (bottomNavigation.getSelectedItemId() != R.id.navigation_home) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        } else {
            // If on home fragment, show exit dialog or exit app
            super.onBackPressed();
        }
    }
}