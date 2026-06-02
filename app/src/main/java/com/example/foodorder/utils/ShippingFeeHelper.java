package com.example.foodorder.utils;

import java.util.HashMap;
import java.util.Map;

public class ShippingFeeHelper {

    // Phí giao hàng theo quận/huyện tại Hà Nội
    private static final Map<String, Integer> SHIPPING_FEE_MAP = new HashMap<>();

    static {
        // Các quận trung tâm - phí thấp
        SHIPPING_FEE_MAP.put("Ba Đình", 15000);
        SHIPPING_FEE_MAP.put("Hoàn Kiếm", 15000);
        SHIPPING_FEE_MAP.put("Hai Bà Trưng", 15000);
        SHIPPING_FEE_MAP.put("Đống Đa", 15000);
        SHIPPING_FEE_MAP.put("Cầu Giấy", 15000);
        SHIPPING_FEE_MAP.put("Thanh Xuân", 15000);
        SHIPPING_FEE_MAP.put("Tây Hồ", 15000);

        // Các quận gần trung tâm - phí trung bình
        SHIPPING_FEE_MAP.put("Long Biên", 18000);
        SHIPPING_FEE_MAP.put("Hoàng Mai", 18000);
        SHIPPING_FEE_MAP.put("Bắc Từ Liêm", 20000);
        SHIPPING_FEE_MAP.put("Nam Từ Liêm", 20000);
        SHIPPING_FEE_MAP.put("Hà Đông", 20000);

        // Các huyện xa trung tâm - phí cao
        SHIPPING_FEE_MAP.put("Thanh Trì", 25000);
        SHIPPING_FEE_MAP.put("Gia Lâm", 30000);
        SHIPPING_FEE_MAP.put("Đông Anh", 35000);
        SHIPPING_FEE_MAP.put("Sóc Sơn", 40000);
        SHIPPING_FEE_MAP.put("Mê Linh", 40000);
        SHIPPING_FEE_MAP.put("Thường Tín", 35000);
        SHIPPING_FEE_MAP.put("Phú Xuyên", 40000);
        SHIPPING_FEE_MAP.put("Ứng Hòa", 45000);
        SHIPPING_FEE_MAP.put("Mỹ Đức", 50000);
        SHIPPING_FEE_MAP.put("Chương Mỹ", 40000);
        SHIPPING_FEE_MAP.put("Thanh Oai", 35000);
        SHIPPING_FEE_MAP.put("Thạch Thất", 45000);
        SHIPPING_FEE_MAP.put("Quốc Oai", 45000);
        SHIPPING_FEE_MAP.put("Hoài Đức", 25000);
        SHIPPING_FEE_MAP.put("Đan Phượng", 30000);
        SHIPPING_FEE_MAP.put("Phúc Thọ", 45000);
        SHIPPING_FEE_MAP.put("Ba Vì", 55000);
    }

    // Phí mặc định
    private static final int DEFAULT_FEE = 20000;

    /**
     * Lấy phí giao hàng dựa trên địa chỉ
     * @param address Địa chỉ đầy đủ
     * @return Phí giao hàng (VNĐ)
     */
    public static int getShippingFee(String address) {
        if (address == null || address.isEmpty()) {
            return DEFAULT_FEE;
        }

        // Tìm tên quận/huyện trong địa chỉ
        for (Map.Entry<String, Integer> entry : SHIPPING_FEE_MAP.entrySet()) {
            if (address.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Nếu không tìm thấy quận, thử tìm theo từ khóa Hà Nội
        if (address.contains("Hà Nội") || address.contains("Hanoi")) {
            return DEFAULT_FEE;
        }

        return DEFAULT_FEE;
    }

    /**
     * Lấy phí giao hàng dựa trên tên quận
     * @param district Tên quận/huyện
     * @return Phí giao hàng (VNĐ)
     */
    public static int getShippingFeeByDistrict(String district) {
        if (district == null || district.isEmpty()) {
            return DEFAULT_FEE;
        }

        for (Map.Entry<String, Integer> entry : SHIPPING_FEE_MAP.entrySet()) {
            if (district.contains(entry.getKey()) || entry.getKey().contains(district)) {
                return entry.getValue();
            }
        }

        return DEFAULT_FEE;
    }

    /**
     * Lấy phí giao hàng mặc định
     * @return Phí mặc định
     */
    public static int getDefaultFee() {
        return DEFAULT_FEE;
    }
}