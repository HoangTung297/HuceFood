package com.example.foodorder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.foodorder.model.Food;
import com.example.foodorder.model.Order;
import com.example.foodorder.model.Voucher;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CacheManager {
    private static final String PREF_NAME = "food_order_cache";
    private static final String KEY_FOODS = "cached_foods";
    private static final String KEY_VOUCHERS = "cached_vouchers";
    private static final String KEY_ORDERS = "cached_orders";
    private static final String KEY_CACHE_TIME = "cache_time";

    private static final long CACHE_DURATION = TimeUnit.MINUTES.toMillis(5);

    private final SharedPreferences prefs;
    private final Gson gson;

    public CacheManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    private boolean isCacheValid() {
        long cacheTime = prefs.getLong(KEY_CACHE_TIME, 0);
        return System.currentTimeMillis() - cacheTime < CACHE_DURATION;
    }

    // ==================== FOODS ====================
    public void cacheFoods(List<Food> foods) {
        if (foods == null) return;
        String json = gson.toJson(foods);
        prefs.edit().putString(KEY_FOODS, json).apply();
        prefs.edit().putLong(KEY_CACHE_TIME, System.currentTimeMillis()).apply();
    }

    public List<Food> getCachedFoods() {
        if (!isCacheValid()) return null;
        String json = prefs.getString(KEY_FOODS, null);
        if (json == null) return null;
        try {
            Type type = new TypeToken<List<Food>>(){}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== VOUCHERS ====================
    public void cacheVouchers(List<Voucher> vouchers) {
        if (vouchers == null) return;
        String json = gson.toJson(vouchers);
        prefs.edit().putString(KEY_VOUCHERS, json).apply();
    }

    public List<Voucher> getCachedVouchers() {
        String json = prefs.getString(KEY_VOUCHERS, null);
        if (json == null) return null;
        try {
            Type type = new TypeToken<List<Voucher>>(){}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== ORDERS ====================
    public void cacheOrders(List<Order> orders) {
        if (orders == null) return;
        String json = gson.toJson(orders);
        prefs.edit().putString(KEY_ORDERS, json).apply();
    }



    public List<Order> getCachedOrders() {
        String json = prefs.getString(KEY_ORDERS, null);
        if (json == null) return null;
        try {
            Type type = new TypeToken<List<Order>>(){}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== CLEAR ====================
    public void clearCache() {
        prefs.edit().clear().apply();
    }
}