package com.example.universalyoganative;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class ConfirmCourseActivity extends AppCompatActivity {
    private YogaCourse course;
    private boolean isEditing;
    private long courseId;
    
    private MaterialTextView tvType, tvDifficulty, tvDescription;
    private MaterialTextView tvDayOfWeek, tvTime, tvDuration, tvCapacity, tvPrice;
    private MaterialTextView tvLocation;
    private MaterialButton btnConfirm, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_course);
        
        // Get course data from intent
        course = (YogaCourse) getIntent().getSerializableExtra("course");
        isEditing = getIntent().getBooleanExtra("isEditing", false);
        courseId = getIntent().getLongExtra("course_id", -1);
        
        if (course == null) {
            Toast.makeText(this, "Error: Course data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupToolbar();
        initializeViews();
        populateFields();
        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }

            v.setPadding(systemBars.left, statusBarHeight, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(isEditing ? "Edit Course Confirmation" : "Course Confirmation");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        // Course information
        tvType = findViewById(R.id.tvType);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvDescription = findViewById(R.id.tvDescription);
        
        // Schedule information
        tvDayOfWeek = findViewById(R.id.tvDayOfWeek);
        tvTime = findViewById(R.id.tvTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvCapacity = findViewById(R.id.tvCapacity);
        tvPrice = findViewById(R.id.tvPrice);
        
        // Location information
        tvLocation = findViewById(R.id.tvLocation);
        
        // Buttons
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);
    }

    private void populateFields() {
        tvType.setText(course.getType());
        tvDifficulty.setText(course.getDifficulty() != null ? course.getDifficulty() : "All Levels");
        tvDescription.setText(course.getDescription() != null && !course.getDescription().isEmpty() 
                ? course.getDescription() : "No description provided");
        
        tvDayOfWeek.setText(course.getDayOfWeek());
        tvTime.setText(course.getTime());
        tvDuration.setText(course.getFormattedDuration());
        tvCapacity.setText(course.getFormattedCapacity());
        tvPrice.setText(course.getFormattedPrice());
        
        tvLocation.setText(course.getLocation() != null && !course.getLocation().isEmpty() 
                ? course.getLocation() : "No location specified");
    }

    private void setupClickListeners() {
        btnConfirm.setOnClickListener(v -> {
            if (isEditing) {
                updateCourse();
            } else {
                saveCourse();
            }
        });
        
        btnBack.setOnClickListener(v -> finish());
    }

    private void saveCourse() {
        try {
            long result = MainActivity.helper.createNewYogaCourse(
                    course.getDayOfWeek(),
                    course.getTime(),
                    course.getCapacity(),
                    course.getDuration(),
                    course.getPrice(),
                    course.getType(),
                    course.getDescription(),
                    course.getDifficulty(),
                    course.getLocation()
            );
            
            if (result > 0) {
                Toast.makeText(this, R.string.course_saved, Toast.LENGTH_SHORT).show();
                
                // Return to main activity
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save course", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCourse() {
        try {
            int result = MainActivity.helper.updateYogaCourse(
                    courseId,
                    course.getDayOfWeek(),
                    course.getTime(),
                    course.getCapacity(),
                    course.getDuration(),
                    course.getPrice(),
                    course.getType(),
                    course.getDescription(),
                    course.getDifficulty(),
                    course.getLocation()
            );
            
            if (result > 0) {
                Toast.makeText(this, R.string.course_updated, Toast.LENGTH_SHORT).show();
                
                // Return to main activity
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to update course", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 