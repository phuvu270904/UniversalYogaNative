package com.example.universalyoganative;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity implements YogaCourseAdapter.OnCourseActionListener, ClassInstanceAdapter.OnInstanceActionListener {
    
    private DatabaseHelper helper;
    private TextInputEditText etSearch, etFromDate, etToDate;
    private AutoCompleteTextView spDayOfWeek, spDifficulty;
    private RangeSlider sliderPriceRange, sliderDurationRange;
    private MaterialButton btnSearch, btnClearFilters;
    private MaterialButtonToggleGroup toggleSearchType;
    private RecyclerView recyclerViewResults;
    private TextView tvResultCount, tvEmptyState;
    private CircularProgressIndicator progressIndicator;
    private View layoutCourseFilters, layoutInstanceFilters;
    
    private YogaCourseAdapter courseAdapter;
    private ClassInstanceAdapter instanceAdapter;
    private List<YogaCourse> courseList;
    private List<ClassInstance> instanceList;
    
    private boolean isSearchingCourses = true;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        helper = MainActivity.helper;
        
        initializeViews();
        setupToolbar();
        setupFilters();
        setupSearchTypeToggle();
        setupRecyclerView();
        setupClickListeners();
        
        // Initialize with empty results
        updateEmptyState();

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

    private void initializeViews() {
        etSearch = findViewById(R.id.etSearch);
        etFromDate = findViewById(R.id.etFromDate);
        etToDate = findViewById(R.id.etToDate);
        spDayOfWeek = findViewById(R.id.spDayOfWeek);
        spDifficulty = findViewById(R.id.spDifficulty);
        sliderPriceRange = findViewById(R.id.sliderPriceRange);
        sliderDurationRange = findViewById(R.id.sliderDurationRange);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        toggleSearchType = findViewById(R.id.toggleSearchType);
        recyclerViewResults = findViewById(R.id.recyclerViewResults);
        tvResultCount = findViewById(R.id.tvResultCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressIndicator = findViewById(R.id.progressIndicator);
        layoutCourseFilters = findViewById(R.id.layoutCourseFilters);
        layoutInstanceFilters = findViewById(R.id.layoutInstanceFilters);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupFilters() {
        // Setup day of week dropdown
        String[] daysOfWeek = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, daysOfWeek);
        spDayOfWeek.setAdapter(dayAdapter);
        
        // Setup difficulty dropdown
        String[] difficulties = {"", "Beginner", "Intermediate", "Advanced"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, difficulties);
        spDifficulty.setAdapter(difficultyAdapter);
        
        // Setup date pickers
        etFromDate.setOnClickListener(v -> showDatePicker(etFromDate));
        etToDate.setOnClickListener(v -> showDatePicker(etToDate));
        
        // Setup real-time search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() > 2) {
                    performSearch();
                } else if (s.toString().trim().isEmpty()) {
                    clearResults();
                }
            }
        });
    }

    private void setupSearchTypeToggle() {
        toggleSearchType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isSearchingCourses = checkedId == R.id.btnSearchCourses;
                updateFilterVisibility();
                clearResults();
            }
        });
    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        instanceList = new ArrayList<>();
        courseAdapter = new YogaCourseAdapter(courseList, this);
        instanceAdapter = new ClassInstanceAdapter(instanceList, this);
        
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResults.setAdapter(courseAdapter);
    }

    private void setupClickListeners() {
        btnSearch.setOnClickListener(v -> performSearch());
        btnClearFilters.setOnClickListener(v -> clearFilters());
    }

    private void updateFilterVisibility() {
        if (isSearchingCourses) {
            layoutCourseFilters.setVisibility(View.VISIBLE);
            layoutInstanceFilters.setVisibility(View.GONE);
            recyclerViewResults.setAdapter(courseAdapter);
        } else {
            layoutCourseFilters.setVisibility(View.GONE);
            layoutInstanceFilters.setVisibility(View.VISIBLE);
            recyclerViewResults.setAdapter(instanceAdapter);
        }
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                editText.setText(dateFormat.format(calendar.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void performSearch() {
        showProgress(true);
        
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulate search delay
                
                if (isSearchingCourses) {
                    searchCourses();
                } else {
                    searchInstances();
                }
                
                runOnUiThread(() -> {
                    showProgress(false);
                    updateResultCount();
                    updateEmptyState();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void searchCourses() {
        String searchQuery = etSearch.getText().toString().trim();
        String dayOfWeek = spDayOfWeek.getText().toString().trim();
        String difficulty = spDifficulty.getText().toString().trim();
        
        List<Float> priceRange = sliderPriceRange.getValues();
        float minPrice = priceRange.get(0);
        float maxPrice = priceRange.get(1);
        
        List<Float> durationRange = sliderDurationRange.getValues();
        int minDuration = durationRange.get(0).intValue();
        int maxDuration = durationRange.get(1).intValue();
        
        // Clear previous results
        courseList.clear();
        
        Cursor cursor = helper.searchCourses(searchQuery, dayOfWeek, difficulty, minPrice, maxPrice, minDuration, maxDuration);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                YogaCourse course = createCourseFromCursor(cursor);
                courseList.add(course);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        runOnUiThread(() -> courseAdapter.notifyDataSetChanged());
    }

    private void searchInstances() {
        String searchQuery = etSearch.getText().toString().trim();
        String fromDate = etFromDate.getText().toString().trim();
        String toDate = etToDate.getText().toString().trim();
        
        // Clear previous results
        instanceList.clear();
        
        Cursor cursor = helper.searchInstances(searchQuery, fromDate, toDate);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ClassInstance instance = createInstanceFromCursor(cursor);
                instanceList.add(instance);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        runOnUiThread(() -> instanceAdapter.notifyDataSetChanged());
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

    @SuppressLint("Range")
    private ClassInstance createInstanceFromCursor(Cursor cursor) {
        ClassInstance instance = new ClassInstance();
        instance.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        instance.setCourseId(cursor.getLong(cursor.getColumnIndex("course_id")));
        instance.setDate(cursor.getString(cursor.getColumnIndex("date")));
        instance.setTeacher(cursor.getString(cursor.getColumnIndex("teacher")));
        instance.setComments(cursor.getString(cursor.getColumnIndex("comments")));
        instance.setPhotoPath(cursor.getString(cursor.getColumnIndex("photo_path")));
        instance.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
        instance.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
        return instance;
    }

    private void clearFilters() {
        etSearch.setText("");
        etFromDate.setText("");
        etToDate.setText("");
        spDayOfWeek.setText("");
        spDifficulty.setText("");
        sliderPriceRange.setValues(0f, 100f);
        sliderDurationRange.setValues(30f, 180f);
        clearResults();
    }

    private void clearResults() {
        courseList.clear();
        instanceList.clear();
        courseAdapter.notifyDataSetChanged();
        instanceAdapter.notifyDataSetChanged();
        updateResultCount();
        updateEmptyState();
    }

    private void updateResultCount() {
        int count = isSearchingCourses ? courseList.size() : instanceList.size();
        String resultText = count == 1 ? "1 result" : count + " results";
        tvResultCount.setText(resultText);
    }

    private void updateEmptyState() {
        int count = isSearchingCourses ? courseList.size() : instanceList.size();
        if (count == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewResults.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewResults.setVisibility(View.VISIBLE);
        }
    }

    private void showProgress(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(!show);
    }

    // YogaCourseAdapter.OnCourseActionListener implementation
    @Override
    public void onEditCourse(YogaCourse course) {
        // Handle edit course action
        Toast.makeText(this, "Edit course: " + course.getType(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteCourse(YogaCourse course) {
        // Handle delete course action
        Toast.makeText(this, "Delete course: " + course.getType(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewInstances(YogaCourse course) {
        // Handle view instances action
        Toast.makeText(this, "View instances for: " + course.getType(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCourseClick(YogaCourse course) {
        // Handle course click action
        Toast.makeText(this, "Course clicked: " + course.getType(), Toast.LENGTH_SHORT).show();
    }

    // ClassInstanceAdapter.OnInstanceActionListener implementation
    @Override
    public void onEditInstance(ClassInstance instance) {
        // Handle edit instance action
        Toast.makeText(this, "Edit instance: " + instance.getDate(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteInstance(ClassInstance instance) {
        // Handle delete instance action
        Toast.makeText(this, "Delete instance: " + instance.getDate(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInstanceClick(ClassInstance instance) {
        // Handle instance click action
        Toast.makeText(this, "Instance clicked: " + instance.getDate(), Toast.LENGTH_SHORT).show();
    }
} 