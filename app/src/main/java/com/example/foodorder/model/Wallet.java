package com.example.foodorder.model;

public class Wallet {
    private String id;
    private String userId;
    private double balance;
    private long createdAt;
    private long updatedAt;

    public Wallet() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Wallet(String userId, double balance) {
        this.userId = userId;
        this.balance = balance;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public double getBalance() { return balance; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setBalance(double balance) { this.balance = balance; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}