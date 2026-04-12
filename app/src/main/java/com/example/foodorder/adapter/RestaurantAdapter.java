package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Restaurant;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private List<Restaurant> restaurantList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Restaurant restaurant);
    }

    public RestaurantAdapter(List<Restaurant> restaurantList) {
        this.restaurantList = restaurantList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.bind(restaurant);
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvDistanceTime, tvDiscount;
        RatingBar ratingBar;
        ImageView ivImage;

        RestaurantViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRestaurantName);
            tvAddress = itemView.findViewById(R.id.tvRestaurantAddress);
            tvDistanceTime = itemView.findViewById(R.id.tvDistanceTime);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ivImage = itemView.findViewById(R.id.ivRestaurantImage);

            itemView.setOnClickListener(v -> {
                if (listener != null && itemView.getTag() instanceof Restaurant) {
                    listener.onItemClick((Restaurant) itemView.getTag());
                }
            });
        }

        void bind(Restaurant restaurant) {
            itemView.setTag(restaurant);
            tvName.setText(restaurant.getName());
            tvAddress.setText(restaurant.getAddress());
            tvDistanceTime.setText(String.format("%.1fkm | %s", restaurant.getDistance(), restaurant.getDeliveryTime()));
            tvDiscount.setText(restaurant.getDiscount());
            ratingBar.setRating((float) restaurant.getRating());
        }
    }
}