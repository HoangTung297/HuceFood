package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Food;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Food> cartItems;
    private OnItemRemoveListener removeListener;

    public interface OnItemRemoveListener {
        void onRemoveClick(Food food);
    }

    public CartAdapter(List<Food> cartItems, OnItemRemoveListener removeListener) {
        this.cartItems = cartItems;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Food food = cartItems.get(position);
        holder.tvFoodName.setText(food.getName());

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvFoodPrice.setText(formatter.format(food.getPrice()) + " VNĐ");

        holder.ivRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemoveClick(food);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvFoodPrice;
        ImageView ivRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvCartFoodName);
            tvFoodPrice = itemView.findViewById(R.id.tvCartFoodPrice);
            ivRemove = itemView.findViewById(R.id.ivRemove);
        }
    }
}