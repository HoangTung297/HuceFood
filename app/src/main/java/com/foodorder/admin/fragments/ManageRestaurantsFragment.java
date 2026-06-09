package com.foodorder.admin.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.foodorder.admin.R;
import com.foodorder.admin.activities.RestaurantFoodsActivity;
import com.foodorder.admin.model.Restaurant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageRestaurantsFragment extends Fragment {

    private RecyclerView rvRestaurants;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private Button btnAdd;
    private RestaurantAdapter adapter;
    private List<Restaurant> restaurantList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_restaurants, container, false);

        db = FirebaseFirestore.getInstance();
        restaurantList = new ArrayList<>();

        rvRestaurants = view.findViewById(R.id.rvRestaurants);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        btnAdd = view.findViewById(R.id.btnAdd);

        setupRecyclerView();
        loadRestaurants();

        btnAdd.setOnClickListener(v -> showAddRestaurantDialog());

        return view;
    }

    private void setupRecyclerView() {
        adapter = new RestaurantAdapter(restaurantList, new RestaurantAdapter.OnRestaurantActionListener() {
            @Override
            public void onEdit(Restaurant restaurant) {
                showEditRestaurantDialog(restaurant);
            }

            @Override
            public void onDelete(Restaurant restaurant, int position) {
                showDeleteConfirmDialog(restaurant, position);
            }

            @Override
            public void onClick(Restaurant restaurant) {
                // Khi click vào nhà hàng, mở activity hiển thị món ăn
                Intent intent = new Intent(getContext(), RestaurantFoodsActivity.class);
                intent.putExtra("restaurant_id", restaurant.getId());
                intent.putExtra("restaurant_name", restaurant.getName());
                startActivity(intent);
            }
        });
        rvRestaurants.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRestaurants.setAdapter(adapter);
    }

    private void loadRestaurants() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("restaurants").get()
                .addOnSuccessListener(query -> {
                    restaurantList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Restaurant restaurant = doc.toObject(Restaurant.class);
                        restaurant.setId(doc.getId());
                        restaurantList.add(restaurant);
                    }
                    adapter.updateList(restaurantList);
                    progressBar.setVisibility(View.GONE);
                    updateEmptyView();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddRestaurantDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_restaurant_form, null);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        EditText etDeliveryTime = dialogView.findViewById(R.id.etDeliveryTime);
        EditText etDiscount = dialogView.findViewById(R.id.etDiscount);
        EditText etImageUrl = dialogView.findViewById(R.id.etImageUrl);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        builder.setTitle("Thêm nhà hàng mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên nhà hàng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Restaurant restaurant = new Restaurant();
                    restaurant.setName(name);
                    restaurant.setAddress(etAddress.getText().toString().trim());
                    restaurant.setDeliveryTime(etDeliveryTime.getText().toString().trim());
                    restaurant.setDiscount(etDiscount.getText().toString().trim());
                    restaurant.setImageUrl(etImageUrl.getText().toString().trim());
                    restaurant.setRating(ratingBar.getRating());
                    restaurant.setDistance(0);

                    db.collection("restaurants").add(restaurant)
                            .addOnSuccessListener(docRef -> {
                                restaurant.setId(docRef.getId());
                                restaurantList.add(restaurant);
                                adapter.updateList(restaurantList);
                                Toast.makeText(getContext(), "Đã thêm nhà hàng", Toast.LENGTH_SHORT).show();
                                updateEmptyView();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditRestaurantDialog(Restaurant restaurant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_restaurant_form, null);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        EditText etDeliveryTime = dialogView.findViewById(R.id.etDeliveryTime);
        EditText etDiscount = dialogView.findViewById(R.id.etDiscount);
        EditText etImageUrl = dialogView.findViewById(R.id.etImageUrl);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        etName.setText(restaurant.getName());
        etAddress.setText(restaurant.getAddress());
        etDeliveryTime.setText(restaurant.getDeliveryTime());
        etDiscount.setText(restaurant.getDiscount());
        etImageUrl.setText(restaurant.getImageUrl());
        ratingBar.setRating((float) restaurant.getRating());

        builder.setTitle("Sửa nhà hàng")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", etName.getText().toString().trim());
                    updates.put("address", etAddress.getText().toString().trim());
                    updates.put("deliveryTime", etDeliveryTime.getText().toString().trim());
                    updates.put("discount", etDiscount.getText().toString().trim());
                    updates.put("imageUrl", etImageUrl.getText().toString().trim());
                    updates.put("rating", ratingBar.getRating());

                    db.collection("restaurants").document(restaurant.getId()).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                restaurant.setName((String) updates.get("name"));
                                restaurant.setAddress((String) updates.get("address"));
                                restaurant.setDeliveryTime((String) updates.get("deliveryTime"));
                                restaurant.setDiscount((String) updates.get("discount"));
                                restaurant.setImageUrl((String) updates.get("imageUrl"));
                                restaurant.setRating((Double) updates.get("rating"));
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "Đã cập nhật", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteConfirmDialog(Restaurant restaurant, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa nhà hàng")
                .setMessage("Bạn có chắc muốn xóa " + restaurant.getName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("restaurants").document(restaurant.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                restaurantList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                                updateEmptyView();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateEmptyView() {
        if (restaurantList.isEmpty()) {
            rvRestaurants.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvRestaurants.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    // ==================== ADAPTER ====================
    static class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
        private List<Restaurant> restaurants;
        private OnRestaurantActionListener listener;

        interface OnRestaurantActionListener {
            void onEdit(Restaurant restaurant);
            void onDelete(Restaurant restaurant, int position);
            void onClick(Restaurant restaurant);  // ← THÊM
        }

        RestaurantAdapter(List<Restaurant> restaurants, OnRestaurantActionListener listener) {
            this.restaurants = restaurants;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_restaurant, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Restaurant restaurant = restaurants.get(position);
            holder.tvName.setText(restaurant.getName());
            holder.tvAddress.setText(restaurant.getAddress());
            holder.tvRating.setText(String.format("%.1f ★", restaurant.getRating()));
            holder.tvDeliveryTime.setText(restaurant.getDeliveryTime());
            holder.tvDiscount.setText(restaurant.getDiscount());

            if (restaurant.getImageUrl() != null && !restaurant.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(restaurant.getImageUrl())
                        .placeholder(R.drawable.ic_restaurant_placeholder)
                        .into(holder.ivImage);
            }

            // Click vào item để xem món ăn
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(restaurant);
                }
            });

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(restaurant));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(restaurant, position));
        }

        @Override
        public int getItemCount() {
            return restaurants.size();
        }

        void updateList(List<Restaurant> newList) {
            this.restaurants = newList;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvName, tvAddress, tvRating, tvDeliveryTime, tvDiscount;
            Button btnEdit, btnDelete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.ivImage);
                tvName = itemView.findViewById(R.id.tvName);
                tvAddress = itemView.findViewById(R.id.tvAddress);
                tvRating = itemView.findViewById(R.id.tvRating);
                tvDeliveryTime = itemView.findViewById(R.id.tvDeliveryTime);
                tvDiscount = itemView.findViewById(R.id.tvDiscount);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}