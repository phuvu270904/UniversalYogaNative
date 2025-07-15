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

public class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase database;
    private static final String DATABASE_NAME = "YogaDB";
    private static final int DATABASE_VERSION = 3; // Incremented for User table

    // Table names
    private static final String TABLE_YOGA_COURSE = "YogaCourse";
    private static final String TABLE_CLASS_INSTANCE = "ClassInstance";
    public static final String TABLE_USER = "User";

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
    private static final String COLUMN_INSTRUCTOR = "instructor";
    private static final String COLUMN_CREATED_DATE = "created_date";
    private static final String COLUMN_SYNC_STATUS = "sync_status";

    // ClassInstance table columns
    private static final String COLUMN_INSTANCE_ID = "_id";
    private static final String COLUMN_COURSE_ID = "course_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TEACHER = "teacher";
    private static final String COLUMN_COMMENTS = "comments";
    private static final String COLUMN_PHOTO_PATH = "photo_path";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    // User table columns
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_ROLE = "role";
    public static final String COLUMN_USER_CREATED_DATE = "created_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Create YogaCourse table
            String CREATE_TABLE_YOGACOURSE = "CREATE TABLE " + TABLE_YOGA_COURSE + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DAY_OF_WEEK + " TEXT NOT NULL, " +
                    COLUMN_TIME + " TEXT NOT NULL, " +
                    COLUMN_CAPACITY + " INTEGER NOT NULL, " +
                    COLUMN_DURATION + " INTEGER NOT NULL, " +
                    COLUMN_PRICE + " REAL NOT NULL, " +
                    COLUMN_TYPE + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_DIFFICULTY + " TEXT, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_INSTRUCTOR + " TEXT, " +
                    COLUMN_CREATED_DATE + " TEXT, " +
                    COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0" + // 0 = not synced, 1 = synced
                    ")";
            db.execSQL(CREATE_TABLE_YOGACOURSE);

            // Create ClassInstance table
            String CREATE_TABLE_CLASSINSTANCE = "CREATE TABLE " + TABLE_CLASS_INSTANCE + "(" +
                    COLUMN_INSTANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_COURSE_ID + " INTEGER NOT NULL, " +
                    COLUMN_DATE + " TEXT NOT NULL, " +
                    COLUMN_TEACHER + " TEXT NOT NULL, " +
                    COLUMN_COMMENTS + " TEXT, " +
                    COLUMN_PHOTO_PATH + " TEXT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(" + COLUMN_COURSE_ID + ") REFERENCES " + TABLE_YOGA_COURSE + "(" + COLUMN_ID + ")" +
                    ")";
            db.execSQL(CREATE_TABLE_CLASSINSTANCE);

            // Create User table
            String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + "(" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_NAME + " TEXT NOT NULL, " +
                    COLUMN_USER_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_USER_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_USER_ROLE + " TEXT NOT NULL DEFAULT 'admin', " +
                    COLUMN_USER_CREATED_DATE + " TEXT NOT NULL" +
                    ")";
            db.execSQL(CREATE_TABLE_USER);

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating tables", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Add User table if upgrading from version 2 or lower
            String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + "(" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_NAME + " TEXT NOT NULL, " +
                    COLUMN_USER_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_USER_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_USER_ROLE + " TEXT NOT NULL DEFAULT 'admin', " +
                    COLUMN_USER_CREATED_DATE + " TEXT NOT NULL" +
                    ")";
            db.execSQL(CREATE_TABLE_USER);
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
                                   String location, String instructor) {
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
        values.put(COLUMN_INSTRUCTOR, instructor);
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
                               String location, String instructor) {
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
        values.put(COLUMN_INSTRUCTOR, instructor);
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

    public Cursor searchCoursesByTeacher(String teacherName) {
        String selection = COLUMN_INSTRUCTOR + " LIKE ?";
        String[] selectionArgs = {"%" + teacherName + "%"};
        return database.query(TABLE_YOGA_COURSE, null, selection, selectionArgs, null, null, 
                             COLUMN_DAY_OF_WEEK + " ASC, " + COLUMN_TIME + " ASC");
    }

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
        return database.update(TABLE_YOGA_COURSE, values, COLUMN_ID + "=?", 
                              new String[]{String.valueOf(courseId)});
    }

    public int markInstanceSynced(long instanceId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_STATUS, 1);
        return database.update(TABLE_CLASS_INSTANCE, values, COLUMN_INSTANCE_ID + "=?", 
                              new String[]{String.valueOf(instanceId)});
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
                    .append(COLUMN_INSTRUCTOR).append(" LIKE ? OR ")
                    .append(COLUMN_DESCRIPTION).append(" LIKE ? OR ")
                    .append(COLUMN_LOCATION).append(" LIKE ?")
                    .append(")");
            String searchPattern = "%" + searchQuery.trim() + "%";
            selectionArgs.add(searchPattern);
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
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
            "users",
            null,
            null,
            null,
            null,
            null,
            "name ASC"
        );
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        return db.update("users", values, "_id = ?", new String[]{String.valueOf(user.getId())}) > 0;
    }
}
