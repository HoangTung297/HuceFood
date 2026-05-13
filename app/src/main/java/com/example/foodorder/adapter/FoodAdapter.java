package com.example.foodorder.adapter;

import android.content.Context;
import android.util.Log;
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

        // Hiển thị tên món ăn
        holder.tvName.setText(food.getName());

        // Hiển thị mô tả (nếu có)
        if (food.getDescription() != null && !food.getDescription().isEmpty()) {
            holder.tvDesc.setText(food.getDescription());
            holder.tvDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        // Hiển thị giá
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(food.getPrice()) + "đ");

        // Hiển thị ảnh
        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(food.getImageUrl())
                    .placeholder(R.drawable.ic_food_default)
                    .error(R.drawable.ic_food_default)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_food_default);
        }

        // Nút thêm vào giỏ
        holder.btnAdd.setOnClickListener(v -> {
            if (onAddToCart != null) {
                onAddToCart.onAddToCartClick(food);
                Log.d("FoodAdapter", "Added to cart: " + food.getName());
            }
        });

        // Click vào item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClick != null) {
                onItemClick.onItemClick(food);
                Log.d("FoodAdapter", "Clicked: " + food.getName());
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
        Log.d("FoodAdapter", "Updated list with " + this.foodList.size() + " items");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice;
        Button btnAdd;
        ImageView ivImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvDesc = itemView.findViewById(R.id.tvFoodDescription);
            tvPrice = itemView.findViewById(R.id.tvFoodPrice);
            btnAdd = itemView.findViewById(R.id.btnAddToCart);
            ivImage = itemView.findViewById(R.id.ivFoodImage);
        }
    }
}