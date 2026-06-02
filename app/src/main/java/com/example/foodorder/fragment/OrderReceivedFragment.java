package com.example.foodorder.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.foodorder.utils.LoginSessionManager;
import com.example.foodorder.utils.RestaurantHelper;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private LoginSessionManager sessionManager;
    private String userId = "user123";
    private boolean isLoading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);
        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        progressBar = view.findViewById(R.id.progressBar);

        repository = FirebaseRepository.getInstance();
        sessionManager = new LoginSessionManager(getContext());
        orderList = new ArrayList<>();

        if (getActivity() != null) {
            userId = sessionManager.getUserId();
            if (userId == null || userId.isEmpty()) {
                userId = getActivity().getSharedPreferences("UserPrefs", 0)
                        .getString("user_email", "user123");
            }
        }

        setupRecyclerView();
        loadOrders();
        return view;
    }

    private void setupRecyclerView() {
        adapter = new OrderReceivedAdapter(orderList, order -> reorder(order), order -> showOrderDetail(order));
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
                if (data != null && !data.isEmpty()) {
                    orderList.addAll(data);
                }
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

        for (Map<String, Object> itemMap : order.getItems()) {
            CartItem cartItem = new CartItem();
            cartItem.setFoodId((String) itemMap.get("foodId"));
            cartItem.setName((String) itemMap.get("name"));
            cartItem.setPrice(((Number) itemMap.get("price")).doubleValue());
            cartItem.setQuantity(((Number) itemMap.get("quantity")).intValue());
            cartItem.setRestaurantId((String) itemMap.get("restaurantId"));
            cartItem.setImageUrl((String) itemMap.get("imageUrl"));
            cartItem.setNote((String) itemMap.get("note"));

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

        new Handler().postDelayed(() -> {
            if (getParentFragment() instanceof OrderFragment) {
                ((OrderFragment) getParentFragment()).switchToCartTab();
            }
        }, 1000);
    }

    private void showOrderDetail(Order order) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_order_detail, null);

        TextView tvOrderId = dialogView.findViewById(R.id.tvOrderId);
        TextView tvRestaurantName = dialogView.findViewById(R.id.tvRestaurantName);
        TextView tvOrderDate = dialogView.findViewById(R.id.tvOrderDate);
        TextView tvPaymentMethod = dialogView.findViewById(R.id.tvPaymentMethod);
        TextView tvItems = dialogView.findViewById(R.id.tvItems);
        TextView tvOrderNote = dialogView.findViewById(R.id.tvOrderNote);
        TextView tvTotalPrice = dialogView.findViewById(R.id.tvTotalPrice);
        TextView tvStatus = dialogView.findViewById(R.id.tvStatus);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        String orderCode = order.getOrderCode();
        if (orderCode == null || orderCode.isEmpty()) {
            String id = order.getId();
            orderCode = id != null && id.length() > 8 ? id.substring(0, 8) : id;
        }
        tvOrderId.setText(orderCode);

        tvRestaurantName.setText(RestaurantHelper.getRestaurantNameFromOrder(order));

        if (order.getCreatedAt() > 0) {
            tvOrderDate.setText(sdf.format(new Date(order.getCreatedAt())));
        } else {
            tvOrderDate.setText("Đang cập nhật");
        }

        String paymentMethod = order.getPaymentMethod();
        if ("COD".equals(paymentMethod)) {
            tvPaymentMethod.setText("💵 Thanh toán khi nhận hàng");
        } else if ("Wallet".equals(paymentMethod)) {
            tvPaymentMethod.setText("💳 Ví điện tử");
        } else {
            tvPaymentMethod.setText(paymentMethod != null ? paymentMethod : "COD");
        }

        tvTotalPrice.setText(f.format(order.getFinalTotal()) + "đ");
        tvStatus.setText("✅ Đã giao thành công");
        tvStatus.setTextColor(0xFF4CAF50);

        StringBuilder itemsText = new StringBuilder();
        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                String name = (String) item.get("name");
                long quantity = 1;
                Object qtyObj = item.get("quantity");
                if (qtyObj instanceof Long) quantity = (Long) qtyObj;
                else if (qtyObj instanceof Double) quantity = ((Double) qtyObj).longValue();
                itemsText.append("• ").append(name).append(" x").append(quantity).append("\n");

                String note = (String) item.get("note");
                if (note != null && !note.isEmpty()) {
                    itemsText.append("  📝 ").append(note).append("\n");
                }
            }
        }
        tvItems.setText(itemsText.toString());

        String orderNote = order.getOrderNote();
        if (orderNote != null && !orderNote.isEmpty()) {
            tvOrderNote.setText(orderNote);
        } else {
            tvOrderNote.setText("Không có ghi chú");
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialog.show();

        btnClose.setOnClickListener(v -> dialog.dismiss());
    }

    public void refreshData() {
        loadOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    // ==================== ADAPTER ====================
    static class OrderReceivedAdapter extends RecyclerView.Adapter<OrderReceivedAdapter.ViewHolder> {
        private List<Order> orders;
        private OnReorderListener reorderListener;
        private OnItemClickListener itemClickListener;

        interface OnReorderListener { void onReorder(Order order); }
        interface OnItemClickListener { void onItemClick(Order order); }

        OrderReceivedAdapter(List<Order> orders, OnReorderListener reorderListener, OnItemClickListener itemClickListener) {
            this.orders = orders;
            this.reorderListener = reorderListener;
            this.itemClickListener = itemClickListener;
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
            holder.bind(order, reorderListener, itemClickListener);
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

            void bind(Order order, OnReorderListener reorderListener, OnItemClickListener itemClickListener) {
                NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

                String orderCode = order.getOrderCode();
                if (orderCode == null || orderCode.isEmpty()) {
                    String id = order.getId();
                    orderCode = id != null && id.length() > 6 ? id.substring(0, 6) : "ORD";
                }
                tvOrderId.setText("Mã: #" + orderCode);

                if (order.getCreatedAt() > 0) {
                    tvOrderDate.setText(sdf.format(new Date(order.getCreatedAt())));
                } else {
                    tvOrderDate.setText("Đang cập nhật");
                }

                tvRestaurantName.setText(RestaurantHelper.getRestaurantNameFromOrder(order));
                tvTotalPrice.setText(f.format(order.getFinalTotal()) + "đ");

                StringBuilder itemsText = new StringBuilder();
                if (order.getItems() != null) {
                    for (Map<String, Object> item : order.getItems()) {
                        String name = (String) item.get("name");
                        long quantity = 1;
                        Object qtyObj = item.get("quantity");
                        if (qtyObj instanceof Long) quantity = (Long) qtyObj;
                        else if (qtyObj instanceof Double) quantity = ((Double) qtyObj).longValue();
                        itemsText.append("• ").append(name).append(" x").append(quantity).append("\n");

                        String note = (String) item.get("note");
                        if (note != null && !note.isEmpty()) {
                            itemsText.append("  📝 ").append(note).append("\n");
                        }
                    }
                }
                tvFoodItems.setText(itemsText.toString());

                btnReorder.setOnClickListener(v -> reorderListener.onReorder(order));
                itemView.setOnClickListener(v -> itemClickListener.onItemClick(order));
            }
        }
    }
}