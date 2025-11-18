package com.example.myapplication.data.repository;

import android.content.Context;

import com.example.myapplication.data.dao.WineDao;
import com.example.myapplication.data.dao.impl.WineDaoImpl;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.model.Wine;

import java.util.List;

public class WineRepository {
    private final WineDao dao;

    public WineRepository(Context ctx) {
        this.dao = new WineDaoImpl(AppDatabase.getInstance(ctx));
    }

    public long add(Wine wine) { return dao.insert(wine); }
    public int update(Wine wine) { return dao.update(wine); }
    public int delete(long id) { return dao.delete(id); }
    public Wine get(long id) { return dao.findById(id); }
    public List<Wine> all() { return dao.findAll(); }
}

