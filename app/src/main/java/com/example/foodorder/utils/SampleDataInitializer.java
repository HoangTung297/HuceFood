package com.example.foodorder.utils;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SampleDataInitializer {

    private static boolean isInitialized = false;

    public static void initSamplePromotions(Context context, String userId) {
        if (isInitialized) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Kiểm tra đã có khuyến mãi mẫu chưa
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "promotion")
                .whereEqualTo("isSample", true)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        createSamplePromotions(db, userId);
                        if (context != null) {
                            Toast.makeText(context, "Đã thêm khuyến mãi mẫu", Toast.LENGTH_SHORT).show();
                        }
                    }
                    isInitialized = true;
                });
    }

    private static void createSamplePromotions(FirebaseFirestore db, String userId) {
        // 1. Khuyến mãi giảm 50%
        Map<String, Object> promo1 = new HashMap<>();
        promo1.put("userId", userId);
        promo1.put("title", "🎉 SIÊU SALE 50%");
        promo1.put("message", "Giảm 50% cho tất cả đơn hàng từ 100.000đ. Mã: SALE50. Áp dụng đến hết tháng này!");
        promo1.put("type", "promotion");
        promo1.put("createdAt", System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L);
        promo1.put("isRead", false);
        promo1.put("isSample", true);

        // 2. Freeship
        Map<String, Object> promo2 = new HashMap<>();
        promo2.put("userId", userId);
        promo2.put("title", "🚀 FREESHIP TOÀN QUỐC");
        promo2.put("message", "Freeship cho mọi đơn hàng từ 50.000đ. Không giới hạn số lượng đơn!");
        promo2.put("type", "promotion");
        promo2.put("createdAt", System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L);
        promo2.put("isRead", false);
        promo2.put("isSample", true);

        // 3. Mua 1 tặng 1
        Map<String, Object> promo3 = new HashMap<>();
        promo3.put("userId", userId);
        promo3.put("title", "🎁 MUA 1 TẶNG 1");
        promo3.put("message", "Mua 1 phần Gà rán, tặng 1 Pepsi. Áp dụng đến hết tuần này!");
        promo3.put("type", "promotion");
        promo3.put("createdAt", System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L);
        promo3.put("isRead", false);
        promo3.put("isSample", true);

        // 4. Giảm 20k
        Map<String, Object> promo4 = new HashMap<>();
        promo4.put("userId", userId);
        promo4.put("title", "💰 GIẢM 20.000Đ");
        promo4.put("message", "Giảm 20.000đ cho đơn hàng từ 80.000đ. Mã: SAVE20");
        promo4.put("type", "promotion");
        promo4.put("createdAt", System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L);
        promo4.put("isRead", false);
        promo4.put("isSample", true);

        // 5. Sinh nhật
        Map<String, Object> promo5 = new HashMap<>();
        promo5.put("userId", userId);
        promo5.put("title", "🎂 ƯU ĐÃI SINH NHẬT");
        promo5.put("message", "Chúc mừng sinh nhật! Nhận ngay voucher 50.000đ cho đơn hàng tiếp theo.");
        promo5.put("type", "promotion");
        promo5.put("createdAt", System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L);
        promo5.put("isRead", false);
        promo5.put("isSample", true);

        db.collection("notifications").add(promo1);
        db.collection("notifications").add(promo2);
        db.collection("notifications").add(promo3);
        db.collection("notifications").add(promo4);
        db.collection("notifications").add(promo5);
    }
}