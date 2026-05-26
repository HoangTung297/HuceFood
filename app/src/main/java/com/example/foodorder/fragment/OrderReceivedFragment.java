package com.example.foodorder.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderReceivedFragment extends Fragment {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private OrderReceivedAdapter adapter;
    private List<Order> orderList;
    private FirebaseRepository repository;
    private String userId = "user123";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);

        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        repository = FirebaseRepository.getInstance();
        orderList = new ArrayList<>();

        if (getActivity() != null) {
            userId = getActivity().getSharedPreferences("UserPrefs", 0)
                    .getString("user_id", "user123");
        }

        setupRecyclerView();
        loadOrders();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new OrderReceivedAdapter(orderList, order -> reorder(order));
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        repository.getOrdersByStatus(userId, "delivered", new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orderList.clear();
                orderList.addAll(data);

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvOrders.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                    adapter.updateList(orderList);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reorder(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            Toast.makeText(getContext(), "Không có món để đặt lại", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Map<String, Object> itemMap : order.getItems()) {
            CartItem cartItem = new CartItem();
            cartItem.setFoodId((String) itemMap.get("foodId"));
            cartItem.setName((String) itemMap.get("name"));
            cartItem.setPrice(((Number) itemMap.get("price")).doubleValue());
            cartItem.setQuantity(((Number) itemMap.get("quantity")).intValue());
            cartItem.setRestaurantId(order.getRestaurantId());
            cartItem.setImageUrl((String) itemMap.get("imageUrl"));

            repository.addToCart(userId, cartItem, new FirebaseRepository.OnDataLoaded<Void>() {
                @Override
                public void onSuccess(Void data) {}
                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }

        Toast.makeText(getContext(), "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();

        // Chuyển sang tab giỏ hàng sau 1 giây
        new Handler().postDelayed(() -> {
            if (getParentFragment() instanceof OrderFragment) {
                ((OrderFragment) getParentFragment()).switchToCartTab();
            }
        }, 1000);
    }

    // ==================== ADAPTER ====================
    static class OrderReceivedAdapter extends RecyclerView.Adapter<OrderReceivedAdapter.ViewHolder> {
        private List<Order> orders;
        private OnReorderListener listener;

        interface OnReorderListener {
            void onReorder(Order order);
        }

        OrderReceivedAdapter(List<Order> orders, OnReorderListener listener) {
            this.orders = orders;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_received_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.bind(order, listener);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        void updateList(List<Order> newList) {
            this.orders = newList;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvOrderDate, tvRestaurantName, tvFoodItems, tvTotalPrice;
            Button btnReorder;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
                tvFoodItems = itemView.findViewById(R.id.tvFoodItems);
                tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
                btnReorder = itemView.findViewById(R.id.btnReorder);
            }

            void bind(Order order, OnReorderListener listener) {
                tvOrderId.setText("Mã: #" + (order.getOrderCode() != null ? order.getOrderCode() : order.getId()));

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                long createdAt = order.getCreatedAt();
                tvOrderDate.setText(sdf.format(new java.util.Date(createdAt)));

                tvRestaurantName.setText(order.getRestaurantName());

                StringBuilder itemsText = new StringBuilder();
                if (order.getItems() != null) {
                    for (Map<String, Object> item : order.getItems()) {
                        String name = item.get("name") != null ? item.get("name").toString() : "Món ăn";
                        int quantity = ((Number) item.get("quantity")).intValue();
                        itemsText.append("• ").append(name).append(" x").append(quantity).append("\n");
                    }
                }
                tvFoodItems.setText(itemsText.toString());
                tvTotalPrice.setText(String.format("%,.0fđ", order.getFinalTotal()));

                btnReorder.setOnClickListener(v -> listener.onReorder(order));
            }
        }
    }

    public void refreshData() {
        loadOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }
}