package com.example.foodorder.model;

import java.io.Serializable;

public class Food implements Serializable {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String imageUrl;
    private int soldCount;
    private double rating;
    private String restaurantName;
    private String restaurantId;
    private String favoriteId;

    public Food() {}

    public Food(String id, String name, String description, double price, String category, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.soldCount = 0;
        this.rating = 0;
        this.restaurantName = "";
        this.restaurantId = "";
        this.favoriteId = "";
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public int getSoldCount() { return soldCount; }
    public double getRating() { return rating; }
    public String getRestaurantName() { return restaurantName; }
    public String getRestaurantId() { return restaurantId; }
    public String getFavoriteId() { return favoriteId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
    public void setRating(double rating) { this.rating = rating; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public void setFavoriteId(String favoriteId) { this.favoriteId = favoriteId; }
}