package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
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

public class FavoriteFoodAdapter extends RecyclerView.Adapter<FavoriteFoodAdapter.ViewHolder> {

    private List<Food> favoriteList;
    private OnItemClickListener itemClickListener;
    private OnRemoveFavoriteListener removeListener;
    private OnAddToCartListener addToCartListener;

    public interface OnItemClickListener {
        void onItemClick(Food food);
    }

    public interface OnRemoveFavoriteListener {
        void onRemoveFavorite(Food food, int position);
    }

    public interface OnAddToCartListener {
        void onAddToCart(Food food);
    }

    public FavoriteFoodAdapter(List<Food> favoriteList,
                               OnItemClickListener itemClickListener,
                               OnRemoveFavoriteListener removeListener,
                               OnAddToCartListener addToCartListener) {
        this.favoriteList = favoriteList != null ? favoriteList : new ArrayList<>();
        this.itemClickListener = itemClickListener;
        this.removeListener = removeListener;
        this.addToCartListener = addToCartListener;
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
        Food food = favoriteList.get(position);
        holder.bind(food, position);
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public void updateList(List<Food> newList) {
        this.favoriteList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName, tvRestaurantName, tvRating, tvPrice;
        RatingBar ratingBar;
        ImageButton btnRemoveFavorite;
        Button btnAddToCart;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnRemoveFavorite = itemView.findViewById(R.id.btnRemoveFavorite);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }

        void bind(Food food, int position) {
            tvFoodName.setText(food.getName());
            tvRestaurantName.setText(food.getRestaurantName());

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvPrice.setText(formatter.format(food.getPrice()) + "đ");

            tvRating.setText(String.format("%.1f", food.getRating()));
            ratingBar.setRating((float) food.getRating());

            if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(food.getImageUrl())
                        .placeholder(R.drawable.ic_food_default)
                        .error(R.drawable.ic_food_default)
                        .into(ivFoodImage);
            } else {
                ivFoodImage.setImageResource(R.drawable.ic_food_default);
            }

            btnRemoveFavorite.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onRemoveFavorite(food, position);
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                if (addToCartListener != null) {
                    addToCartListener.onAddToCart(food);
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