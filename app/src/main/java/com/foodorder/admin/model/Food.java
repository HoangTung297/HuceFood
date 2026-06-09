package com.foodorder.admin.model;

import java.io.Serializable;

public class Food implements Serializable {
    private String id;
    private String name;
    private String description;
    private double price;
    private int imageResource;
    private String imageUrl;
    private String category;
    private String restaurantId;
    private String restaurantName;
    private String favoriteId;
    private double rating;
    private int soldCount;
    private boolean isBestSeller;
    private double distance;

    // Constructor mặc định (cần cho Firebase)
    public Food() {}

    // Constructor chính
    public Food(String id, String name, String description, double price, String category, String restaurantId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.restaurantId = restaurantId;
        this.rating = 4.0;
        this.soldCount = 0;
        this.isBestSeller = false;
        this.distance = 1.0;
        this.imageUrl = "";
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getImageResource() { return imageResource; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public String getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public double getRating() { return rating; }
    public int getSoldCount() { return soldCount; }
    public boolean isBestSeller() { return isBestSeller; }
    public double getDistance() { return distance; }

    public String getFavoriteId() { return favoriteId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategory(String category) { this.category = category; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public void setRating(double rating) { this.rating = rating; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
    public void setBestSeller(boolean bestSeller) { isBestSeller = bestSeller; }
    public void setDistance(double distance) { this.distance = distance; }

    public void setFavoriteId(String favoriteId) { this.favoriteId = favoriteId; }
}