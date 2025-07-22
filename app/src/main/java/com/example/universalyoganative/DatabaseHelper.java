package com.example.universalyoganative;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase database;
    private static final String DATABASE_NAME = "YogaDB";
    private static final int DATABASE_VERSION = 12; // Updated for firestore_id columns

    // Table names
    private static final String TABLE_YOGA_COURSE = "YogaCourse";
    private static final String TABLE_CLASS_INSTANCE = "ClassInstance";
    public static final String TABLE_USER = "User";
    private static final String TABLE_BOOKINGS = "Bookings"; // Changed to match case convention

    // YogaCourse table columns
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_FIRESTORE_ID = "firestore_id";
    private static final String COLUMN_DAY_OF_WEEK = "dayofweek";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_CAPACITY = "capacity";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DIFFICULTY = "difficulty";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_CREATED_DATE = "created_date";
    private static final String COLUMN_SYNC_STATUS = "sync_status";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_LEVEL = "level";
    private static final String COLUMN_IMAGE_PATH = "image_path";
    private static final String COLUMN_CREATED_BY = "created_by";

    // ClassInstance table columns
    private static final String COLUMN_INSTANCE_ID = "_id";
    private static final String COLUMN_INSTANCE_FIRESTORE_ID = "firestore_id";
    private static final String COLUMN_COURSE_ID = "course_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TEACHER = "teacher";
    private static final String COLUMN_COMMENTS = "comments";
    private static final String COLUMN_PHOTO_PATH = "photo_path";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_STATUS = "status";

    // User table columns
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_FIRESTORE_ID = "firestore_id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_ROLE = "role";
    public static final String COLUMN_USER_CREATED_DATE = "created_date";
    public static final String COLUMN_USER_SYNC_STATUS = "sync_status";

    // Booking related columns
    private static final String COLUMN_BOOKING_ID = "_id";
    private static final String COLUMN_BOOKING_FIRESTORE_ID = "firestore_id";
    private static final String COLUMN_BOOKING_CLASS_ID = "class_id";
    private static final String COLUMN_BOOKING_USER_ID = "user_id";
    private static final String COLUMN_BOOKING_SYNC_STATUS = "sync_status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Create YogaCourse table
            String CREATE_TABLE_YOGA_COURSE = "CREATE TABLE " + TABLE_YOGA_COURSE + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_FIRESTORE_ID + " TEXT UNIQUE,"
                    + COLUMN_DAY_OF_WEEK + " TEXT,"
                    + COLUMN_TIME + " TEXT,"
                    + COLUMN_CAPACITY + " INTEGER,"
                    + COLUMN_DURATION + " INTEGER,"
                    + COLUMN_PRICE + " REAL,"
                    + COLUMN_TYPE + " TEXT,"
                    + COLUMN_DESCRIPTION + " TEXT,"
                    + COLUMN_DIFFICULTY + " TEXT,"
                    + COLUMN_LOCATION + " TEXT,"
                    + COLUMN_CREATED_DATE + " TEXT,"
                    + COLUMN_SYNC_STATUS + " TEXT DEFAULT 'edited'"
                    + ")";
            db.execSQL(CREATE_TABLE_YOGA_COURSE);

            // Create User table
            String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_FIRESTORE_ID + " TEXT UNIQUE,"
                    + COLUMN_USER_NAME + " TEXT,"
                    + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
                    + COLUMN_USER_PASSWORD + " TEXT,"
                    + COLUMN_USER_ROLE + " TEXT,"
                    + COLUMN_USER_CREATED_DATE + " TEXT,"
                    + COLUMN_USER_SYNC_STATUS + " TEXT DEFAULT 'edited'"
                    + ")";
            db.execSQL(CREATE_TABLE_USER);

            // Create ClassInstance table
            String CREATE_TABLE_CLASS_INSTANCE = "CREATE TABLE " + TABLE_CLASS_INSTANCE + "("
                    + COLUMN_INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_INSTANCE_FIRESTORE_ID + " TEXT UNIQUE,"
                    + COLUMN_COURSE_ID + " INTEGER,"
                    + COLUMN_DATE + " TEXT,"
                    + COLUMN_TEACHER + " TEXT,"
                    + COLUMN_COMMENTS + " TEXT,"
                    + COLUMN_PHOTO_PATH + " TEXT,"
                    + COLUMN_LATITUDE + " REAL,"
                    + COLUMN_LONGITUDE + " REAL,"
                    + COLUMN_STATUS + " TEXT,"
                    + COLUMN_SYNC_STATUS + " TEXT DEFAULT 'edited',"
                    + "FOREIGN KEY(" + COLUMN_COURSE_ID + ") REFERENCES " + TABLE_YOGA_COURSE + "(" + COLUMN_ID + ")"
                    + ")";
            db.execSQL(CREATE_TABLE_CLASS_INSTANCE);

            // Create Bookings table
            String CREATE_TABLE_BOOKINGS = "CREATE TABLE " + TABLE_BOOKINGS + "("
                    + COLUMN_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_BOOKING_FIRESTORE_ID + " TEXT UNIQUE,"
                    + COLUMN_BOOKING_CLASS_ID + " INTEGER,"
                    + COLUMN_BOOKING_USER_ID + " INTEGER,"
                    + COLUMN_BOOKING_SYNC_STATUS + " TEXT DEFAULT 'edited',"
                    + "FOREIGN KEY(" + COLUMN_BOOKING_CLASS_ID + ") REFERENCES " + TABLE_CLASS_INSTANCE + "(" + COLUMN_INSTANCE_ID + "),"
                    + "FOREIGN KEY(" + COLUMN_BOOKING_USER_ID + ") REFERENCES " + TABLE_USER + "(" + COLUMN_ID + ")"
                    + ")";
            db.execSQL(CREATE_TABLE_BOOKINGS);

            Log.d(this.getClass().getName(), "Database tables created successfully");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(this.getClass().getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        try {
            // Handle migrations based on version changes
            if (oldVersion < 7) {
                // Migration to version 7 (if needed)
                Log.d(this.getClass().getName(), "Migrating to version 7");
            }
            
            if (oldVersion < 8) {
                // Migration to version 8 (if needed)
                Log.d(this.getClass().getName(), "Migrating to version 8");
            }
            
            if (oldVersion < 9) {
                // Migration to version 9 - Add sync_status enum to all tables
                Log.d(this.getClass().getName(), "Migrating to version 9 - Adding sync_status enum");
                
                // Add sync_status column to User table if it doesn't exist
                try {
                    db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + COLUMN_USER_SYNC_STATUS + " TEXT DEFAULT 'edited'");
                } catch (Exception e) {
                    Log.d(this.getClass().getName(), "User sync_status column already exists or error: " + e.getMessage());
                }
                
                // Update existing sync_status columns to use enum values
                try {
                    db.execSQL("UPDATE " + TABLE_YOGA_COURSE + " SET " + COLUMN_SYNC_STATUS + " = 'edited' WHERE " + COLUMN_SYNC_STATUS + " = '0'");
                    db.execSQL("UPDATE " + TABLE_YOGA_COURSE + " SET " + COLUMN_SYNC_STATUS + " = 'sync' WHERE " + COLUMN_SYNC_STATUS + " = '1'");
                } catch (Exception e) {
                    Log.d(this.getClass().getName(), "Error updating YogaCourse sync_status: " + e.getMessage());
                }
                
                try {
                    db.execSQL("UPDATE " + TABLE_CLASS_INSTANCE + " SET " + COLUMN_SYNC_STATUS + " = 'edited' WHERE " + COLUMN_SYNC_STATUS + " = '0'");
                    db.execSQL("UPDATE " + TABLE_CLASS_INSTANCE + " SET " + COLUMN_SYNC_STATUS + " = 'sync' WHERE " + COLUMN_SYNC_STATUS + " = '1'");
                } catch (Exception e) {
                    Log.d(this.getClass().getName(), "Error updating ClassInstance sync_status: " + e.getMessage());
                }
                
                // Add sync_status column to Bookings table if it doesn't exist
                try {
                    db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN " + COLUMN_BOOKING_SYNC_STATUS + " TEXT DEFAULT 'edited'");
                } catch (Exception e) {
                    Log.d(this.getClass().getName(), "Bookings sync_status column already exists or error: " + e.getMessage());
                }
            }
            
            if (oldVersion < 10) {
                // Migration to version 10 (if needed)
                Log.d(this.getClass().getName(), "Migrating to version 10");
                // Add any version 10 specific migrations here
            }
            
            if (oldVersion < 11) {
                // Migration to version 11 (if needed)
                Log.d(this.getClass().getName(), "Migrating to version 11");
                // Add any version 11 specific migrations here
            }
            
            if (oldVersion < 12) {
                // Migration to version 12 - Add firestore_id columns
                Log.d(this.getClass().getName(), "Migrating to version 12 - Adding firestore_id columns");
                try {
                    db.execSQL("ALTER TABLE " + TABLE_YOGA_COURSE + " ADD COLUMN " + COLUMN_FIRESTORE_ID + " TEXT UNIQUE");
                    db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + COLUMN_FIRESTORE_ID + " TEXT UNIQUE");
                    db.execSQL("ALTER TABLE " + TABLE_CLASS_INSTANCE + " ADD COLUMN " + COLUMN_INSTANCE_FIRESTORE_ID + " TEXT UNIQUE");
                    db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN " + COLUMN_BOOKING_FIRESTORE_ID + " TEXT UNIQUE");
                } catch (Exception e) {
                    Log.d(this.getClass().getName(), "Firestore ID columns already exist or error: " + e.getMessage());
                }
            }
            
            Log.d(this.getClass().getName(), "Database upgraded to version " + newVersion);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "Error during database upgrade", e);
            // If upgrade fails, recreate the database
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_COURSE);
            onCreate(db);
        }
    }

    // USER AUTHENTICATION METHODS

    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error hashing password", e);
            return password; // Fallback to plain text (not recommended for production)
        }
    }

    /**
     * Register a new user
     */
    public long registerUser(String name, String email, String password, String role) {
        // Check if email already exists
        if (emailExists(email)) {
            return -1; // Email already exists
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_EMAIL, email.toLowerCase().trim());
        values.put(COLUMN_USER_PASSWORD, hashPassword(password));
        values.put(COLUMN_USER_ROLE, role);
        values.put(COLUMN_USER_CREATED_DATE, String.valueOf(System.currentTimeMillis()));
        values.put(COLUMN_USER_SYNC_STATUS, SyncStatus.EDITED.getValue());
        
        return database.insertOrThrow(TABLE_USER, null, values);
    }

    /**
     * Authenticate user login
     */
    public User authenticateUser(String email, String password) {
        String hashedPassword = hashPassword(password);
        String selection = COLUMN_USER_EMAIL + " = ? AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {email.toLowerCase().trim(), hashedPassword};
        
        Cursor cursor = database.query(TABLE_USER, null, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            User user = createUserFromCursor(cursor);
            cursor.close();
            return user;
        }
        
        if (cursor != null) cursor.close();
        return null;
    }

    /**
     * Check if email already exists
     */
    public boolean emailExists(String email) {
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email.toLowerCase().trim()};
        
        Cursor cursor = database.query(TABLE_USER, new String[]{COLUMN_USER_ID}, 
                                     selection, selectionArgs, null, null, null);
        
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    /**
     * Get user by ID
     */
    public User getUserById(long userId) {
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = database.query(TABLE_USER, null, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            User user = createUserFromCursor(cursor);
            cursor.close();
            return user;
        }
        
        if (cursor != null) cursor.close();
        return null;
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email.toLowerCase().trim()};
        
        Cursor cursor = database.query(TABLE_USER, null, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            User user = createUserFromCursor(cursor);
            cursor.close();
            return user;
        }
        
        if (cursor != null) cursor.close();
        return null;
    }

    /**
     * Get all users (excluding deleted)
     */
    public Cursor readAllUsers() {
        String selection = COLUMN_USER_ROLE + " = ? AND " + COLUMN_USER_SYNC_STATUS + " != ?";
        String[] selectionArgs = new String[]{"user", SyncStatus.DELETED.getValue()};
        return database.query(TABLE_USER, null, selection, selectionArgs, null, null, 
                            COLUMN_USER_CREATED_DATE + " DESC");
    }

    /**
     * Update user information
     */
    public int updateUser(long userId, String name, String email, String role) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_EMAIL, email.toLowerCase().trim());
        values.put(COLUMN_USER_ROLE, role);
        values.put(COLUMN_USER_SYNC_STATUS, SyncStatus.EDITED.getValue());
        
        return database.update(TABLE_USER, values, COLUMN_USER_ID + "=?", 
                              new String[]{String.valueOf(userId)});
    }

    /**
     * Change user password
     */
    public int changeUserPassword(long userId, String newPassword) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_PASSWORD, hashPassword(newPassword));
        
        return database.update(TABLE_USER, values, COLUMN_USER_ID + "=?", 
                              new String[]{String.valueOf(userId)});
    }

    /**
     * Soft delete user - set sync_status to deleted
     */
    public void deleteUser(long userId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_SYNC_STATUS, SyncStatus.DELETED.getValue());
        database.update(TABLE_USER, values, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    /**
     * Create User object from cursor
     */
    public User createUserFromCursor(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ROLE)));
        user.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_CREATED_DATE)));
        
        // Handle sync_status - use default if column doesn't exist (for backward compatibility)
        try {
            String syncStatus = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_SYNC_STATUS));
            user.setSyncStatus(syncStatus != null ? syncStatus : SyncStatus.EDITED.getValue());
        } catch (Exception e) {
            user.setSyncStatus(SyncStatus.EDITED.getValue());
        }
        
        // Handle firestore_id - use null if column doesn't exist (for backward compatibility)
        try {
            String firestoreId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_FIRESTORE_ID));
            user.setFirestoreId(firestoreId);
        } catch (Exception e) {
            user.setFirestoreId(null);
        }
        
        return user;
    }

    /**
     * Check if any admin users exist
     */
    public boolean hasAdminUsers() {
        String selection = COLUMN_USER_ROLE + " = ?";
        String[] selectionArgs = {"admin"};
        
        Cursor cursor = database.query(TABLE_USER, new String[]{COLUMN_USER_ID}, 
                                     selection, selectionArgs, null, null, null);
        
        boolean hasAdmin = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return hasAdmin;
    }

    // YOGA COURSE CRUD OPERATIONS

    public long createNewYogaCourse(String dayOfWeek, String time, int capacity, int duration, 
                                   float price, String type, String description, String difficulty, 
                                   String location) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY_OF_WEEK, dayOfWeek);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_CAPACITY, capacity);
        values.put(COLUMN_DURATION, duration);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DIFFICULTY, difficulty);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_CREATED_DATE, String.valueOf(System.currentTimeMillis()));
        values.put(COLUMN_SYNC_STATUS, SyncStatus.EDITED.getValue());
        
        return database.insertOrThrow(TABLE_YOGA_COURSE, null, values);
    }

    public Cursor readAllYogaCourse() {
        String selection = COLUMN_SYNC_STATUS + " != ?";
        String[] selectionArgs = new String[]{SyncStatus.DELETED.getValue()};
        return database.query(TABLE_YOGA_COURSE, null, selection, selectionArgs, null, null, 
                             COLUMN_DAY_OF_WEEK + " ASC, " + COLUMN_TIME + " ASC");
    }

    public Cursor readYogaCourse(long id) {
        return database.query(TABLE_YOGA_COURSE, null, COLUMN_ID + "=?", 
                             new String[]{String.valueOf(id)}, null, null, null);
    }

    public int updateYogaCourse(long id, String dayOfWeek, String time, int capacity, int duration,
                               float price, String type, String description, String difficulty,
                               String location) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY_OF_WEEK, dayOfWeek);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_CAPACITY, capacity);
        values.put(COLUMN_DURATION, duration);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DIFFICULTY, difficulty);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_SYNC_STATUS, SyncStatus.EDITED.getValue());
        
        return database.update(TABLE_YOGA_COURSE, values, COLUMN_ID + "=?", 
                              new String[]{String.valueOf(id)});
    }

    public int deleteYogaCourse(long id) {
        // First soft delete all class instances for this course
        ContentValues instanceValues = new ContentValues();
        instanceValues.put(COLUMN_SYNC_STATUS, SyncStatus.DELETED.getValue());
        database.update(TABLE_CLASS_INSTANCE, instanceValues, COLUMN_COURSE_ID + "=?", 
                       new String[]{String.valueOf(id)});
        
        // Then soft delete the course
        ContentValues courseValues = new ContentValues();
        courseValues.put(COLUMN_SYNC_STATUS, SyncStatus.DELETED.getValue());
        return database.update(TABLE_YOGA_COURSE, courseValues, COLUMN_ID + "=?", 
                              new String[]{String.valueOf(id)});
    }

    // CLASS INSTANCE CRUD OPERATIONS

    public long createClassInstance(long courseId, String date, String teacher, String comments,
                                   String photoPath, double latitude, double longitude) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_COURSE_ID, courseId);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TEACHER, teacher);
        values.put(COLUMN_COMMENTS, comments);
        values.put(COLUMN_PHOTO_PATH, photoPath);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_SYNC_STATUS, SyncStatus.EDITED.getValue());
        
        return database.insertOrThrow(TABLE_CLASS_INSTANCE, null, values);
    }

    public Cursor readAllClassInstances() {
        String selection = COLUMN_SYNC_STATUS + " != ?";
        String[] selectionArgs = new String[]{SyncStatus.DELETED.getValue()};
        return database.query(TABLE_CLASS_INSTANCE, null, selection, selectionArgs, null, null, 
                             COLUMN_DATE + " ASC");
    }

    public Cursor readClassInstancesByCourse(long courseId) {
        String selection = COLUMN_COURSE_ID + "=? AND " + COLUMN_SYNC_STATUS + " != ?";
        String[] selectionArgs = new String[]{String.valueOf(courseId), SyncStatus.DELETED.getValue()};
        return database.query(TABLE_CLASS_INSTANCE, null, selection, selectionArgs, null, null, 
                             COLUMN_DATE + " ASC");
    }

    public Cursor readClassInstance(long instanceId) {
        return database.query(TABLE_CLASS_INSTANCE, null, COLUMN_INSTANCE_ID + "=?", 
                             new String[]{String.valueOf(instanceId)}, null, null, null);
    }

    public int updateClassInstance(long instanceId, String date, String teacher, String comments,
                                  String photoPath, double latitude, double longitude) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TEACHER, teacher);
        values.put(COLUMN_COMMENTS, comments);
        values.put(COLUMN_PHOTO_PATH, photoPath);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_SYNC_STATUS, SyncStatus.EDITED.getValue());
        
        return database.update(TABLE_CLASS_INSTANCE, values, COLUMN_INSTANCE_ID + "=?", 
                              new String[]{String.valueOf(instanceId)});
    }

    public int deleteClassInstance(long instanceId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, SyncStatus.DELETED.getValue());
        return database.update(TABLE_CLASS_INSTANCE, values, COLUMN_INSTANCE_ID + "=?", 
                              new String[]{String.valueOf(instanceId)});
    }

    // SEARCH FUNCTIONALITY

    public Cursor searchInstancesByTeacher(String teacherName) {
        String selection = COLUMN_TEACHER + " LIKE ? AND " + COLUMN_SYNC_STATUS + " != ?";
        String[] selectionArgs = {"%" + teacherName + "%", SyncStatus.DELETED.getValue()};
        return database.query(TABLE_CLASS_INSTANCE, null, selection, selectionArgs, null, null, 
                             COLUMN_DATE + " ASC");
    }

    public Cursor searchCoursesByDay(String dayOfWeek) {
        String selection = COLUMN_DAY_OF_WEEK + " = ? AND " + COLUMN_SYNC_STATUS + " != ?";
        String[] selectionArgs = {dayOfWeek, SyncStatus.DELETED.getValue()};
        return database.query(TABLE_YOGA_COURSE, null, selection, selectionArgs, null, null, 
                             COLUMN_TIME + " ASC");
    }

    public Cursor searchInstancesByDate(String date) {
        String selection = COLUMN_DATE + " = ? AND " + COLUMN_SYNC_STATUS + " != ?";
        String[] selectionArgs = {date, SyncStatus.DELETED.getValue()};
        return database.query(TABLE_CLASS_INSTANCE, null, selection, selectionArgs, null, null, 
                             COLUMN_DATE + " ASC");
    }

    // CLOUD SYNC FUNCTIONALITY

    public Cursor getUnsyncedCourses() {
        String selection = COLUMN_SYNC_STATUS + " = ? OR " + COLUMN_SYNC_STATUS + " = ?";
        String[] selectionArgs = {SyncStatus.EDITED.getValue(), SyncStatus.DELETED.getValue()};
        return database.query(TABLE_YOGA_COURSE, null, selection, selectionArgs, null, null, null);
    }

    public Cursor getUnsyncedInstances() {
        String selection = COLUMN_SYNC_STATUS + " = ? OR " + COLUMN_SYNC_STATUS + " = ?";
        String[] selectionArgs = {SyncStatus.EDITED.getValue(), SyncStatus.DELETED.getValue()};
        return database.query(TABLE_CLASS_INSTANCE, null, selection, selectionArgs, null, null, null);
    }

    public Cursor getUnsyncedUsers() {
        String selection = COLUMN_USER_SYNC_STATUS + " = ? OR " + COLUMN_USER_SYNC_STATUS + " = ?";
        String[] selectionArgs = {SyncStatus.EDITED.getValue(), SyncStatus.DELETED.getValue()};
        return database.query(TABLE_USER, null, selection, selectionArgs, null, null, null);
    }

    public Cursor getUnsyncedBookings() {
        String selection = COLUMN_BOOKING_SYNC_STATUS + " = ? OR " + COLUMN_BOOKING_SYNC_STATUS + " = ?";
        String[] selectionArgs = {SyncStatus.EDITED.getValue(), SyncStatus.DELETED.getValue()};
        return database.query(TABLE_BOOKINGS, null, selection, selectionArgs, null, null, null);
    }

    public int markCourseSynced(long courseId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, SyncStatus.SYNC.getValue());
        return database.update(TABLE_YOGA_COURSE, values, COLUMN_ID + "=?", 
                              new String[]{String.valueOf(courseId)});
    }

    public int markInstanceSynced(long instanceId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, SyncStatus.SYNC.getValue());
        return database.update(TABLE_CLASS_INSTANCE, values, COLUMN_INSTANCE_ID + "=?", 
                              new String[]{String.valueOf(instanceId)});
    }

    public int markUserSynced(long userId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_SYNC_STATUS, SyncStatus.SYNC.getValue());
        return database.update(TABLE_USER, values, COLUMN_USER_ID + "=?", 
                              new String[]{String.valueOf(userId)});
    }

    public int markBookingSynced(long bookingId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOKING_SYNC_STATUS, SyncStatus.SYNC.getValue());
        return database.update(TABLE_BOOKINGS, values, COLUMN_BOOKING_ID + "=?", 
                              new String[]{String.valueOf(bookingId)});
    }

    public void resetDatabase() {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCE);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_COURSE);
        onCreate(database);
    }

    // UTILITY METHODS FOR SYNC STATUS

    /**
     * Get count of records by sync status for a specific table
     */
    public int getCountBySyncStatus(String tableName, String syncStatus) {
        String selection = "sync_status = ?";
        String[] selectionArgs = {syncStatus};
        Cursor cursor = database.query(tableName, new String[]{"COUNT(*)"}, selection, selectionArgs, null, null, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    /**
     * Get all records that need to be synced (edited or deleted)
     */
    public Cursor getAllUnsyncedRecords() {
        String query = "SELECT 'yoga_course' as table_name, _id as record_id, sync_status FROM " + TABLE_YOGA_COURSE + 
                      " WHERE sync_status IN (?, ?) " +
                      "UNION ALL " +
                      "SELECT 'class_instance' as table_name, _id as record_id, sync_status FROM " + TABLE_CLASS_INSTANCE + 
                      " WHERE sync_status IN (?, ?) " +
                      "UNION ALL " +
                      "SELECT 'user' as table_name, _id as record_id, sync_status FROM " + TABLE_USER + 
                      " WHERE sync_status IN (?, ?) " +
                      "UNION ALL " +
                      "SELECT 'booking' as table_name, _id as record_id, sync_status FROM " + TABLE_BOOKINGS + 
                      " WHERE sync_status IN (?, ?)";
        
        String[] selectionArgs = {
            SyncStatus.EDITED.getValue(), SyncStatus.DELETED.getValue(),
            SyncStatus.EDITED.getValue(), SyncStatus.DELETED.getValue(),
            SyncStatus.EDITED.getValue(), SyncStatus.DELETED.getValue(),
            SyncStatus.EDITED.getValue(), SyncStatus.DELETED.getValue()
        };
        
        return database.rawQuery(query, selectionArgs);
    }

    // UTILITY METHODS

    public Cursor getDetailedCourseInfo(long courseId) {
        String query = "SELECT c.*, " +
                      "(SELECT COUNT(*) FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_COURSE_ID + " = c." + COLUMN_ID + ") as instance_count " +
                      "FROM " + TABLE_YOGA_COURSE + " c " +
                      "WHERE c." + COLUMN_ID + " = ?";
        return database.rawQuery(query, new String[]{String.valueOf(courseId)});
    }

    // COMPREHENSIVE SEARCH METHODS

    public Cursor searchCourses(String searchQuery, String dayOfWeek, String difficulty, 
                               float minPrice, float maxPrice, int minDuration, int maxDuration) {
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList<>();
        
        // Exclude deleted records
        selection.append(COLUMN_SYNC_STATUS).append(" != ?");
        selectionArgs.add(SyncStatus.DELETED.getValue());
        
        // General search query across multiple fields
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            selection.append(" AND (")
                    .append(COLUMN_TYPE).append(" LIKE ? OR ")
                    .append(COLUMN_DESCRIPTION).append(" LIKE ? OR ")
                    .append(COLUMN_LOCATION).append(" LIKE ?")
                    .append(")");
            String searchPattern = "%" + searchQuery.trim() + "%";
            selectionArgs.add(searchPattern);
            selectionArgs.add(searchPattern);
            selectionArgs.add(searchPattern);
        }
        
        // Day of week filter
        if (dayOfWeek != null && !dayOfWeek.trim().isEmpty()) {
            selection.append(" AND ").append(COLUMN_DAY_OF_WEEK).append(" = ?");
            selectionArgs.add(dayOfWeek);
        }
        
        // Difficulty filter
        if (difficulty != null && !difficulty.trim().isEmpty()) {
            selection.append(" AND ").append(COLUMN_DIFFICULTY).append(" = ?");
            selectionArgs.add(difficulty);
        }
        
        // Price range filter
        if (minPrice >= 0 || maxPrice >= 0) {
            if (minPrice >= 0 && maxPrice >= 0) {
                selection.append(" AND ").append(COLUMN_PRICE).append(" BETWEEN ? AND ?");
                selectionArgs.add(String.valueOf(minPrice));
                selectionArgs.add(String.valueOf(maxPrice));
            } else if (minPrice >= 0) {
                selection.append(" AND ").append(COLUMN_PRICE).append(" >= ?");
                selectionArgs.add(String.valueOf(minPrice));
            } else {
                selection.append(" AND ").append(COLUMN_PRICE).append(" <= ?");
                selectionArgs.add(String.valueOf(maxPrice));
            }
        }
        
        // Duration range filter
        if (minDuration > 0 || maxDuration > 0) {
            if (minDuration > 0 && maxDuration > 0) {
                selection.append(" AND ").append(COLUMN_DURATION).append(" BETWEEN ? AND ?");
                selectionArgs.add(String.valueOf(minDuration));
                selectionArgs.add(String.valueOf(maxDuration));
            } else if (minDuration > 0) {
                selection.append(" AND ").append(COLUMN_DURATION).append(" >= ?");
                selectionArgs.add(String.valueOf(minDuration));
            } else {
                selection.append(" AND ").append(COLUMN_DURATION).append(" <= ?");
                selectionArgs.add(String.valueOf(maxDuration));
            }
        }
        
        String[] selectionArgsArray = selectionArgs.toArray(new String[0]);
        String selectionString = selection.toString();
        
        return database.query(TABLE_YOGA_COURSE, null, selectionString, selectionArgsArray, 
                             null, null, COLUMN_DAY_OF_WEEK + " ASC, " + COLUMN_TIME + " ASC");
    }

    public Cursor searchInstances(String searchQuery, String fromDate, String toDate) {
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList<>();
        
        // Exclude deleted records
        selection.append(COLUMN_SYNC_STATUS).append(" != ?");
        selectionArgs.add(SyncStatus.DELETED.getValue());
        
        // General search query across multiple fields
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            selection.append(" AND (")
                    .append(COLUMN_TEACHER).append(" LIKE ? OR ")
                    .append(COLUMN_COMMENTS).append(" LIKE ?")
                    .append(")");
            String searchPattern = "%" + searchQuery.trim() + "%";
            selectionArgs.add(searchPattern);
            selectionArgs.add(searchPattern);
        }
        
        // Date range filter
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            selection.append(" AND ").append(COLUMN_DATE).append(" >= ?");
            selectionArgs.add(fromDate);
        }
        
        if (toDate != null && !toDate.trim().isEmpty()) {
            selection.append(" AND ").append(COLUMN_DATE).append(" <= ?");
            selectionArgs.add(toDate);
        }
        
        String[] selectionArgsArray = selectionArgs.toArray(new String[0]);
        String selectionString = selection.toString();
        
        return database.query(TABLE_CLASS_INSTANCE, null, selectionString, selectionArgsArray, 
                             null, null, COLUMN_DATE + " DESC");
    }

    public Cursor searchCoursesByType(String type) {
        String selection = COLUMN_TYPE + " LIKE ? AND " + COLUMN_SYNC_STATUS + " != ?";
        String[] selectionArgs = {"%" + type + "%", SyncStatus.DELETED.getValue()};
        return database.query(TABLE_YOGA_COURSE, null, selection, selectionArgs, null, null, 
                             COLUMN_DAY_OF_WEEK + " ASC, " + COLUMN_TIME + " ASC");
    }

    public Cursor searchInstancesByDateRange(String fromDate, String toDate) {
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList<>();
        
        // Exclude deleted records
        selection.append(COLUMN_SYNC_STATUS).append(" != ?");
        selectionArgs.add(SyncStatus.DELETED.getValue());
        
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            selection.append(" AND ").append(COLUMN_DATE).append(" >= ?");
            selectionArgs.add(fromDate);
        }
        
        if (toDate != null && !toDate.trim().isEmpty()) {
            selection.append(" AND ").append(COLUMN_DATE).append(" <= ?");
            selectionArgs.add(toDate);
        }
        
        String[] selectionArgsArray = selectionArgs.toArray(new String[0]);
        String selectionString = selection.toString();
        
        return database.query(TABLE_CLASS_INSTANCE, null, selectionString, selectionArgsArray, 
                             null, null, COLUMN_DATE + " DESC");
    }

    public Cursor getCoursesWithInstanceCount() {
        String query = "SELECT c.*, " +
                      "(SELECT COUNT(*) FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_COURSE_ID + " = c." + COLUMN_ID + " AND " + COLUMN_SYNC_STATUS + " != ?) as instance_count " +
                      "FROM " + TABLE_YOGA_COURSE + " c " +
                      "WHERE c." + COLUMN_SYNC_STATUS + " != ? " +
                      "ORDER BY c." + COLUMN_DAY_OF_WEEK + " ASC, c." + COLUMN_TIME + " ASC";
        return database.rawQuery(query, new String[]{SyncStatus.DELETED.getValue(), SyncStatus.DELETED.getValue()});
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USER_SYNC_STATUS + " != ?";
        String[] selectionArgs = {SyncStatus.DELETED.getValue()};
        return db.query(
            TABLE_USER,
            null,
            selection,
            selectionArgs,
            null,
            null,
            COLUMN_USER_NAME + " ASC"
        );
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        values.put(COLUMN_USER_SYNC_STATUS, SyncStatus.EDITED.getValue());
        return db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(user.getId())}) > 0;
    }

    // Booking related methods
    public long createBooking(int classId, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOKING_CLASS_ID, classId);
        values.put(COLUMN_BOOKING_USER_ID, userId);
        values.put(COLUMN_BOOKING_SYNC_STATUS, SyncStatus.EDITED.getValue());
        return db.insert(TABLE_BOOKINGS, null, values);
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b." + COLUMN_BOOKING_ID + ", b." + COLUMN_BOOKING_CLASS_ID + ", b." + COLUMN_BOOKING_USER_ID + ", "
                + "u." + COLUMN_USER_EMAIL + ", "
                + "ci." + COLUMN_DATE + ", "
                + "yc." + COLUMN_TIME + ", "
                + "yc." + COLUMN_PRICE + ", "
                + "yc." + COLUMN_DURATION
                + " FROM " + TABLE_BOOKINGS + " b"
                + " JOIN " + TABLE_USER + " u ON b." + COLUMN_BOOKING_USER_ID + " = u." + COLUMN_ID
                + " JOIN " + TABLE_CLASS_INSTANCE + " ci ON b." + COLUMN_BOOKING_CLASS_ID + " = ci." + COLUMN_INSTANCE_ID
                + " JOIN " + TABLE_YOGA_COURSE + " yc ON ci." + COLUMN_COURSE_ID + " = yc." + COLUMN_ID
                + " WHERE b." + COLUMN_BOOKING_SYNC_STATUS + " != ? "
                + " AND u." + COLUMN_USER_SYNC_STATUS + " != ? "
                + " AND ci." + COLUMN_SYNC_STATUS + " != ? "
                + " AND yc." + COLUMN_SYNC_STATUS + " != ? "
                + " ORDER BY ci." + COLUMN_DATE + " ASC";

        SQLiteDatabase dbReadable = this.getReadableDatabase();
        Cursor cursor = dbReadable.rawQuery(query, new String[]{
            SyncStatus.DELETED.getValue(), 
            SyncStatus.DELETED.getValue(), 
            SyncStatus.DELETED.getValue(), 
            SyncStatus.DELETED.getValue()
        });

        if (cursor.moveToFirst()) {
            do {
                Booking booking = new Booking(
                    cursor.getInt(0),    // booking_id
                    cursor.getInt(1),    // class_id
                    cursor.getInt(2),    // user_id
                    cursor.getString(3), // user_email
                    cursor.getString(4), // class_date
                    cursor.getString(5), // course_time
                    cursor.getDouble(6), // price
                    cursor.getInt(7)     // duration
                );
                bookings.add(booking);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }

    public List<Booking> getBookingsByUserId(long userId) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b." + COLUMN_BOOKING_ID + ", b." + COLUMN_BOOKING_CLASS_ID + ", b." + COLUMN_BOOKING_USER_ID + ", "
                + "u." + COLUMN_USER_EMAIL + ", "
                + "ci." + COLUMN_DATE + ", "
                + "yc." + COLUMN_TIME + ", "
                + "yc." + COLUMN_PRICE + ", "
                + "yc." + COLUMN_DURATION
                + " FROM " + TABLE_BOOKINGS + " b"
                + " JOIN " + TABLE_USER + " u ON b." + COLUMN_BOOKING_USER_ID + " = u." + COLUMN_ID
                + " JOIN " + TABLE_CLASS_INSTANCE + " ci ON b." + COLUMN_BOOKING_CLASS_ID + " = ci." + COLUMN_INSTANCE_ID
                + " JOIN " + TABLE_YOGA_COURSE + " yc ON ci." + COLUMN_COURSE_ID + " = yc." + COLUMN_ID
                + " WHERE b." + COLUMN_BOOKING_USER_ID + " = ? "
                + " AND b." + COLUMN_BOOKING_SYNC_STATUS + " != ? "
                + " AND u." + COLUMN_USER_SYNC_STATUS + " != ? "
                + " AND ci." + COLUMN_SYNC_STATUS + " != ? "
                + " AND yc." + COLUMN_SYNC_STATUS + " != ? "
                + " ORDER BY ci." + COLUMN_DATE + " ASC";

        SQLiteDatabase dbReadable = this.getReadableDatabase();
        Cursor cursor = dbReadable.rawQuery(query, new String[]{
            String.valueOf(userId),
            SyncStatus.DELETED.getValue(), 
            SyncStatus.DELETED.getValue(), 
            SyncStatus.DELETED.getValue(), 
            SyncStatus.DELETED.getValue()
        });

        if (cursor.moveToFirst()) {
            do {
                Booking booking = new Booking(
                    cursor.getInt(0),    // booking_id
                    cursor.getInt(1),    // class_id
                    cursor.getInt(2),    // user_id
                    cursor.getString(3), // user_email
                    cursor.getString(4), // class_date
                    cursor.getString(5), // course_time
                    cursor.getDouble(6), // price
                    cursor.getInt(7)     // duration
                );
                bookings.add(booking);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }

    public boolean deleteBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOKING_SYNC_STATUS, SyncStatus.DELETED.getValue());
        return db.update(TABLE_BOOKINGS, values, COLUMN_BOOKING_ID + " = ?",
                new String[]{String.valueOf(bookingId)}) > 0;
    }

    // FIRESTORE ID MANAGEMENT METHODS

    /**
     * Update Firestore ID for a user
     */
    public int updateUserFirestoreId(long userId, String firestoreId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_FIRESTORE_ID, firestoreId);
        return database.update(TABLE_USER, values, COLUMN_USER_ID + "=?", 
                              new String[]{String.valueOf(userId)});
    }

    /**
     * Update Firestore ID for a yoga course
     */
    public int updateCourseFirestoreId(long courseId, String firestoreId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRESTORE_ID, firestoreId);
        return database.update(TABLE_YOGA_COURSE, values, COLUMN_ID + "=?", 
                              new String[]{String.valueOf(courseId)});
    }

    /**
     * Update Firestore ID for a class instance
     */
    public int updateInstanceFirestoreId(long instanceId, String firestoreId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_INSTANCE_FIRESTORE_ID, firestoreId);
        return database.update(TABLE_CLASS_INSTANCE, values, COLUMN_INSTANCE_ID + "=?", 
                              new String[]{String.valueOf(instanceId)});
    }

    /**
     * Update Firestore ID for a booking
     */
    public int updateBookingFirestoreId(long bookingId, String firestoreId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOKING_FIRESTORE_ID, firestoreId);
        return database.update(TABLE_BOOKINGS, values, COLUMN_BOOKING_ID + "=?", 
                              new String[]{String.valueOf(bookingId)});
    }

    /**
     * Check if user exists by Firestore ID
     */
    public User getUserByFirestoreId(String firestoreId) {
        if (firestoreId == null) return null;
        
        String selection = COLUMN_USER_FIRESTORE_ID + " = ?";
        String[] selectionArgs = {firestoreId};
        
        Cursor cursor = database.query(TABLE_USER, null, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            User user = createUserFromCursor(cursor);
            cursor.close();
            return user;
        }
        
        if (cursor != null) cursor.close();
        return null;
    }

    /**
     * Check if course exists by Firestore ID
     */
    public Cursor getCourseByFirestoreId(String firestoreId) {
        if (firestoreId == null) return null;
        
        String selection = COLUMN_FIRESTORE_ID + " = ?";
        String[] selectionArgs = {firestoreId};
        
        return database.query(TABLE_YOGA_COURSE, null, selection, selectionArgs, null, null, null);
    }

    /**
     * Check if class instance exists by Firestore ID
     */
    public Cursor getInstanceByFirestoreId(String firestoreId) {
        if (firestoreId == null) return null;
        
        String selection = COLUMN_INSTANCE_FIRESTORE_ID + " = ?";
        String[] selectionArgs = {firestoreId};
        
        return database.query(TABLE_CLASS_INSTANCE, null, selection, selectionArgs, null, null, null);
    }

    /**
     * Check if booking exists by Firestore ID
     */
    public Cursor getBookingByFirestoreId(String firestoreId) {
        if (firestoreId == null) return null;
        
        String selection = COLUMN_BOOKING_FIRESTORE_ID + " = ?";
        String[] selectionArgs = {firestoreId};
        
        return database.query(TABLE_BOOKINGS, null, selection, selectionArgs, null, null, null);
    }

    /**
     * Get Firestore ID for a course by local course ID
     */
    public String getCourseFirestoreId(long courseId) {
        String[] columns = {COLUMN_FIRESTORE_ID};
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(courseId)};
        
        Cursor cursor = database.query(TABLE_YOGA_COURSE, columns, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            String firestoreId = cursor.getString(cursor.getColumnIndex(COLUMN_FIRESTORE_ID));
            cursor.close();
            return firestoreId;
        }
        
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    /**
     * Get local course ID by Firestore ID
     */
    public long getCourseIdByFirestoreId(String firestoreId) {
        if (firestoreId == null) return -1;
        
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_FIRESTORE_ID + " = ?";
        String[] selectionArgs = {firestoreId};
        
        Cursor cursor = database.query(TABLE_YOGA_COURSE, columns, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            long courseId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            cursor.close();
            return courseId;
        }
        
        if (cursor != null) {
            cursor.close();
        }
        return -1;
    }

    /**
     * Get Firestore ID for an instance by local instance ID
     */
    public String getInstanceFirestoreId(long instanceId) {
        String[] columns = {COLUMN_INSTANCE_FIRESTORE_ID};
        String selection = COLUMN_INSTANCE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(instanceId)};
        
        Cursor cursor = database.query(TABLE_CLASS_INSTANCE, columns, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            String firestoreId = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_FIRESTORE_ID));
            cursor.close();
            return firestoreId;
        }
        
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    /**
     * Get local instance ID by Firestore ID
     */
    public long getInstanceIdByFirestoreId(String firestoreId) {
        if (firestoreId == null) return -1;
        
        String[] columns = {COLUMN_INSTANCE_ID};
        String selection = COLUMN_INSTANCE_FIRESTORE_ID + " = ?";
        String[] selectionArgs = {firestoreId};
        
        Cursor cursor = database.query(TABLE_CLASS_INSTANCE, columns, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            long instanceId = cursor.getLong(cursor.getColumnIndex(COLUMN_INSTANCE_ID));
            cursor.close();
            return instanceId;
        }
        
        if (cursor != null) {
            cursor.close();
        }
        return -1;
    }

    /**
     * Get Firestore ID for a user by local user ID
     */
    public String getUserFirestoreId(long userId) {
        String[] columns = {COLUMN_USER_FIRESTORE_ID};
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        Cursor cursor = database.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            String firestoreId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_FIRESTORE_ID));
            cursor.close();
            return firestoreId;
        }
        
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    /**
     * Get local user ID by Firestore ID
     */
    public long getUserIdByFirestoreId(String firestoreId) {
        if (firestoreId == null) return -1;
        
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_FIRESTORE_ID + " = ?";
        String[] selectionArgs = {firestoreId};
        
        Cursor cursor = database.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            long userId = cursor.getLong(cursor.getColumnIndex(COLUMN_USER_ID));
            cursor.close();
            return userId;
        }
        
        if (cursor != null) {
            cursor.close();
        }
        return -1;
    }
}
