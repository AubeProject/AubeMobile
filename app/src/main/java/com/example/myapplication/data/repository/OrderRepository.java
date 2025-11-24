package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.dao.OrderDao;
import com.example.myapplication.data.dao.impl.OrderDaoImpl;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.model.Order;
import java.util.List;

public class OrderRepository {
    private final OrderDao dao;
    public OrderRepository(Context ctx) { this.dao = new OrderDaoImpl(AppDatabase.getInstance(ctx)); }
    public long add(Order o) { return dao.insert(o); }
    public int update(Order o) { return dao.update(o); }
    public int delete(long id) { return dao.delete(id); }
    public Order get(long id) { return dao.findById(id); }
    public List<Order> all() { return dao.findAll(); }
    public int nextNumber() { return dao.nextOrderNumber(); }
}

