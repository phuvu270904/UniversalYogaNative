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
    private static final int DATABASE_VERSION = 8; // Incremented for adding sync_status to User and Bookings tables

    // Table names
    private static final String TABLE_YOGA_COURSE = "YogaCourse";
    private static final String TABLE_CLASS_INSTANCE = "ClassInstance";
    public static final String TABLE_USER = "User";
    private static final String TABLE_BOOKINGS = "Bookings"; // Changed to match case convention

    // YogaCourse table columns
    private static final String COLUMN_ID = "_id";
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
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_ROLE = "role";
    public static final String COLUMN_USER_CREATED_DATE = "created_date";
    public static final String COLUMN_USER_SYNC_STATUS = "sync_status";

    // Booking related columns
    private static final String COLUMN_BOOKING_ID = "_id";
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
                    + COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0"
                    + ")";
            db.execSQL(CREATE_TABLE_YOGA_COURSE);

            // Create User table
            String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_USER_NAME + " TEXT,"
                    + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
                    + COLUMN_USER_PASSWORD + " TEXT,"
                    + COLUMN_USER_ROLE + " TEXT,"
                    + COLUMN_USER_CREATED_DATE + " TEXT,"
                    + COLUMN_USER_SYNC_STATUS + " INTEGER DEFAULT 0"
                    + ")";
            db.execSQL(CREATE_TABLE_USER);

            // Create ClassInstance table
            String CREATE_TABLE_CLASS_INSTANCE = "CREATE TABLE " + TABLE_CLASS_INSTANCE + "("
                    + COLUMN_INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_COURSE_ID + " INTEGER,"
                    + COLUMN_DATE + " TEXT,"
                    + COLUMN_TEACHER + " TEXT,"
                    + COLUMN_COMMENTS + " TEXT,"
                    + COLUMN_PHOTO_PATH + " TEXT,"
                    + COLUMN_LATITUDE + " REAL,"
                    + COLUMN_LONGITUDE + " REAL,"
                    + COLUMN_STATUS + " TEXT,"
                    + COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0,"
                    + "FOREIGN KEY(" + COLUMN_COURSE_ID + ") REFERENCES " + TABLE_YOGA_COURSE + "(" + COLUMN_ID + ")"
                    + ")";
            db.execSQL(CREATE_TABLE_CLASS_INSTANCE);

            // Create Bookings table
            String CREATE_TABLE_BOOKINGS = "CREATE TABLE " + TABLE_BOOKINGS + "("
                    + COLUMN_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_BOOKING_CLASS_ID + " INTEGER,"
                    + COLUMN_BOOKING_USER_ID + " INTEGER,"
                    + COLUMN_BOOKING_SYNC_STATUS + " INTEGER DEFAULT 0,"
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
        if (oldVersion < 7) {
            // Add sync_status column to User table if it doesn't exist
            try {
                db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + COLUMN_USER_SYNC_STATUS + " INTEGER DEFAULT 0");
                // Update existing records to have sync_status = 0
                db.execSQL("UPDATE " + TABLE_USER + " SET " + COLUMN_USER_SYNC_STATUS + " = 0 WHERE " + COLUMN_USER_SYNC_STATUS + " IS NULL");
            } catch (Exception e) {
                Log.d("DatabaseHelper", "sync_status column already exists in User table");
            }
            
            // Add sync_status column to Bookings table if it doesn't exist
            try {
                db.execSQL("ALTER TABLE " + TABLE_BOOKINGS + " ADD COLUMN " + COLUMN_BOOKING_SYNC_STATUS + " INTEGER DEFAULT 0");
                // Update existing records to have sync_status = 0
                db.execSQL("UPDATE " + TABLE_BOOKINGS + " SET " + COLUMN_BOOKING_SYNC_STATUS + " = 0 WHERE " + COLUMN_BOOKING_SYNC_STATUS + " IS NULL");
            } catch (Exception e) {
                Log.d("DatabaseHelper", "sync_status column already exists in Bookings table");
            }
            
            // Ensure existing courses and instances have sync_status = 0
            try {
                db.execSQL("UPDATE " + TABLE_YOGA_COURSE + " SET " + COLUMN_SYNC_STATUS + " = 0 WHERE " + COLUMN_SYNC_STATUS + " IS NULL");
            } catch (Exception e) {
                Log.d("DatabaseHelper", "Error updating existing courses sync status");
            }
            
            try {
                db.execSQL("UPDATE " + TABLE_CLASS_INSTANCE + " SET " + COLUMN_SYNC_STATUS + " = 0 WHERE " + COLUMN_SYNC_STATUS + " IS NULL");
            } catch (Exception e) {
                Log.d("DatabaseHelper", "Error updating existing instances sync status");
            }
        }
        
        Log.d(this.getClass().getName(), "Database upgraded to version " + newVersion);
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
        values.put(COLUMN_USER_SYNC_STATUS, 0);
        
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
     * Get all users
     */
    public Cursor readAllUsers() {
        String selection = COLUMN_USER_ROLE + " = ?";
        String[] selectionArgs = new String[]{"user"};
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
     * Delete user
     */
    public void deleteUser(long userId) {
        database.delete(TABLE_USER, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    /**
     * Create User object from cursor
     */
    private User createUserFromCursor(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ROLE)));
        user.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_CREATED_DATE)));
        user.setSyncStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_SYNC_STATUS)));
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
        values.put(COLUMN_SYNC_STATUS, 0);
        
        return database.insertOrThrow(TABLE_YOGA_COURSE, null, values);
    }

    public Cursor readAllYogaCourse() {
        return database.query(TABLE_YOGA_COURSE, null, null, null, null, null, 
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
        values.put(COLUMN_SYNC_STATUS, 0);
        
        return database.update(TABLE_YOGA_COURSE, values, COLUMN_ID + "=?", 
                              new String[]{String.valueOf(id)});
    }

    public int deleteYogaCourse(long id) {
        // First delete all class instances for this course
        database.delete(TABLE_CLASS_INSTANCE, COLUMN_COURSE_ID + "=?", 
                       new String[]{String.valueOf(id)});
        
        // Then delete the course
        return database.delete(TABLE_YOGA_COURSE, COLUMN_ID + "=?", 
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
        values.put(COLUMN_SYNC_STATUS, 0);
        
        return database.insertOrThrow(TABLE_CLASS_INSTANCE, null, values);
    }

    public Cursor readAllClassInstances() {
        return database.query(TABLE_CLASS_INSTANCE, null, null, null, null, null, 
                             COLUMN_DATE + " ASC");
    }

    public Cursor readClassInstancesByCourse(long courseId) {
        return database.query(TABLE_CLASS_INSTANCE, null, COLUMN_COURSE_ID + "=?", 
                             new String[]{String.valueOf(courseId)}, null, null, 
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
        values.put(COLUMN_SYNC_STATUS, 0);
        
        return database.update(TABLE_CLASS_INSTANCE, values, COLUMN_INSTANCE_ID + "=?", 
                              new String[]{String.valueOf(instanceId)});
    }

    public int deleteClassInstance(long instanceId) {
        return database.delete(TABLE_CLASS_INSTANCE, COLUMN_INSTANCE_ID + "=?", 
                              new String[]{String.valueOf(instanceId)});
    }

    // SEARCH FUNCTIONALITY

    public Cursor searchInstancesByTeacher(String teacherName) {
        String selection = COLUMN_TEACHER + " LIKE ?";
        String[] selectionArgs = {"%" + teacherName + "%"};
        return database.query(TABLE_CLASS_INSTANCE, null, selection, selectionArgs, null, null, 
                             COLUMN_DATE + " ASC");
    }

    public Cursor searchCoursesByDay(String dayOfWeek) {
        String selection = COLUMN_DAY_OF_WEEK + " = ?";
        String[] selectionArgs = {dayOfWeek};
        return database.query(TABLE_YOGA_COURSE, null, selection, selectionArgs, null, null, 
                             COLUMN_TIME + " ASC");
    }

    public Cursor searchInstancesByDate(String date) {
        String selection = COLUMN_DATE + " = ?";
        String[] selectionArgs = {date};
        return database.query(TABLE_CLASS_INSTANCE, null, selection, selectionArgs, null, null, 
                             COLUMN_DATE + " ASC");
    }

    // CLOUD SYNC FUNCTIONALITY

    public Cursor getUnsyncedCourses() {
        return database.query(TABLE_YOGA_COURSE, null, COLUMN_SYNC_STATUS + " = 0", 
                             null, null, null, null);
    }

    public Cursor getUnsyncedInstances() {
        return database.query(TABLE_CLASS_INSTANCE, null, COLUMN_SYNC_STATUS + " = 0", 
                             null, null, null, null);
    }

    public int markCourseSynced(long courseId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, 1);
        int result = database.update(TABLE_YOGA_COURSE, values, COLUMN_ID + "=?", 
                              new String[]{String.valueOf(courseId)});
        Log.d("DatabaseHelper", "markCourseSynced - Course ID: " + courseId + ", Update result: " + result);
        return result;
    }

    public int markInstanceSynced(long instanceId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, 1);
        int result = database.update(TABLE_CLASS_INSTANCE, values, COLUMN_INSTANCE_ID + "=?", 
                              new String[]{String.valueOf(instanceId)});
        Log.d("DatabaseHelper", "markInstanceSynced - Instance ID: " + instanceId + ", Update result: " + result);
        return result;
    }

    /**
     * Mark all courses as unsynced (useful for testing or after sync issues)
     */
    public int markAllCoursesUnsynced() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, 0);
        return database.update(TABLE_YOGA_COURSE, values, null, null);
    }

    /**
     * Mark all instances as unsynced (useful for testing or after sync issues)
     */
    public int markAllInstancesUnsynced() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, 0);
        return database.update(TABLE_CLASS_INSTANCE, values, null, null);
    }

    /**
     * Get sync statistics
     */
    public int getSyncedCoursesCount() {
        Cursor cursor = database.query(TABLE_YOGA_COURSE, new String[]{"COUNT(*)"}, 
                                     COLUMN_SYNC_STATUS + " = 1", null, null, null, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getSyncedInstancesCount() {
        Cursor cursor = database.query(TABLE_CLASS_INSTANCE, new String[]{"COUNT(*)"}, 
                                     COLUMN_SYNC_STATUS + " = 1", null, null, null, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public void resetDatabase() {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCE);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_COURSE);
        onCreate(database);
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
        
        // General search query across multiple fields
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            selection.append("(")
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
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COLUMN_DAY_OF_WEEK).append(" = ?");
            selectionArgs.add(dayOfWeek);
        }
        
        // Difficulty filter
        if (difficulty != null && !difficulty.trim().isEmpty()) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COLUMN_DIFFICULTY).append(" = ?");
            selectionArgs.add(difficulty);
        }
        
        // Price range filter
        if (minPrice >= 0 || maxPrice >= 0) {
            if (selection.length() > 0) selection.append(" AND ");
            if (minPrice >= 0 && maxPrice >= 0) {
                selection.append(COLUMN_PRICE).append(" BETWEEN ? AND ?");
                selectionArgs.add(String.valueOf(minPrice));
                selectionArgs.add(String.valueOf(maxPrice));
            } else if (minPrice >= 0) {
                selection.append(COLUMN_PRICE).append(" >= ?");
                selectionArgs.add(String.valueOf(minPrice));
            } else {
                selection.append(COLUMN_PRICE).append(" <= ?");
                selectionArgs.add(String.valueOf(maxPrice));
            }
        }
        
        // Duration range filter
        if (minDuration > 0 || maxDuration > 0) {
            if (selection.length() > 0) selection.append(" AND ");
            if (minDuration > 0 && maxDuration > 0) {
                selection.append(COLUMN_DURATION).append(" BETWEEN ? AND ?");
                selectionArgs.add(String.valueOf(minDuration));
                selectionArgs.add(String.valueOf(maxDuration));
            } else if (minDuration > 0) {
                selection.append(COLUMN_DURATION).append(" >= ?");
                selectionArgs.add(String.valueOf(minDuration));
            } else {
                selection.append(COLUMN_DURATION).append(" <= ?");
                selectionArgs.add(String.valueOf(maxDuration));
            }
        }
        
        String[] selectionArgsArray = selectionArgs.toArray(new String[0]);
        String selectionString = selection.length() > 0 ? selection.toString() : null;
        
        return database.query(TABLE_YOGA_COURSE, null, selectionString, selectionArgsArray, 
                             null, null, COLUMN_DAY_OF_WEEK + " ASC, " + COLUMN_TIME + " ASC");
    }

    public Cursor searchInstances(String searchQuery, String fromDate, String toDate) {
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList<>();
        
        // General search query across multiple fields
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            selection.append("(")
                    .append(COLUMN_TEACHER).append(" LIKE ? OR ")
                    .append(COLUMN_COMMENTS).append(" LIKE ?")
                    .append(")");
            String searchPattern = "%" + searchQuery.trim() + "%";
            selectionArgs.add(searchPattern);
            selectionArgs.add(searchPattern);
        }
        
        // Date range filter
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COLUMN_DATE).append(" >= ?");
            selectionArgs.add(fromDate);
        }
        
        if (toDate != null && !toDate.trim().isEmpty()) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COLUMN_DATE).append(" <= ?");
            selectionArgs.add(toDate);
        }
        
        String[] selectionArgsArray = selectionArgs.toArray(new String[0]);
        String selectionString = selection.length() > 0 ? selection.toString() : null;
        
        return database.query(TABLE_CLASS_INSTANCE, null, selectionString, selectionArgsArray, 
                             null, null, COLUMN_DATE + " DESC");
    }

    public Cursor searchCoursesByType(String type) {
        String selection = COLUMN_TYPE + " LIKE ?";
        String[] selectionArgs = {"%" + type + "%"};
        return database.query(TABLE_YOGA_COURSE, null, selection, selectionArgs, null, null, 
                             COLUMN_DAY_OF_WEEK + " ASC, " + COLUMN_TIME + " ASC");
    }

    public Cursor searchInstancesByDateRange(String fromDate, String toDate) {
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList<>();
        
        if (fromDate != null && !fromDate.trim().isEmpty()) {
            selection.append(COLUMN_DATE).append(" >= ?");
            selectionArgs.add(fromDate);
        }
        
        if (toDate != null && !toDate.trim().isEmpty()) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COLUMN_DATE).append(" <= ?");
            selectionArgs.add(toDate);
        }
        
        String[] selectionArgsArray = selectionArgs.toArray(new String[0]);
        String selectionString = selection.length() > 0 ? selection.toString() : null;
        
        return database.query(TABLE_CLASS_INSTANCE, null, selectionString, selectionArgsArray, 
                             null, null, COLUMN_DATE + " DESC");
    }

    public Cursor getCoursesWithInstanceCount() {
        String query = "SELECT c.*, " +
                      "(SELECT COUNT(*) FROM " + TABLE_CLASS_INSTANCE + " WHERE " + COLUMN_COURSE_ID + " = c." + COLUMN_ID + ") as instance_count " +
                      "FROM " + TABLE_YOGA_COURSE + " c " +
                      "ORDER BY c." + COLUMN_DAY_OF_WEEK + " ASC, c." + COLUMN_TIME + " ASC";
        return database.rawQuery(query, null);
    }

    public Cursor getAllUsers() {
        return database.query(TABLE_USER, null, null, null, null, null, COLUMN_USER_NAME + " ASC");
    }

    public boolean updateUser(User user) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        return database.update(TABLE_USER, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(user.getId())}) > 0;
    }

    // Booking related methods
    public long createBooking(int classId, long userId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOKING_CLASS_ID, classId);
        values.put(COLUMN_BOOKING_USER_ID, userId);
        values.put(COLUMN_BOOKING_SYNC_STATUS, 0);
        return database.insert(TABLE_BOOKINGS, null, values);
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
                + " ORDER BY ci." + COLUMN_DATE + " ASC";

        Cursor cursor = database.rawQuery(query, null);

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
                + " WHERE b." + COLUMN_BOOKING_USER_ID + " = ?"
                + " ORDER BY ci." + COLUMN_DATE + " ASC";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Booking booking = new Booking(
                    cursor.getInt(0),    // booking_id
                    cursor.getInt(1),    // class_id
                    cursor.getLong(2),   // user_id
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
        return database.delete(TABLE_BOOKINGS, COLUMN_BOOKING_ID + " = ?",
                new String[]{String.valueOf(bookingId)}) > 0;
    }

    public int markAllUsersUnsynced() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_SYNC_STATUS, 0);
        return database.update(TABLE_USER, values, null, null);
    }

    public int markAllBookingsUnsynced() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOKING_SYNC_STATUS, 0);
        return database.update(TABLE_BOOKINGS, values, null, null);
    }

    public int getUnsyncedUsersCount() {
        Cursor cursor = database.query(TABLE_USER, new String[]{"COUNT(*)"}, 
                                     COLUMN_USER_SYNC_STATUS + " = 0", null, null, null, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getUnsyncedBookingsCount() {
        Cursor cursor = database.query(TABLE_BOOKINGS, new String[]{"COUNT(*)"}, 
                                     COLUMN_BOOKING_SYNC_STATUS + " = 0", null, null, null, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    /**
     * Get all unsynced users
     */
    public Cursor getUnsyncedUsers() {
        return database.query(TABLE_USER, null, COLUMN_USER_SYNC_STATUS + " = 0", 
                             null, null, null, COLUMN_USER_CREATED_DATE + " DESC");
    }

    /**
     * Get all unsynced bookings
     */
    public List<Booking> getUnsyncedBookings() {
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
                + " WHERE b." + COLUMN_BOOKING_SYNC_STATUS + " = 0"
                + " ORDER BY ci." + COLUMN_DATE + " ASC";

        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Booking booking = new Booking(
                    cursor.getInt(0),    // booking_id
                    cursor.getInt(1),    // class_id
                    cursor.getLong(2),   // user_id
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

    /**
     * Mark user as synced
     */
    public int markUserSynced(long userId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_SYNC_STATUS, 1);
        return database.update(TABLE_USER, values, COLUMN_USER_ID + "=?", 
                              new String[]{String.valueOf(userId)});
    }

    /**
     * Mark booking as synced
     */
    public int markBookingSynced(long bookingId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOKING_SYNC_STATUS, 1);
        return database.update(TABLE_BOOKINGS, values, COLUMN_BOOKING_ID + "=?", 
                              new String[]{String.valueOf(bookingId)});
    }

    /**
     * Reset all sync statuses to 0 (useful for testing)
     */
    public void resetAllSyncStatuses() {
        // Reset courses sync status
        ContentValues courseValues = new ContentValues();
        courseValues.put(COLUMN_SYNC_STATUS, 0);
        database.update(TABLE_YOGA_COURSE, courseValues, null, null);
        
        // Reset instances sync status
        ContentValues instanceValues = new ContentValues();
        instanceValues.put(COLUMN_SYNC_STATUS, 0);
        database.update(TABLE_CLASS_INSTANCE, instanceValues, null, null);
        
        // Reset users sync status
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_USER_SYNC_STATUS, 0);
        database.update(TABLE_USER, userValues, null, null);
        
        // Reset bookings sync status
        ContentValues bookingValues = new ContentValues();
        bookingValues.put(COLUMN_BOOKING_SYNC_STATUS, 0);
        database.update(TABLE_BOOKINGS, bookingValues, null, null);
        
        Log.d("DatabaseHelper", "All sync statuses reset to 0");
    }

    /**
     * Get sync status statistics for debugging
     */
    public String getSyncStatusReport() {
        StringBuilder report = new StringBuilder();
        
        // Check courses sync status
        Cursor syncedCourses = database.query(TABLE_YOGA_COURSE, new String[]{"COUNT(*)"}, 
                                             COLUMN_SYNC_STATUS + " = 1", null, null, null, null);
        Cursor unsyncedCourses = database.query(TABLE_YOGA_COURSE, new String[]{"COUNT(*)"}, 
                                               COLUMN_SYNC_STATUS + " = 0", null, null, null, null);
        
        int syncedCoursesCount = 0;
        int unsyncedCoursesCount = 0;
        
        if (syncedCourses != null && syncedCourses.moveToFirst()) {
            syncedCoursesCount = syncedCourses.getInt(0);
            syncedCourses.close();
        }
        if (unsyncedCourses != null && unsyncedCourses.moveToFirst()) {
            unsyncedCoursesCount = unsyncedCourses.getInt(0);
            unsyncedCourses.close();
        }
        
        report.append("Courses - Synced: ").append(syncedCoursesCount)
              .append(", Unsynced: ").append(unsyncedCoursesCount).append("\n");
        
        // Check instances sync status
        Cursor syncedInstances = database.query(TABLE_CLASS_INSTANCE, new String[]{"COUNT(*)"}, 
                                               COLUMN_SYNC_STATUS + " = 1", null, null, null, null);
        Cursor unsyncedInstances = database.query(TABLE_CLASS_INSTANCE, new String[]{"COUNT(*)"}, 
                                                 COLUMN_SYNC_STATUS + " = 0", null, null, null, null);
        
        int syncedInstancesCount = 0;
        int unsyncedInstancesCount = 0;
        
        if (syncedInstances != null && syncedInstances.moveToFirst()) {
            syncedInstancesCount = syncedInstances.getInt(0);
            syncedInstances.close();
        }
        if (unsyncedInstances != null && unsyncedInstances.moveToFirst()) {
            unsyncedInstancesCount = unsyncedInstances.getInt(0);
            unsyncedInstances.close();
        }
        
        report.append("Instances - Synced: ").append(syncedInstancesCount)
              .append(", Unsynced: ").append(unsyncedInstancesCount).append("\n");
        
        // Check users sync status
        Cursor syncedUsers = database.query(TABLE_USER, new String[]{"COUNT(*)"}, 
                                           COLUMN_USER_SYNC_STATUS + " = 1", null, null, null, null);
        Cursor unsyncedUsers = database.query(TABLE_USER, new String[]{"COUNT(*)"}, 
                                             COLUMN_USER_SYNC_STATUS + " = 0", null, null, null, null);
        
        int syncedUsersCount = 0;
        int unsyncedUsersCount = 0;
        
        if (syncedUsers != null && syncedUsers.moveToFirst()) {
            syncedUsersCount = syncedUsers.getInt(0);
            syncedUsers.close();
        }
        if (unsyncedUsers != null && unsyncedUsers.moveToFirst()) {
            unsyncedUsersCount = unsyncedUsers.getInt(0);
            unsyncedUsers.close();
        }
        
        report.append("Users - Synced: ").append(syncedUsersCount)
              .append(", Unsynced: ").append(unsyncedUsersCount).append("\n");
        
        // Check bookings sync status
        Cursor syncedBookings = database.query(TABLE_BOOKINGS, new String[]{"COUNT(*)"}, 
                                              COLUMN_BOOKING_SYNC_STATUS + " = 1", null, null, null, null);
        Cursor unsyncedBookings = database.query(TABLE_BOOKINGS, new String[]{"COUNT(*)"}, 
                                                COLUMN_BOOKING_SYNC_STATUS + " = 0", null, null, null, null);
        
        int syncedBookingsCount = 0;
        int unsyncedBookingsCount = 0;
        
        if (syncedBookings != null && syncedBookings.moveToFirst()) {
            syncedBookingsCount = syncedBookings.getInt(0);
            syncedBookings.close();
        }
        if (unsyncedBookings != null && unsyncedBookings.moveToFirst()) {
            unsyncedBookingsCount = unsyncedBookings.getInt(0);
            unsyncedBookings.close();
        }
        
        report.append("Bookings - Synced: ").append(syncedBookingsCount)
              .append(", Unsynced: ").append(unsyncedBookingsCount);
        
        return report.toString();
    }

    /**
     * Get yoga course by ID
     */
    public YogaCourse getYogaCourseById(long courseId) {
        Cursor cursor = database.query(TABLE_YOGA_COURSE, null, COLUMN_ID + "=?", 
                                     new String[]{String.valueOf(courseId)}, null, null, null);
        YogaCourse course = null;
        if (cursor != null && cursor.moveToFirst()) {
            course = new YogaCourse();
            course.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
            course.setDayOfWeek(cursor.getString(cursor.getColumnIndex(COLUMN_DAY_OF_WEEK)));
            course.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)));
            course.setCapacity(cursor.getInt(cursor.getColumnIndex(COLUMN_CAPACITY)));
            course.setDuration(cursor.getInt(cursor.getColumnIndex(COLUMN_DURATION)));
            course.setPrice(cursor.getFloat(cursor.getColumnIndex(COLUMN_PRICE)));
            course.setType(cursor.getString(cursor.getColumnIndex(COLUMN_TYPE)));
            course.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
            course.setDifficulty(cursor.getString(cursor.getColumnIndex(COLUMN_DIFFICULTY)));
            course.setLocation(cursor.getString(cursor.getColumnIndex(COLUMN_LOCATION)));
            course.setCreatedDate(cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_DATE)));
            course.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_SYNC_STATUS)));
            cursor.close();
        }
        return course;
    }

    /**
     * Get class instance by ID
     */
    public ClassInstance getClassInstanceById(long instanceId) {
        Cursor cursor = database.query(TABLE_CLASS_INSTANCE, null, COLUMN_INSTANCE_ID + "=?", 
                                     new String[]{String.valueOf(instanceId)}, null, null, null);
        ClassInstance instance = null;
        if (cursor != null && cursor.moveToFirst()) {
            instance = new ClassInstance();
            instance.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_INSTANCE_ID)));
            instance.setCourseId(cursor.getLong(cursor.getColumnIndex(COLUMN_COURSE_ID)));
            instance.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
            instance.setTeacher(cursor.getString(cursor.getColumnIndex(COLUMN_TEACHER)));
            instance.setComments(cursor.getString(cursor.getColumnIndex(COLUMN_COMMENTS)));
            instance.setPhotoPath(cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_PATH)));
            instance.setLatitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)));
            instance.setLongitude(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)));
            instance.setSyncStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_SYNC_STATUS)));
            cursor.close();
        }
        return instance;
    }

    /**
     * Get booking by ID
     */
    public Booking getBookingById(long bookingId) {
        Cursor cursor = database.query(TABLE_BOOKINGS, null, COLUMN_BOOKING_ID + "=?", 
                                     new String[]{String.valueOf(bookingId)}, null, null, null);
        Booking booking = null;
        if (cursor != null && cursor.moveToFirst()) {
            booking = new Booking();
            booking.setBookingId(cursor.getInt(cursor.getColumnIndex(COLUMN_BOOKING_ID)));
            booking.setClassId(cursor.getInt(cursor.getColumnIndex(COLUMN_BOOKING_CLASS_ID)));
            booking.setUserId(cursor.getLong(cursor.getColumnIndex(COLUMN_BOOKING_USER_ID)));
            cursor.close();
        }
        return booking;
    }
}
