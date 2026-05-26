package com.example.foodorder.model;

public class User {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private long createdAt;
    private String password;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public User() {}

    public User(String id, String name, String email, String phone, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getAvatarUrl() { return avatarUrl; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}