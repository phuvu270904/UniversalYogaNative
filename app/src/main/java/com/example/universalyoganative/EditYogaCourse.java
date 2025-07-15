package com.example.universalyoganative;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

public class EditYogaCourse extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    // UI Components
    private AutoCompleteTextView spType, spDifficulty, spDayOfWeek, spTime, spDuration, spCapacity;
    private TextInputEditText etDescription, etPrice, etLocation;
    private TextInputLayout layoutDescription, layoutPrice, layoutLocation;
    private MaterialButton btnSave, btnCancel, btnGetLocation;
    private CircularProgressIndicator progressIndicator;
    
    // Location services
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    
    // Course data
    private long courseId;
    private YogaCourse originalCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_yoga_course);
        
        // Get course ID from intent
        courseId = getIntent().getLongExtra("course_id", -1);
        if (courseId == -1) {
            Toast.makeText(this, "Error: Invalid course ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupToolbar();
        initializeViews();
        setupDropdowns();
        setupClickListeners();
        loadCourseData();

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
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.edit_course_title);
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        // Dropdowns
        spType = findViewById(R.id.spType);
        spDifficulty = findViewById(R.id.spDifficulty);
        spDayOfWeek = findViewById(R.id.spDayOfWeek);
        spTime = findViewById(R.id.spTime);
        spDuration = findViewById(R.id.spDuration);
        spCapacity = findViewById(R.id.spCapacity);
        
        // Text inputs
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etLocation = findViewById(R.id.etLocation);
        
        // Input layouts for validation
        layoutDescription = (TextInputLayout) etDescription.getParent().getParent();
        layoutPrice = (TextInputLayout) etPrice.getParent().getParent();
        layoutLocation = (TextInputLayout) etLocation.getParent().getParent();
        
        // Buttons
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        
        // Progress indicator
        progressIndicator = findViewById(R.id.progressIndicator);
    }

    private void setupDropdowns() {
        // Type dropdown
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.yoga_type, android.R.layout.simple_dropdown_item_1line);
        spType.setAdapter(typeAdapter);
        
        // Difficulty dropdown
        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(
                this, R.array.difficulty_level, android.R.layout.simple_dropdown_item_1line);
        spDifficulty.setAdapter(difficultyAdapter);
        
        // Day of week dropdown
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                this, R.array.day_of_week, android.R.layout.simple_dropdown_item_1line);
        spDayOfWeek.setAdapter(dayAdapter);
        
        // Time dropdown
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(
                this, R.array.time_of_day, android.R.layout.simple_dropdown_item_1line);
        spTime.setAdapter(timeAdapter);
        
        // Duration dropdown
        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(
                this, R.array.duration_options, android.R.layout.simple_dropdown_item_1line);
        spDuration.setAdapter(durationAdapter);
        
        // Capacity dropdown
        ArrayAdapter<CharSequence> capacityAdapter = ArrayAdapter.createFromResource(
                this, R.array.capacity_options, android.R.layout.simple_dropdown_item_1line);
        spCapacity.setAdapter(capacityAdapter);
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                showConfirmationDialog();
            }
        });
        
        btnCancel.setOnClickListener(v -> finish());
        
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
    }

    @SuppressLint("Range")
    private void loadCourseData() {
        Cursor cursor = MainActivity.helper.readYogaCourse(courseId);
        
        if (cursor != null && cursor.moveToFirst()) {
            originalCourse = new YogaCourse();
            originalCourse.setId(cursor.getLong(cursor.getColumnIndex("_id")));
            originalCourse.setType(cursor.getString(cursor.getColumnIndex("type")));
            originalCourse.setDifficulty(cursor.getString(cursor.getColumnIndex("difficulty")));
            originalCourse.setDayOfWeek(cursor.getString(cursor.getColumnIndex("dayofweek")));
            originalCourse.setTime(cursor.getString(cursor.getColumnIndex("time")));
            originalCourse.setDuration(cursor.getInt(cursor.getColumnIndex("duration")));
            originalCourse.setCapacity(cursor.getInt(cursor.getColumnIndex("capacity")));
            originalCourse.setPrice(cursor.getFloat(cursor.getColumnIndex("price")));
            originalCourse.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            originalCourse.setLocation(cursor.getString(cursor.getColumnIndex("location")));
            
            populateFields();
            cursor.close();
        } else {
            Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void populateFields() {
        spType.setText(originalCourse.getType(), false);
        spDifficulty.setText(originalCourse.getDifficulty() != null ? originalCourse.getDifficulty() : "", false);
        spDayOfWeek.setText(originalCourse.getDayOfWeek(), false);
        spTime.setText(originalCourse.getTime(), false);
        spDuration.setText(String.valueOf(originalCourse.getDuration()), false);
        spCapacity.setText(String.valueOf(originalCourse.getCapacity()), false);
        etPrice.setText(String.valueOf(originalCourse.getPrice()));
        etDescription.setText(originalCourse.getDescription() != null ? originalCourse.getDescription() : "");
        etLocation.setText(originalCourse.getLocation() != null ? originalCourse.getLocation() : "");
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Clear previous errors
        clearErrors();
        
        // Validate required fields
        if (TextUtils.isEmpty(spType.getText())) {
            setError(findViewById(R.id.spType), "Please select a yoga type");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(spDayOfWeek.getText())) {
            setError(findViewById(R.id.spDayOfWeek), "Please select a day of week");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(spTime.getText())) {
            setError(findViewById(R.id.spTime), "Please select a time");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(spDuration.getText())) {
            setError(findViewById(R.id.spDuration), "Please select duration");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(spCapacity.getText())) {
            setError(findViewById(R.id.spCapacity), "Please select capacity");
            isValid = false;
        }
        
        // Validate price
        String priceText = etPrice.getText().toString().trim();
        if (TextUtils.isEmpty(priceText)) {
            layoutPrice.setError(getString(R.string.error_required_field));
            isValid = false;
        } else {
            try {
                float price = Float.parseFloat(priceText);
                if (price <= 0) {
                    layoutPrice.setError(getString(R.string.error_invalid_price));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                layoutPrice.setError(getString(R.string.error_invalid_price));
                isValid = false;
            }
        }
        
        return isValid;
    }

    private void setError(View view, String message) {
        if (view.getParent().getParent() instanceof TextInputLayout) {
            ((TextInputLayout) view.getParent().getParent()).setError(message);
        }
    }

    private void clearErrors() {
        layoutDescription.setError(null);
        layoutPrice.setError(null);
        layoutLocation.setError(null);
    }

    private void showConfirmationDialog() {
        YogaCourse course = createCourseFromForm();
        
        Intent intent = new Intent(this, ConfirmCourseActivity.class);
        intent.putExtra("course", course);
        intent.putExtra("isEditing", true);
        intent.putExtra("course_id", courseId);
        startActivity(intent);
        finish();
    }

    private YogaCourse createCourseFromForm() {
        YogaCourse course = new YogaCourse();
        course.setId(courseId);
        
        course.setType(spType.getText().toString());
        course.setDifficulty(spDifficulty.getText().toString());
        course.setDayOfWeek(spDayOfWeek.getText().toString());
        course.setTime(spTime.getText().toString());
        course.setDuration(Integer.parseInt(spDuration.getText().toString()));
        course.setCapacity(Integer.parseInt(spCapacity.getText().toString()));
        course.setPrice(Float.parseFloat(etPrice.getText().toString()));
        course.setDescription(etDescription.getText().toString());
        course.setLocation(etLocation.getText().toString());
        
        return course;
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        
        progressIndicator.setVisibility(View.VISIBLE);
        btnGetLocation.setEnabled(false);
        
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnGetLocation.setEnabled(true);
                    
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        
                        // Set a formatted location string
                        String locationText = String.format(Locale.getDefault(), 
                                "Lat: %.6f, Lng: %.6f", currentLatitude, currentLongitude);
                        etLocation.setText(locationText);
                        
                        Toast.makeText(this, "Location captured successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnGetLocation.setEnabled(true);
                    Toast.makeText(this, "Failed to get location: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 