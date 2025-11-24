package com.example.myapplication.data.dao.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplication.data.dao.OrderDao;
import com.example.myapplication.data.db.AppDatabase;
import com.example.myapplication.data.model.Order;
import com.example.myapplication.data.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderDaoImpl implements OrderDao {
    private final AppDatabase dbHelper;
    public OrderDaoImpl(AppDatabase dbHelper) { this.dbHelper = dbHelper; }

    @Override
    public long insert(Order order) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues v = new ContentValues();
            v.put(AppDatabase.COL_ORDER_NUMBER, order.getNumber());
            v.put(AppDatabase.COL_ORDER_CLIENT_ID, order.getClientId());
            v.put(AppDatabase.COL_ORDER_DATE, order.getDateEpochMillis());
            v.put(AppDatabase.COL_ORDER_STATUS, order.getStatus());
            v.put(AppDatabase.COL_ORDER_PAYMENT, order.getPayment());
            long id = db.insert(AppDatabase.TABLE_ORDERS, null, v);
            if (id != -1) order.setId(id);
            // items
            if (order.getItems() != null) {
                for (OrderItem it : order.getItems()) {
                    ContentValues iv = new ContentValues();
                    iv.put(AppDatabase.COL_ORDER_ITEM_ORDER_ID, id);
                    iv.put(AppDatabase.COL_ORDER_ITEM_WINE_ID, it.getWineId());
                    iv.put(AppDatabase.COL_ORDER_ITEM_QTY, it.getQuantity());
                    long iid = db.insert(AppDatabase.TABLE_ORDER_ITEMS, null, iv);
                    if (iid != -1) it.setId(iid);
                    it.setOrderId(id);
                }
            }
            return id;
        }
    }

    @Override
    public int update(Order order) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues v = new ContentValues();
            v.put(AppDatabase.COL_ORDER_CLIENT_ID, order.getClientId());
            v.put(AppDatabase.COL_ORDER_DATE, order.getDateEpochMillis());
            v.put(AppDatabase.COL_ORDER_STATUS, order.getStatus());
            v.put(AppDatabase.COL_ORDER_PAYMENT, order.getPayment());
            int rows = db.update(AppDatabase.TABLE_ORDERS, v, AppDatabase.COL_ORDER_ID + "=?", new String[]{String.valueOf(order.getId())});
            // simplistic item handling: delete then re-insert
            db.delete(AppDatabase.TABLE_ORDER_ITEMS, AppDatabase.COL_ORDER_ITEM_ORDER_ID + "=?", new String[]{String.valueOf(order.getId())});
            if (order.getItems() != null) {
                for (OrderItem it : order.getItems()) {
                    ContentValues iv = new ContentValues();
                    iv.put(AppDatabase.COL_ORDER_ITEM_ORDER_ID, order.getId());
                    iv.put(AppDatabase.COL_ORDER_ITEM_WINE_ID, it.getWineId());
                    iv.put(AppDatabase.COL_ORDER_ITEM_QTY, it.getQuantity());
                    long iid = db.insert(AppDatabase.TABLE_ORDER_ITEMS, null, iv);
                    if (iid != -1) it.setId(iid);
                    it.setOrderId(order.getId());
                }
            }
            return rows;
        }
    }

    @Override
    public int delete(long id) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            db.delete(AppDatabase.TABLE_ORDER_ITEMS, AppDatabase.COL_ORDER_ITEM_ORDER_ID + "=?", new String[]{String.valueOf(id)});
            return db.delete(AppDatabase.TABLE_ORDERS, AppDatabase.COL_ORDER_ID + "=?", new String[]{String.valueOf(id)});
        }
    }

    @Override
    public Order findById(long id) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor c = db.query(AppDatabase.TABLE_ORDERS, null, AppDatabase.COL_ORDER_ID + "=?", new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                Order o = fromCursor(c);
                o.setItems(loadItems(db, o.getId()));
                return o;
            }
        }
        return null;
    }

    @Override
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor c = db.query(AppDatabase.TABLE_ORDERS, null, null, null, null, null, AppDatabase.COL_ORDER_DATE + " DESC")) {
            while (c.moveToNext()) {
                Order o = fromCursor(c);
                o.setItems(loadItems(db, o.getId()));
                list.add(o);
            }
        }
        return list;
    }

    @Override
    public int nextOrderNumber() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor c = db.rawQuery("SELECT MAX(" + AppDatabase.COL_ORDER_NUMBER + ") FROM " + AppDatabase.TABLE_ORDERS, null)) {
            if (c.moveToFirst()) {
                int max = c.isNull(0) ? 0 : c.getInt(0);
                return max + 1;
            }
        }
        return 1;
    }

    private Order fromCursor(Cursor cur) {
        Order o = new Order();
        o.setId(cur.getLong(cur.getColumnIndexOrThrow(AppDatabase.COL_ORDER_ID)));
        o.setNumber(cur.getInt(cur.getColumnIndexOrThrow(AppDatabase.COL_ORDER_NUMBER)));
        o.setClientId(cur.getLong(cur.getColumnIndexOrThrow(AppDatabase.COL_ORDER_CLIENT_ID)));
        o.setDateEpochMillis(cur.getLong(cur.getColumnIndexOrThrow(AppDatabase.COL_ORDER_DATE)));
        o.setStatus(cur.getString(cur.getColumnIndexOrThrow(AppDatabase.COL_ORDER_STATUS)));
        o.setPayment(cur.getString(cur.getColumnIndexOrThrow(AppDatabase.COL_ORDER_PAYMENT)));
        return o;
    }

    private List<OrderItem> loadItems(SQLiteDatabase db, Long orderId) {
        List<OrderItem> items = new ArrayList<>();
        try (Cursor c = db.query(AppDatabase.TABLE_ORDER_ITEMS, null, AppDatabase.COL_ORDER_ITEM_ORDER_ID + "=?", new String[]{String.valueOf(orderId)}, null, null, null)) {
            while (c.moveToNext()) {
                OrderItem it = new OrderItem();
                it.setId(c.getLong(c.getColumnIndexOrThrow(AppDatabase.COL_ORDER_ITEM_ID)));
                it.setOrderId(c.getLong(c.getColumnIndexOrThrow(AppDatabase.COL_ORDER_ITEM_ORDER_ID)));
                it.setWineId(c.getLong(c.getColumnIndexOrThrow(AppDatabase.COL_ORDER_ITEM_WINE_ID)));
                it.setQuantity(c.getInt(c.getColumnIndexOrThrow(AppDatabase.COL_ORDER_ITEM_QTY)));
                items.add(it);
            }
        }
        return items;
    }
}
