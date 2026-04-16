package com.example.foodorder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.foodorder.model.Food;
import com.example.foodorder.model.Restaurant;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class FoodDetailActivity extends AppCompatActivity {

    private ImageView ivFoodImage;
    private TextView tvFoodName, tvFoodDescription, tvFoodPrice, tvRestaurantName, tvRestaurantAddress;
    private RatingBar ratingBar;
    private Button btnAddToCart;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;

    private Food food;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        // Nhận dữ liệu từ Intent
        food = (Food) getIntent().getSerializableExtra("food");

        initViews();
        setupToolbar();
        displayFoodInfo();
        setupListeners();
    }

    private void initViews() {
        ivFoodImage = findViewById(R.id.ivFoodImage);
        tvFoodName = findViewById(R.id.tvFoodName);
        tvFoodDescription = findViewById(R.id.tvFoodDescription);
        tvFoodPrice = findViewById(R.id.tvFoodPrice);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvRestaurantAddress = findViewById(R.id.tvRestaurantAddress);
        ratingBar = findViewById(R.id.ratingBar);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (food != null) {
            collapsingToolbar.setTitle(food.getName());
        }
    }

    private void displayFoodInfo() {
        if (food != null) {
            tvFoodName.setText(food.getName());
            tvFoodDescription.setText(food.getDescription());

            // Định dạng giá
            String price = String.format("%,.0f VNĐ", food.getPrice());
            tvFoodPrice.setText(price);

            tvRestaurantName.setText(food.getRestaurantName());
            ratingBar.setRating((float) food.getRating());
            tvRestaurantAddress.setText("📍 " + food.getRestaurantName() + " - Gần bạn");
        }
    }

    private void setupListeners() {
        // Nút thêm vào giỏ hàng
        btnAddToCart.setOnClickListener(v -> {
            // Tạo Intent để gửi kết quả về
            Intent resultIntent = new Intent();
            resultIntent.putExtra("added_food", food);
            setResult(RESULT_OK, resultIntent);

            // Thông báo và đóng activity
            Toast.makeText(this, "Đã thêm " + food.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Click vào tên nhà hàng để xem chi tiết
        tvRestaurantName.setOnClickListener(v -> {
            // Tạo đối tượng Restaurant từ thông tin có sẵn
            Restaurant restaurant = new Restaurant(
                    food.getRestaurantId(),
                    food.getRestaurantName(),
                    "Đang cập nhật",
                    food.getRating(),
                    1.0,
                    "30phút",
                    "Giảm 10%",
                    ""
            );

            Intent intent = new Intent(this, RestaurantDetailActivity.class);
            intent.putExtra("restaurant", restaurant);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}