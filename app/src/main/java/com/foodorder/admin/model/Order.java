package com.foodorder.admin.model;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class Order {
    private String id;
    private String userId;
    private String orderCode;
    private String restaurantId;
    private String restaurantName;
    private List<Map<String, Object>> items;
    private double subtotal;
    private double deliveryFee;
    private double discount;
    private double finalTotal;
    private String status;
    private Object createdAt;      // Đổi từ long sang Object
    private Object updatedAt;      // Đổi từ long sang Object
    private Object deliveredAt;    // Đã sửa ở lần trước
    private long cancelledAt;
    private String paymentMethod;
    private String paymentStatus;
    private String orderNote;
    private String deliveryName;
    private String deliveryPhone;
    private String deliveryAddress;
    private double rating;
    private String review;
    private boolean isRated;

    // Constructors
    public Order() {}

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getOrderCode() { return orderCode; }
    public String getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public List<Map<String, Object>> getItems() { return items; }
    public double getSubtotal() { return subtotal; }
    public double getDeliveryFee() { return deliveryFee; }
    public double getDiscount() { return discount; }
    public double getFinalTotal() { return finalTotal; }
    public String getStatus() { return status; }
    public long getCancelledAt() { return cancelledAt; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getOrderNote() { return orderNote; }
    public String getDeliveryName() { return deliveryName; }
    public String getDeliveryPhone() { return deliveryPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public double getRating() { return rating; }
    public String getReview() { return review; }
    public boolean isRated() { return isRated; }

    // Xử lý createdAt (hỗ trợ cả Long và Timestamp)
    public long getCreatedAtMillis() {
        if (createdAt == null) return 0;
        if (createdAt instanceof Long) {
            return (Long) createdAt;
        } else if (createdAt instanceof Timestamp) {
            return ((Timestamp) createdAt).toDate().getTime();
        }
        return 0;
    }

    public Object getCreatedAt() { return createdAt; }

    // Xử lý updatedAt (hỗ trợ cả Long và Timestamp)
    public long getUpdatedAtMillis() {
        if (updatedAt == null) return 0;
        if (updatedAt instanceof Long) {
            return (Long) updatedAt;
        } else if (updatedAt instanceof Timestamp) {
            return ((Timestamp) updatedAt).toDate().getTime();
        }
        return 0;
    }

    public Object getUpdatedAt() { return updatedAt; }

    // Xử lý deliveredAt (hỗ trợ cả Long và Timestamp)
    public long getDeliveredAtMillis() {
        if (deliveredAt == null) return 0;
        if (deliveredAt instanceof Long) {
            return (Long) deliveredAt;
        } else if (deliveredAt instanceof Timestamp) {
            return ((Timestamp) deliveredAt).toDate().getTime();
        }
        return 0;
    }

    public Object getDeliveredAt() { return deliveredAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public void setRestaurantName(String restaurantName) { this.restaurantName = restaurantName; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }
    public void setDiscount(double discount) { this.discount = discount; }
    public void setFinalTotal(double finalTotal) { this.finalTotal = finalTotal; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(Object createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Object updatedAt) { this.updatedAt = updatedAt; }
    public void setDeliveredAt(Object deliveredAt) { this.deliveredAt = deliveredAt; }
    public void setCancelledAt(long cancelledAt) { this.cancelledAt = cancelledAt; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setOrderNote(String orderNote) { this.orderNote = orderNote; }
    public void setDeliveryName(String deliveryName) { this.deliveryName = deliveryName; }
    public void setDeliveryPhone(String deliveryPhone) { this.deliveryPhone = deliveryPhone; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setRating(double rating) { this.rating = rating; }
    public void setReview(String review) { this.review = review; }
    public void setRated(boolean rated) { isRated = rated; }
}