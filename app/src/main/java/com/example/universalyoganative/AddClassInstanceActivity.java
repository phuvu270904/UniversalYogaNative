package com.example.universalyoganative;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddClassInstanceActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int LOCATION_PERMISSION_REQUEST = 101;
    
    private long courseId;
    private String courseName;
    private YogaCourse course;
    
    private TextView tvCourseName, tvCourseSchedule, tvLocationStatus;
    private TextInputEditText etDate, etTeacher, etComments;
    private ImageView ivPhoto;
    private MaterialButton btnTakePhoto, btnSelectPhoto, btnCaptureLocation, btnSave, btnCancel;
    
    private String selectedDate = "";
    private String photoPath = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    
    private LocationManager locationManager;
    
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class_instance);

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
        setupClickListeners();
        loadCourseData();
        initializeLaunchers();
        
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        
        // Set default date to today
        setCurrentDate();

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
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        
        etDate = findViewById(R.id.etDate);
        etTeacher = findViewById(R.id.etTeacher);
        etComments = findViewById(R.id.etComments);
        
        ivPhoto = findViewById(R.id.ivPhoto);
        
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnCaptureLocation = findViewById(R.id.btnCaptureLocation);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupClickListeners() {
        etDate.setOnClickListener(v -> showDatePicker());
        
        btnTakePhoto.setOnClickListener(v -> takePhoto());
        btnSelectPhoto.setOnClickListener(v -> selectPhoto());
        btnCaptureLocation.setOnClickListener(v -> captureLocation());
        
        btnSave.setOnClickListener(v -> saveInstance());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void initializeLaunchers() {
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            ivPhoto.setImageBitmap(imageBitmap);
                            photoPath = saveImageToInternalStorage(imageBitmap);
                        }
                    }
                }
            }
        );

        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        ivPhoto.setImageURI(imageUri);
                        photoPath = imageUri.toString();
                    }
                }
            }
        );
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

    private YogaCourse createCourseFromCursor(Cursor cursor) {
        YogaCourse course = new YogaCourse();
        course.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        course.setDayOfWeek(cursor.getString(cursor.getColumnIndex("dayofweek")));
        course.setTime(cursor.getString(cursor.getColumnIndex("time")));
        course.setType(cursor.getString(cursor.getColumnIndex("type")));
        course.setInstructor(cursor.getString(cursor.getColumnIndex("instructor")));
        return course;
    }

    private void populateCourseInfo() {
        tvCourseName.setText(course.getType());
        tvCourseSchedule.setText(course.getDayOfWeek() + " at " + course.getTime());
        
        // Pre-fill teacher field with course instructor if available
        if (course.getInstructor() != null && !course.getInstructor().trim().isEmpty()) {
            etTeacher.setText(course.getInstructor());
        }
    }

    private void setCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = dateFormat.format(calendar.getTime());
        
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        etDate.setText(displayFormat.format(calendar.getTime()));
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedDate = dateFormat.format(selectedCalendar.getTime());
                
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                etDate.setText(displayFormat.format(selectedCalendar.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            return;
        }
        
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectPhoto() {
        Intent selectPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (selectPictureIntent.resolveActivity(getPackageManager()) != null) {
            galleryLauncher.launch(selectPictureIntent);
        } else {
            Toast.makeText(this, "Gallery not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void captureLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                tvLocationStatus.setText(String.format(Locale.getDefault(), "Location captured: %.6f, %.6f", latitude, longitude));
                tvLocationStatus.setTextColor(getColor(R.color.md_theme_primary));
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            File directory = new File(getFilesDir(), "class_photos");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            String fileName = "class_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);
            
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] bitmapData = stream.toByteArray();
            
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void saveInstance() {
        String teacher = etTeacher.getText().toString().trim();
        String comments = etComments.getText().toString().trim();
        
        // Validation
        if (TextUtils.isEmpty(selectedDate)) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(teacher)) {
            Toast.makeText(this, "Please enter teacher name", Toast.LENGTH_SHORT).show();
            etTeacher.requestFocus();
            return;
        }
        
        try {
            long result = MainActivity.helper.createClassInstance(
                courseId,
                selectedDate,
                teacher,
                comments,
                photoPath,
                latitude,
                longitude
            );
            
            if (result > 0) {
                Toast.makeText(this, "Class instance saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save class instance", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving class instance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    Toast.makeText(this, "Camera permission required to take photos", Toast.LENGTH_SHORT).show();
                }
                break;
                
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureLocation();
                } else {
                    Toast.makeText(this, "Location permission required to capture location", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
} 