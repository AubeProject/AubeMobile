package com.example.myapplication.data.dao;

import com.example.myapplication.data.model.User;

public interface UserDao {
    long insert(User user);
    boolean existsByEmail(String email);
    User findByEmailAndPassword(String email, String password);
}

