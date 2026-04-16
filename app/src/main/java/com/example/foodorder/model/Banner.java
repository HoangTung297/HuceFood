package com.example.foodorder.model;

import java.io.Serializable;

public class Banner implements Serializable {
    private int id;
    private String imageUrl;
    private String title;

    public Banner(int id, String imageUrl, String title) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
    }

    public int getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public String getTitle() { return title; }

    public void setId(int id) { this.id = id; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTitle(String title) { this.title = title; }
}