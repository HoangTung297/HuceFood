package com.example.foodorder.model;

public class Voucher {
    private int id;
    private String title;
    private String discount;
    private String description;

    public Voucher(int id, String title, String discount, String description) {
        this.id = id;
        this.title = title;
        this.discount = discount;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}