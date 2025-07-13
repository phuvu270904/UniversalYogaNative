package com.example.universalyoganative;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        helper = new DatabaseHelper(getApplicationContext());

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        initializeViews();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup click listeners
        setupClickListeners();
        
        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        recyclerViewCourses = findViewById(R.id.recyclerViewCourses);
        tvCourseCount = findViewById(R.id.tvCourseCount);
        tvInstanceCount = findViewById(R.id.tvInstanceCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        courseAdapter = new YogaCourseAdapter(courseList, this);
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCourses.setAdapter(courseAdapter);
    }

    private void setupClickListeners() {
        // Floating Action Button
        FloatingActionButton fabAddCourse = findViewById(R.id.fabAddCourse);
        fabAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateYogaCourse.class);
            startActivity(intent);
        });

        // Search Button
        MaterialButton btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Sync Button
        MaterialButton btnSync = findViewById(R.id.btnSync);
        btnSync.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SyncActivity.class);
            startActivity(intent);
        });

        // Reset Database Button
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
        // Update course count
        Cursor courseCursor = helper.readAllYogaCourse();
        int courseCount = courseCursor != null ? courseCursor.getCount() : 0;
        if (courseCursor != null) courseCursor.close();
        tvCourseCount.setText(String.valueOf(courseCount));

        // Update instance count
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

    // YogaCourseAdapter.OnCourseActionListener implementation
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
}