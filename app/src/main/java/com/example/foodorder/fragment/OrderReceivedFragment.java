package com.example.foodorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    private ProgressBar progressBar;
    private OrderReceivedAdapter adapter;
    private List<Order> orderList;
    private FirebaseRepository repository;
    private String userId = "user123";
    private boolean isLoading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);
        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        progressBar = view.findViewById(R.id.progressBar);

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
        if (isLoading) return;
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        repository.getOrdersByStatus(userId, "delivered", new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orderList.clear();
                orderList.addAll(data);
                adapter.updateList(orderList);

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvOrders.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                }
                progressBar.setVisibility(View.GONE);
                isLoading = false;
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                isLoading = false;
            }
        });
    }

    private void reorder(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            Toast.makeText(getContext(), "Không có món để đặt lại", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        for (Map<String, Object> itemMap : order.getItems()) {
            CartItem cartItem = new CartItem();
            cartItem.setFoodId((String) itemMap.get("foodId"));
            cartItem.setName((String) itemMap.get("name"));
            cartItem.setPrice(((Number) itemMap.get("price")).doubleValue());
            cartItem.setQuantity(((Number) itemMap.get("quantity")).intValue());
            cartItem.setRestaurantId(order.getRestaurantId());
            cartItem.setImageUrl((String) itemMap.get("imageUrl"));
            repository.addToCart(userId, cartItem, new FirebaseRepository.OnDataLoaded<Void>() {
                @Override public void onSuccess(Void data) {}
                @Override public void onError(String error) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
        progressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
    }

    static class OrderReceivedAdapter extends RecyclerView.Adapter<OrderReceivedAdapter.ViewHolder> {
        private List<Order> orders;
        private OnReorderListener listener;

        interface OnReorderListener { void onReorder(Order order); }
        OrderReceivedAdapter(List<Order> orders, OnReorderListener listener) {
            this.orders = orders;
            this.listener = listener;
        }
        void updateList(List<Order> newList) { this.orders = newList; notifyDataSetChanged(); }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_received_order, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(orders.get(position), listener);
        }

        @Override public int getItemCount() { return orders.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvOrderDate, tvRestaurantName, tvFoodItems, tvTotalPrice;
            Button btnReorder;
            ViewHolder(@NonNull View v) {
                super(v);
                tvOrderId = v.findViewById(R.id.tvOrderId);
                tvOrderDate = v.findViewById(R.id.tvOrderDate);
                tvRestaurantName = v.findViewById(R.id.tvRestaurantName);
                tvFoodItems = v.findViewById(R.id.tvFoodItems);
                tvTotalPrice = v.findViewById(R.id.tvTotalPrice);
                btnReorder = v.findViewById(R.id.btnReorder);
            }
            void bind(Order order, OnReorderListener listener) {
                tvOrderId.setText("Mã: " + (order.getOrderCode() != null ? order.getOrderCode() : order.getId()));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                tvOrderDate.setText(sdf.format(new java.util.Date(order.getCreatedAt())));
                tvRestaurantName.setText(order.getRestaurantName());
                StringBuilder sb = new StringBuilder();
                if (order.getItems() != null) {
                    for (Map<String, Object> item : order.getItems()) {
                        sb.append("• ").append(item.get("name")).append(" x").append(item.get("quantity")).append("\n");
                    }
                }
                tvFoodItems.setText(sb.toString());
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