package com.example.myapplication.data.dao.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.data.dao.CompanyDao;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.model.Company;

import java.util.ArrayList;
import java.util.List;

public class CompanyDaoImpl implements CompanyDao {
    private final AppDatabase dbHelper;

    public CompanyDaoImpl(AppDatabase db) { 
        this.dbHelper = db; 
    }

    @Override 
    public long insert(Company company) { 
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDatabase.COL_COMPANY_NAME, company.getName());
        values.put(AppDatabase.COL_COMPANY_ADDRESS, company.getAddress());
        values.put(AppDatabase.COL_COMPANY_LATITUDE, company.getLatitude());
        values.put(AppDatabase.COL_COMPANY_LONGITUDE, company.getLongitude());
        return db.insert(AppDatabase.TABLE_COMPANIES, null, values);
    }

    @Override 
    public int update(Company company) { 
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDatabase.COL_COMPANY_NAME, company.getName());
        values.put(AppDatabase.COL_COMPANY_ADDRESS, company.getAddress());
        values.put(AppDatabase.COL_COMPANY_LATITUDE, company.getLatitude());
        values.put(AppDatabase.COL_COMPANY_LONGITUDE, company.getLongitude());
        return db.update(AppDatabase.TABLE_COMPANIES, values, AppDatabase.COL_COMPANY_ID + " = ?", new String[]{String.valueOf(company.getId())});
    }

    @Override 
    public int delete(Company company) { 
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(AppDatabase.TABLE_COMPANIES, AppDatabase.COL_COMPANY_ID + " = ?", new String[]{String.valueOf(company.getId())});
    }

    @Override 
    public Company findById(long id) { 
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(AppDatabase.TABLE_COMPANIES, null, AppDatabase.COL_COMPANY_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Company company = cursorToCompany(cursor);
            cursor.close();
            return company;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    @Override 
    public List<Company> findAll() { 
        List<Company> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(AppDatabase.TABLE_COMPANIES, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToCompany(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Override 
    public Company getFirstCompany() { 
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(AppDatabase.TABLE_COMPANIES, null, null, null, null, null, null, "1");
        if (cursor != null && cursor.moveToFirst()) {
            Company company = cursorToCompany(cursor);
            cursor.close();
            return company;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    private Company cursorToCompany(Cursor cursor) {
        Company c = new Company();
        c.setId(cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabase.COL_COMPANY_ID)));
        c.setName(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabase.COL_COMPANY_NAME)));
        c.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabase.COL_COMPANY_ADDRESS)));
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(AppDatabase.COL_COMPANY_LATITUDE))) {
            c.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(AppDatabase.COL_COMPANY_LATITUDE)));
        }
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(AppDatabase.COL_COMPANY_LONGITUDE))) {
            c.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(AppDatabase.COL_COMPANY_LONGITUDE)));
        }
        return c;
    }
}