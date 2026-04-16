package com.example.foodorder.model;

import java.io.Serializable;

public class Restaurant implements Serializable {  // THÊM implements Serializable

    private int id;
    private String name;
    private String address;
    private double rating;
    private double distance;
    private String deliveryTime;
    private String discount;
    private String imageUrl;

    public Restaurant(int id, String name, String address, double rating,
                      double distance, String deliveryTime, String discount, String imageUrl) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.distance = distance;
        this.deliveryTime = deliveryTime;
        this.discount = discount;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getRating() { return rating; }
    public double getDistance() { return distance; }
    public String getDeliveryTime() { return deliveryTime; }
    public String getDiscount() { return discount; }
    public String getImageUrl() { return imageUrl; }

    // Setters (nếu cần)
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setRating(double rating) { this.rating = rating; }
    public void setDistance(double distance) { this.distance = distance; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }
    public void setDiscount(String discount) { this.discount = discount; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}