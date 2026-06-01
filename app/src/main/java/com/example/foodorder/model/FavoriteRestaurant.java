package com.example.foodorder.model;

public class FavoriteRestaurant {
    private String id;
    private String userId;
    private String restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantImage;
    private double rating;
    private long addedAt;

    public FavoriteRestaurant() {}

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public String getRestaurantAddress() { return restaurantAddress; }
    public String getRestaurantImage() { return restaurantImage; }
    public double getRating() { return rating; }
    public long getAddedAt() { return addedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public void setRestaurantAddress(String restaurantAddress) { this.restaurantAddress = restaurantAddress; }
    public void setRestaurantImage(String restaurantImage) { this.restaurantImage = restaurantImage; }
    public void setRating(double rating) { this.rating = rating; }
    public void setAddedAt(long addedAt) { this.addedAt = addedAt; }
}