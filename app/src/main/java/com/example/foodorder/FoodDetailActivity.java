package com.example.foodorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodorder.adapter.FoodAdapter;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Food;
import com.example.foodorder.repository.FirebaseRepository;
import com.example.foodorder.utils.LoginSessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FoodDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivFoodImage;
    private ImageButton ivFavorite;
    private TextView tvFoodName, tvPrice, tvRestaurant, tvDescription, tvSoldCount, tvRating;
    private RatingBar ratingBar;
    private Button btnAddToCart, btnViewRestaurant;
    private EditText etNote;
    private RecyclerView rvSimilarFoods;

    private Food currentFood;
    private List<Food> similarFoodsList;
    private FoodAdapter similarFoodsAdapter;
    private FirebaseFirestore db;
    private FirebaseRepository repository;
    private LoginSessionManager sessionManager;
    private String userId = "";
    private int quantity = 1;
    private boolean isFavorite = false;
    private String favoriteDocId = null;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        db = FirebaseFirestore.getInstance();
        repository = FirebaseRepository.getInstance();
        sessionManager = new LoginSessionManager(this);

        userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userId = prefs.getString("user_email", "tung@gmail.com");
        }

        initViews();
        setupToolbar();
        getFoodData();
        setupClickListeners();
        loadSimilarFoods();
        checkIfFavorite();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivFoodImage = findViewById(R.id.ivFoodImage);
        ivFavorite = findViewById(R.id.ivFavorite);
        tvFoodName = findViewById(R.id.tvFoodName);
        tvPrice = findViewById(R.id.tvPrice);
        tvRestaurant = findViewById(R.id.tvRestaurant);
        tvDescription = findViewById(R.id.tvDescription);
        tvSoldCount = findViewById(R.id.tvSoldCount);
        tvRating = findViewById(R.id.tvRating);
        ratingBar = findViewById(R.id.ratingBar);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnViewRestaurant = findViewById(R.id.btnViewRestaurant);
        etNote = findViewById(R.id.etNote);
        rvSimilarFoods = findViewById(R.id.rvSimilarFoods);

        similarFoodsList = new ArrayList<>();
        similarFoodsAdapter = new FoodAdapter(similarFoodsList, this::onFoodClick, this::onAddToCart);
        rvSimilarFoods.setLayoutManager(new LinearLayoutManager(this));
        rvSimilarFoods.setAdapter(similarFoodsAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết món ăn");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void getFoodData() {
        currentFood = (Food) getIntent().getSerializableExtra("food");
        if (currentFood != null) {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

            tvFoodName.setText(currentFood.getName());
            tvPrice.setText(formatter.format(currentFood.getPrice()) + "đ");
            tvRestaurant.setText(currentFood.getRestaurantName() != null ? currentFood.getRestaurantName() : "Nhà hàng");
            tvDescription.setText(currentFood.getDescription() != null && !currentFood.getDescription().isEmpty()
                    ? currentFood.getDescription() : "Chưa có mô tả chi tiết");
            tvSoldCount.setText("Đã bán: " + currentFood.getSoldCount());
            tvRating.setText(String.format("%.1f", currentFood.getRating()));
            ratingBar.setRating((float) currentFood.getRating());

            if (currentFood.getImageUrl() != null && !currentFood.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(currentFood.getImageUrl())
                        .placeholder(R.drawable.ic_food_default)
                        .error(R.drawable.ic_food_default)
                        .into(ivFoodImage);
            }
        }
    }

    private void setupClickListeners() {
        btnAddToCart.setOnClickListener(v -> addToCart());
        btnViewRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestaurantDetailActivity.class);
            intent.putExtra("restaurantName", currentFood.getRestaurantName());
            startActivity(intent);
        });

        ivFavorite.setOnClickListener(v -> {
            if (isProcessing) return;
            isProcessing = true;

            if (isFavorite) {
                updateFavoriteIcon(false);
                removeFromFavorites();
            } else {
                updateFavoriteIcon(true);
                addToFavorites();
            }
        });
    }

    private void addToCart() {
        CartItem cartItem = new CartItem();
        cartItem.setFoodId(currentFood.getId());
        cartItem.setName(currentFood.getName());
        cartItem.setPrice(currentFood.getPrice());
        cartItem.setQuantity(quantity);
        cartItem.setRestaurantId(currentFood.getRestaurantId());
        cartItem.setImageUrl(currentFood.getImageUrl());
        cartItem.setNote(etNote != null ? etNote.getText().toString() : "");

        repository.addToCart(userId, cartItem, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(FoodDetailActivity.this, "Đã thêm " + currentFood.getName() + " vào giỏ", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(FoodDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onFoodClick(Food food) {
        Intent intent = new Intent(this, FoodDetailActivity.class);
        intent.putExtra("food", food);
        startActivity(intent);
    }

    private void onAddToCart(Food food) {
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
                Toast.makeText(FoodDetailActivity.this, "Đã thêm " + food.getName(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(FoodDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSimilarFoods() {
        db.collection("foods")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    similarFoodsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (currentFood != null && !doc.getId().equals(currentFood.getId())) {
                            String name = doc.getString("name");
                            if (name == null) name = "Món ăn";

                            Double price = doc.getDouble("price");
                            if (price == null) price = 0.0;

                            String imageUrl = doc.getString("imageUrl");
                            if (imageUrl == null) imageUrl = "";

                            String restaurantName = doc.getString("restaurant");
                            if (restaurantName == null) restaurantName = "Nhà hàng";

                            String restaurantId = doc.getString("restaurantId");
                            if (restaurantId == null) restaurantId = "";

                            Food food = new Food(doc.getId(), name, "", price, "", "");
                            food.setImageUrl(imageUrl);
                            food.setRestaurantName(restaurantName);
                            food.setRestaurantId(restaurantId);
                            similarFoodsList.add(food);
                        }
                    }
                    similarFoodsAdapter.updateList(similarFoodsList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải món gợi ý: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkIfFavorite() {
        if (userId.isEmpty() || currentFood == null) return;

        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("foodId", currentFood.getId())
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        isFavorite = true;
                        favoriteDocId = query.getDocuments().get(0).getId();
                        updateFavoriteIcon(true);
                    } else {
                        isFavorite = false;
                        favoriteDocId = null;
                        updateFavoriteIcon(false);
                    }
                })
                .addOnFailureListener(e -> updateFavoriteIcon(false));
    }

    private void updateFavoriteIcon(boolean isFav) {
        if (isFav) {
            ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
            ivFavorite.setColorFilter(ContextCompat.getColor(this, R.color.red));
        } else {
            ivFavorite.setImageResource(R.drawable.ic_favorite);
            ivFavorite.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    private void addToFavorites() {
        if (userId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            updateFavoriteIcon(false);
            isProcessing = false;
            return;
        }

        Map<String, Object> favorite = new HashMap<>();
        favorite.put("userId", userId);
        favorite.put("foodId", currentFood.getId());
        favorite.put("foodName", currentFood.getName());
        favorite.put("foodDescription", currentFood.getDescription() != null ? currentFood.getDescription() : "");
        favorite.put("foodImage", currentFood.getImageUrl() != null ? currentFood.getImageUrl() : "");
        favorite.put("restaurantName", currentFood.getRestaurantName() != null ? currentFood.getRestaurantName() : "");
        favorite.put("restaurantId", currentFood.getRestaurantId());
        favorite.put("price", currentFood.getPrice());
        favorite.put("rating", currentFood.getRating());
        favorite.put("addedAt", System.currentTimeMillis());

        db.collection("favorites").add(favorite)
                .addOnSuccessListener(doc -> {
                    isFavorite = true;
                    favoriteDocId = doc.getId();
                    isProcessing = false;
                })
                .addOnFailureListener(e -> {
                    updateFavoriteIcon(false);
                    Toast.makeText(FoodDetailActivity.this, "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isProcessing = false;
                });
    }

    private void removeFromFavorites() {
        if (favoriteDocId != null) {
            db.collection("favorites").document(favoriteDocId).delete()
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        favoriteDocId = null;
                        isProcessing = false;
                    })
                    .addOnFailureListener(e -> {
                        updateFavoriteIcon(true);
                        Toast.makeText(FoodDetailActivity.this, "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        isProcessing = false;
                    });
        } else {
            db.collection("favorites")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("foodId", currentFood.getId())
                    .get()
                    .addOnSuccessListener(query -> {
                        for (QueryDocumentSnapshot doc : query) {
                            doc.getReference().delete();
                        }
                        isFavorite = false;
                        favoriteDocId = null;
                        isProcessing = false;
                    });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}