package com.example.myapplication.data.dao.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.data.dao.UserDao;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.model.User;

public class UserDaoImpl implements UserDao {
    private final AppDatabase dbHelper;

    public UserDaoImpl(AppDatabase dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public long insert(User user) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(AppDatabase.COL_EMAIL, user.getEmail());
            values.put(AppDatabase.COL_PASSWORD, user.getPassword());
            long id = db.insert(AppDatabase.TABLE_USERS, null, values);
            if (id != -1) user.setId(id);
            return id;
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor c = db.query(AppDatabase.TABLE_USERS, new String[]{AppDatabase.COL_ID}, AppDatabase.COL_EMAIL + "=?", new String[]{email}, null, null, null)) {
            return c.getCount() > 0;
        }
    }

    @Override
    public User findByEmailAndPassword(String email, String password) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor c = db.query(AppDatabase.TABLE_USERS,
                     new String[]{AppDatabase.COL_ID, AppDatabase.COL_EMAIL, AppDatabase.COL_PASSWORD},
                     AppDatabase.COL_EMAIL + "=? AND " + AppDatabase.COL_PASSWORD + "=?",
                     new String[]{email, password}, null, null, null)) {
            if (c.moveToFirst()) {
                long id = c.getLong(0);
                String e = c.getString(1);
                String p = c.getString(2);
                return new User(id, e, p);
            }
            return null;
        }
    }
}

