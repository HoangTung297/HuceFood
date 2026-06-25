package com.example.foodorder.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.foodorder.model.BankAccount;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Food;
import com.example.foodorder.model.Order;
import com.example.foodorder.model.Rating;
import com.example.foodorder.model.Restaurant;
import com.example.foodorder.model.Voucher;
import com.example.foodorder.model.Wallet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
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
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
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
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    // ==================== ORDERS ====================
    public void createOrder(Order order, OnDataLoaded<String> callback) {
        Date now = new Date();
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(now);
        }
        if (order.getUpdatedAt() == null) {
            order.setUpdatedAt(now);
        }

        // DÙNG ORDER CODE LÀM ID DOCUMENT
        String orderCode = order.getOrderCode();
        if (orderCode == null || orderCode.isEmpty()) {
            orderCode = "ORD" + System.currentTimeMillis();
            order.setOrderCode(orderCode);
        }
        final String finalOrderId = orderCode;

        Log.d(TAG, "========== TẠO ĐƠN HÀNG ==========");
        Log.d(TAG, "📌 Order ID: " + finalOrderId);
        Log.d(TAG, "Status: " + order.getStatus());
        Log.d(TAG, "📦 Delivery Name: " + order.getDeliveryName());
        Log.d(TAG, "📞 Delivery Phone: " + order.getDeliveryPhone());
        Log.d(TAG, "📍 Delivery Address: " + order.getDeliveryAddress());
        Log.d(TAG, "==================================");

        Map<String, Object> orderData = new HashMap<>();

        orderData.put("userId", order.getUserId());
        orderData.put("orderCode", order.getOrderCode());
        orderData.put("restaurantId", order.getRestaurantId());
        orderData.put("restaurantName", order.getRestaurantName());
        orderData.put("items", order.getItems());
        orderData.put("subtotal", order.getSubtotal());
        orderData.put("deliveryFee", order.getDeliveryFee());
        orderData.put("discount", order.getDiscount());
        orderData.put("finalTotal", order.getFinalTotal());
        orderData.put("status", order.getStatus());

        // ===== LƯU DƯỚI DẠNG DATE =====
        orderData.put("createdAt", order.getCreatedAt());
        orderData.put("updatedAt", order.getUpdatedAt());
        // ==============================

        orderData.put("paymentMethod", order.getPaymentMethod());
        orderData.put("paymentStatus", order.getPaymentStatus());
        orderData.put("orderNote", order.getOrderNote() != null ? order.getOrderNote() : "");
        orderData.put("deliveryName", order.getDeliveryName() != null ? order.getDeliveryName() : "");
        orderData.put("deliveryPhone", order.getDeliveryPhone() != null ? order.getDeliveryPhone() : "");
        orderData.put("deliveryAddress", order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "");
        orderData.put("voucherCode", order.getVoucherCode() != null ? order.getVoucherCode() : "");
        orderData.put("voucherDiscount", order.getVoucherDiscount());
        orderData.put("deliveredAt", null);
        orderData.put("cancelledAt", null);
        orderData.put("rating", 0.0);
        orderData.put("review", "");
        orderData.put("isRated", false);
        orderData.put("ratedAt", null);
        orderData.put("paymentTransactionId", "");
        orderData.put("statusText", order.getStatusText() != null ? order.getStatusText() : "");
        orderData.put("statusColor", order.getStatusColor());
        orderData.put("orderSummary", order.getOrderSummary() != null ? order.getOrderSummary() : "");

        db.collection("orders").document(finalOrderId).set(orderData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "✅ Order created with ID: " + finalOrderId);
                        order.setId(finalOrderId);
                        callback.onSuccess(finalOrderId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "❌ Error creating order: " + e.getMessage());
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void getOrdersByStatus(String userId, String status, OnDataLoaded<List<Order>> callback) {
        Log.d(TAG, "Getting orders - userId: " + userId + ", status: " + status);

        db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Order order = convertQueryDocumentToOrder(doc);
                            orders.add(order);
                        }
                        orders.sort((a, b) -> {
                            if (a.getCreatedAt() == null) return 1;
                            if (b.getCreatedAt() == null) return -1;
                            return b.getCreatedAt().compareTo(a.getCreatedAt());
                        });
                        Log.d(TAG, "Found " + orders.size() + " orders with status: " + status);
                        callback.onSuccess(orders);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error: " + e.getMessage());
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void getUserOrders(String userId, OnDataLoaded<List<Order>> callback) {
        db.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Order order = convertQueryDocumentToOrder(doc);
                            orders.add(order);
                        }
                        orders.sort((a, b) -> {
                            if (a.getCreatedAt() == null) return 1;
                            if (b.getCreatedAt() == null) return -1;
                            return b.getCreatedAt().compareTo(a.getCreatedAt());
                        });
                        callback.onSuccess(orders);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    // ==================== GET ORDER BY ID ====================
    public void getOrderById(String orderId, OnDataLoaded<Order> callback) {
        Log.d(TAG, "Getting order by ID: " + orderId);

        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Order order = convertDocumentSnapshotToOrder(documentSnapshot);
                            Log.d(TAG, "Order found: " + order.getOrderCode() + " - " + order.getRestaurantName());
                            callback.onSuccess(order);
                        } else {
                            Log.e(TAG, "Order not found with ID: " + orderId);
                            callback.onError("Order not found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error getting order: " + e.getMessage());
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void updateOrderStatus(String orderId, String status, OnDataLoaded<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", new Date());

        db.collection("orders").document(orderId).update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Order status updated to: " + status);
                        if (callback != null) callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error updating status: " + e.getMessage());
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    public void cancelOrder(String orderId, OnDataLoaded<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "cancelled");
        updates.put("cancelledAt", new Date());
        updates.put("updatedAt", new Date());

        db.collection("orders").document(orderId).update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Order cancelled: " + orderId);
                        if (callback != null) callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error cancelling order: " + e.getMessage());
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    public void deleteOrder(String orderId, OnDataLoaded<Void> callback) {
        db.collection("orders").document(orderId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Order deleted: " + orderId);
                        if (callback != null) callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error deleting order: " + e.getMessage());
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    public void updateOrderRating(String orderId, double rating, String review, OnDataLoaded<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("rating", rating);
        updates.put("review", review);
        updates.put("isRated", true);
        updates.put("ratedAt", new Date());

        db.collection("orders").document(orderId).update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Order rating updated for: " + orderId);
                        if (callback != null) callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error updating rating: " + e.getMessage());
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    // ==================== CONVERT METHODS ====================
    private Order convertQueryDocumentToOrder(QueryDocumentSnapshot doc) {
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

        // ===== XỬ LÝ CẢ 2 TRƯỜNG HỢP: Date VÀ Long =====
        Object createdAtObj = doc.get("createdAt");
        if (createdAtObj instanceof Date) {
            order.setCreatedAt((Date) createdAtObj);
        } else if (createdAtObj instanceof Long) {
            order.setCreatedAt(new Date((Long) createdAtObj));
        } else {
            order.setCreatedAt(null);
        }

        Object updatedAtObj = doc.get("updatedAt");
        if (updatedAtObj instanceof Date) {
            order.setUpdatedAt((Date) updatedAtObj);
        } else if (updatedAtObj instanceof Long) {
            order.setUpdatedAt(new Date((Long) updatedAtObj));
        } else {
            order.setUpdatedAt(null);
        }

        Object deliveredAtObj = doc.get("deliveredAt");
        if (deliveredAtObj instanceof Date) {
            order.setDeliveredAt((Date) deliveredAtObj);
        } else if (deliveredAtObj instanceof Long) {
            Long value = (Long) deliveredAtObj;
            order.setDeliveredAt(value > 0 ? new Date(value) : null);
        } else {
            order.setDeliveredAt(null);
        }

        Object cancelledAtObj = doc.get("cancelledAt");
        if (cancelledAtObj instanceof Date) {
            order.setCancelledAt((Date) cancelledAtObj);
        } else if (cancelledAtObj instanceof Long) {
            Long value = (Long) cancelledAtObj;
            order.setCancelledAt(value > 0 ? new Date(value) : null);
        } else {
            order.setCancelledAt(null);
        }

        Object ratedAtObj = doc.get("ratedAt");
        if (ratedAtObj instanceof Date) {
            order.setRatedAt((Date) ratedAtObj);
        } else if (ratedAtObj instanceof Long) {
            Long value = (Long) ratedAtObj;
            order.setRatedAt(value > 0 ? new Date(value) : null);
        } else {
            order.setRatedAt(null);
        }
        // ===================================================

        order.setVoucherCode(doc.getString("voucherCode"));
        order.setVoucherDiscount(getDouble(doc, "voucherDiscount"));
        order.setRating(getDouble(doc, "rating"));
        order.setReview(doc.getString("review"));
        Boolean isRated = doc.getBoolean("isRated");
        order.setRated(isRated != null && isRated);
        order.setDeliveryName(doc.getString("deliveryName"));
        order.setDeliveryPhone(doc.getString("deliveryPhone"));
        order.setDeliveryAddress(doc.getString("deliveryAddress"));
        return order;
    }

    private Order convertDocumentSnapshotToOrder(DocumentSnapshot doc) {
        Order order = new Order();
        order.setId(doc.getId());
        order.setUserId(doc.getString("userId"));
        order.setOrderCode(doc.getString("orderCode"));
        order.setRestaurantId(doc.getString("restaurantId"));
        order.setRestaurantName(doc.getString("restaurantName"));
        order.setItems((List<Map<String, Object>>) doc.get("items"));
        order.setSubtotal(getDoubleFromDoc(doc, "subtotal"));
        order.setDeliveryFee(getDoubleFromDoc(doc, "deliveryFee"));
        order.setDiscount(getDoubleFromDoc(doc, "discount"));
        order.setFinalTotal(getDoubleFromDoc(doc, "finalTotal"));
        order.setStatus(doc.getString("status"));
        order.setPaymentMethod(doc.getString("paymentMethod"));
        order.setPaymentStatus(doc.getString("paymentStatus"));
        order.setOrderNote(doc.getString("orderNote"));

        // ===== XỬ LÝ CẢ 2 TRƯỜNG HỢP: Date VÀ Long =====
        Object createdAtObj = doc.get("createdAt");
        if (createdAtObj instanceof Date) {
            order.setCreatedAt((Date) createdAtObj);
        } else if (createdAtObj instanceof Long) {
            order.setCreatedAt(new Date((Long) createdAtObj));
        } else {
            order.setCreatedAt(null);
        }

        Object updatedAtObj = doc.get("updatedAt");
        if (updatedAtObj instanceof Date) {
            order.setUpdatedAt((Date) updatedAtObj);
        } else if (updatedAtObj instanceof Long) {
            order.setUpdatedAt(new Date((Long) updatedAtObj));
        } else {
            order.setUpdatedAt(null);
        }

        Object deliveredAtObj = doc.get("deliveredAt");
        if (deliveredAtObj instanceof Date) {
            order.setDeliveredAt((Date) deliveredAtObj);
        } else if (deliveredAtObj instanceof Long) {
            Long value = (Long) deliveredAtObj;
            order.setDeliveredAt(value > 0 ? new Date(value) : null);
        } else {
            order.setDeliveredAt(null);
        }

        Object cancelledAtObj = doc.get("cancelledAt");
        if (cancelledAtObj instanceof Date) {
            order.setCancelledAt((Date) cancelledAtObj);
        } else if (cancelledAtObj instanceof Long) {
            Long value = (Long) cancelledAtObj;
            order.setCancelledAt(value > 0 ? new Date(value) : null);
        } else {
            order.setCancelledAt(null);
        }

        Object ratedAtObj = doc.get("ratedAt");
        if (ratedAtObj instanceof Date) {
            order.setRatedAt((Date) ratedAtObj);
        } else if (ratedAtObj instanceof Long) {
            Long value = (Long) ratedAtObj;
            order.setRatedAt(value > 0 ? new Date(value) : null);
        } else {
            order.setRatedAt(null);
        }
        // ===================================================

        order.setVoucherCode(doc.getString("voucherCode"));
        order.setVoucherDiscount(getDoubleFromDoc(doc, "voucherDiscount"));
        order.setRating(getDoubleFromDoc(doc, "rating"));
        order.setReview(doc.getString("review"));
        Boolean isRated = doc.getBoolean("isRated");
        order.setRated(isRated != null && isRated);
        order.setDeliveryName(doc.getString("deliveryName"));
        order.setDeliveryPhone(doc.getString("deliveryPhone"));
        order.setDeliveryAddress(doc.getString("deliveryAddress"));
        return order;
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

    private double getDoubleFromDoc(DocumentSnapshot doc, String field) {
        Object value = doc.get(field);
        if (value == null) return 0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        return 0;
    }

    private long getLongFromDoc(DocumentSnapshot doc, String field) {
        Object value = doc.get(field);
        if (value == null) return 0;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Double) return ((Double) value).longValue();
        return 0;
    }

    // ==================== RATINGS ====================
    public void addRating(Rating rating, OnDataLoaded<Void> callback) {
        rating.setCreatedAt(System.currentTimeMillis()); // Dùng long vì Rating dùng long
        db.collection("ratings").add(rating)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        rating.setId(documentReference.getId());
                        if (callback != null) callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    public void getRatingsByOrder(String orderId, OnDataLoaded<List<Rating>> callback) {
        db.collection("ratings")
                .whereEqualTo("orderId", orderId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Rating> ratings = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Rating rating = doc.toObject(Rating.class);
                            rating.setId(doc.getId());
                            ratings.add(rating);
                        }
                        if (callback != null) callback.onSuccess(ratings);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    // ==================== RESTAURANTS ====================
    public void getAllRestaurants(OnDataLoaded<List<Restaurant>> callback) {
        db.collection("restaurants").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Restaurant> restaurants = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Restaurant restaurant = doc.toObject(Restaurant.class);
                            restaurant.setId(doc.getId());
                            restaurants.add(restaurant);
                        }
                        if (callback != null) callback.onSuccess(restaurants);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    public void getRestaurantById(String restaurantId, OnDataLoaded<Restaurant> callback) {
        db.collection("restaurants").document(restaurantId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                            restaurant.setId(documentSnapshot.getId());
                            if (callback != null) callback.onSuccess(restaurant);
                        } else {
                            if (callback != null) callback.onError("Restaurant not found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    // ==================== VOUCHERS ====================
    public void getAllVouchers(OnDataLoaded<List<Voucher>> callback) {
        db.collection("vouchers")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Voucher> vouchers = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Voucher voucher = doc.toObject(Voucher.class);
                            voucher.setId(doc.getId());
                            vouchers.add(voucher);
                        }
                        if (callback != null) callback.onSuccess(vouchers);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    public void getVoucherByCode(String code, OnDataLoaded<Voucher> callback) {
        db.collection("vouchers")
                .whereEqualTo("code", code)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Voucher voucher = queryDocumentSnapshots.getDocuments().get(0).toObject(Voucher.class);
                            voucher.setId(queryDocumentSnapshots.getDocuments().get(0).getId());
                            if (callback != null) callback.onSuccess(voucher);
                        } else {
                            if (callback != null) callback.onError("Voucher not found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    // ==================== WALLET ====================
    public void getWallet(String userId, OnDataLoaded<Wallet> callback) {
        db.collection("wallets").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Wallet wallet = documentSnapshot.toObject(Wallet.class);
                            if (callback != null) callback.onSuccess(wallet);
                        } else {
                            Wallet newWallet = new Wallet();
                            newWallet.setUserId(userId);
                            newWallet.setBalance(0);
                            db.collection("wallets").document(userId).set(newWallet)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            if (callback != null) callback.onSuccess(newWallet);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            if (callback != null) callback.onError(e.getMessage());
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    public void updateWalletBalance(String userId, double newBalance, OnDataLoaded<Void> callback) {
        Log.d(TAG, "Cập nhật số dư ví - userId: " + userId + ", newBalance: " + newBalance);

        Map<String, Object> updates = new HashMap<>();
        updates.put("balance", newBalance);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("wallets").document(userId).update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "✅ Cập nhật số dư ví thành công!");
                        if (callback != null) callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "❌ Lỗi cập nhật số dư ví: " + e.getMessage());
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    // ==================== BANK ACCOUNTS ====================
    public void getLinkedBankAccounts(String userId, OnDataLoaded<List<BankAccount>> callback) {
        Log.d(TAG, "Getting bank accounts for userId: " + userId);

        db.collection("bankAccounts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("linked", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<BankAccount> accounts = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            BankAccount account = doc.toObject(BankAccount.class);
                            account.setId(doc.getId());
                            accounts.add(account);
                        }
                        if (callback != null) callback.onSuccess(accounts);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    public void addBankAccount(BankAccount account, OnDataLoaded<String> callback) {
        Log.d(TAG, "Adding bank account for userId: " + account.getUserId());

        db.collection("bankAccounts").add(account)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (callback != null) callback.onSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    public void deleteBankAccount(String accountId, OnDataLoaded<Void> callback) {
        db.collection("bankAccounts").document(accountId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (callback != null) callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null) callback.onError(e.getMessage());
                    }
                });
    }

    // ==================== INTERFACE ====================
    public interface OnDataLoaded<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}