package com.example.myapplication.data.model;

public class Client {
    private Long id;
    private String name;       // Nome/Razão Social
    private String document;   // CNPJ/CPF
    private String address;    // Endereço ou localização
    private String responsible;// Responsável
    private String phone;      // Telefone

    public Client() {}

    public Client(Long id, String name, String document, String address, String responsible, String phone) {
        this.id = id;
        this.name = name;
        this.document = document;
        this.address = address;
        this.responsible = responsible;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getResponsible() { return responsible; }
    public void setResponsible(String responsible) { this.responsible = responsible; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

