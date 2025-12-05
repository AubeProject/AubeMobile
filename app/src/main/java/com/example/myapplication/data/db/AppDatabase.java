package com.example.myapplication.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "app.db";
    public static final int DATABASE_VERSION = 6; // bump for companies table

    // User table
    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";

    // Wines table
    public static final String TABLE_WINES = "wines";
    public static final String COL_WINE_ID = "id";
    public static final String COL_WINE_NAME = "name";
    public static final String COL_WINE_TYPE = "type"; 
    public static final String COL_WINE_YEAR = "year"; 
    public static final String COL_WINE_PRICE = "price"; 
    public static final String COL_WINE_NOTES = "notes"; 
    public static final String COL_WINE_PAIRING = "pairing"; 
    public static final String COL_WINE_IMAGE_URI = "image_uri"; 
    public static final String COL_WINE_QUANTITY = "quantity";

    // Clients table
    public static final String TABLE_CLIENTS = "clients";
    public static final String COL_CLIENT_ID = "id";
    public static final String COL_CLIENT_NAME = "name";
    public static final String COL_CLIENT_DOCUMENT = "document";
    public static final String COL_CLIENT_ADDRESS = "address";
    public static final String COL_CLIENT_RESPONSIBLE = "responsible";
    public static final String COL_CLIENT_PHONE = "phone";

    // Orders table
    public static final String TABLE_ORDERS = "orders";
    public static final String COL_ORDER_ID = "id";
    public static final String COL_ORDER_NUMBER = "number";
    public static final String COL_ORDER_CLIENT_ID = "client_id";
    public static final String COL_ORDER_DATE = "date"; 
    public static final String COL_ORDER_STATUS = "status"; 
    public static final String COL_ORDER_PAYMENT = "payment";

    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String COL_ORDER_ITEM_ID = "id";
    public static final String COL_ORDER_ITEM_ORDER_ID = "order_id";
    public static final String COL_ORDER_ITEM_WINE_ID = "wine_id";
    public static final String COL_ORDER_ITEM_QTY = "quantity";

    // Companies table
    public static final String TABLE_COMPANIES = "companies";
    public static final String COL_COMPANY_ID = "id";
    public static final String COL_COMPANY_NAME = "name";
    public static final String COL_COMPANY_ADDRESS = "address";
    public static final String COL_COMPANY_LATITUDE = "latitude";
    public static final String COL_COMPANY_LONGITUDE = "longitude";

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
                COL_WINE_IMAGE_URI + " TEXT, " +
                COL_WINE_QUANTITY + " INTEGER DEFAULT 0" +
                ");";
        db.execSQL(CREATE_WINES);

        String CREATE_CLIENTS = "CREATE TABLE IF NOT EXISTS " + TABLE_CLIENTS + " (" +
                COL_CLIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CLIENT_NAME + " TEXT NOT NULL, " +
                COL_CLIENT_DOCUMENT + " TEXT, " +
                COL_CLIENT_ADDRESS + " TEXT, " +
                COL_CLIENT_RESPONSIBLE + " TEXT, " +
                COL_CLIENT_PHONE + " TEXT" +
                ");";
        db.execSQL(CREATE_CLIENTS);

        String CREATE_ORDERS = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS + " (" +
                COL_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ORDER_NUMBER + " INTEGER NOT NULL, " +
                COL_ORDER_CLIENT_ID + " INTEGER, " +
                COL_ORDER_DATE + " INTEGER, " +
                COL_ORDER_STATUS + " TEXT, " +
                COL_ORDER_PAYMENT + " TEXT, " +
                "FOREIGN KEY(" + COL_ORDER_CLIENT_ID + ") REFERENCES " + TABLE_CLIENTS + "(" + COL_CLIENT_ID + ")" +
                ");";
        db.execSQL(CREATE_ORDERS);

        String CREATE_ORDER_ITEMS = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDER_ITEMS + " (" +
                COL_ORDER_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ORDER_ITEM_ORDER_ID + " INTEGER NOT NULL, " +
                COL_ORDER_ITEM_WINE_ID + " INTEGER, " +
                COL_ORDER_ITEM_QTY + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COL_ORDER_ITEM_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + COL_ORDER_ID + ")," +
                "FOREIGN KEY(" + COL_ORDER_ITEM_WINE_ID + ") REFERENCES " + TABLE_WINES + "(" + COL_WINE_ID + ")" +
                ");";
        db.execSQL(CREATE_ORDER_ITEMS);

        String CREATE_COMPANIES = "CREATE TABLE IF NOT EXISTS " + TABLE_COMPANIES + " (" +
                COL_COMPANY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_COMPANY_NAME + " TEXT, " +
                COL_COMPANY_ADDRESS + " TEXT, " +
                COL_COMPANY_LATITUDE + " REAL, " +
                COL_COMPANY_LONGITUDE + " REAL" +
                ");";
        db.execSQL(CREATE_COMPANIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_WINES + " ADD COLUMN " + COL_WINE_QUANTITY + " INTEGER DEFAULT 0;");
        }
        if (oldVersion < 4) {
            String CREATE_CLIENTS = "CREATE TABLE IF NOT EXISTS " + TABLE_CLIENTS + " (" +
                    COL_CLIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_CLIENT_NAME + " TEXT NOT NULL, " +
                    COL_CLIENT_DOCUMENT + " TEXT, " +
                    COL_CLIENT_ADDRESS + " TEXT, " +
                    COL_CLIENT_RESPONSIBLE + " TEXT, " +
                    COL_CLIENT_PHONE + " TEXT" +
                    ");";
            db.execSQL(CREATE_CLIENTS);
        }
        if (oldVersion < 5) {
            String CREATE_ORDERS = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS + " (" +
                    COL_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ORDER_NUMBER + " INTEGER NOT NULL, " +
                    COL_ORDER_CLIENT_ID + " INTEGER, " +
                    COL_ORDER_DATE + " INTEGER, " +
                    COL_ORDER_STATUS + " TEXT, " +
                    COL_ORDER_PAYMENT + " TEXT, " +
                    "FOREIGN KEY(" + COL_ORDER_CLIENT_ID + ") REFERENCES " + TABLE_CLIENTS + "(" + COL_CLIENT_ID + ")" +
                    ");";
            db.execSQL(CREATE_ORDERS);
            String CREATE_ORDER_ITEMS = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDER_ITEMS + " (" +
                    COL_ORDER_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ORDER_ITEM_ORDER_ID + " INTEGER NOT NULL, " +
                    COL_ORDER_ITEM_WINE_ID + " INTEGER, " +
                    COL_ORDER_ITEM_QTY + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + COL_ORDER_ITEM_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + COL_ORDER_ID + ")," +
                    "FOREIGN KEY(" + COL_ORDER_ITEM_WINE_ID + ") REFERENCES " + TABLE_WINES + "(" + COL_WINE_ID + ")" +
                    ");";
            db.execSQL(CREATE_ORDER_ITEMS);
        }
        if (oldVersion < 6) {
            String CREATE_COMPANIES = "CREATE TABLE IF NOT EXISTS " + TABLE_COMPANIES + " (" +
                    COL_COMPANY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_COMPANY_NAME + " TEXT, " +
                    COL_COMPANY_ADDRESS + " TEXT, " +
                    COL_COMPANY_LATITUDE + " REAL, " +
                    COL_COMPANY_LONGITUDE + " REAL" +
                    ");";
            db.execSQL(CREATE_COMPANIES);
        }
    }
}