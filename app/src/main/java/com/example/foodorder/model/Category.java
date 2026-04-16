package com.example.foodorder.model;

public class Category {
    private int id;
    private String name;
    private String icon;
    private int imageRes;

    public Category(int id, String name, String icon, int imageRes) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.imageRes = imageRes;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public int getImageRes() { return imageRes; }
}