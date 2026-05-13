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
import com.example.foodorder.model.Food;
import com.example.foodorder.model.Restaurant;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDetailActivity extends AppCompatActivity {

    // Views
    private ImageView ivRestaurantImage;
    private TextView tvRestaurantName;
    private TextView tvRestaurantAddress;
    private TextView tvRating;              // THÊM DÒNG NÀY
    private TextView tvDeliveryTime;
    private TextView tvDiscount;
    private RatingBar ratingBar;
    private RecyclerView rvFoods;
    private CollapsingToolbarLayout collapsingToolbar;  // SỬA TÊN
    private Toolbar toolbar;

    // Data
    private Restaurant restaurant;
    private List<Food> restaurantFoods;
    private FoodAdapter foodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

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
        tvRating = findViewById(R.id.tvRating);           // THÊM
        tvDeliveryTime = findViewById(R.id.tvDeliveryTime);
        tvDiscount = findViewById(R.id.tvDiscount);
        ratingBar = findViewById(R.id.ratingBar);
        rvFoods = findViewById(R.id.rvFoods);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);  // SỬA
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
            tvRating.setText(String.valueOf(restaurant.getRating()));
            ratingBar.setRating((float) restaurant.getRating());
        }
    }

    private void loadRestaurantFoods() {
        restaurantFoods = new ArrayList<>();

        // TODO: Thêm món ăn mẫu hoặc load từ Firebase
        if (restaurantFoods.isEmpty()) {
            // Thêm món mẫu để test
            Food sampleFood = new Food("1", "Món đặc biệt", "Món ngon của nhà hàng", 50000, "Fast Food", restaurant.getId());
            sampleFood.setRestaurantName(restaurant.getName());
            restaurantFoods.add(sampleFood);
        }

        foodAdapter = new FoodAdapter(restaurantFoods,
                food -> {
                    Intent intent = new Intent(this, FoodDetailActivity.class);
                    intent.putExtra("food", food);
                    startActivity(intent);
                },
                food -> {
                    Toast.makeText(this, "Đã thêm " + food.getName(), Toast.LENGTH_SHORT).show();
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