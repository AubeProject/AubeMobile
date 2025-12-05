package com.example.myapplication.data.repository;

import android.content.Context;

import com.example.myapplication.data.dao.CompanyDao;
import com.example.myapplication.data.dao.impl.CompanyDaoImpl;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.model.Company;

import java.util.List;

public class CompanyRepository {
    private final CompanyDao dao;

    public CompanyRepository(Context context) {
        this.dao = new CompanyDaoImpl(AppDatabase.getInstance(context));
    }

    public long insert(Company company) { return dao.insert(company); }
    public int update(Company company) { return dao.update(company); }
    public int delete(Company company) { return dao.delete(company); }
    public Company findById(long id) { return dao.findById(id); }
    public List<Company> findAll() { return dao.findAll(); }
    public Company getFirstCompany() { return dao.getFirstCompany(); }
}