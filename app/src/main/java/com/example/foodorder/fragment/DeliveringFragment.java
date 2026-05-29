package com.example.foodorder.fragment;

import android.app.AlertDialog;
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
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeliveringFragment extends Fragment {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private DeliveringAdapter adapter;
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
        adapter = new DeliveringAdapter(orderList,
                order -> {
                    showOrderDetail(order);
                },
                (order, position) -> {
                    showCancelConfirmDialog(order, position);
                },
                (order, position) -> {
                    confirmReceivedOrder(order, position);
                }
        );
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    }

    private void showCancelConfirmDialog(Order order, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc muốn hủy đơn hàng " + order.getOrderCode() + " không?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    cancelOrder(order, position);
                })
                .setNegativeButton("Quay lại", null)
                .show();
    }

    private void confirmReceivedOrder(Order order, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận đã nhận hàng")
                .setMessage("Bạn đã nhận được đơn hàng " + order.getOrderCode() + " chưa?")
                .setPositiveButton("Đã nhận", (dialog, which) -> {
                    updateOrderStatus(order, "delivered", position);
                })
                .setNegativeButton("Chưa", null)
                .show();
    }

    private void cancelOrder(Order order, int position) {
        progressBar.setVisibility(View.VISIBLE);
        repository.cancelOrder(order.getId(), new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                orderList.remove(position);
                adapter.updateList(orderList);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Đã hủy đơn hàng " + order.getOrderCode(), Toast.LENGTH_SHORT).show();

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }

                refreshOtherFragments();
            }
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderStatus(Order order, String status, int position) {
        progressBar.setVisibility(View.VISIBLE);
        repository.updateOrderStatus(order.getId(), status, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                orderList.remove(position);
                adapter.updateList(orderList);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Đã cập nhật trạng thái đơn hàng", Toast.LENGTH_SHORT).show();

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }

                refreshOtherFragments();
            }
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Sửa method showOrderDetail
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

        tvOrderId.setText(order.getOrderCode());
        tvRestaurantName.setText(order.getRestaurantName());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvOrderDate.setText(sdf.format(new java.util.Date(order.getCreatedAt())));
        tvPaymentMethod.setText(order.getPaymentMethod().equals("COD") ? "Thanh toán khi nhận hàng" : "Ví MoMo");
        tvTotalPrice.setText(String.format("%,.0fđ", order.getFinalTotal()));
        tvStatus.setText(order.getStatusText());

        // Hiển thị danh sách món kèm ghi chú
        StringBuilder itemsText = new StringBuilder();
        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                String name = (String) item.get("name");
                long quantity = ((Number) item.get("quantity")).longValue();
                itemsText.append("• ").append(name).append(" x").append(quantity);

                // HIỂN THỊ GHI CHÚ CỦA MÓN
                String note = (String) item.get("note");
                if (note != null && !note.isEmpty()) {
                    itemsText.append("\n  📝 ").append(note);
                }
                itemsText.append("\n");
            }
        }
        tvItems.setText(itemsText.toString());

        // Hiển thị ghi chú đơn hàng
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

    private void refreshOtherFragments() {
        if (getParentFragment() instanceof OrderFragment) {
            OrderFragment orderFragment = (OrderFragment) getParentFragment();
            orderFragment.refreshAllTabs();
        }
    }

    private void loadOrders() {
        if (isLoading) return;
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        List<String> statusList = Arrays.asList("pending", "preparing", "shipping");
        repository.getUserOrders(userId, new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orderList.clear();
                for (Order order : data) {
                    if (statusList.contains(order.getStatus())) {
                        orderList.add(order);
                    }
                }

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvOrders.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                    adapter.updateList(orderList);
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

    // THÊM METHOD NÀY
    public void refreshData() {
        loadOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    // Adapter inner class
    static class DeliveringAdapter extends RecyclerView.Adapter<DeliveringAdapter.ViewHolder> {
        private List<Order> orders;
        private OnItemClickListener listener;
        private OnCancelClickListener cancelListener;
        private OnReceivedClickListener receivedListener;

        interface OnItemClickListener { void onItemClick(Order order); }
        interface OnCancelClickListener { void onCancelClick(Order order, int position); }
        interface OnReceivedClickListener { void onReceivedClick(Order order, int position); }

        DeliveringAdapter(List<Order> orders, OnItemClickListener listener,
                          OnCancelClickListener cancelListener, OnReceivedClickListener receivedListener) {
            this.orders = orders;
            this.listener = listener;
            this.cancelListener = cancelListener;
            this.receivedListener = receivedListener;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_delivering_order, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(orders.get(position), listener, cancelListener, receivedListener, position);
        }

        @Override public int getItemCount() { return orders.size(); }

        void updateList(List<Order> newList) {
            this.orders = newList;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvOrderDate, tvRestaurantName, tvFoodItems, tvTotalPrice, tvStatus;
            Button btnCancel, btnReceived;

            ViewHolder(@NonNull View v) {
                super(v);
                tvOrderId = v.findViewById(R.id.tvOrderId);
                tvOrderDate = v.findViewById(R.id.tvOrderDate);
                tvRestaurantName = v.findViewById(R.id.tvRestaurantName);
                tvFoodItems = v.findViewById(R.id.tvFoodItems);
                tvTotalPrice = v.findViewById(R.id.tvTotalPrice);
                tvStatus = v.findViewById(R.id.tvStatus);
                btnCancel = v.findViewById(R.id.btnCancel);
                btnReceived = v.findViewById(R.id.btnReceived);
            }

            void bind(Order order, OnItemClickListener listener, OnCancelClickListener cancelListener,
                      OnReceivedClickListener receivedListener, int position) {
                tvOrderId.setText("Mã: " + (order.getOrderCode() != null ? order.getOrderCode() : order.getId()));
                tvOrderDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(new java.util.Date(order.getCreatedAt())));
                tvRestaurantName.setText(order.getRestaurantName());

                StringBuilder sb = new StringBuilder();
                if (order.getItems() != null) {
                    for (Map<String, Object> item : order.getItems()) {
                        sb.append("• ").append(item.get("name")).append(" x").append(item.get("quantity")).append("\n");
                    }
                }
                tvFoodItems.setText(sb.toString());
                tvTotalPrice.setText(String.format("%,.0fđ", order.getFinalTotal()));
                tvStatus.setText(order.getStatusText());

                btnCancel.setOnClickListener(v -> cancelListener.onCancelClick(order, position));
                btnReceived.setOnClickListener(v -> receivedListener.onReceivedClick(order, position));
                itemView.setOnClickListener(v -> listener.onItemClick(order));
            }
        }
    }
}