package com.example.foodorder.model;

import java.io.Serializable;

public class Food implements Serializable {  // THÊM implements Serializable

    private int id;
    private String name;
    private String description;
    private double price;
    private int imageResource;
    private String category;
    private int restaurantId;
    private String restaurantName;
    private double rating;
    private int soldCount;
    private boolean isBestSeller;
    private double distance;

    public Food(int id, String name, String description, double price,
                int imageResource, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResource = imageResource;
        this.category = category;
        this.rating = 4.0;
        this.soldCount = 0;
        this.isBestSeller = false;
        this.distance = 1.0;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getImageResource() { return imageResource; }
    public String getCategory() { return category; }
    public int getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public double getRating() { return rating; }
    public int getSoldCount() { return soldCount; }
    public boolean isBestSeller() { return isBestSeller; }
    public double getDistance() { return distance; }

    // Setters
    public void setRestaurantId(int restaurantId) { this.restaurantId = restaurantId; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public void setRating(double rating) { this.rating = rating; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
    public void setBestSeller(boolean bestSeller) { isBestSeller = bestSeller; }
    public void setDistance(double distance) { this.distance = distance; }
}