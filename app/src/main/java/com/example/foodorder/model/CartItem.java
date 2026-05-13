package com.example.foodorder.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private Food food;
    private int quantity;
    private String note;
    private double totalPrice;
    private String restaurantId;
    private String restaurantName;

    public CartItem(Food food, int quantity) {
        this.food = food;
        this.quantity = quantity;
        this.note = "";
        this.restaurantId = String.valueOf(food.getRestaurantId());
        this.restaurantName = food.getRestaurantName();
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        this.totalPrice = food.getPrice() * quantity;
    }

    public Food getFood() { return food; }
    public int getQuantity() { return quantity; }
    public String getNote() { return note; }
    public double getTotalPrice() { return totalPrice; }
    public String getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void incrementQuantity() {
        this.quantity++;
        calculateTotalPrice();
    }

    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
            calculateTotalPrice();
        }
    }
}