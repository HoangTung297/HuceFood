package com.example.foodorder;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.foodorder.fragment.*;
import com.example.foodorder.model.Food;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private List<Food> cartList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cartList = new ArrayList<>();
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Mặc định hiển thị HomeFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        int itemId = item.getItemId();

                        if (itemId == R.id.nav_home) {
                            selectedFragment = new HomeFragment();
                        } else if (itemId == R.id.nav_order) {
                            selectedFragment = new OrderFragment();
                        } else if (itemId == R.id.nav_favorite) {
                            selectedFragment = new FavoriteFragment();
                        } else if (itemId == R.id.nav_notification) {
                            selectedFragment = new NotificationFragment();
                        } else if (itemId == R.id.nav_profile) {
                            selectedFragment = new ProfileFragment();
                        }

                        if (selectedFragment != null) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, selectedFragment)
                                    .commit();
                        }
                        return true;
                    }
                });
    }

    // Lấy danh sách giỏ hàng
    public List<Food> getCartList() {
        return cartList;
    }

    // Thêm vào giỏ hàng
    public void addToCart(Food food) {
        cartList.add(food);
        Toast.makeText(this, "Đã thêm " + food.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    // Xóa giỏ hàng
    public void clearCart() {
        cartList.clear();
    }
}