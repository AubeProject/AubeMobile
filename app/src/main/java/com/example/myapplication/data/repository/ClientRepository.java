package com.example.myapplication.data.repository;

import android.content.Context;

import com.example.myapplication.data.dao.ClientDao;
import com.example.myapplication.data.dao.impl.ClientDaoImpl;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.model.Client;

import java.util.List;

public class ClientRepository {
    private final ClientDao dao;

    public ClientRepository(Context ctx) {
        this.dao = new ClientDaoImpl(AppDatabase.getInstance(ctx));
    }

    public long add(Client c) { return dao.insert(c); }
    public int update(Client c) { return dao.update(c); }
    public int delete(long id) { return dao.delete(id); }
    public Client get(long id) { return dao.findById(id); }
    public List<Client> all() { return dao.findAll(); }
}

