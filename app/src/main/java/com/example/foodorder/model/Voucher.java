package com.example.foodorder.model;

import java.io.Serializable;

public class Voucher implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String code;
    private String title;
    private String name;
    private String description;
    private String discountType; // fixed, percent, freeship
    private double discountValue;
    private double minOrder;
    private double minFoodPrice;
    private boolean isActive;
    private boolean isGlobal;
    private boolean isUsed;
    private long expiryDate;
    private long receivedAt;
    private int quantity;
    private int usedCount;
    private String userId;

    public Voucher() {}

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDiscountType() { return discountType; }
    public double getDiscountValue() { return discountValue; }
    public double getDiscount() { return discountValue; }
    public double getMinOrder() { return minOrder; }
    public double getMinFoodPrice() { return minFoodPrice; }
    public boolean isActive() { return isActive; }
    public boolean isGlobal() { return isGlobal; }
    public boolean isUsed() { return isUsed; }
    public long getExpiryDate() { return expiryDate; }
    public long getReceivedAt() { return receivedAt; }
    public int getQuantity() { return quantity; }
    public int getUsedCount() { return usedCount; }
    public String getUserId() { return userId; }

    public void setId(String id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setTitle(String title) { this.title = title; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }
    public void setMinOrder(double minOrder) { this.minOrder = minOrder; }
    public void setMinFoodPrice(double minFoodPrice) { this.minFoodPrice = minFoodPrice; }
    public void setActive(boolean active) { isActive = active; }
    public void setGlobal(boolean global) { isGlobal = global; }
    public void setUsed(boolean used) { isUsed = used; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }
    public void setReceivedAt(long receivedAt) { this.receivedAt = receivedAt; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    public void setUserId(String userId) { this.userId = userId; }
}