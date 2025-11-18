package com.example.myapplication.data.dao.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.data.dao.WineDao;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.model.Wine;

import java.util.ArrayList;
import java.util.List;

public class WineDaoImpl implements WineDao {
    private final AppDatabase dbHelper;

    public WineDaoImpl(AppDatabase dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public long insert(Wine wine) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues v = toValues(wine);
            v.remove(AppDatabase.COL_WINE_ID); // ensure not set
            long id = db.insert(AppDatabase.TABLE_WINES, null, v);
            if (id != -1) wine.setId(id);
            return id;
        }
    }

    @Override
    public int update(Wine wine) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues v = toValues(wine);
            return db.update(AppDatabase.TABLE_WINES, v, AppDatabase.COL_WINE_ID + "=?", new String[]{String.valueOf(wine.getId())});
        }
    }

    @Override
    public int delete(long id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            return db.delete(AppDatabase.TABLE_WINES, AppDatabase.COL_WINE_ID + "=?", new String[]{String.valueOf(id)});
        }
    }

    @Override
    public Wine findById(long id) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor c = db.query(AppDatabase.TABLE_WINES, null, AppDatabase.COL_WINE_ID + "=?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) return fromCursor(c);
            return null;
        }
    }

    @Override
    public List<Wine> findAll() {
        List<Wine> list = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor c = db.query(AppDatabase.TABLE_WINES, null, null, null, null, null, AppDatabase.COL_WINE_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    private static ContentValues toValues(Wine w) {
        ContentValues v = new ContentValues();
        if (w.getId() != null) v.put(AppDatabase.COL_WINE_ID, w.getId());
        v.put(AppDatabase.COL_WINE_NAME, w.getName());
        v.put(AppDatabase.COL_WINE_TYPE, w.getType());
        if (w.getYear() != null) v.put(AppDatabase.COL_WINE_YEAR, w.getYear()); else v.putNull(AppDatabase.COL_WINE_YEAR);
        if (w.getPrice() != null) v.put(AppDatabase.COL_WINE_PRICE, w.getPrice()); else v.putNull(AppDatabase.COL_WINE_PRICE);
        v.put(AppDatabase.COL_WINE_NOTES, w.getNotes());
        v.put(AppDatabase.COL_WINE_PAIRING, w.getPairing());
        v.put(AppDatabase.COL_WINE_IMAGE_URI, w.getImageUri());
        return v;
    }

    private static Wine fromCursor(Cursor c) {
        Wine w = new Wine();
        w.setId(c.getLong(c.getColumnIndexOrThrow(AppDatabase.COL_WINE_ID)));
        w.setName(c.getString(c.getColumnIndexOrThrow(AppDatabase.COL_WINE_NAME)));
        w.setType(c.getString(c.getColumnIndexOrThrow(AppDatabase.COL_WINE_TYPE)));
        if (!c.isNull(c.getColumnIndexOrThrow(AppDatabase.COL_WINE_YEAR))) w.setYear(c.getInt(c.getColumnIndexOrThrow(AppDatabase.COL_WINE_YEAR)));
        if (!c.isNull(c.getColumnIndexOrThrow(AppDatabase.COL_WINE_PRICE))) w.setPrice(c.getDouble(c.getColumnIndexOrThrow(AppDatabase.COL_WINE_PRICE)));
        w.setNotes(c.getString(c.getColumnIndexOrThrow(AppDatabase.COL_WINE_NOTES)));
        w.setPairing(c.getString(c.getColumnIndexOrThrow(AppDatabase.COL_WINE_PAIRING)));
        w.setImageUri(c.getString(c.getColumnIndexOrThrow(AppDatabase.COL_WINE_IMAGE_URI)));
        return w;
    }
}

