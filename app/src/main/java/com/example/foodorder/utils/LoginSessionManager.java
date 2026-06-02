package com.example.foodorder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class LoginSessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_ADDRESS = "userAddress";
    private static final String KEY_DELIVERY_ADDRESS = "deliveryAddress";
    private static final String TAG = "SessionManager";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public LoginSessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Method với 5 tham số (đầy đủ)
    public void createLoginSession(String userId, String email, String userName, String phone, String address) {
        Log.d(TAG, "Creating session - userId: " + userId + ", email: " + email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_USER_ADDRESS, address);
        editor.putString(KEY_DELIVERY_ADDRESS, address);
        editor.apply();
    }

    // Method với 3 tham số (overload - dùng khi không có phone và address)
    public void createLoginSession(String userId, String email, String userName) {
        Log.d(TAG, "Creating session (3 params) - userId: " + userId + ", email: " + email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_PHONE, "");
        editor.putString(KEY_USER_ADDRESS, "");
        editor.putString(KEY_DELIVERY_ADDRESS, "");
        editor.apply();
    }

    public void updateUserInfo(String name, String phone, String address) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_USER_ADDRESS, address);
        editor.putString(KEY_DELIVERY_ADDRESS, address);
        editor.apply();
        Log.d(TAG, "Updated user info");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public String getUserPhone() {
        return pref.getString(KEY_USER_PHONE, "");
    }

    public String getUserAddress() {
        return pref.getString(KEY_USER_ADDRESS, "");
    }

    public String getDeliveryAddress() {
        return pref.getString(KEY_DELIVERY_ADDRESS, "");
    }

    public void logout() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "Session cleared");
    }
}