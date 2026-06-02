package com.example.foodorder.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String foodId;
    private String name;
    private double price;
    private int quantity;
    private String restaurantId;
    private String imageUrl;
    private String note;

    public CartItem() {}

    public String getFoodId() { return foodId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getRestaurantId() { return restaurantId; }
    public String getImageUrl() { return imageUrl; }
    public String getNote() { return note; }

    public void setFoodId(String foodId) { this.foodId = foodId; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setNote(String note) { this.note = note; }

    public double getTotalPrice() { return price * quantity; }
}