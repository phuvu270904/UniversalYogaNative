package com.example.universalyoganative;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase database;

    public DatabaseHelper(Context context) {
        super(context, "YogaDB", null, 1);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String CREATE_TABLE_YOGACOURSE = "create table YogaCourse(_id integer primary key autoincrement," +
                    "dayofweek text, time text, price float, type text, description text)";
            db.execSQL(CREATE_TABLE_YOGACOURSE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "YogaCourse");
        Log.e(this.getClass().getName(), "YogaCourse" + " upgrade to version " + 
            newVersion + " - old data lost");
        onCreate(db);
    }

    public long createNewYogaCourse(String dow, String time, float p, String type, String des) {
        ContentValues rowValues = new ContentValues();
        rowValues.put("dayofweek", dow);
        rowValues.put("time", time);
        rowValues.put("price", p);
        rowValues.put("type", type);
        rowValues.put("description", des);
        return database.insertOrThrow("YogaCourse", null, rowValues);
    }

    public Cursor readAllYogaCourse() {
        Cursor results = database.query("YogaCourse",
            new String[] {"_id", "dayofweek", "time", "price", "type", "description"},
            null, null, null, null, null);
        results.moveToFirst();
        return results;
    }
}
