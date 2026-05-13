package com.example.foodorder.model;

import java.io.Serializable;

public class Restaurant implements Serializable {
    private String id;
    private String name;
    private String address;
    private double rating;
    private String imageUrl;

    // Constructor mặc định (cần cho Firebase)
    public Restaurant() {}

    // Constructor đầy đủ
    public Restaurant(String id, String name, String address, double rating, String imageUrl) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setRating(double rating) { this.rating = rating; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}