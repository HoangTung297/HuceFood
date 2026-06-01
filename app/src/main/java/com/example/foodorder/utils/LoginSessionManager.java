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
    private static final String TAG = "SessionManager";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public LoginSessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String userId, String email, String userName) {
        Log.d(TAG, "Creating session - userId: " + userId + ", email: " + email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        String userId = pref.getString(KEY_USER_ID, "");
        Log.d(TAG, "getUserId: " + userId);
        return userId;
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public void logout() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "Session cleared");
    }
}