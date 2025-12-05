package com.example.myapplication.data.model;

public class Wine {
    private Long id;
    private String name;
    private String type;
    private Integer year;
    private Double price;
    private String notes;
    private String pairing;
    private String imageUri;
    private Integer quantity;

    public Wine() {}

    public Wine(Long id, String name, String type, Integer year, Double price, String notes, String pairing, String imageUri, Integer quantity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.year = year;
        this.price = price;
        this.notes = notes;
        this.pairing = pairing;
        this.imageUri = imageUri;
        this.quantity = quantity;
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getPairing() { return pairing; }
    public void setPairing(String pairing) { this.pairing = pairing; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
