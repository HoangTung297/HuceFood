package com.example.foodorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.adapter.FoodAdapter;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Food;
import com.example.foodorder.repository.FirebaseRepository;
import com.example.foodorder.utils.LoginSessionManager;
import com.example.foodorder.utils.StringUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etSearch;
    private ImageView ivClear, ivBack;
    private TextView tvResultCount, tvSuggestionTitle;
    private RecyclerView rvResults, rvSuggestions;

    private List<Food> allFoodsList;
    private List<Food> resultList;
    private List<Food> suggestionList;

    private FoodAdapter resultAdapter;
    private FoodAdapter suggestionAdapter;
    private FirebaseFirestore db;
    private FirebaseRepository repository;
    private LoginSessionManager sessionManager;
    private String userId = "user123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = FirebaseFirestore.getInstance();
        repository = FirebaseRepository.getInstance();
        sessionManager = new LoginSessionManager(this);

        // Lấy userId từ session
        userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userId = prefs.getString("user_email", "user123");
            if (userId == null || userId.isEmpty()) {
                userId = prefs.getString("user_id", "user123");
            }
        }

        initViews();
        setupToolbar();
        loadAllFoods();
        setupSearch();
        loadSuggestions();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.etSearch);
        ivClear = findViewById(R.id.ivClear);
        ivBack = findViewById(R.id.ivBack);
        tvResultCount = findViewById(R.id.tvResultCount);
        tvSuggestionTitle = findViewById(R.id.tvSuggestionTitle);
        rvResults = findViewById(R.id.rvResults);
        rvSuggestions = findViewById(R.id.rvSuggestions);

        allFoodsList = new ArrayList<>();
        resultList = new ArrayList<>();
        suggestionList = new ArrayList<>();

        resultAdapter = new FoodAdapter(resultList, this::onFoodClick, this::onAddToCart);
        suggestionAdapter = new FoodAdapter(suggestionList, this::onFoodClick, this::onAddToCart);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(resultAdapter);

        rvSuggestions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSuggestions.setAdapter(suggestionAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("");
        }

        ivBack.setOnClickListener(v -> finish());
    }

    private void loadAllFoods() {
        db.collection("foods").limit(100).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allFoodsList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name == null) name = "Món ăn";

                        String description = doc.getString("description");
                        if (description == null) description = "";

                        Double price = doc.getDouble("price");
                        if (price == null) price = 0.0;

                        String category = doc.getString("category");
                        if (category == null) category = "Khác";

                        String imageUrl = doc.getString("imageUrl");
                        if (imageUrl == null) imageUrl = "";

                        Long soldCount = doc.getLong("soldCount");
                        if (soldCount == null) soldCount = 0L;

                        Double rating = doc.getDouble("rating");
                        if (rating == null) rating = 0.0;

                        String restaurantName = doc.getString("restaurant");
                        if (restaurantName == null) restaurantName = "Nhà hàng";

                        String restaurantId = doc.getString("restaurantId");
                        if (restaurantId == null) restaurantId = "";

                        Food food = new Food(doc.getId(), name, description, price, category, "");
                        food.setImageUrl(imageUrl);
                        food.setSoldCount(soldCount.intValue());
                        food.setRating(rating);
                        food.setRestaurantName(restaurantName);
                        food.setRestaurantId(restaurantId);
                        allFoodsList.add(food);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupSearch() {
        etSearch.requestFocus();

        String searchQuery = getIntent().getStringExtra("search_query");
        if (searchQuery != null && !searchQuery.isEmpty()) {
            etSearch.setText(searchQuery);
            performSearch(searchQuery);
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    ivClear.setVisibility(View.GONE);
                    resultList.clear();
                    resultAdapter.updateList(resultList);
                    tvResultCount.setText("");
                    rvResults.setVisibility(View.GONE);
                    rvSuggestions.setVisibility(View.VISIBLE);
                    tvSuggestionTitle.setVisibility(View.VISIBLE);
                } else {
                    ivClear.setVisibility(View.VISIBLE);
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });

        ivClear.setOnClickListener(v -> {
            etSearch.setText("");
            ivClear.setVisibility(View.GONE);
            resultList.clear();
            resultAdapter.updateList(resultList);
            rvResults.setVisibility(View.GONE);
            rvSuggestions.setVisibility(View.VISIBLE);
            tvSuggestionTitle.setVisibility(View.VISIBLE);
            tvResultCount.setText("");
            etSearch.requestFocus();
        });
    }

    private void performSearch(String query) {
        String searchQuery = query.toLowerCase().trim();
        resultList.clear();

        for (Food food : allFoodsList) {
            if (StringUtils.containsIgnoreCaseAndAccent(food.getName(), searchQuery) ||
                    StringUtils.containsIgnoreCaseAndAccent(food.getDescription(), searchQuery) ||
                    StringUtils.containsIgnoreCaseAndAccent(food.getCategory(), searchQuery) ||
                    StringUtils.containsIgnoreCaseAndAccent(food.getRestaurantName(), searchQuery)) {
                resultList.add(food);
            }
        }

        resultAdapter.updateList(resultList);

        if (resultList.isEmpty()) {
            tvResultCount.setText("Không tìm thấy kết quả cho \"" + query + "\"");
            rvResults.setVisibility(View.VISIBLE);
            rvSuggestions.setVisibility(View.GONE);
            tvSuggestionTitle.setVisibility(View.GONE);
        } else {
            tvResultCount.setText("Tìm thấy " + resultList.size() + " kết quả cho \"" + query + "\"");
            rvResults.setVisibility(View.VISIBLE);
            rvSuggestions.setVisibility(View.GONE);
            tvSuggestionTitle.setVisibility(View.GONE);
        }

        saveSearchHistory(query);
    }

    private void loadSuggestions() {
        db.collection("foods")
                .orderBy("soldCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    suggestionList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name == null) name = "Món ăn";

                        Double price = doc.getDouble("price");
                        if (price == null) price = 0.0;

                        String imageUrl = doc.getString("imageUrl");
                        if (imageUrl == null) imageUrl = "";

                        Double rating = doc.getDouble("rating");
                        if (rating == null) rating = 0.0;

                        String restaurantName = doc.getString("restaurant");
                        if (restaurantName == null) restaurantName = "Nhà hàng";

                        String restaurantId = doc.getString("restaurantId");
                        if (restaurantId == null) restaurantId = "";

                        Food food = new Food(doc.getId(), name, "", price, "", "");
                        food.setImageUrl(imageUrl);
                        food.setRating(rating);
                        food.setRestaurantName(restaurantName);
                        food.setRestaurantId(restaurantId);
                        suggestionList.add(food);
                    }
                    suggestionAdapter.updateList(suggestionList);
                })
                .addOnFailureListener(e -> {
                    addDefaultSuggestions();
                });
    }

    private void addDefaultSuggestions() {
        suggestionList.clear();

        String[] defaultFoods = {"Phở bò", "Gà rán", "Pizza", "Bún chả", "Cơm tấm", "Trà sữa"};
        String[] defaultPrices = {"55000", "45000", "159000", "45000", "40000", "35000"};

        for (int i = 0; i < defaultFoods.length; i++) {
            Food food = new Food("suggest" + i, defaultFoods[i], "", Double.parseDouble(defaultPrices[i]), "", "");
            suggestionList.add(food);
        }
        suggestionAdapter.updateList(suggestionList);
    }

    private void saveSearchHistory(String query) {
        if (query == null || query.trim().isEmpty()) return;

        SharedPreferences prefs = getSharedPreferences("SearchPrefs", MODE_PRIVATE);
        String history = prefs.getString("search_history", "");
        if (!history.contains(query)) {
            String newHistory = query + "," + history;
            String[] parts = newHistory.split(",");
            if (parts.length > 10) {
                newHistory = "";
                for (int i = 0; i < 10; i++) {
                    newHistory += parts[i] + (i < 9 ? "," : "");
                }
            }
            prefs.edit().putString("search_history", newHistory).apply();
        }
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
                Toast.makeText(SearchActivity.this, "Đã thêm " + food.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(SearchActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}