package com.example.myapplication.data.dao;

import com.example.myapplication.data.model.Wine;
import java.util.List;

public interface WineDao {
    long insert(Wine wine);
    int update(Wine wine);
    int delete(long id);
    Wine findById(long id);
    List<Wine> findAll();
}

