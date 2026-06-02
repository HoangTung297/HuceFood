package com.example.foodorder.utils;

import com.example.foodorder.model.Order;
import java.util.HashMap;
import java.util.Map;

public class RestaurantHelper {

    private static final Map<String, String> RESTAURANT_NAME_MAP = new HashMap<>();
    static {
        RESTAURANT_NAME_MAP.put("pho_thin", "Phở Thìn");
        RESTAURANT_NAME_MAP.put("pho", "Phở Thìn");
        RESTAURANT_NAME_MAP.put("kfc", "KFC");
        RESTAURANT_NAME_MAP.put("cong_ca_phe", "Cộng Cà Phê");
        RESTAURANT_NAME_MAP.put("cong", "Cộng Cà Phê");
        RESTAURANT_NAME_MAP.put("com_tam", "Cơm Tấm Ba Ghiền");
        RESTAURANT_NAME_MAP.put("pizza_hut", "Pizza Hut");
        RESTAURANT_NAME_MAP.put("pizza", "Pizza Hut");
        RESTAURANT_NAME_MAP.put("lotteria", "Lotteria");
        RESTAURANT_NAME_MAP.put("ding_tea", "Ding Tea");
        RESTAURANT_NAME_MAP.put("mcdonalds", "McDonald's");
        RESTAURANT_NAME_MAP.put("bo_to_quan", "Bò Tơ Quán");
    }

    private static final String[] FOOD_KEYWORDS = {"Phở", "Cà phê", "Cơm", "Pizza", "Burger",
            "Gà", "Trà", "Sữa", "Bánh", "Cháo", "Bún", "Mì", "Khoai", "Coca", "Spaghetti", "Pepsi"};

    public static boolean isFoodName(String name) {
        if (name == null) return true;
        for (String keyword : FOOD_KEYWORDS) {
            if (name.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static String getRestaurantNameFromId(String restaurantId) {
        if (restaurantId == null || restaurantId.isEmpty()) {
            return null;
        }
        String name = RESTAURANT_NAME_MAP.get(restaurantId.toLowerCase());
        if (name != null) return name;

        // Nếu không tìm thấy, trả về null để dùng fallback
        return null;
    }

    public static String getRestaurantNameFromOrder(Order order) {
        if (order == null) return "Nhà hàng";

        // 1. Lấy từ restaurantId của Order
        String restaurantId = order.getRestaurantId();
        if (restaurantId != null && !restaurantId.isEmpty()) {
            String name = getRestaurantNameFromId(restaurantId);
            if (name != null) {
                return name;
            }
        }

        // 2. Lấy từ restaurantName của Order
        String restaurantName = order.getRestaurantName();
        if (restaurantName != null && !restaurantName.isEmpty() && !isFoodName(restaurantName)) {
            return restaurantName;
        }

        // 3. Lấy từ item đầu tiên
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            Map<String, Object> firstItem = order.getItems().get(0);

            String itemRestaurantId = (String) firstItem.get("restaurantId");
            if (itemRestaurantId != null && !itemRestaurantId.isEmpty()) {
                String name = getRestaurantNameFromId(itemRestaurantId);
                if (name != null) {
                    return name;
                }
            }

            String itemRestaurantName = (String) firstItem.get("restaurantName");
            if (itemRestaurantName != null && !itemRestaurantName.isEmpty() && !isFoodName(itemRestaurantName)) {
                return itemRestaurantName;
            }

            String itemName = (String) firstItem.get("name");
            if (itemName != null && !itemName.isEmpty()) {
                itemName = itemName.replaceAll("\\s+x\\d+$", "").trim();
                return itemName;
            }
        }

        return "Nhà hàng";
    }
}