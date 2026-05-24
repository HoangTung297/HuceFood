package com.example.foodorder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodorder.adapter.FoodAdapter;
import com.example.foodorder.model.Food;
import com.example.foodorder.model.Restaurant;
import com.example.foodorder.repository.FirebaseRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RestaurantDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivRestaurantImage;
    private TextView tvRestaurantName, tvAddress, tvDeliveryTime, tvDiscount, tvRating;
    private RatingBar ratingBar;
    private RecyclerView rvFoods;
    private ProgressBar progressBar;

    private List<Food> foodList;
    private FoodAdapter foodAdapter;
    private FirebaseFirestore db;
    private FirebaseRepository repository;
    private String userId = "user123";
    private Restaurant currentRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);

        db = FirebaseFirestore.getInstance();
        repository = FirebaseRepository.getInstance();

        if (getSharedPreferences("UserPrefs", MODE_PRIVATE) != null) {
            userId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .getString("user_id", "user123");
        }

        initViews();
        setupToolbar();
        getRestaurantData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivRestaurantImage = findViewById(R.id.ivRestaurantImage);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvAddress = findViewById(R.id.tvAddress);
        tvDeliveryTime = findViewById(R.id.tvDeliveryTime);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvRating = findViewById(R.id.tvRating);
        ratingBar = findViewById(R.id.ratingBar);
        rvFoods = findViewById(R.id.rvFoods);
        progressBar = findViewById(R.id.progressBar);

        foodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(foodList, this::onFoodClick, this::onAddToCart);
        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        rvFoods.setAdapter(foodAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết nhà hàng");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void getRestaurantData() {
        String restaurantId = getIntent().getStringExtra("restaurantId");
        String restaurantName = getIntent().getStringExtra("restaurantName");

        if (restaurantId != null && !restaurantId.isEmpty()) {
            loadRestaurantById(restaurantId);
        } else if (restaurantName != null && !restaurantName.isEmpty()) {
            loadRestaurantByName(restaurantName);
        } else {
            Toast.makeText(this, "Thiếu thông tin nhà hàng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadRestaurantById(String restaurantId) {
        db.collection("restaurants").document(restaurantId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentRestaurant = doc.toObject(Restaurant.class);
                        if (currentRestaurant != null) {
                            currentRestaurant.setId(doc.getId());
                            displayRestaurantInfo();
                            loadFoods(restaurantId);
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy nhà hàng", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadRestaurantByName(String restaurantName) {
        db.collection("restaurants")
                .whereEqualTo("name", restaurantName)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        currentRestaurant = query.getDocuments().get(0).toObject(Restaurant.class);
                        if (currentRestaurant != null) {
                            String foundId = query.getDocuments().get(0).getId();
                            currentRestaurant.setId(foundId);
                            displayRestaurantInfo();
                            loadFoods(foundId);
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy nhà hàng: " + restaurantName, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayRestaurantInfo() {
        if (currentRestaurant == null) return;

        tvRestaurantName.setText(currentRestaurant.getName());
        tvAddress.setText(currentRestaurant.getAddress() != null ? currentRestaurant.getAddress() : "Đang cập nhật");
        tvDeliveryTime.setText(currentRestaurant.getDeliveryTime() != null ? currentRestaurant.getDeliveryTime() : "30 phút");
        tvDiscount.setText(currentRestaurant.getDiscount() != null ? currentRestaurant.getDiscount() : "Đang cập nhật");
        tvRating.setText(String.valueOf(currentRestaurant.getRating()));
        ratingBar.setRating((float) currentRestaurant.getRating());

        if (currentRestaurant.getImageUrl() != null && !currentRestaurant.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentRestaurant.getImageUrl())
                    .placeholder(R.drawable.ic_food_default)
                    .error(R.drawable.ic_food_default)
                    .into(ivRestaurantImage);
        }
    }

    private void loadFoods(String restaurantId) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("foods")
                .whereEqualTo("restaurantId", restaurantId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    foodList.clear();
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        if (name == null || name.isEmpty()) name = "Món ăn";

                        Double price = doc.getDouble("price");
                        if (price == null) price = 0.0;

                        String imageUrl = doc.getString("imageUrl");
                        if (imageUrl == null) imageUrl = "";

                        Long soldCount = doc.getLong("soldCount");
                        if (soldCount == null) soldCount = 0L;

                        Double rating = doc.getDouble("rating");
                        if (rating == null) rating = 0.0;

                        String restaurantName = doc.getString("restaurant");
                        if (restaurantName == null || restaurantName.isEmpty()) {
                            restaurantName = currentRestaurant != null ? currentRestaurant.getName() : "Nhà hàng";
                        }

                        Food food = new Food(id, name, "", price, "", "");
                        food.setImageUrl(imageUrl);
                        food.setSoldCount(soldCount.intValue());
                        food.setRating(rating);
                        food.setRestaurantName(restaurantName);
                        food.setRestaurantId(restaurantId);
                        foodList.add(food);
                    }

                    foodAdapter.updateList(foodList);
                    progressBar.setVisibility(View.GONE);

                    if (foodList.isEmpty()) {
                        Toast.makeText(this, "Chưa có món ăn", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void onFoodClick(Food food) {
        Intent intent = new Intent(this, FoodDetailActivity.class);
        intent.putExtra("food", food);
        startActivity(intent);
    }

    private void onAddToCart(Food food) {
        com.example.foodorder.model.CartItem cartItem = new com.example.foodorder.model.CartItem();
        cartItem.setFoodId(food.getId());
        cartItem.setName(food.getName());
        cartItem.setPrice(food.getPrice());
        cartItem.setQuantity(1);
        cartItem.setRestaurantId(food.getRestaurantName());
        cartItem.setImageUrl(food.getImageUrl());

        repository.addToCart(userId, cartItem, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(RestaurantDetailActivity.this, "Đã thêm " + food.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(RestaurantDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}