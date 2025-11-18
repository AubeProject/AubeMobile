package com.example.myapplication.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "app.db";
    public static final int DATABASE_VERSION = 2; // bumped due to wines table

    // User table
    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";

    // Wines table
    public static final String TABLE_WINES = "wines";
    public static final String COL_WINE_ID = "id";
    public static final String COL_WINE_NAME = "name";
    public static final String COL_WINE_TYPE = "type"; // Tinto, Branco, Rosé, Espumante...
    public static final String COL_WINE_YEAR = "year"; // safra
    public static final String COL_WINE_PRICE = "price"; // REAL
    public static final String COL_WINE_NOTES = "notes"; // notas de degustação
    public static final String COL_WINE_PAIRING = "pairing"; // harmonização
    public static final String COL_WINE_IMAGE_URI = "image_uri"; // URI da imagem no device

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
        String CREATE_USERS = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT" +
                ");";
        db.execSQL(CREATE_USERS);

        String CREATE_WINES = "CREATE TABLE IF NOT EXISTS " + TABLE_WINES + " (" +
                COL_WINE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WINE_NAME + " TEXT NOT NULL, " +
                COL_WINE_TYPE + " TEXT, " +
                COL_WINE_YEAR + " INTEGER, " +
                COL_WINE_PRICE + " REAL, " +
                COL_WINE_NOTES + " TEXT, " +
                COL_WINE_PAIRING + " TEXT, " +
                COL_WINE_IMAGE_URI + " TEXT" +
                ");";
        db.execSQL(CREATE_WINES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String CREATE_WINES = "CREATE TABLE IF NOT EXISTS " + TABLE_WINES + " (" +
                    COL_WINE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_WINE_NAME + " TEXT NOT NULL, " +
                    COL_WINE_TYPE + " TEXT, " +
                    COL_WINE_YEAR + " INTEGER, " +
                    COL_WINE_PRICE + " REAL, " +
                    COL_WINE_NOTES + " TEXT, " +
                    COL_WINE_PAIRING + " TEXT, " +
                    COL_WINE_IMAGE_URI + " TEXT" +
                    ");";
            db.execSQL(CREATE_WINES);
        }
    }
}
