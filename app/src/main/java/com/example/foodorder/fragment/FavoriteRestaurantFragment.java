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
import com.example.foodorder.R;
import com.example.foodorder.RestaurantDetailActivity;
import com.example.foodorder.model.Restaurant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteRestaurantFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LinearLayout layoutEmpty;
    private FavoriteRestaurantAdapter adapter;
    private List<Restaurant> favoriteList;
    private FirebaseFirestore db;
    private String userId = "user123";
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_restaurant, container, false);

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
        adapter = new FavoriteRestaurantAdapter(favoriteList,
                restaurant -> {
                    Intent intent = new Intent(getContext(), RestaurantDetailActivity.class);
                    intent.putExtra("restaurantId", restaurant.getId());
                    startActivity(intent);
                },
                (restaurant) -> {
                    removeFromFavorites(restaurant);
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

        Map<String, Restaurant> restaurantMap = new HashMap<>();

        db.collection("favorite_restaurants")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String restaurantId = doc.getString("restaurantId");
                        if (restaurantId == null || restaurantId.isEmpty()) continue;

                        String name = doc.getString("restaurantName");
                        String address = doc.getString("restaurantAddress");
                        String imageUrl = doc.getString("restaurantImage");
                        Double rating = doc.getDouble("rating");
                        String favoriteId = doc.getId();

                        if (!restaurantMap.containsKey(restaurantId) ||
                                favoriteId.compareTo(restaurantMap.get(restaurantId).getFavoriteId()) > 0) {

                            Restaurant restaurant = new Restaurant();
                            restaurant.setId(restaurantId);
                            restaurant.setName(name != null ? name : "Nhà hàng");
                            restaurant.setAddress(address != null ? address : "Đang cập nhật");
                            restaurant.setImageUrl(imageUrl != null ? imageUrl : "");
                            restaurant.setRating(rating != null ? rating : 0);
                            restaurant.setFavoriteId(favoriteId);
                            restaurantMap.put(restaurantId, restaurant);
                        }
                    }

                    favoriteList.clear();
                    favoriteList.addAll(restaurantMap.values());

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

    private void removeFromFavorites(Restaurant restaurant) {
        if (restaurant.getFavoriteId() != null) {
            String favoriteId = restaurant.getFavoriteId();
            String restaurantName = restaurant.getName();

            db.collection("favorite_restaurants").document(favoriteId).delete()
                    .addOnSuccessListener(aVoid -> {
                        // Xóa bằng cách tìm theo favoriteId
                        int indexToRemove = -1;
                        for (int i = 0; i < favoriteList.size(); i++) {
                            if (favoriteList.get(i).getFavoriteId().equals(favoriteId)) {
                                indexToRemove = i;
                                break;
                            }
                        }

                        if (indexToRemove != -1) {
                            favoriteList.remove(indexToRemove);
                            adapter.notifyItemRemoved(indexToRemove);
                            Toast.makeText(getContext(), "Đã xóa " + restaurantName + " khỏi yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            // Nếu không tìm thấy, reload lại
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
    static class FavoriteRestaurantAdapter extends RecyclerView.Adapter<FavoriteRestaurantAdapter.ViewHolder> {
        private List<Restaurant> favorites;
        private OnItemClickListener itemClickListener;
        private OnRemoveClickListener removeClickListener;

        interface OnItemClickListener { void onItemClick(Restaurant restaurant); }
        interface OnRemoveClickListener { void onRemoveClick(Restaurant restaurant); }

        FavoriteRestaurantAdapter(List<Restaurant> favorites, OnItemClickListener itemClickListener, OnRemoveClickListener removeClickListener) {
            this.favorites = favorites;
            this.itemClickListener = itemClickListener;
            this.removeClickListener = removeClickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite_restaurant, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Restaurant restaurant = favorites.get(position);
            holder.bind(restaurant, itemClickListener, removeClickListener);
        }

        @Override
        public int getItemCount() {
            return favorites.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivRestaurantImage, ivRemove;
            TextView tvRestaurantName, tvAddress, tvRating;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivRestaurantImage = itemView.findViewById(R.id.ivRestaurantImage);
                ivRemove = itemView.findViewById(R.id.ivRemove);
                tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvRating = itemView.findViewById(R.id.tvRating);
            }

            void bind(Restaurant restaurant, OnItemClickListener itemClickListener, OnRemoveClickListener removeClickListener) {
                tvRestaurantName.setText(restaurant.getName());
                tvAddress.setText(restaurant.getAddress());
                tvRating.setText(String.format("%.1f ★", restaurant.getRating()));

                if (restaurant.getImageUrl() != null && !restaurant.getImageUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(restaurant.getImageUrl())
                            .placeholder(R.drawable.ic_restaurant_placeholder)
                            .error(R.drawable.ic_restaurant_placeholder)
                            .into(ivRestaurantImage);
                }

                ivRemove.setOnClickListener(v -> {
                    if (removeClickListener != null) {
                        removeClickListener.onRemoveClick(restaurant);
                    }
                });

                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(restaurant);
                    }
                });
            }
        }
    }
}