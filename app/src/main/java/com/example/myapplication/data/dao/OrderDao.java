package com.example.myapplication.data.dao;

import com.example.myapplication.data.model.Order;
import java.util.List;

public interface OrderDao {
    long insert(Order order);
    int update(Order order);
    int delete(long id);
    Order findById(long id);
    List<Order> findAll();
    int nextOrderNumber();
}

