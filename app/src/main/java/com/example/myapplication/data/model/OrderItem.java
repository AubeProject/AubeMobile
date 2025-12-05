package com.example.myapplication.data.model;

public class OrderItem {
    private Long id;
    private Long orderId;
    private Long wineId;
    private Integer quantity;
    private Wine wine; // optional loaded reference

    public OrderItem() {}
    public OrderItem(Long wineId, Integer quantity) { this.wineId = wineId; this.quantity = quantity; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getWineId() { return wineId; }
    public void setWineId(Long wineId) { this.wineId = wineId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Wine getWine() { return wine; }
    public void setWine(Wine wine) { this.wine = wine; }
}
