package com.example.foodorder.model;

public class Voucher {
    private String id;
    private String code;
    private String title;
    private String name;
    private String description;
    private String discountType; // "fixed", "percent", "freeship"
    private double discountValue;
    private double minOrder;
    private long expiryDate;
    private boolean isActive;
    private int quantity;
    private int usedCount;
    private String imageUrl;

    public Voucher() {}

    // Constructor cũ (giữ lại để tương thích)
    public Voucher(int id, String title, String discount, String description) {
        this.id = String.valueOf(id);
        this.title = title;
        this.description = description;
        if (discount != null) {
            if (discount.contains("%")) {
                this.discountType = "percent";
                this.discountValue = Double.parseDouble(discount.replace("%", "").trim());
            } else {
                this.discountType = "fixed";
                this.discountValue = Double.parseDouble(discount.replace("đ", "").trim().replace(".", ""));
            }
        }
        this.isActive = true;
    }

    // Getters
    public String getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDiscountType() { return discountType; }
    public double getDiscountValue() { return discountValue; }
    public double getDiscount() { return discountValue; }
    public double getMinOrder() { return minOrder; }
    public long getExpiryDate() { return expiryDate; }
    public boolean isActive() { return isActive; }
    public int getQuantity() { return quantity; }
    public int getUsedCount() { return usedCount; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setTitle(String title) { this.title = title; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }
    public void setMinOrder(double minOrder) { this.minOrder = minOrder; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }
    public void setActive(boolean active) { isActive = active; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}