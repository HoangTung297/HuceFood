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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private List<Restaurant> restaurantList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Restaurant restaurant);
    }

    // Constructor với listener
    public RestaurantAdapter(List<Restaurant> restaurantList, OnItemClickListener listener) {
        this.restaurantList = restaurantList != null ? restaurantList : new ArrayList<>();
        this.listener = listener;
    }

    // Constructor không có listener
    public RestaurantAdapter(List<Restaurant> restaurantList) {
        this(restaurantList, null);
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
        return restaurantList.size();
    }

    // Cập nhật toàn bộ danh sách
    public void updateList(List<Restaurant> newList) {
        this.restaurantList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Thêm một nhà hàng vào cuối danh sách
    public void addItem(Restaurant restaurant) {
        restaurantList.add(restaurant);
        notifyItemInserted(restaurantList.size() - 1);
    }

    // Thêm nhiều nhà hàng vào cuối danh sách
    public void addItems(List<Restaurant> newItems) {
        int startPosition = restaurantList.size();
        restaurantList.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }

    // Xóa một nhà hàng theo vị trí
    public void removeItem(int position) {
        if (position >= 0 && position < restaurantList.size()) {
            restaurantList.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Xóa tất cả
    public void clear() {
        restaurantList.clear();
        notifyDataSetChanged();
    }

    // Lấy danh sách hiện tại
    public List<Restaurant> getCurrentList() {
        return restaurantList;
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRestaurantImage;
        TextView tvRestaurantName, tvRestaurantAddress, tvDistanceTime, tvDiscount;
        RatingBar ratingBar;

        RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRestaurantImage = itemView.findViewById(R.id.ivRestaurantImage);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvRestaurantAddress = itemView.findViewById(R.id.tvRestaurantAddress);
            tvDistanceTime = itemView.findViewById(R.id.tvDistanceTime);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }

        void bind(final Restaurant restaurant, final OnItemClickListener listener) {
            tvRestaurantName.setText(restaurant.getName());
            tvRestaurantAddress.setText(restaurant.getAddress());

            // Hiển thị khoảng cách và thời gian giao hàng
            String distanceTime = String.format(Locale.getDefault(),
                    "%.1fkm | %s", restaurant.getDistance(), restaurant.getDeliveryTime());
            tvDistanceTime.setText(distanceTime);

            tvDiscount.setText(restaurant.getDiscount());
            ratingBar.setRating((float) restaurant.getRating());

            // Xử lý click
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onItemClick(restaurant));
            }
        }
    }
}