package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item, int position);
        void onNoteChanged(CartItem item, String note);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
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
        CartItem item = cartItems.get(position);
        holder.bind(item, listener, position);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateList(List<CartItem> newList) {
        this.cartItems = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName, tvPrice, tvQuantity;
        EditText etNote;
        ImageButton btnDecrease, btnIncrease, btnDelete, btnNote;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            etNote = itemView.findViewById(R.id.etNote);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnNote = itemView.findViewById(R.id.btnNote);
        }

        void bind(CartItem item, OnCartItemChangeListener listener, int position) {
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

            // Xử lý ghi chú
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                etNote.setText(item.getNote());
            }
            etNote.setVisibility(View.GONE);

            btnNote.setOnClickListener(v -> {
                if (etNote.getVisibility() == View.GONE) {
                    etNote.setVisibility(View.VISIBLE);
                    etNote.requestFocus();
                } else {
                    etNote.setVisibility(View.GONE);
                    String note = etNote.getText().toString();
                    item.setNote(note);
                    if (listener != null) {
                        listener.onNoteChanged(item, note);
                    }
                }
            });

            etNote.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String note = etNote.getText().toString();
                    item.setNote(note);
                    if (listener != null) {
                        listener.onNoteChanged(item, note);
                    }
                    etNote.setVisibility(View.GONE);
                }
            });

            btnIncrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                item.setQuantity(newQuantity);
                tvQuantity.setText(String.valueOf(newQuantity));
                if (listener != null) {
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    int newQuantity = item.getQuantity() - 1;
                    item.setQuantity(newQuantity);
                    tvQuantity.setText(String.valueOf(newQuantity));
                    if (listener != null) {
                        listener.onQuantityChanged(item, newQuantity);
                    }
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(item, position);
                }
            });
        }
    }
}