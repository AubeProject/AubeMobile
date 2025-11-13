package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "app.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT" +
                ");";
        db.execSQL(CREATE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean addUser(String email, String password) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COL_EMAIL, email);
            values.put(COL_PASSWORD, password);
            long id = db.insert(TABLE_USERS, null, values);
            return id != -1;
        }
    }

    public boolean checkUser(String email, String password) {
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID}, COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                     new String[]{email, password}, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    public boolean checkUserExists(String email) {
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID}, COL_EMAIL + "=?",
                     new String[]{email}, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }
}
