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
import com.example.foodorder.repository.FirebaseRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoriteFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LinearLayout layoutEmpty;
    private FavoriteAdapter adapter;
    private List<Food> favoriteList;
    private FirebaseFirestore db;
    private FirebaseRepository repository;
    private String userId = "user123";

    // Khai báo interface ở đây (không có static)
    interface OnItemClickListener {
        void onItemClick(Food food);
    }

    interface OnRemoveClickListener {
        void onRemoveClick(Food food, int position);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        rvFavorites = view.findViewById(R.id.rvFavorites);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        db = FirebaseFirestore.getInstance();
        repository = FirebaseRepository.getInstance();
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
        adapter = new FavoriteAdapter(favoriteList,
                food -> {
                    Intent intent = new Intent(getContext(), FoodDetailActivity.class);
                    intent.putExtra("food", food);
                    startActivity(intent);
                },
                (food, position) -> {
                    removeFromFavorites(food, position);
                }
        );
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(adapter);
    }

    private void loadFavorites() {
        if (userId.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
            return;
        }

        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String foodId = doc.getString("foodId");
                        String foodName = doc.getString("foodName");
                        String foodImage = doc.getString("foodImage");
                        String restaurantName = doc.getString("restaurantName");
                        Double price = doc.getDouble("price");
                        Double rating = doc.getDouble("rating");
                        String favoriteId = doc.getId();

                        Food food = new Food(foodId != null ? foodId : "",
                                foodName != null ? foodName : "Món ăn",
                                "",
                                price != null ? price : 0,
                                "",
                                "");
                        food.setImageUrl(foodImage != null ? foodImage : "");
                        food.setRestaurantName(restaurantName != null ? restaurantName : "Nhà hàng");
                        food.setRating(rating != null ? rating : 0);
                        food.setFavoriteId(favoriteId);
                        favoriteList.add(food);
                    }

                    if (favoriteList.isEmpty()) {
                        rvFavorites.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvFavorites.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeFromFavorites(Food food, int position) {
        if (food.getFavoriteId() != null && !food.getFavoriteId().isEmpty()) {
            db.collection("favorites").document(food.getFavoriteId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        favoriteList.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(getContext(), "Đã xóa " + food.getName() + " khỏi yêu thích", Toast.LENGTH_SHORT).show();

                        if (favoriteList.isEmpty()) {
                            rvFavorites.setVisibility(View.GONE);
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    // ==================== ADAPTER ====================
    class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
        private List<Food> favorites;
        private OnItemClickListener itemClickListener;
        private OnRemoveClickListener removeClickListener;

        FavoriteAdapter(List<Food> favorites, OnItemClickListener itemClickListener, OnRemoveClickListener removeClickListener) {
            this.favorites = favorites;
            this.itemClickListener = itemClickListener;
            this.removeClickListener = removeClickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Food food = favorites.get(position);
            holder.bind(food, itemClickListener, removeClickListener, position);
        }

        @Override
        public int getItemCount() {
            return favorites.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivFoodImage;
            TextView tvFoodName, tvRestaurantName, tvPrice, tvRating;
            ImageView ivRemove;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
                tvFoodName = itemView.findViewById(R.id.tvFoodName);
                tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvRating = itemView.findViewById(R.id.tvRating);
                ivRemove = itemView.findViewById(R.id.ivRemove);
            }

            void bind(Food food, OnItemClickListener itemClickListener, OnRemoveClickListener removeClickListener, int position) {
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

                if (ivRemove != null) {
                    ivRemove.setOnClickListener(v -> {
                        if (removeClickListener != null) {
                            removeClickListener.onRemoveClick(food, position);
                        }
                    });
                }

                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(food);
                    }
                });
            }
        }
    }
}