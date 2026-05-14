package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodorder.R;
import com.example.foodorder.model.Restaurant;
import java.util.List;

public class SuggestRestaurantAdapter extends RecyclerView.Adapter<SuggestRestaurantAdapter.ViewHolder> {

    private List<Restaurant> restaurantList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Restaurant restaurant);
    }

    public SuggestRestaurantAdapter(List<Restaurant> restaurantList) {
        this.restaurantList = restaurantList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Restaurant> newList) {
        this.restaurantList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggest_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.bind(restaurant, listener);
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRestaurantImage;
        TextView tvRestaurantName;
        TextView tvRating;
        TextView tvDistance;
        TextView tvDeliveryTime;
        TextView tvDiscount;
        RatingBar ratingBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRestaurantImage = itemView.findViewById(R.id.ivRestaurantImage);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvDeliveryTime = itemView.findViewById(R.id.tvDeliveryTime);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }

        void bind(Restaurant restaurant, OnItemClickListener listener) {
            tvRestaurantName.setText(restaurant.getName());
            tvRating.setText(String.format("%.1f", restaurant.getRating()));
            ratingBar.setRating((float) restaurant.getRating());
            tvDistance.setText(String.format("%.1fkm", restaurant.getDistance()));
            tvDeliveryTime.setText(restaurant.getDeliveryTime());
            tvDiscount.setText(restaurant.getDiscount());

            if (restaurant.getImageUrl() != null && !restaurant.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(restaurant.getImageUrl())
                        .placeholder(R.drawable.ic_food_default)
                        .into(ivRestaurantImage);
            }

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onItemClick(restaurant));
            }
        }
    }
}