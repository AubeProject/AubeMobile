package com.example.myapplication.data.dao.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.data.dao.ClientDao;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.model.Client;

import java.util.ArrayList;
import java.util.List;

public class ClientDaoImpl implements ClientDao {
    private final AppDatabase dbHelper;

    public ClientDaoImpl(AppDatabase dbHelper) { this.dbHelper = dbHelper; }

    @Override
    public long insert(Client c) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues v = toValues(c);
            v.remove(AppDatabase.COL_CLIENT_ID);
            long id = db.insert(AppDatabase.TABLE_CLIENTS, null, v);
            if (id != -1) c.setId(id);
            return id;
        }
    }

    @Override
    public int update(Client c) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues v = toValues(c);
            return db.update(AppDatabase.TABLE_CLIENTS, v, AppDatabase.COL_CLIENT_ID + "=?", new String[]{String.valueOf(c.getId())});
        }
    }

    @Override
    public int delete(long id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            return db.delete(AppDatabase.TABLE_CLIENTS, AppDatabase.COL_CLIENT_ID + "=?", new String[]{String.valueOf(id)});
        }
    }

    @Override
    public Client findById(long id) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor c = db.query(AppDatabase.TABLE_CLIENTS, null, AppDatabase.COL_CLIENT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        }
    }

    @Override
    public List<Client> findAll() {
        List<Client> list = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor c = db.query(AppDatabase.TABLE_CLIENTS, null, null, null, null, null, AppDatabase.COL_CLIENT_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    private static ContentValues toValues(Client c) {
        ContentValues v = new ContentValues();
        if (c.getId() != null) v.put(AppDatabase.COL_CLIENT_ID, c.getId());
        v.put(AppDatabase.COL_CLIENT_NAME, c.getName());
        v.put(AppDatabase.COL_CLIENT_DOCUMENT, c.getDocument());
        v.put(AppDatabase.COL_CLIENT_ADDRESS, c.getAddress());
        v.put(AppDatabase.COL_CLIENT_RESPONSIBLE, c.getResponsible());
        v.put(AppDatabase.COL_CLIENT_PHONE, c.getPhone());
        return v;
    }

    private static Client fromCursor(Cursor cur) {
        Client c = new Client();
        c.setId(cur.getLong(cur.getColumnIndexOrThrow(AppDatabase.COL_CLIENT_ID)));
        c.setName(cur.getString(cur.getColumnIndexOrThrow(AppDatabase.COL_CLIENT_NAME)));
        c.setDocument(cur.getString(cur.getColumnIndexOrThrow(AppDatabase.COL_CLIENT_DOCUMENT)));
        c.setAddress(cur.getString(cur.getColumnIndexOrThrow(AppDatabase.COL_CLIENT_ADDRESS)));
        c.setResponsible(cur.getString(cur.getColumnIndexOrThrow(AppDatabase.COL_CLIENT_RESPONSIBLE)));
        c.setPhone(cur.getString(cur.getColumnIndexOrThrow(AppDatabase.COL_CLIENT_PHONE)));
        return c;
    }
}

