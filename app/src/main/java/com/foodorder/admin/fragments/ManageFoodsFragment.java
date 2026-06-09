package com.foodorder.admin.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.foodorder.admin.R;
import com.foodorder.admin.model.Food;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageFoodsFragment extends Fragment {

    private RecyclerView rvFoods;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private Button btnAdd;
    private FoodAdapter adapter;
    private List<Food> foodList;
    private FirebaseFirestore db;
    private NumberFormat currencyFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_foods, container, false);

        db = FirebaseFirestore.getInstance();
        foodList = new ArrayList<>();
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        rvFoods = view.findViewById(R.id.rvFoods);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        btnAdd = view.findViewById(R.id.btnAdd);

        setupRecyclerView();
        loadFoods();

        btnAdd.setOnClickListener(v -> showAddFoodDialog());

        return view;
    }

    private void setupRecyclerView() {
        adapter = new FoodAdapter(foodList, new FoodAdapter.OnFoodActionListener() {
            @Override
            public void onEdit(Food food) {
                showEditFoodDialog(food);
            }

            @Override
            public void onDelete(Food food, int position) {
                showDeleteConfirmDialog(food, position);
            }
        });
        rvFoods.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFoods.setAdapter(adapter);
    }

    private void loadFoods() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("foods").get()
                .addOnSuccessListener(query -> {
                    foodList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Food food = doc.toObject(Food.class);
                        food.setId(doc.getId());
                        foodList.add(food);
                    }
                    adapter.updateList(foodList);
                    progressBar.setVisibility(View.GONE);
                    updateEmptyView();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddFoodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_food_form, null);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);
        EditText etImageUrl = dialogView.findViewById(R.id.etImageUrl);
        EditText etRestaurantName = dialogView.findViewById(R.id.etRestaurantName);

        builder.setTitle("Thêm món ăn mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tên món", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (priceStr.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập giá", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Food food = new Food();
                    food.setName(name);
                    food.setDescription(etDescription.getText().toString().trim());
                    food.setPrice(Double.parseDouble(priceStr));
                    food.setCategory(etCategory.getText().toString().trim());
                    food.setImageUrl(etImageUrl.getText().toString().trim());
                    food.setRestaurantName(etRestaurantName.getText().toString().trim());
                    food.setSoldCount(0);
                    food.setRating(0);

                    db.collection("foods").add(food)
                            .addOnSuccessListener(docRef -> {
                                food.setId(docRef.getId());
                                foodList.add(food);
                                adapter.updateList(foodList);
                                Toast.makeText(getContext(), "Đã thêm món ăn", Toast.LENGTH_SHORT).show();
                                updateEmptyView();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditFoodDialog(Food food) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_food_form, null);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);
        EditText etImageUrl = dialogView.findViewById(R.id.etImageUrl);
        EditText etRestaurantName = dialogView.findViewById(R.id.etRestaurantName);

        etName.setText(food.getName());
        etDescription.setText(food.getDescription());
        etPrice.setText(String.valueOf(food.getPrice()));
        etCategory.setText(food.getCategory());
        etImageUrl.setText(food.getImageUrl());
        etRestaurantName.setText(food.getRestaurantName());

        builder.setTitle("Sửa món ăn")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", etName.getText().toString().trim());
                    updates.put("description", etDescription.getText().toString().trim());
                    updates.put("price", Double.parseDouble(etPrice.getText().toString().trim()));
                    updates.put("category", etCategory.getText().toString().trim());
                    updates.put("imageUrl", etImageUrl.getText().toString().trim());
                    updates.put("restaurantName", etRestaurantName.getText().toString().trim());

                    db.collection("foods").document(food.getId()).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                food.setName((String) updates.get("name"));
                                food.setDescription((String) updates.get("description"));
                                food.setPrice((Double) updates.get("price"));
                                food.setCategory((String) updates.get("category"));
                                food.setImageUrl((String) updates.get("imageUrl"));
                                food.setRestaurantName((String) updates.get("restaurantName"));
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

    private void showDeleteConfirmDialog(Food food, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa món ăn")
                .setMessage("Bạn có chắc muốn xóa " + food.getName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("foods").document(food.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                foodList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(getContext(), "Đã xóa món ăn", Toast.LENGTH_SHORT).show();
                                updateEmptyView();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateEmptyView() {
        if (foodList.isEmpty()) {
            rvFoods.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvFoods.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    // ==================== ADAPTER ====================
    static class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
        private List<Food> foods;
        private OnFoodActionListener listener;
        private NumberFormat formatter;

        interface OnFoodActionListener {
            void onEdit(Food food);
            void onDelete(Food food, int position);
        }

        FoodAdapter(List<Food> foods, OnFoodActionListener listener) {
            this.foods = foods;
            this.listener = listener;
            this.formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_food, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Food food = foods.get(position);
            holder.tvName.setText(food.getName());
            holder.tvRestaurant.setText(food.getRestaurantName() != null ? food.getRestaurantName() : "Nhà hàng");
            holder.tvPrice.setText(formatter.format(food.getPrice()) + "đ");

            if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(food.getImageUrl())
                        .placeholder(R.drawable.ic_food_default)
                        .into(holder.ivImage);
            }

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(food));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(food, position));
        }

        @Override
        public int getItemCount() {
            return foods.size();
        }

        void updateList(List<Food> newList) {
            this.foods = newList;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvName, tvRestaurant, tvPrice;
            Button btnEdit, btnDelete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.ivImage);
                tvName = itemView.findViewById(R.id.tvName);
                tvRestaurant = itemView.findViewById(R.id.tvRestaurant);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}