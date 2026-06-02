package com.example.foodorder.utils;

import com.example.foodorder.model.Order;
import java.util.HashMap;
import java.util.Map;

public class RestaurantNameHelper {

    // Map ánh xạ restaurantId -> tên nhà hàng thật
    private static final Map<String, String> RESTAURANT_NAME_MAP = new HashMap<>();

    // Danh sách từ khóa tên món ăn
    private static final String[] FOOD_KEYWORDS = {"Phở", "Cà phê", "Cơm", "Pizza", "Burger",
            "Gà", "Trà", "Sữa", "Bánh", "Cháo", "Bún", "Mì", "Xôi", "Nem", "Chả", "Khoai",
            "Coca", "Pepsi", "Kem", "Sinh tố", "Nước ép", "Lẩu", "Nướng", "Salad", "Sushi",
            "Ramen", "Tonkatsu", "Tempura", "Matcha", "Hamburger", "Cheese", "Chocolate"};

    static {
        // Ánh xạ restaurantId -> tên nhà hàng
        RESTAURANT_NAME_MAP.put("pho_thin", "Phở Thìn");
        RESTAURANT_NAME_MAP.put("pho", "Phở Thìn");
        RESTAURANT_NAME_MAP.put("phothin", "Phở Thìn");
        RESTAURANT_NAME_MAP.put("kfc", "KFC");
        RESTAURANT_NAME_MAP.put("cong_ca_phe", "Cộng Cà Phê");
        RESTAURANT_NAME_MAP.put("cong", "Cộng Cà Phê");
        RESTAURANT_NAME_MAP.put("congca phe", "Cộng Cà Phê");
        RESTAURANT_NAME_MAP.put("com_tam", "Cơm Tấm Ba Ghiền");
        RESTAURANT_NAME_MAP.put("comtam", "Cơm Tấm Ba Ghiền");
        RESTAURANT_NAME_MAP.put("pizza_hut", "Pizza Hut");
        RESTAURANT_NAME_MAP.put("pizza", "Pizza Hut");
        RESTAURANT_NAME_MAP.put("lotteria", "Lotteria");
        RESTAURANT_NAME_MAP.put("ding_tea", "Ding Tea");
        RESTAURANT_NAME_MAP.put("dingtea", "Ding Tea");
        RESTAURANT_NAME_MAP.put("mcdonalds", "McDonald's");
        RESTAURANT_NAME_MAP.put("mcdonald", "McDonald's");
        RESTAURANT_NAME_MAP.put("bo_to_quan", "Bò Tơ Quán");
        RESTAURANT_NAME_MAP.put("botoquan", "Bò Tơ Quán");

        // Ánh xạ trực tiếp từ tên món -> tên nhà hàng (fallback)
        RESTAURANT_NAME_MAP.put("Phở Bò", "Phở Thìn");
        RESTAURANT_NAME_MAP.put("Phở", "Phở Thìn");
        RESTAURANT_NAME_MAP.put("Cà phê sữa đá", "Cộng Cà Phê");
        RESTAURANT_NAME_MAP.put("Cà phê đen", "Cộng Cà Phê");
        RESTAURANT_NAME_MAP.put("Cà phê", "Cộng Cà Phê");
        RESTAURANT_NAME_MAP.put("Cơm Tấm", "Cơm Tấm Ba Ghiền");
        RESTAURANT_NAME_MAP.put("Cơm", "Cơm Tấm Ba Ghiền");
        RESTAURANT_NAME_MAP.put("Pizza Hải Sản", "Pizza Hut");
        RESTAURANT_NAME_MAP.put("Pizza", "Pizza Hut");
        RESTAURANT_NAME_MAP.put("Gà rán", "KFC");
        RESTAURANT_NAME_MAP.put("Gà", "KFC");
        RESTAURANT_NAME_MAP.put("Trà sữa", "Ding Tea");
        RESTAURANT_NAME_MAP.put("Trà", "Ding Tea");
    }

    /**
     * Lấy tên nhà hàng chính xác từ Order
     * @param order Đối tượng Order
     * @return Tên nhà hàng đúng
     */
    public static String getRestaurantName(Order order) {
        if (order == null) return "Nhà hàng";

        // 1. Ưu tiên lấy từ restaurantId
        String restaurantId = order.getRestaurantId();
        if (restaurantId != null && !restaurantId.isEmpty()) {
            String name = RESTAURANT_NAME_MAP.get(restaurantId.toLowerCase());
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }

        // 2. Lấy từ restaurantName
        String restaurantName = order.getRestaurantName();
        if (restaurantName != null && !restaurantName.isEmpty()) {
            // Kiểm tra trong map trước
            String mappedName = RESTAURANT_NAME_MAP.get(restaurantName);
            if (mappedName != null && !mappedName.isEmpty()) {
                return mappedName;
            }
            // Nếu không phải tên món, trả về trực tiếp
            if (!isFoodName(restaurantName)) {
                return restaurantName;
            }
        }

        // 3. Fallback: lấy từ tên món đầu tiên trong items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            Object firstItem = order.getItems().get(0);
            if (firstItem instanceof Map) {
                Map<String, Object> item = (Map<String, Object>) firstItem;
                String itemName = (String) item.get("name");
                if (itemName != null && !itemName.isEmpty()) {
                    // Loại bỏ phần " x1", " x2" ở cuối
                    itemName = itemName.replaceAll("\\s+x\\d+$", "").trim();
                    // Kiểm tra trong map
                    String mappedName = RESTAURANT_NAME_MAP.get(itemName);
                    if (mappedName != null && !mappedName.isEmpty()) {
                        return mappedName;
                    }
                    // Lấy tên món làm tên quán
                    return itemName + " (Quán)";
                }
            }
        }

        return "Nhà hàng";
    }

    /**
     * Lấy tên nhà hàng từ restaurantId
     * @param restaurantId ID nhà hàng
     * @return Tên nhà hàng
     */
    public static String getRestaurantNameById(String restaurantId) {
        if (restaurantId == null || restaurantId.isEmpty()) {
            return "Nhà hàng";
        }
        String name = RESTAURANT_NAME_MAP.get(restaurantId.toLowerCase());
        return name != null ? name : "Nhà hàng";
    }

    /**
     * Kiểm tra xem chuỗi có phải là tên món ăn không
     * @param name Tên cần kiểm tra
     * @return true nếu là tên món ăn
     */
    private static boolean isFoodName(String name) {
        if (name == null) return true;
        for (String keyword : FOOD_KEYWORDS) {
            if (name.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}