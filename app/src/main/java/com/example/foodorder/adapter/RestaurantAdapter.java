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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.bind(restaurant, listener);
    }

    @Override
    public int getItemCount() {
        return restaurantList != null ? restaurantList.size() : 0;
    }

    public void updateList(List<Restaurant> newList) {
        this.restaurantList = newList;
        notifyDataSetChanged();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurantName, tvRestaurantAddress;
        RatingBar ratingBar;
        ImageView ivRestaurantImage;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvRestaurantAddress = itemView.findViewById(R.id.tvRestaurantAddress);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ivRestaurantImage = itemView.findViewById(R.id.ivRestaurantImage);
        }

        public void bind(final Restaurant restaurant, final OnItemClickListener listener) {
            tvRestaurantName.setText(restaurant.getName());
            tvRestaurantAddress.setText(restaurant.getAddress());
            ratingBar.setRating((float) restaurant.getRating());

            // Xử lý click
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onItemClick(restaurant));
            }
        }
    }
}