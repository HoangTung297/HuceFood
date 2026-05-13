package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.CartItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartGroupAdapter extends RecyclerView.Adapter<CartGroupAdapter.GroupViewHolder> {

    private List<CartGroup> groupList;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemDeleted(CartItem item);
        void onNoteChanged(CartItem item, String note);
    }

    public static class CartGroup {
        public String restaurantName;
        public String restaurantId;
        public List<CartItem> items;
        public double groupTotal;

        public CartGroup(String restaurantName, String restaurantId) {
            this.restaurantName = restaurantName;
            this.restaurantId = restaurantId;
            this.items = new ArrayList<>();
            this.groupTotal = 0;
        }
    }

    public CartGroupAdapter(List<CartItem> cartItems, OnCartChangeListener listener) {
        this.listener = listener;
        this.groupList = groupByRestaurant(cartItems);
    }

    private List<CartGroup> groupByRestaurant(List<CartItem> cartItems) {
        Map<String, CartGroup> groupMap = new HashMap<>();

        for (CartItem item : cartItems) {
            String key = item.getRestaurantId();
            if (!groupMap.containsKey(key)) {
                groupMap.put(key, new CartGroup(item.getRestaurantName(), key));
            }
            groupMap.get(key).items.add(item);
        }

        for (CartGroup group : groupMap.values()) {
            double total = 0;
            for (CartItem item : group.items) {
                total += item.getTotalPrice();
            }
            group.groupTotal = total;
        }

        return new ArrayList<>(groupMap.values());
    }

    public void updateData(List<CartItem> cartItems) {
        this.groupList = groupByRestaurant(cartItems);
        notifyDataSetChanged();
    }

    public List<CartGroup> getGroupList() {
        return groupList;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        CartGroup group = groupList.get(position);
        holder.bind(group);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvRestaurantName, tvGroupTotal;
        RecyclerView rvItems;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvGroupTotal = itemView.findViewById(R.id.tvRestaurantTotal);
            rvItems = itemView.findViewById(R.id.rvCartItems);
        }

        void bind(CartGroup group) {
            tvRestaurantName.setText(group.restaurantName);
            tvGroupTotal.setText(String.format("%,.0fđ", group.groupTotal));

            ProductItemAdapter itemAdapter = new ProductItemAdapter(group.items,
                    new ProductItemAdapter.OnProductItemChangeListener() {
                        @Override
                        public void onQuantityChange(CartItem item, int newQuantity) {
                            if (listener != null) {
                                listener.onQuantityChanged(item, newQuantity);
                            }
                        }

                        @Override
                        public void onDelete(CartItem item) {
                            if (listener != null) {
                                listener.onItemDeleted(item);
                            }
                        }

                        @Override
                        public void onNoteChange(CartItem item, String note) {
                            if (listener != null) {
                                listener.onNoteChanged(item, note);
                            }
                        }
                    });

            rvItems.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvItems.setAdapter(itemAdapter);
        }
    }
}