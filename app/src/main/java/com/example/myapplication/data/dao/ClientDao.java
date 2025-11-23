package com.example.myapplication.data.dao;

import com.example.myapplication.data.model.Client;
import java.util.List;

public interface ClientDao {
    long insert(Client client);
    int update(Client client);
    int delete(long id);
    Client findById(long id);
    List<Client> findAll();
}

