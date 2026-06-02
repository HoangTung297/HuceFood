package com.example.foodorder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorder.utils.LoginSessionManager;

public class MainActivity extends AppCompatActivity {

    private LoginSessionManager sessionManager;
    private static final int SPLASH_DELAY = 2000; // 2 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new LoginSessionManager(this);

        new Handler().postDelayed(() -> {
            if (sessionManager.isLoggedIn()) {
                // Đã đăng nhập -> vào HomeActivity
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // Chưa đăng nhập -> vào LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            finish();
        }, SPLASH_DELAY);
    }
}