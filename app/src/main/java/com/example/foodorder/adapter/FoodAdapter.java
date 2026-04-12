package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Food;
import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<Food> foodList;
    private OnItemClickListener listener;
    private OnAddToCartClickListener cartListener;

    public interface OnItemClickListener {
        void onItemClick(Food food);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Food food);
    }

    public FoodAdapter(List<Food> foodList, OnItemClickListener listener, OnAddToCartClickListener cartListener) {
        this.foodList = foodList != null ? foodList : new ArrayList<>();
        this.listener = listener;
        this.cartListener = cartListener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.bind(food, listener, cartListener);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public void updateList(List<Food> newList) {
        this.foodList = newList;
        notifyDataSetChanged();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName, tvFoodDescription, tvFoodPrice, tvFoodCategory;
        Button btnAddToCart;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodDescription = itemView.findViewById(R.id.tvFoodDescription);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvFoodCategory = itemView.findViewById(R.id.tvFoodCategory);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }

        void bind(final Food food, final OnItemClickListener listener, final OnAddToCartClickListener cartListener) {
            tvFoodName.setText(food.getName());
            tvFoodDescription.setText(food.getDescription());
            tvFoodPrice.setText(String.format("%,.0f VNĐ", food.getPrice()));
            tvFoodCategory.setText(food.getCategory());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(food);
                }
            });

            btnAddToCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cartListener.onAddToCartClick(food);
                }
            });
        }
    }
}