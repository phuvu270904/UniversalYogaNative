package com.example.universalyoganative;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase database;
    private static final String DATABASE_NAME = "YogaDB";
    private static final int DATABASE_VERSION = 2;

    // Table names
    private static final String TABLE_YOGA_COURSE = "YogaCourse";
    private static final String TABLE_CLASS_INSTANCE = "ClassInstance";

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

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating tables", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_COURSE);
        Log.e(this.getClass().getName(), "Database upgrade to version " + newVersion + " - old data lost");
        onCreate(db);
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
}
