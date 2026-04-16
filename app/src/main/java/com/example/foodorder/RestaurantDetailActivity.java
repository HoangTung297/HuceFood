package com.example.foodorder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.adapter.FoodAdapter;
import com.example.foodorder.database.DatabaseHelper;
import com.example.foodorder.model.Food;
import com.example.foodorder.model.Restaurant;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDetailActivity extends AppCompatActivity {

    private ImageView ivRestaurantImage;
    private TextView tvRestaurantName, tvRestaurantAddress, tvDeliveryTime, tvDiscount;
    private RatingBar ratingBar;
    private RecyclerView rvFoods;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;

    private Restaurant restaurant;
    private List<Food> restaurantFoods;
    private FoodAdapter foodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        // Nhận dữ liệu từ Intent
        restaurant = (Restaurant) getIntent().getSerializableExtra("restaurant");

        initViews();
        setupToolbar();
        displayRestaurantInfo();
        loadRestaurantFoods();
    }

    private void initViews() {
        ivRestaurantImage = findViewById(R.id.ivRestaurantImage);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvRestaurantAddress = findViewById(R.id.tvRestaurantAddress);
        tvDeliveryTime = findViewById(R.id.tvDeliveryTime);
        tvDiscount = findViewById(R.id.tvDiscount);
        ratingBar = findViewById(R.id.ratingBar);
        rvFoods = findViewById(R.id.rvFoods);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (restaurant != null) {
            collapsingToolbar.setTitle(restaurant.getName());
        }
    }

    private void displayRestaurantInfo() {
        if (restaurant != null) {
            tvRestaurantName.setText(restaurant.getName());
            tvRestaurantAddress.setText("📍 " + restaurant.getAddress());
            tvDeliveryTime.setText("⏱️ " + restaurant.getDeliveryTime());
            tvDiscount.setText(restaurant.getDiscount());
            ratingBar.setRating((float) restaurant.getRating());
        }
    }

    private void loadRestaurantFoods() {
        restaurantFoods = new ArrayList<>();

        // Tạo món ăn mẫu cho nhà hàng này
        for (int i = 1; i <= 10; i++) {
            Food food = new Food(
                    i,
                    "Món đặc biệt " + i,
                    "Món ngon đặc trưng của " + restaurant.getName(),
                    30000 + (i * 5000),
                    0,
                    i % 2 == 0 ? "Fast Food" : "Món Việt"
            );
            food.setRestaurantName(restaurant.getName());
            food.setRestaurantId(restaurant.getId());
            food.setRating(4.0 + (i % 10) * 0.1);
            restaurantFoods.add(food);
        }

        // Setup FoodAdapter với sự kiện click và thêm vào giỏ
        foodAdapter = new FoodAdapter(restaurantFoods,
                food -> {
                    // Mở chi tiết món ăn
                    Intent intent = new Intent(this, FoodDetailActivity.class);
                    intent.putExtra("food", food);
                    startActivity(intent);
                },
                food -> {
                    // Thêm vào giỏ hàng
                    // Gửi kết quả về HomeActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("added_food", food);
                    setResult(RESULT_OK, resultIntent);
                    Toast.makeText(this, "Đã thêm " + food.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
        );

        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(foodAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}