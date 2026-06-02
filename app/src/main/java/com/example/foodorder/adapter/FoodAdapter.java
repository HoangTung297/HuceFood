package com.example.foodorder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodorder.R;
import com.example.foodorder.model.Food;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    private List<Food> foodList;
    private OnItemClickListener onItemClick;
    private OnAddToCartClickListener onAddToCart;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Food food);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Food food);
    }

    public FoodAdapter(List<Food> foodList, OnItemClickListener onItemClick, OnAddToCartClickListener onAddToCart) {
        this.foodList = foodList != null ? foodList : new ArrayList<>();
        this.onItemClick = onItemClick;
        this.onAddToCart = onAddToCart;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Food food = foodList.get(position);
        if (food == null) return;

        // Tên món
        holder.tvName.setText(food.getName());

        // Mô tả
        String description = food.getDescription();
        if (description != null && !description.isEmpty()) {
            holder.tvDesc.setText(description);
            holder.tvDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        // Giá
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(food.getPrice()) + "đ");

        // Rating
        double rating = food.getRating();
        if (rating > 0) {
            holder.tvRating.setText(String.format("%.1f", rating));
            holder.tvRating.setVisibility(View.VISIBLE);
        } else {
            holder.tvRating.setVisibility(View.GONE);
        }

        // Ảnh
        String imageUrl = food.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_food_default)
                    .error(R.drawable.ic_food_default)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_food_default);
        }

        // Nút thêm - Truyền cả food (đã có restaurantId)
        holder.btnAdd.setOnClickListener(v -> {
            if (onAddToCart != null) {
                onAddToCart.onAddToCartClick(food);
            }
        });

        // Click item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.onItemClick(food);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public void updateList(List<Food> newList) {
        this.foodList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice, tvRating;
        Button btnAdd;
        ImageView ivImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvDesc = itemView.findViewById(R.id.tvFoodDescription);
            tvPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnAdd = itemView.findViewById(R.id.btnAddToCart);
            ivImage = itemView.findViewById(R.id.ivFoodImage);
        }
    }
}