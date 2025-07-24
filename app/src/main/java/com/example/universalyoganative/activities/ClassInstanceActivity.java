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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ClassInstanceActivity extends AppCompatActivity implements ClassInstanceAdapter.OnInstanceActionListener {
    private long courseId;
    private String courseName;
    private YogaCourse course;
    
    private TextView tvCourseName, tvCourseSchedule, tvInstanceCount;
    private RecyclerView recyclerViewInstances;
    private View emptyStateContainer;
    private FloatingActionButton fabAddInstance;
    
    private ClassInstanceAdapter instanceAdapter;
    private List<ClassInstance> instanceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_instance);

        // Get course data from intent
        courseId = getIntent().getLongExtra("course_id", -1);
        courseName = getIntent().getStringExtra("course_name");
        
        if (courseId == -1) {
            Toast.makeText(this, "Error: Course not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
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

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        tvCourseName = findViewById(R.id.tvCourseName);
        tvCourseSchedule = findViewById(R.id.tvCourseSchedule);
        tvInstanceCount = findViewById(R.id.tvInstanceCount);
        recyclerViewInstances = findViewById(R.id.recyclerViewInstances);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        fabAddInstance = findViewById(R.id.fabAddInstance);
    }

    private void setupRecyclerView() {
        instanceList = new ArrayList<>();
        instanceAdapter = new ClassInstanceAdapter(instanceList, this);
        recyclerViewInstances.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewInstances.setAdapter(instanceAdapter);
    }

    private void setupClickListeners() {
        fabAddInstance.setOnClickListener(v -> {
            Intent intent = new Intent(ClassInstanceActivity.this, AddClassInstanceActivity.class);
            intent.putExtra("course_id", courseId);
            intent.putExtra("course_name", courseName);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourseData();
        loadInstances();
    }

    private void loadCourseData() {
        Cursor cursor = MainActivity.helper.readYogaCourse(courseId);
        if (cursor != null && cursor.moveToFirst()) {
            course = createCourseFromCursor(cursor);
            populateCourseInfo();
            cursor.close();
        } else {
            Toast.makeText(this, "Error loading course data", Toast.LENGTH_SHORT).show();
            finish();
        }
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
        return course;
    }

    private void populateCourseInfo() {
        tvCourseName.setText(course.getType());
        tvCourseSchedule.setText(course.getDayOfWeek() + " at " + course.getTime());
    }

    @SuppressLint("Range")
    private void loadInstances() {
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
                instance.setSyncStatus(String.valueOf(cursor.getInt(cursor.getColumnIndex("sync_status"))));
                
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
        tvInstanceCount.setText(String.valueOf(count));
    }

    private void updateEmptyState() {
        if (instanceList.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            recyclerViewInstances.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerViewInstances.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditInstance(ClassInstance instance) {
        Intent intent = new Intent(this, EditClassInstanceActivity.class);
        intent.putExtra("instance_id", instance.getId());
        intent.putExtra("course_id", courseId);
        intent.putExtra("course_name", courseName);
        startActivity(intent);
    }

    @Override
    public void onDeleteInstance(ClassInstance instance) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Instance")
                .setMessage("Are you sure you want to delete this class instance? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = MainActivity.helper.deleteClassInstance(instance.getId());
                    if (result > 0) {
                        Toast.makeText(this, "Instance deleted successfully", Toast.LENGTH_SHORT).show();
                        loadInstances();
                    } else {
                        Toast.makeText(this, "Failed to delete instance", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onInstanceClick(ClassInstance instance) {
        // Show instance details in a dialog or navigate to detail view
        showInstanceDetails(instance);
    }

    private void showInstanceDetails(ClassInstance instance) {
        String details = "Date: " + instance.getFormattedDate() + "\n" +
                        "Teacher: " + instance.getTeacher() + "\n" +
                        "Comments: " + (instance.getComments() != null ? instance.getComments() : "No comments") + "\n" +
                        "Photo: " + (instance.hasPhoto() ? "Yes" : "No") + "\n" +
                        "Location: " + (instance.hasLocation() ? instance.getFormattedLocation() : "No location") + "\n" +
                        "Synced: " + (instance.isSynced() ? "Yes" : "No");

        new AlertDialog.Builder(this)
                .setTitle("Instance Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }
} 