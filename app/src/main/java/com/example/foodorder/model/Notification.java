package com.example.foodorder.model;

public class Notification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private long createdAt;
    private boolean isRead;

    public Notification() {}

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setRead(boolean read) { isRead = read; }
}