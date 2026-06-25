package com.example.foodorder.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Order {
    // Thông tin cơ bản
    private String id;
    private String userId;
    private String orderCode;

    // Thông tin nhà hàng
    private String restaurantId;
    private String restaurantName;

    // Thông tin đơn hàng
    private List<Map<String, Object>> items;
    private double subtotal;
    private double deliveryFee;
    private double discount;
    private double finalTotal;

    // Voucher
    private String voucherCode;
    private double voucherDiscount;

    // Trạng thái đơn hàng
    private String status; // pending, confirmed, preparing, shipping, delivered, cancelled

    // ===== THỜI GIAN DƯỚI DẠNG DATE =====
    private Date createdAt;
    private Date updatedAt;
    private Date deliveredAt;
    private Date cancelledAt;
    private Date ratedAt;
    // ====================================

    // Thanh toán
    private String paymentMethod; // COD, Banking, Momo
    private String paymentStatus; // pending, paid

    // Đánh giá
    private double rating;
    private String review;
    private boolean isRated;

    // Ghi chú
    private String orderNote;

    // Thông tin giao hàng
    private String deliveryAddress;
    private String deliveryPhone;
    private String deliveryName;

    // Cho phép xóa lịch sử
    private boolean isDeletable = true;

    public Order() {}

    // ==================== GETTERS ====================
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
    public String getVoucherCode() { return voucherCode; }
    public double getVoucherDiscount() { return voucherDiscount; }
    public String getStatus() { return status; }

    // ===== GETTERS CHO DATE =====
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public Date getDeliveredAt() { return deliveredAt; }
    public Date getCancelledAt() { return cancelledAt; }
    public Date getRatedAt() { return ratedAt; }
    // ============================

    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public double getRating() { return rating; }
    public String getReview() { return review; }
    public boolean isRated() { return isRated; }
    public String getOrderNote() { return orderNote; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getDeliveryPhone() { return deliveryPhone; }
    public String getDeliveryName() { return deliveryName; }
    public boolean isDeletable() { return isDeletable; }

    // Helper methods - Giữ lại để tương thích với code cũ
    public long getCreatedAtLong() {
        return createdAt != null ? createdAt.getTime() : 0;
    }
    public long getDeliveredAtLong() {
        return deliveredAt != null ? deliveredAt.getTime() : 0;
    }
    public long getCancelledAtLong() {
        return cancelledAt != null ? cancelledAt.getTime() : 0;
    }

    public double getTotalPrice() { return subtotal; }
    public double getFinalTotalPrice() { return finalTotal; }

    // ==================== SETTERS ====================
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
    public void setTotalPrice(double totalPrice) { this.subtotal = totalPrice; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    public void setVoucherDiscount(double voucherDiscount) { this.voucherDiscount = voucherDiscount; }
    public void setStatus(String status) { this.status = status; }

    // ===== SETTERS CHO DATE =====
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public void setDeliveredAt(Date deliveredAt) { this.deliveredAt = deliveredAt; }
    public void setCancelledAt(Date cancelledAt) { this.cancelledAt = cancelledAt; }
    public void setRatedAt(Date ratedAt) { this.ratedAt = ratedAt; }
    // ============================

    // Setter cho long (tương thích ngược)
    public void setCreatedAt(long createdAt) {
        this.createdAt = new Date(createdAt);
    }
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = new Date(updatedAt);
    }
    public void setDeliveredAt(long deliveredAt) {
        this.deliveredAt = deliveredAt > 0 ? new Date(deliveredAt) : null;
    }
    public void setCancelledAt(long cancelledAt) {
        this.cancelledAt = cancelledAt > 0 ? new Date(cancelledAt) : null;
    }
    public void setRatedAt(long ratedAt) {
        this.ratedAt = ratedAt > 0 ? new Date(ratedAt) : null;
    }

    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setRating(double rating) { this.rating = rating; }
    public void setReview(String review) { this.review = review; }
    public void setRated(boolean rated) { isRated = rated; }
    public void setOrderNote(String orderNote) { this.orderNote = orderNote; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public void setDeliveryPhone(String deliveryPhone) { this.deliveryPhone = deliveryPhone; }
    public void setDeliveryName(String deliveryName) { this.deliveryName = deliveryName; }
    public void setDeletable(boolean deletable) { isDeletable = deletable; }

    // ==================== TRẠNG THÁI ====================

    public String getStatusText() {
        switch (status) {
            case "pending": return "⏳ Chờ xác nhận";
            case "confirmed": return "✅ Đã xác nhận";
            case "preparing": return "🍳 Đang chuẩn bị";
            case "shipping": return "🚚 Đang giao hàng";
            case "delivered": return "📦 Đã giao thành công";
            case "cancelled": return "❌ Đã hủy";
            default: return status;
        }
    }

    public int getStatusColor() {
        switch (status) {
            case "pending": return 0xFFFF9800;
            case "confirmed": return 0xFF2196F3;
            case "preparing": return 0xFF9C27B0;
            case "shipping": return 0xFF00BCD4;
            case "delivered": return 0xFF4CAF50;
            case "cancelled": return 0xFFF44336;
            default: return 0xFF757575;
        }
    }

    public boolean isCancellable() {
        return "pending".equals(status) || "confirmed".equals(status);
    }

    public boolean canRate() {
        return "delivered".equals(status) && !isRated;
    }

    public boolean canReorder() {
        return "delivered".equals(status);
    }

    public String getOrderSummary() {
        StringBuilder sb = new StringBuilder();
        if (items != null) {
            for (int i = 0; i < Math.min(3, items.size()); i++) {
                Map<String, Object> item = items.get(i);
                String name = (String) item.get("name");
                long quantity = ((Number) item.get("quantity")).longValue();
                if (i > 0) sb.append(", ");
                sb.append(name).append(" x").append(quantity);
            }
            if (items.size() > 3) {
                sb.append("... +").append(items.size() - 3).append(" món");
            }
        }
        return sb.toString();
    }

    public String getPaymentMethodText() {
        switch (paymentMethod) {
            case "COD": return "💵 Thanh toán khi nhận hàng";
            case "Banking": return "🏦 Chuyển khoản ngân hàng";
            case "Momo": return "📱 Ví MoMo";
            default: return paymentMethod;
        }
    }
}