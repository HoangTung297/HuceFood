package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Rating;
import java.util.List;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder> {

    private List<Rating> items;
    private OnRatingSubmitListener listener;

    public interface OnRatingSubmitListener {
        void onRatingSubmit(Rating item, float rating);
    }

    public RatingAdapter(List<Rating> items, OnRatingSubmitListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rating, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rating item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<Rating> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvOrderInfo;
        RatingBar ratingBar;
        Button btnSubmit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvOrderInfo = itemView.findViewById(R.id.tvOrderInfo);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnSubmit = itemView.findViewById(R.id.btnSubmit);
        }

        void bind(Rating item, OnRatingSubmitListener listener) {
            // Rating model không có getFoodName(), hiển thị restaurantId hoặc orderId
            tvFoodName.setText("Nhà hàng: " + (item.getRestaurantId() != null ? item.getRestaurantId() : "Unknown"));
            tvOrderInfo.setText("Đơn hàng: #" + item.getOrderId());

            btnSubmit.setOnClickListener(v -> {
                float rating = ratingBar.getRating();
                if (rating == 0) {
                    Toast.makeText(v.getContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (listener != null) {
                    listener.onRatingSubmit(item, rating);
                }
            });
        }
    }
}