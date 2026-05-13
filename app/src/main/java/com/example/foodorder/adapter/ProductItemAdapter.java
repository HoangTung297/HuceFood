package com.example.foodorder.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Food;
import java.util.List;

public class ProductItemAdapter extends RecyclerView.Adapter<ProductItemAdapter.ViewHolder> {

    private List<CartItem> items;
    private OnProductItemChangeListener listener;

    public interface OnProductItemChangeListener {
        void onQuantityChange(CartItem item, int newQuantity);
        void onDelete(CartItem item);
        void onNoteChange(CartItem item, String note);
    }

    public ProductItemAdapter(List<CartItem> items, OnProductItemChangeListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_product, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = items.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName, tvNote, tvPrice, tvQuantity;
        ImageButton btnDecrease, btnIncrease, btnDelete;
        private OnProductItemChangeListener listener;

        ViewHolder(@NonNull View itemView, OnProductItemChangeListener listener) {
            super(itemView);
            this.listener = listener;

            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(CartItem cartItem) {
            Food food = cartItem.getFood();
            tvFoodName.setText(food.getName());
            tvPrice.setText(String.format("%,.0fđ", food.getPrice()));
            tvQuantity.setText(String.valueOf(cartItem.getQuantity()));

            if (cartItem.getNote() != null && !cartItem.getNote().isEmpty()) {
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText("📝 " + cartItem.getNote());
            } else {
                tvNote.setVisibility(View.GONE);
            }

            tvNote.setOnClickListener(v -> showNoteDialog(cartItem));

            btnIncrease.setOnClickListener(v -> {
                int newQty = cartItem.getQuantity() + 1;
                if (listener != null) {
                    listener.onQuantityChange(cartItem, newQty);
                }
            });

            btnDecrease.setOnClickListener(v -> {
                int newQty = cartItem.getQuantity() - 1;
                if (listener != null) {
                    listener.onQuantityChange(cartItem, newQty);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(cartItem);
                }
            });
        }

        private void showNoteDialog(CartItem cartItem) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Ghi chú cho món");

            final EditText input = new EditText(itemView.getContext());
            input.setHint("VD: ít đường, không hành, thêm tương...");
            input.setText(cartItem.getNote());
            builder.setView(input);

            builder.setPositiveButton("Lưu", (dialog, which) -> {
                String note = input.getText().toString();
                if (listener != null) {
                    listener.onNoteChange(cartItem, note);
                }
            });
            builder.setNegativeButton("Hủy", null);
            builder.show();
        }
    }
}