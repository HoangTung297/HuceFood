package com.example.foodorder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.example.foodorder.repository.FirebaseRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivFoodImage;
    private ImageButton ivFavorite;
    private TextView tvFoodName, tvPrice, tvRestaurant, tvDescription, tvSoldCount, tvRating;
    private RatingBar ratingBar;
    private Button btnAddToCart, btnViewRestaurant;
    private RecyclerView rvSimilarFoods;

    private Food currentFood;
    private List<Food> similarFoodsList;
    private FoodAdapter similarFoodsAdapter;
    private FirebaseFirestore db;
    private FirebaseRepository repository;
    private String userId = "user123";
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        db = FirebaseFirestore.getInstance();
        repository = FirebaseRepository.getInstance();

        if (getSharedPreferences("UserPrefs", MODE_PRIVATE) != null) {
            userId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .getString("user_id", "user123");
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
            tvRestaurant.setText(currentFood.getRestaurantName());
            tvDescription.setText(currentFood.getDescription() != null && !currentFood.getDescription().isEmpty()
                    ? currentFood.getDescription() : "Chưa có mô tả chi tiết cho món ăn này");
            tvSoldCount.setText("Đã bán: " + currentFood.getSoldCount());
            tvRating.setText(String.valueOf(currentFood.getRating()));
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
        btnAddToCart.setOnClickListener(v -> {
            com.example.foodorder.model.CartItem cartItem = new com.example.foodorder.model.CartItem();
            cartItem.setFoodId(currentFood.getId());
            cartItem.setName(currentFood.getName());
            cartItem.setPrice(currentFood.getPrice());
            cartItem.setQuantity(1);
            cartItem.setRestaurantId(currentFood.getRestaurantName());
            cartItem.setImageUrl(currentFood.getImageUrl());

            repository.addToCart(userId, cartItem, new FirebaseRepository.OnDataLoaded<Void>() {
                @Override
                public void onSuccess(Void data) {
                    Toast.makeText(FoodDetailActivity.this, "Đã thêm " + currentFood.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(FoodDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnViewRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(this, RestaurantDetailActivity.class);
            intent.putExtra("restaurantName", currentFood.getRestaurantName());
            startActivity(intent);
        });

        ivFavorite.setOnClickListener(v -> {
            if (isFavorite) {
                removeFromFavorites();
            } else {
                addToFavorites();
            }
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
                        if (!doc.getId().equals(currentFood.getId())) {
                            String name = doc.getString("name");
                            if (name == null) name = "Món ăn";

                            Double price = doc.getDouble("price");
                            if (price == null) price = 0.0;

                            String imageUrl = doc.getString("imageUrl");
                            if (imageUrl == null) imageUrl = "";

                            String restaurantName = doc.getString("restaurant");
                            if (restaurantName == null) restaurantName = "Nhà hàng";

                            Food food = new Food(doc.getId(), name, "", price, "", "");
                            food.setImageUrl(imageUrl);
                            food.setRestaurantName(restaurantName);
                            similarFoodsList.add(food);
                        }
                    }
                    similarFoodsAdapter.updateList(similarFoodsList);
                    similarFoodsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải món gợi ý: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkIfFavorite() {
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("foodId", currentFood.getId())
                .get()
                .addOnSuccessListener(query -> {
                    isFavorite = !query.isEmpty();
                    ivFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
                });
    }

    private void addToFavorites() {
        java.util.HashMap<String, Object> favorite = new java.util.HashMap<>();
        favorite.put("userId", userId);
        favorite.put("foodId", currentFood.getId());
        favorite.put("addedAt", System.currentTimeMillis());

        db.collection("favorites").add(favorite)
                .addOnSuccessListener(doc -> {
                    isFavorite = true;
                    ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
                    Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeFromFavorites() {
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("foodId", currentFood.getId())
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        doc.getReference().delete();
                    }
                    isFavorite = false;
                    ivFavorite.setImageResource(R.drawable.ic_favorite);
                    Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                });
    }
}