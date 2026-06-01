package com.example.foodorder.model;

import java.io.Serializable;

public class Wallet implements Serializable {
    private String userId;
    private double balance;
    private long updatedAt;

    public Wallet() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}