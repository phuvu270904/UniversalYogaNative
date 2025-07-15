package com.example.universalyoganative;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements YogaCourseAdapter.OnCourseActionListener {
    public static DatabaseHelper helper;
    private RecyclerView recyclerViewCourses;
    private YogaCourseAdapter courseAdapter;
    private TextView tvCourseCount, tvInstanceCount, tvEmptyState;
    private List<YogaCourse> courseList;
    private SessionManager sessionManager;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        helper = new DatabaseHelper(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set toolbar title with welcome message
        User currentUser = sessionManager.getLoggedInUser();
        if (currentUser != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Welcome, " + currentUser.getName());
        }

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();
        
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

    private void initializeViews() {
        recyclerViewCourses = findViewById(R.id.recyclerViewCourses);
        tvCourseCount = findViewById(R.id.tvCourseCount);
        tvInstanceCount = findViewById(R.id.tvInstanceCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        navView = findViewById(R.id.nav_view);
    }

    private void setupBottomNavigation() {
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Already on home, do nothing
                return true;
            } else if (itemId == R.id.navigation_account) {
                Toast.makeText(this, "Account - Coming soon!", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_bookings) {
                Toast.makeText(this, "Bookings - Coming soon!", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        courseAdapter = new YogaCourseAdapter(courseList, this);
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCourses.setAdapter(courseAdapter);
    }

    private void setupClickListeners() {
        FloatingActionButton fabAddCourse = findViewById(R.id.fabAddCourse);
        fabAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateYogaCourse.class);
            startActivity(intent);
        });

        MaterialButton btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        MaterialButton btnSync = findViewById(R.id.btnSync);
        btnSync.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SyncActivity.class);
            startActivity(intent);
        });

        MaterialButton btnResetDb = findViewById(R.id.btnResetDb);
        btnResetDb.setOnClickListener(v -> showResetDatabaseDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
        updateStatistics();
    }

    private void loadCourses() {
        courseList.clear();
        Cursor cursor = helper.readAllYogaCourse();
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                YogaCourse course = createCourseFromCursor(cursor);
                courseList.add(course);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        courseAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    @SuppressLint("Range")
    private YogaCourse createCourseFromCursor(Cursor cursor) {
        YogaCourse course = new YogaCourse();
        course.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        course.setDayOfWeek(cursor.getString(cursor.getColumnIndex("dayofweek")));
        course.setTime(cursor.getString(cursor.getColumnIndex("time")));
        course.setCapacity(cursor.getInt(cursor.getColumnIndex("capacity")));
        course.setDuration(cursor.getInt(cursor.getColumnIndex("duration")));
        course.setPrice(cursor.getFloat(cursor.getColumnIndex("price")));
        course.setType(cursor.getString(cursor.getColumnIndex("type")));
        course.setDescription(cursor.getString(cursor.getColumnIndex("description")));
        course.setDifficulty(cursor.getString(cursor.getColumnIndex("difficulty")));
        course.setLocation(cursor.getString(cursor.getColumnIndex("location")));
        course.setInstructor(cursor.getString(cursor.getColumnIndex("instructor")));
        return course;
    }

    private void updateStatistics() {
        Cursor courseCursor = helper.readAllYogaCourse();
        int courseCount = courseCursor != null ? courseCursor.getCount() : 0;
        if (courseCursor != null) courseCursor.close();
        tvCourseCount.setText(String.valueOf(courseCount));

        Cursor instanceCursor = helper.readAllClassInstances();
        int instanceCount = instanceCursor != null ? instanceCursor.getCount() : 0;
        if (instanceCursor != null) instanceCursor.close();
        tvInstanceCount.setText(String.valueOf(instanceCount));
    }

    private void updateEmptyState() {
        if (courseList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewCourses.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewCourses.setVisibility(View.VISIBLE);
        }
    }

    private void showResetDatabaseDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Database")
                .setMessage(R.string.confirm_reset_database)
                .setPositiveButton("Reset", (dialog, which) -> {
                    helper.resetDatabase();
                    loadCourses();
                    updateStatistics();
                    Toast.makeText(this, "Database reset successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditCourse(YogaCourse course) {
        Intent intent = new Intent(this, EditYogaCourse.class);
        intent.putExtra("course_id", course.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteCourse(YogaCourse course) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage(R.string.confirm_delete_course)
                .setPositiveButton("Delete", (dialog, which) -> {
                    helper.deleteYogaCourse(course.getId());
                    loadCourses();
                    updateStatistics();
                    Toast.makeText(this, R.string.course_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onViewInstances(YogaCourse course) {
        Intent intent = new Intent(this, ClassInstanceActivity.class);
        intent.putExtra("course_id", course.getId());
        intent.putExtra("course_name", course.getType());
        startActivity(intent);
    }

    @Override
    public void onCourseClick(YogaCourse course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra("course_id", course.getId());
        startActivity(intent);
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
}