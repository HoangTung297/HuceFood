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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<Food> foodList;
    private OnItemClickListener itemClickListener;
    private OnAddToCartClickListener addToCartClickListener;

    public interface OnItemClickListener {
        void onItemClick(Food food);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Food food);
    }

    public FoodAdapter(List<Food> foodList,
                       OnItemClickListener itemClickListener,
                       OnAddToCartClickListener addToCartClickListener) {
        this.foodList = foodList != null ? foodList : new ArrayList<>();
        this.itemClickListener = itemClickListener;
        this.addToCartClickListener = addToCartClickListener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.bind(food, itemClickListener, addToCartClickListener);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public void updateList(List<Food> newList) {
        this.foodList = newList != null ? newList : new ArrayList<>();
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

        void bind(final Food food,
                  final OnItemClickListener itemClickListener,
                  final OnAddToCartClickListener addToCartClickListener) {

            tvFoodName.setText(food.getName());
            tvFoodDescription.setText(food.getDescription());
            tvFoodCategory.setText(food.getCategory());

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvFoodPrice.setText(formatter.format(food.getPrice()) + " VNĐ");

            if (itemClickListener != null) {
                itemView.setOnClickListener(v -> itemClickListener.onItemClick(food));
            }

            if (addToCartClickListener != null) {
                btnAddToCart.setOnClickListener(v -> addToCartClickListener.onAddToCartClick(food));
            }
        }
    }
}