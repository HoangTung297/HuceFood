package com.example.foodorder.model;

public class Rating {
    private String id;
    private String userId;
    private String restaurantId;
    private String orderId;
    private double rating;
    private String comment;
    private long createdAt;

    public Rating() {}

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getRestaurantId() { return restaurantId; }
    public String getOrderId() { return orderId; }
    public double getRating() { return rating; }
    public String getComment() { return comment; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setRating(double rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}