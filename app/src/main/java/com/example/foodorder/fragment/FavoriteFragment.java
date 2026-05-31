package com.example.foodorder.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.FoodDetailActivity;
import com.example.foodorder.HomeActivity;
import com.example.foodorder.R;
import com.example.foodorder.adapter.FavoriteFoodAdapter;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Food;
import com.example.foodorder.repository.FirebaseRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private Button btnGoToHome;

    private FavoriteFoodAdapter adapter;
    private List<Food> favoriteList;
    private Set<String> favoriteIds;
    private FirebaseFirestore db;
    private FirebaseRepository repository;
    private String userId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        initViews(view);
        setupToolbar();
        setupRecyclerView();

        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);
            userId = prefs.getString("user_id", "");
            if (userId.isEmpty()) {
                userId = prefs.getString("user_email", "user123");
            }
        }

        db = FirebaseFirestore.getInstance();
        repository = FirebaseRepository.getInstance();

        loadFavorites();

        return view;
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rvFavorites);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        toolbar = view.findViewById(R.id.toolbar);
        btnGoToHome = view.findViewById(R.id.btnGoToHome);

        favoriteList = new ArrayList<>();
        favoriteIds = new HashSet<>();

        btnGoToHome.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });
    }

    private void setupToolbar() {
        // SỬA LẠI PHẦN TOOLBAR - TRÁNH NULL
        if (getActivity() != null) {
            // Đảm bảo toolbar không null
            if (toolbar != null) {
                // Nếu activity có hỗ trợ action bar thì set
                if (((HomeActivity) getActivity()).getSupportActionBar() != null) {
                    ((HomeActivity) getActivity()).setSupportActionBar(toolbar);
                    ((HomeActivity) getActivity()).getSupportActionBar().setTitle("Yêu thích của tôi");
                    ((HomeActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        }

        // XỬ LÝ NÚT BACK - CÁCH AN TOÀN
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                // Quay lại Fragment trước đó thay vì finish activity
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new FavoriteFoodAdapter(
                favoriteList,
                this::onFoodClick,
                this::onRemoveFavorite,
                this::onAddToCart
        );
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(adapter);
    }

    private void loadFavorites() {
        if (userId.isEmpty()) {
            showEmptyState();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        rvFavorites.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteList.clear();
                    favoriteIds.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String foodId = doc.getString("foodId");

                        if (favoriteIds.contains(foodId)) {
                            continue;
                        }

                        String foodName = doc.getString("foodName");
                        String foodDescription = doc.getString("foodDescription");
                        String foodImage = doc.getString("foodImage");
                        String restaurantName = doc.getString("restaurantName");
                        Double price = doc.getDouble("price");
                        Double rating = doc.getDouble("rating");
                        if (rating == null) rating = 0.0;

                        Food food = new Food(foodId, foodName, foodDescription != null ? foodDescription : "",
                                price != null ? price : 0, "", "");
                        food.setImageUrl(foodImage != null ? foodImage : "");
                        food.setRestaurantName(restaurantName != null ? restaurantName : "");
                        food.setRating(rating);

                        favoriteList.add(food);
                        favoriteIds.add(foodId);
                    }

                    progressBar.setVisibility(View.GONE);

                    if (favoriteList.isEmpty()) {
                        showEmptyState();
                    } else {
                        rvFavorites.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                        adapter.updateList(favoriteList);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void showEmptyState() {
        rvFavorites.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void onFoodClick(Food food) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), FoodDetailActivity.class);
        intent.putExtra("food", food);
        startActivity(intent);
    }

    private void onRemoveFavorite(Food food, int position) {
        if (userId.isEmpty()) return;

        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("foodId", food.getId())
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        doc.getReference().delete();
                    }
                    favoriteList.remove(position);
                    favoriteIds.remove(food.getId());
                    adapter.updateList(favoriteList);

                    if (favoriteList.isEmpty()) {
                        showEmptyState();
                    }

                    Toast.makeText(getContext(), "Đã xóa " + food.getName() + " khỏi yêu thích", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void onAddToCart(Food food) {
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
                Toast.makeText(getContext(), "Đã thêm " + food.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }
}