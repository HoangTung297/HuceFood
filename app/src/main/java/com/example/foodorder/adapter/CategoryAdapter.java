package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Category;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private OnCategoryClickListener listener;
    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        boolean isSelected = (position == selectedPosition);

        holder.bind(category, isSelected);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category, position);
            }
            setSelectedPosition(position);
        });
    }

    public void setSelectedPosition(int position) {
        if (selectedPosition == position) return;
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvIcon;
        CardView cardView;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvIcon = itemView.findViewById(R.id.tvCategoryIcon);
            cardView = itemView.findViewById(R.id.cardCategory);
        }

        void bind(Category category, boolean isSelected) {
            tvName.setText(category.getName());
            tvIcon.setText(category.getIcon());

            if (isSelected) {
                cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.primary_color));
                tvName.setTextColor(itemView.getContext().getColor(android.R.color.white));
                tvIcon.setTextColor(itemView.getContext().getColor(android.R.color.white));
                tvName.setTypeface(tvName.getTypeface(), android.graphics.Typeface.BOLD);
            } else {
                cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.surface));
                tvName.setTextColor(itemView.getContext().getColor(R.color.text_primary));
                tvIcon.setTextColor(itemView.getContext().getColor(R.color.text_primary));
                tvName.setTypeface(tvName.getTypeface(), android.graphics.Typeface.NORMAL);
            }
        }
    }
}