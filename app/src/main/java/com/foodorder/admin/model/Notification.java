package com.foodorder.admin.model;

import java.io.Serializable;

public class Notification implements Serializable {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type;
    private long createdAt;
    private boolean isRead;
    private String orderId;
    private String imageUrl;

    public Notification() {}

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public long getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }
    public String getOrderId() { return orderId; }
    public String getImageUrl() { return imageUrl; }

    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setType(String type) { this.type = type; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setRead(boolean read) { isRead = read; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTypeIcon() {
        switch (type) {
            case "order": return "🛒";
            case "promotion": return "🎉";
            default: return "📬";
        }
    }

    public String getTimeAgo() {
        long now = System.currentTimeMillis();
        long diff = now - createdAt;

        if (diff < 60 * 1000) {
            return "Vừa xong";
        } else if (diff < 60 * 60 * 1000) {
            return (diff / (60 * 1000)) + " phút trước";
        } else if (diff < 24 * 60 * 60 * 1000) {
            return (diff / (60 * 60 * 1000)) + " giờ trước";
        } else {
            return (diff / (24 * 60 * 60 * 1000)) + " ngày trước";
        }
    }
}