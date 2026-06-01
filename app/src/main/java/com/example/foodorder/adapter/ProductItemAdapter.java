package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodorder.R;
import com.example.foodorder.model.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductItemAdapter extends RecyclerView.Adapter<ProductItemAdapter.ViewHolder> {

    private List<CartItem> items;
    private OnItemClickListener listener;
    private OnQuantityChangeListener quantityListener;
    private OnItemDeleteListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(CartItem item);
    }

    public interface OnQuantityChangeListener {
        void onQuantityChanged(CartItem item, int newQuantity);
    }

    public interface OnItemDeleteListener {
        void onDeleteClick(CartItem item, int position);
    }

    public ProductItemAdapter(List<CartItem> items,
                              OnItemClickListener listener,
                              OnQuantityChangeListener quantityListener,
                              OnItemDeleteListener deleteListener) {
        this.items = items;
        this.listener = listener;
        this.quantityListener = quantityListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.bind(item, listener, quantityListener, deleteListener, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<CartItem> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName, tvPrice, tvQuantity;
        ImageButton btnDecrease, btnIncrease, btnDelete;   // Đã sửa thành ImageButton

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            // tvNote đã bị loại bỏ hoàn toàn
        }

        void bind(CartItem item,
                  OnItemClickListener listener,
                  OnQuantityChangeListener quantityListener,
                  OnItemDeleteListener deleteListener,
                  int position) {

            tvFoodName.setText(item.getName());

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvPrice.setText(formatter.format(item.getPrice()) + "đ");
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.ic_food_default)
                        .into(ivFoodImage);
            }

            btnIncrease.setOnClickListener(v -> {
                if (quantityListener != null) {
                    int newQuantity = item.getQuantity() + 1;
                    item.setQuantity(newQuantity);
                    quantityListener.onQuantityChanged(item, newQuantity);
                    tvQuantity.setText(String.valueOf(newQuantity));
                }
            });

            btnDecrease.setOnClickListener(v -> {
                if (quantityListener != null && item.getQuantity() > 1) {
                    int newQuantity = item.getQuantity() - 1;
                    item.setQuantity(newQuantity);
                    quantityListener.onQuantityChanged(item, newQuantity);
                    tvQuantity.setText(String.valueOf(newQuantity));
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(item, position);
                }
            });

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onItemClick(item));
            }
        }
    }
}