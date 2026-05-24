package com.example.foodorder.repository;

import android.util.Log;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Food;
import com.example.foodorder.model.Order;
import com.example.foodorder.model.Rating;
import com.example.foodorder.model.Restaurant;
import com.example.foodorder.model.Voucher;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseRepository {
    private static FirebaseRepository instance;
    private final FirebaseFirestore db;
    private static final String TAG = "FirebaseRepo";

    private FirebaseRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseRepository getInstance() {
        if (instance == null) {
            instance = new FirebaseRepository();
        }
        return instance;
    }

    // ==================== CART ====================
    public void getCart(String userId, OnDataLoaded<List<CartItem>> callback) {
        db.collection("carts").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) documentSnapshot.get("items");
                        List<CartItem> cartItems = new ArrayList<>();
                        if (itemsMap != null) {
                            for (Map<String, Object> map : itemsMap) {
                                CartItem item = new CartItem();
                                item.setFoodId((String) map.get("foodId"));
                                item.setName((String) map.get("name"));
                                item.setPrice(((Number) map.get("price")).doubleValue());
                                item.setQuantity(((Number) map.get("quantity")).intValue());
                                item.setRestaurantId((String) map.get("restaurantId"));
                                item.setImageUrl((String) map.get("imageUrl"));
                                item.setNote((String) map.get("note"));
                                cartItems.add(item);
                            }
                        }
                        callback.onSuccess(cartItems);
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void addToCart(String userId, CartItem item, OnDataLoaded<Void> callback) {
        getCart(userId, new OnDataLoaded<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> cartItems) {
                boolean found = false;
                for (CartItem existing : cartItems) {
                    if (existing.getFoodId().equals(item.getFoodId())) {
                        existing.setQuantity(existing.getQuantity() + item.getQuantity());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    cartItems.add(item);
                }
                saveCart(userId, cartItems, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void updateCartItem(String userId, CartItem item, OnDataLoaded<Void> callback) {
        getCart(userId, new OnDataLoaded<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> cartItems) {
                for (int i = 0; i < cartItems.size(); i++) {
                    if (cartItems.get(i).getFoodId().equals(item.getFoodId())) {
                        cartItems.set(i, item);
                        break;
                    }
                }
                saveCart(userId, cartItems, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void removeFromCart(String userId, String foodId, OnDataLoaded<Void> callback) {
        getCart(userId, new OnDataLoaded<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> cartItems) {
                cartItems.removeIf(item -> item.getFoodId().equals(foodId));
                saveCart(userId, cartItems, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void clearCart(String userId, OnDataLoaded<Void> callback) {
        saveCart(userId, new ArrayList<>(), callback);
    }

    private void saveCart(String userId, List<CartItem> cartItems, OnDataLoaded<Void> callback) {
        List<Map<String, Object>> itemsMap = new ArrayList<>();
        for (CartItem item : cartItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("foodId", item.getFoodId());
            map.put("name", item.getName());
            map.put("price", item.getPrice());
            map.put("quantity", item.getQuantity());
            map.put("restaurantId", item.getRestaurantId());
            map.put("imageUrl", item.getImageUrl());
            map.put("note", item.getNote());
            itemsMap.add(map);
        }

        Map<String, Object> cartData = new HashMap<>();
        cartData.put("userId", userId);
        cartData.put("items", itemsMap);
        cartData.put("updatedAt", System.currentTimeMillis());

        db.collection("carts").document(userId).set(cartData)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== ORDERS ====================
    public void createOrder(Order order, OnDataLoaded<String> callback) {
        order.setCreatedAt(System.currentTimeMillis());
        order.setStatus("pending");
        order.setUpdatedAt(System.currentTimeMillis());

        db.collection("orders").add(order)
                .addOnSuccessListener(documentReference -> {
                    order.setId(documentReference.getId());
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getUserOrders(String userId, OnDataLoaded<List<Order>> callback) {
        db.collection("orders")
                .whereEqualTo("userId", userId)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = new Order();
                        order.setId(doc.getId());
                        order.setUserId(doc.getString("userId"));
                        order.setOrderCode(doc.getString("orderCode"));
                        order.setRestaurantId(doc.getString("restaurantId"));
                        order.setRestaurantName(doc.getString("restaurantName"));
                        order.setItems((List<Map<String, Object>>) doc.get("items"));
                        order.setSubtotal(getDouble(doc, "subtotal"));
                        order.setDeliveryFee(getDouble(doc, "deliveryFee"));
                        order.setDiscount(getDouble(doc, "discount"));
                        order.setFinalTotal(getDouble(doc, "finalTotal"));
                        order.setStatus(doc.getString("status"));
                        order.setPaymentMethod(doc.getString("paymentMethod"));
                        order.setPaymentStatus(doc.getString("paymentStatus"));
                        order.setOrderNote(doc.getString("orderNote"));
                        order.setVoucherCode(doc.getString("voucherCode"));
                        order.setVoucherDiscount(getDouble(doc, "voucherDiscount"));
                        order.setRating(getDouble(doc, "rating"));
                        order.setReview(doc.getString("review"));
                        order.setRated(doc.getBoolean("isRated") != null && doc.getBoolean("isRated"));
                        order.setCreatedAt(getLong(doc, "createdAt"));
                        order.setUpdatedAt(getLong(doc, "updatedAt"));
                        order.setDeliveredAt(getLong(doc, "deliveredAt"));
                        order.setCancelledAt(getLong(doc, "cancelledAt"));
                        order.setRatedAt(getLong(doc, "ratedAt"));
                        orders.add(order);
                    }
                    Collections.sort(orders, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getOrdersByStatus(String userId, String status, OnDataLoaded<List<Order>> callback) {
        db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", status)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = new Order();
                        order.setId(doc.getId());
                        order.setUserId(doc.getString("userId"));
                        order.setOrderCode(doc.getString("orderCode"));
                        order.setRestaurantId(doc.getString("restaurantId"));
                        order.setRestaurantName(doc.getString("restaurantName"));
                        order.setItems((List<Map<String, Object>>) doc.get("items"));
                        order.setSubtotal(getDouble(doc, "subtotal"));
                        order.setDeliveryFee(getDouble(doc, "deliveryFee"));
                        order.setDiscount(getDouble(doc, "discount"));
                        order.setFinalTotal(getDouble(doc, "finalTotal"));
                        order.setStatus(doc.getString("status"));
                        order.setPaymentMethod(doc.getString("paymentMethod"));
                        order.setPaymentStatus(doc.getString("paymentStatus"));
                        order.setOrderNote(doc.getString("orderNote"));
                        order.setVoucherCode(doc.getString("voucherCode"));
                        order.setVoucherDiscount(getDouble(doc, "voucherDiscount"));
                        order.setRating(getDouble(doc, "rating"));
                        order.setReview(doc.getString("review"));
                        order.setRated(doc.getBoolean("isRated") != null && doc.getBoolean("isRated"));
                        order.setCreatedAt(getLong(doc, "createdAt"));
                        order.setUpdatedAt(getLong(doc, "updatedAt"));
                        order.setDeliveredAt(getLong(doc, "deliveredAt"));
                        order.setCancelledAt(getLong(doc, "cancelledAt"));
                        order.setRatedAt(getLong(doc, "ratedAt"));
                        orders.add(order);
                    }
                    Collections.sort(orders, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void cancelOrder(String orderId, OnDataLoaded<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "cancelled");
        updates.put("cancelledAt", System.currentTimeMillis());
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("orders").document(orderId).update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void deleteOrder(String orderId, OnDataLoaded<Void> callback) {
        db.collection("orders").document(orderId).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateOrderStatus(String orderId, String status, OnDataLoaded<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", System.currentTimeMillis());

        if ("delivered".equals(status)) {
            updates.put("deliveredAt", System.currentTimeMillis());
        } else if ("cancelled".equals(status)) {
            updates.put("cancelledAt", System.currentTimeMillis());
        }

        db.collection("orders").document(orderId).update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void updateOrderRating(String orderId, double rating, String review, OnDataLoaded<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("rating", rating);
        updates.put("review", review);
        updates.put("isRated", true);
        updates.put("ratedAt", System.currentTimeMillis());

        db.collection("orders").document(orderId).update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== RATINGS ====================
    public void addRating(Rating rating, OnDataLoaded<Void> callback) {
        rating.setCreatedAt(System.currentTimeMillis());
        db.collection("ratings").add(rating)
                .addOnSuccessListener(documentReference -> {
                    rating.setId(documentReference.getId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getRatingsByOrder(String orderId, OnDataLoaded<List<Rating>> callback) {
        db.collection("ratings")
                .whereEqualTo("orderId", orderId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Rating> ratings = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Rating rating = doc.toObject(Rating.class);
                        rating.setId(doc.getId());
                        ratings.add(rating);
                    }
                    callback.onSuccess(ratings);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== RESTAURANTS ====================
    public void getAllRestaurants(OnDataLoaded<List<Restaurant>> callback) {
        db.collection("restaurants").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Restaurant> restaurants = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Restaurant restaurant = doc.toObject(Restaurant.class);
                        restaurant.setId(doc.getId());
                        restaurants.add(restaurant);
                    }
                    callback.onSuccess(restaurants);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getRestaurantById(String restaurantId, OnDataLoaded<Restaurant> callback) {
        db.collection("restaurants").document(restaurantId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                        restaurant.setId(documentSnapshot.getId());
                        callback.onSuccess(restaurant);
                    } else {
                        callback.onError("Restaurant not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== VOUCHERS ====================
    public void getAllVouchers(OnDataLoaded<List<Voucher>> callback) {
        db.collection("vouchers")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Voucher> vouchers = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Voucher voucher = doc.toObject(Voucher.class);
                        voucher.setId(doc.getId());
                        vouchers.add(voucher);
                    }
                    callback.onSuccess(vouchers);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getVoucherByCode(String code, OnDataLoaded<Voucher> callback) {
        db.collection("vouchers")
                .whereEqualTo("code", code)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Voucher voucher = queryDocumentSnapshots.getDocuments().get(0).toObject(Voucher.class);
                        voucher.setId(queryDocumentSnapshots.getDocuments().get(0).getId());
                        callback.onSuccess(voucher);
                    } else {
                        callback.onError("Voucher not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== HELPER METHODS ====================
    private double getDouble(QueryDocumentSnapshot doc, String field) {
        Object value = doc.get(field);
        if (value == null) return 0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        return 0;
    }

    private long getLong(QueryDocumentSnapshot doc, String field) {
        Object value = doc.get(field);
        if (value == null) return 0;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Double) return ((Double) value).longValue();
        return 0;
    }

    // ==================== INTERFACE ====================
    public interface OnDataLoaded<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}