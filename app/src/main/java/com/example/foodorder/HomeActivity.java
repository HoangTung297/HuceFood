package com.example.foodorder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.foodorder.fragment.CartFragment;
import com.example.foodorder.fragment.FavoriteFragment;
import com.example.foodorder.fragment.HomeFragment;
import com.example.foodorder.fragment.NotificationFragment;
import com.example.foodorder.fragment.OrderFragment;
import com.example.foodorder.fragment.ProfileFragment;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Food;
import com.example.foodorder.repository.FirebaseRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // Khai báo biến
    private BottomNavigationView bottomNavigationView;
    private FirebaseRepository repository;
    private String userId = "user123";
    private List<CartItem> cartItemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo repository
        repository = FirebaseRepository.getInstance();
        cartItemsList = new ArrayList<>();

        // Lấy userId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", "user123");

        // Khởi tạo bottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set listener cho bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                try {
                    if (itemId == R.id.nav_home) {
                        selectedFragment = new HomeFragment();
                    } else if (itemId == R.id.nav_order) {
                        selectedFragment = new OrderFragment();
                    } else if (itemId == R.id.nav_notification) {
                        selectedFragment = new NotificationFragment();
                    } else if (itemId == R.id.nav_favorite) {
                        selectedFragment = new FavoriteFragment();
                    } else if (itemId == R.id.nav_profile) {
                        selectedFragment = new ProfileFragment();
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(HomeActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        // Load giỏ hàng từ Firebase
        loadCartFromFirebase();

        // Mặc định chọn Home
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    /**
     * Load giỏ hàng từ Firebase
     */
    private void loadCartFromFirebase() {
        repository.getCart(userId, new FirebaseRepository.OnDataLoaded<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> data) {
                cartItemsList.clear();
                cartItemsList.addAll(data);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Lỗi tải giỏ hàng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Thêm món ăn vào giỏ hàng
     */
    public void addToCart(Food food) {
        CartItem cartItem = new CartItem();
        cartItem.setFoodId(food.getId());
        cartItem.setName(food.getName());
        cartItem.setPrice(food.getPrice());
        cartItem.setQuantity(1);
        cartItem.setRestaurantId(food.getRestaurantName());
        cartItem.setImageUrl(food.getImageUrl());

        repository.addToCart(userId, cartItem, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadCartFromFirebase();
                Toast.makeText(HomeActivity.this, "Đã thêm " + food.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Lấy danh sách giỏ hàng
     */
    public List<CartItem> getCartList() {
        return new ArrayList<>(cartItemsList);
    }

    /**
     * Xóa một món khỏi giỏ hàng
     */
    public void removeFromCart(String foodId) {
        repository.removeFromCart(userId, foodId, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadCartFromFirebase();
                Toast.makeText(HomeActivity.this, "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Lỗi xóa: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cập nhật số lượng món trong giỏ hàng
     */
    public void updateCartItem(CartItem item) {
        repository.updateCartItem(userId, item, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadCartFromFirebase();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void clearCart() {
        repository.clearCart(userId, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                cartItemsList.clear();
                Toast.makeText(HomeActivity.this, "Đã xóa giỏ hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Điều hướng đến Profile
     */
    public void navigateToProfile() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }
    }

    /**
     * Điều hướng đến Lịch sử đơn hàng
     */
    public void navigateToOrderHistory() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_order);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartFromFirebase();
    }
}