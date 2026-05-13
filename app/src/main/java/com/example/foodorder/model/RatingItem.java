package com.example.foodorder.model;

import java.io.Serializable;

public class RatingItem implements Serializable {
    private int id;
    private String foodName;
    private String orderInfo;
    private boolean isRated;

    public RatingItem(int id, String foodName, String orderInfo, boolean isRated) {
        this.id = id;
        this.foodName = foodName;
        this.orderInfo = orderInfo;
        this.isRated = isRated;
    }

    public int getId() { return id; }
    public String getFoodName() { return foodName; }
    public String getOrderInfo() { return orderInfo; }
    public boolean isRated() { return isRated; }

    public void setId(int id) { this.id = id; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public void setOrderInfo(String orderInfo) { this.orderInfo = orderInfo; }
    public void setRated(boolean rated) { isRated = rated; }
}
