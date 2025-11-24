package com.example.myapplication.data.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long id;
    private Integer number; // sequential
    private Long clientId;
    private Long dateEpochMillis;
    private String status; // PENDING, DELIVERED, CANCELLED, DRAFT
    private String payment;
    private List<OrderItem> items = new ArrayList<>();
    private Client client; // optional loaded reference

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getNumber() { return number; }
    public void setNumber(Integer number) { this.number = number; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Long getDateEpochMillis() { return dateEpochMillis; }
    public void setDateEpochMillis(Long dateEpochMillis) { this.dateEpochMillis = dateEpochMillis; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPayment() { return payment; }
    public void setPayment(String payment) { this.payment = payment; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public double total() {
        double sum = 0;
        for (OrderItem it : items) {
            if (it.getWine() != null && it.getWine().getPrice() != null) {
                sum += it.getWine().getPrice() * it.getQuantity();
            }
        }
        return sum;
    }

    public int itemCount() {
        int c = 0; for (OrderItem it : items) c += it.getQuantity(); return c;
    }
}
