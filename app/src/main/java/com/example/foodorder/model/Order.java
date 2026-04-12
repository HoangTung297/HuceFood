package com.example.foodorder.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private List<Food> foodItems;
    private double totalPrice;
    private String orderDate;
    private String status;

    public Order(int id, int userId, String orderDate, String status) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
        this.foodItems = new ArrayList<>();
        this.totalPrice = 0;
    }

    public void addFood(Food food) {
        foodItems.add(food);
        totalPrice += food.getPrice();
    }

    // Getters và Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public List<Food> getFoodItems() { return foodItems; }
    public void setFoodItems(List<Food> foodItems) { this.foodItems = foodItems; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}