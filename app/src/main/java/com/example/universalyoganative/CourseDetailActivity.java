package com.example.universalyoganative;

import android.annotation.SuppressLint;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {
    private long courseId;
    private YogaCourse course;
    
    // UI Components
    private TextView tvType, tvDifficulty, tvDescription;
    private TextView tvDayOfWeek, tvTime, tvDuration, tvCapacity, tvPrice;
    private TextView tvLocation, tvInstanceCount, tvEmptyState;
    private MaterialButton btnEdit, btnDelete;
    private FloatingActionButton fabAddInstance;
    private RecyclerView recyclerViewInstances;
    
    // Instance management
    private List<ClassInstance> instanceList;
    private ClassInstanceAdapter instanceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get course ID from intent
        courseId = getIntent().getLongExtra("course_id", -1);
        if (courseId == -1) {
            Toast.makeText(this, "Error: Invalid course ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setContentView(R.layout.activity_course_detail);
        
        setupToolbar();
        setupBottomNavigation();
        initializeViews();
        setupRecyclerView();
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
                    startActivity(new Intent(this, BookingsActivity.class));
                    finish();
                    return true;
                }
                return false;
            });

            // Since this is a detail activity, we might want to highlight home or not set any specific item
            // navView.setSelectedItemId(R.id.navigation_home);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.course_details_title);
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
        tvInstanceCount = findViewById(R.id.tvInstanceCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        
        // Buttons
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        fabAddInstance = findViewById(R.id.fabAddInstance);
        
        // RecyclerView
        recyclerViewInstances = findViewById(R.id.recyclerViewInstances);
    }

    private void setupRecyclerView() {
        instanceList = new ArrayList<>();
        instanceAdapter = new ClassInstanceAdapter(instanceList, new ClassInstanceAdapter.OnInstanceActionListener() {
            @Override
            public void onEditInstance(ClassInstance instance) {
                Intent intent = new Intent(CourseDetailActivity.this, EditClassInstanceActivity.class);
                intent.putExtra("instance_id", instance.getId());
                intent.putExtra("course_id", courseId);
                startActivity(intent);
            }

            @Override
            public void onDeleteInstance(ClassInstance instance) {
                showDeleteInstanceDialog(instance);
            }

            @Override
            public void onInstanceClick(ClassInstance instance) {
                // Show instance details or do nothing
                Toast.makeText(CourseDetailActivity.this, "Instance: " + instance.getFormattedDate(), Toast.LENGTH_SHORT).show();
            }
        });
        
        recyclerViewInstances.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewInstances.setAdapter(instanceAdapter);
    }

    private void setupClickListeners() {
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditYogaCourse.class);
            intent.putExtra("course_id", courseId);
            startActivity(intent);
        });
        
        btnDelete.setOnClickListener(v -> showDeleteCourseDialog());
        
        fabAddInstance.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddClassInstanceActivity.class);
            intent.putExtra("course_id", courseId);
            intent.putExtra("course_name", course.getType());
            intent.putExtra("course_day", course.getDayOfWeek());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourseData();
        loadInstanceData();
    }

    @SuppressLint("Range")
    private void loadCourseData() {
        Cursor cursor = MainActivity.helper.readYogaCourse(courseId);
        
        if (cursor != null && cursor.moveToFirst()) {
            course = new YogaCourse();
            course.setId(cursor.getLong(cursor.getColumnIndex("_id")));
            course.setType(cursor.getString(cursor.getColumnIndex("type")));
            course.setDifficulty(cursor.getString(cursor.getColumnIndex("difficulty")));
            course.setDayOfWeek(cursor.getString(cursor.getColumnIndex("dayofweek")));
            course.setTime(cursor.getString(cursor.getColumnIndex("time")));
            course.setDuration(cursor.getInt(cursor.getColumnIndex("duration")));
            course.setCapacity(cursor.getInt(cursor.getColumnIndex("capacity")));
            course.setPrice(cursor.getFloat(cursor.getColumnIndex("price")));
            course.setDescription(cursor.getString(cursor.getColumnIndex("description")));
            course.setLocation(cursor.getString(cursor.getColumnIndex("location")));
            
            populateFields();
            cursor.close();
        } else {
            Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void populateFields() {
        tvType.setText(course.getType());
        tvDifficulty.setText(course.getDifficulty() != null ? course.getDifficulty() : "All Levels");
        tvDescription.setText(course.getDescription() != null && !course.getDescription().isEmpty() 
                ? course.getDescription() : "No description available");
        
        tvDayOfWeek.setText(course.getDayOfWeek());
        tvTime.setText(course.getTime());
        tvDuration.setText(course.getFormattedDuration());
        tvCapacity.setText(course.getFormattedCapacity());
        tvPrice.setText(course.getFormattedPrice());
        
        tvLocation.setText(course.getLocation() != null && !course.getLocation().isEmpty() 
                ? course.getLocation() : "No location specified");
    }

    @SuppressLint("Range")
    private void loadInstanceData() {
        instanceList.clear();
        Cursor cursor = MainActivity.helper.readClassInstancesByCourse(courseId);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ClassInstance instance = new ClassInstance();
                instance.setId(cursor.getLong(cursor.getColumnIndex("_id")));
                instance.setCourseId(cursor.getLong(cursor.getColumnIndex("course_id")));
                instance.setDate(cursor.getString(cursor.getColumnIndex("date")));
                instance.setTeacher(cursor.getString(cursor.getColumnIndex("teacher")));
                instance.setComments(cursor.getString(cursor.getColumnIndex("comments")));
                instance.setPhotoPath(cursor.getString(cursor.getColumnIndex("photo_path")));
                instance.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
                instance.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
                
                instanceList.add(instance);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        instanceAdapter.notifyDataSetChanged();
        updateInstanceCount();
        updateEmptyState();
    }

    private void updateInstanceCount() {
        int count = instanceList.size();
        tvInstanceCount.setText(String.format(getString(R.string.instance_count), count));
    }

    private void updateEmptyState() {
        if (instanceList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewInstances.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewInstances.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteCourseDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage(R.string.confirm_delete_course)
                .setPositiveButton("Delete", (dialog, which) -> {
                    MainActivity.helper.deleteYogaCourse(courseId);
                    Toast.makeText(this, R.string.course_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteInstanceDialog(ClassInstance instance) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Instance")
                .setMessage(R.string.confirm_delete_instance)
                .setPositiveButton("Delete", (dialog, which) -> {
                    MainActivity.helper.deleteClassInstance(instance.getId());
                    loadInstanceData();
                    Toast.makeText(this, R.string.instance_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
} 