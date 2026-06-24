package com.example.foodorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;
import com.example.foodorder.adapter.ViewPagerAdapter;
import com.example.foodorder.fragment.FavoriteFragment;
import com.example.foodorder.fragment.HomeFragment;
import com.example.foodorder.fragment.NotificationFragment;
import com.example.foodorder.fragment.OrderFragment;
import com.example.foodorder.fragment.ProfileFragment;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Food;
import com.example.foodorder.repository.FirebaseRepository;
import com.example.foodorder.utils.LoginSessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    public static final String ACTION_REFRESH_CART = "com.example.foodorder.REFRESH_CART";

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private FirebaseRepository repository;
    private LoginSessionManager sessionManager;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        repository = FirebaseRepository.getInstance();
        sessionManager = new LoginSessionManager(this);

        userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userId = prefs.getString("user_email", "tung@gmail.com");
        }

        viewPager = findViewById(R.id.viewPager2);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        setupViewPager();
        setupBottomNavigation();
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        adapter.addFragment(new HomeFragment(), "Trang chủ");
        adapter.addFragment(new OrderFragment(), "Đơn hàng");
        adapter.addFragment(new NotificationFragment(), "Thông báo");
        adapter.addFragment(new FavoriteFragment(), "Yêu thích");
        adapter.addFragment(new ProfileFragment(), "Tôi");

        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);


        //thiết lập bottom navi
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.nav_home);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.nav_order);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.nav_notification);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.nav_favorite);
                        break;
                    case 4:
                        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
                        break;
                }
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    viewPager.setCurrentItem(0);
                    return true;
                } else if (itemId == R.id.nav_order) {
                    viewPager.setCurrentItem(1);
                    return true;
                } else if (itemId == R.id.nav_notification) {
                    viewPager.setCurrentItem(2);
                    return true;
                } else if (itemId == R.id.nav_favorite) {
                    viewPager.setCurrentItem(3);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    viewPager.setCurrentItem(4);
                    return true;
                }
                return false;
            }
        });
    }

    public void addToCart(Food food) {
        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }

        CartItem cartItem = new CartItem();
        cartItem.setFoodId(food.getId());
        cartItem.setName(food.getName());
        cartItem.setPrice(food.getPrice());
        cartItem.setQuantity(1);
        cartItem.setRestaurantId(food.getRestaurantId());
        cartItem.setImageUrl(food.getImageUrl());

        repository.addToCart(userId, cartItem, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(HomeActivity.this, "Đã thêm " + food.getName() + " vào giỏ", Toast.LENGTH_SHORT).show();

                // Gửi broadcast bằng LocalBroadcastManager
                Intent intent = new Intent(ACTION_REFRESH_CART);
                LocalBroadcastManager.getInstance(HomeActivity.this).sendBroadcast(intent);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
//điều hướng
    public void navigateToProfile() {
        viewPager.setCurrentItem(4);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
    }

    public void navigateToHome() {
        viewPager.setCurrentItem(0);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    public void navigateToCart() {
        viewPager.setCurrentItem(1);
        bottomNavigationView.setSelectedItemId(R.id.nav_order);

        OrderFragment orderFragment = (OrderFragment) getSupportFragmentManager()
                .findFragmentByTag("f1");
        if (orderFragment != null) {
            orderFragment.switchToCartTab();
        }
    }

    public void navigateToOrderHistory() {
        viewPager.setCurrentItem(1);
        bottomNavigationView.setSelectedItemId(R.id.nav_order);

        OrderFragment orderFragment = (OrderFragment) getSupportFragmentManager()
                .findFragmentByTag("f1");
        if (orderFragment != null) {
            orderFragment.switchToHistoryTab();
        }
    }

    public void navigateToDelivering() {
        viewPager.setCurrentItem(1);
        bottomNavigationView.setSelectedItemId(R.id.nav_order);

        OrderFragment orderFragment = (OrderFragment) getSupportFragmentManager()
                .findFragmentByTag("f1");
        if (orderFragment != null) {
            orderFragment.switchToDeliveringTab();
        }
    }
}