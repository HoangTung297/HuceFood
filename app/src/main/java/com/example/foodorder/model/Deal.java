package com.example.foodorder.model;

import java.io.Serializable;

public class Deal implements Serializable {
    private int id;
    private String name;
    private String discount;
    private String count;
    private String date;

    public Deal(int id, String name, String discount, String count, String date) {
        this.id = id;
        this.name = name;
        this.discount = discount;
        this.count = count;
        this.date = date;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDiscount() { return discount; }
    public String getCount() { return count; }
    public String getDate() { return date; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDiscount(String discount) { this.discount = discount; }
    public void setCount(String count) { this.count = count; }
    public void setDate(String date) { this.date = date; }
}
