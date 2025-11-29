package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.model.Company;

import java.util.List;

@Dao
public interface CompanyDao {
    @Insert
    long insert(Company company);

    @Update
    int update(Company company);

    @Delete
    int delete(Company company);

    @Query("SELECT * FROM companies WHERE id = :id")
    Company findById(long id);

    @Query("SELECT * FROM companies")
    List<Company> findAll();

    @Query("SELECT * FROM companies LIMIT 1")
    Company getFirstCompany();
}