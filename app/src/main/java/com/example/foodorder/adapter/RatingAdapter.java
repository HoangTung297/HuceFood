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
import com.example.foodorder.model.RatingItem;
import java.util.List;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder> {

    private List<RatingItem> items;
    private OnRatingSubmitListener listener;

    public interface OnRatingSubmitListener {
        void onRatingSubmit(RatingItem item, float rating);
    }

    public RatingAdapter(List<RatingItem> items) {
        this.items = items;
    }

    public void setOnRatingSubmitListener(OnRatingSubmitListener listener) {
        this.listener = listener;
    }

    public void updateList(List<RatingItem> newList) {
        this.items = newList;
        notifyDataSetChanged();
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
        RatingItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
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

        void bind(RatingItem item, OnRatingSubmitListener listener) {
            tvFoodName.setText(item.getFoodName());
            tvOrderInfo.setText(item.getOrderInfo());
            ratingBar.setRating(0);

            btnSubmit.setOnClickListener(v -> {
                float rating = ratingBar.getRating();
                if (rating > 0) {
                    if (listener != null) {
                        listener.onRatingSubmit(item, rating);
                    }
                    Toast.makeText(v.getContext(), "Đã đánh giá " + rating + " sao", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}