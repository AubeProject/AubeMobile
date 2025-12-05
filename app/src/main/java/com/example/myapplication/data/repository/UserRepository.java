package com.example.myapplication.data.repository;

import android.content.Context;

import com.example.myapplication.data.dao.UserDao;
import com.example.myapplication.data.dao.impl.UserDaoImpl;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.model.User;

public class UserRepository {
    private final UserDao userDao;

    public UserRepository(Context context) {
        this.userDao = new UserDaoImpl(AppDatabase.getInstance(context));
    }

    public boolean register(String email, String password) {
        if (userDao.existsByEmail(email)) return false;
        long id = userDao.insert(new User(email, password));
        return id != -1;
    }

    public boolean authenticate(String email, String password) {
        return userDao.findByEmailAndPassword(email, password) != null;
    }

    public boolean exists(String email) {
        return userDao.existsByEmail(email);
    }
}
