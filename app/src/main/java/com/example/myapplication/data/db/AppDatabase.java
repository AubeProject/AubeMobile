package com.example.myapplication.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "app.db";
    public static final int DATABASE_VERSION = 1;

    // User table
    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";

    private static volatile AppDatabase INSTANCE; // singleton

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppDatabase(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    private AppDatabase(Context context) {
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
}

