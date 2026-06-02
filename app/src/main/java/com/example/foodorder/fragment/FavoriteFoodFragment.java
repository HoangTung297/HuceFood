package com.example.foodorder.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodorder.FoodDetailActivity;
import com.example.foodorder.R;
import com.example.foodorder.model.Food;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FavoriteFoodFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LinearLayout layoutEmpty;
    private FavoriteFoodAdapter adapter;
    private List<Food> favoriteList;
    private FirebaseFirestore db;
    private String userId = "user123";
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_food, container, false);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        db = FirebaseFirestore.getInstance();
        favoriteList = new ArrayList<>();

        if (getActivity() != null) {
            userId = getActivity().getSharedPreferences("UserPrefs", 0)
                    .getString("user_id", "user123");
            if (userId.isEmpty()) {
                userId = getActivity().getSharedPreferences("UserPrefs", 0)
                        .getString("user_email", "user123");
            }
        }

        setupRecyclerView();
        loadFavorites();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new FavoriteFoodAdapter(favoriteList,
                food -> {
                    Intent intent = new Intent(getContext(), FoodDetailActivity.class);
                    intent.putExtra("food", food);
                    startActivity(intent);
                },
                (food) -> {
                    removeFromFavorites(food);
                }
        );
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(adapter);
    }

    private void loadFavorites() {
        if (isLoading) return;
        if (userId.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
            return;
        }

        isLoading = true;

        // Map để lưu food theo foodId
        Map<String, Food> foodMap = new HashMap<>();

        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String foodId = doc.getString("foodId");
                        if (foodId == null || foodId.isEmpty()) continue;

                        String foodName = doc.getString("foodName");
                        String foodImage = doc.getString("foodImage");
                        String restaurantName = doc.getString("restaurantName");
                        Double price = doc.getDouble("price");
                        Double rating = doc.getDouble("rating");
                        String favoriteId = doc.getId();

                        // Nếu chưa có trong map thì thêm vào
                        if (!foodMap.containsKey(foodId)) {
                            Food food = new Food(foodId,
                                    foodName != null ? foodName : "Món ăn",
                                    "",
                                    price != null ? price : 0,
                                    "",
                                    "");
                            food.setImageUrl(foodImage != null ? foodImage : "");
                            food.setRestaurantName(restaurantName != null ? restaurantName : "Nhà hàng");
                            food.setRating(rating != null ? rating : 0);
                            food.setFavoriteId(favoriteId);
                            foodMap.put(foodId, food);
                        }
                    }

                    favoriteList.clear();
                    favoriteList.addAll(foodMap.values());

                    adapter.notifyDataSetChanged();
                    isLoading = false;

                    if (favoriteList.isEmpty()) {
                        rvFavorites.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvFavorites.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isLoading = false;
                });
    }

    private void removeFromFavorites(Food food) {
        String favoriteId = food.getFavoriteId();
        if (favoriteId != null && !favoriteId.isEmpty()) {
            String foodName = food.getName();

            db.collection("favorites").document(favoriteId).delete()
                    .addOnSuccessListener(aVoid -> {
                        // Tìm và xóa khỏi danh sách
                        int indexToRemove = -1;
                        for (int i = 0; i < favoriteList.size(); i++) {
                            if (favoriteList.get(i).getFavoriteId() != null &&
                                    favoriteList.get(i).getFavoriteId().equals(favoriteId)) {
                                indexToRemove = i;
                                break;
                            }
                        }

                        if (indexToRemove != -1) {
                            favoriteList.remove(indexToRemove);
                            adapter.notifyItemRemoved(indexToRemove);
                            Toast.makeText(getContext(), "Đã xóa " + foodName + " khỏi yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            loadFavorites();
                        }

                        if (favoriteList.isEmpty()) {
                            rvFavorites.setVisibility(View.GONE);
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            loadFavorites();
        }
    }

    public void refreshData() {
        loadFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    // Adapter
    static class FavoriteFoodAdapter extends RecyclerView.Adapter<FavoriteFoodAdapter.ViewHolder> {
        private List<Food> favorites;
        private OnItemClickListener itemClickListener;
        private OnRemoveClickListener removeClickListener;

        interface OnItemClickListener { void onItemClick(Food food); }
        interface OnRemoveClickListener { void onRemoveClick(Food food); }

        FavoriteFoodAdapter(List<Food> favorites, OnItemClickListener itemClickListener, OnRemoveClickListener removeClickListener) {
            this.favorites = favorites;
            this.itemClickListener = itemClickListener;
            this.removeClickListener = removeClickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite_food, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Food food = favorites.get(position);
            holder.bind(food, itemClickListener, removeClickListener);
        }

        @Override
        public int getItemCount() {
            return favorites.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivFoodImage, ivRemove;
            TextView tvFoodName, tvRestaurantName, tvPrice, tvRating;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
                ivRemove = itemView.findViewById(R.id.ivRemove);
                tvFoodName = itemView.findViewById(R.id.tvFoodName);
                tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvRating = itemView.findViewById(R.id.tvRating);
            }

            void bind(Food food, OnItemClickListener itemClickListener, OnRemoveClickListener removeClickListener) {
                NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));

                tvFoodName.setText(food.getName());
                tvRestaurantName.setText(food.getRestaurantName());
                tvPrice.setText(f.format(food.getPrice()) + "đ");
                tvRating.setText(String.format("%.1f", food.getRating()) + " ★");

                if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(food.getImageUrl())
                            .placeholder(R.drawable.ic_food_default)
                            .error(R.drawable.ic_food_default)
                            .into(ivFoodImage);
                }

                ivRemove.setOnClickListener(v -> {
                    if (removeClickListener != null) {
                        removeClickListener.onRemoveClick(food);
                    }
                });

                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(food);
                    }
                });
            }
        }
    }
}