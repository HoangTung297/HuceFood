package com.example.foodorder.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;
import com.example.foodorder.utils.LoginSessionManager;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeliveringFragment extends Fragment {

    private RecyclerView rvDeliveringOrders;
    private LinearLayout layoutEmpty;
    private OrderAdapter adapter;
    private List<Order> orderList;
    private FirebaseRepository repository;
    private LoginSessionManager sessionManager;
    private static final String TAG = "DeliveringFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivering, container, false);

        rvDeliveringOrders = view.findViewById(R.id.rvDeliveringOrders);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        repository = FirebaseRepository.getInstance();
        sessionManager = new LoginSessionManager(requireContext());
        orderList = new ArrayList<>();

        setupRecyclerView();
        loadOrders();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(orderList,
                order -> showOrderDetailDialog(order),
                order -> showCancelConfirmDialog(order),
                order -> showReceivedConfirmDialog(order)
        );
        rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDeliveringOrders.setAdapter(adapter);
    }

    private void showOrderDetailDialog(Order order) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_order_detail);

        // Ánh xạ view
        TextView tvOrderId = dialog.findViewById(R.id.tvOrderId);
        TextView tvStatus = dialog.findViewById(R.id.tvStatus);
        TextView tvRestaurantName = dialog.findViewById(R.id.tvRestaurantName);
        TextView tvOrderDate = dialog.findViewById(R.id.tvOrderDate);
        TextView tvPaymentMethod = dialog.findViewById(R.id.tvPaymentMethod);
        TextView tvItems = dialog.findViewById(R.id.tvItems);
        TextView tvOrderNote = dialog.findViewById(R.id.tvOrderNote);
        TextView tvTotalPrice = dialog.findViewById(R.id.tvTotalPrice);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

        // Set dữ liệu
        String orderCode = order.getOrderCode();
        if (orderCode == null || orderCode.isEmpty()) {
            String id = order.getId();
            orderCode = id != null && id.length() > 8 ? id.substring(0, 8) : id;
        }
        tvOrderId.setText(orderCode);

        String status = order.getStatus();
        if ("pending".equals(status)) {
            tvStatus.setText("Chờ xác nhận");
            tvStatus.setTextColor(0xFFFF9800);
        } else if ("delivering".equals(status)) {
            tvStatus.setText("Đang giao");
            tvStatus.setTextColor(0xFF2196F3);
        } else if ("delivered".equals(status)) {
            tvStatus.setText("Đã giao");
            tvStatus.setTextColor(0xFF4CAF50);
        } else if ("cancelled".equals(status)) {
            tvStatus.setText("Đã hủy");
            tvStatus.setTextColor(0xFFF44336);
        }

        tvRestaurantName.setText(order.getRestaurantName() != null ? order.getRestaurantName() : "Nhà hàng");

        if (order.getCreatedAt() > 0) {
            tvOrderDate.setText(sdf.format(new Date(order.getCreatedAt())));
        } else {
            tvOrderDate.setText("Đang cập nhật");
        }

        String paymentMethod = order.getPaymentMethod();
        if ("COD".equals(paymentMethod)) {
            tvPaymentMethod.setText("Thanh toán khi nhận hàng");
        } else if ("Banking".equals(paymentMethod)) {
            tvPaymentMethod.setText("Chuyển khoản ngân hàng");
        } else if ("Wallet".equals(paymentMethod)) {
            tvPaymentMethod.setText("Ví điện tử");
        } else {
            tvPaymentMethod.setText(paymentMethod != null ? paymentMethod : "COD");
        }

        // Danh sách món
        StringBuilder items = new StringBuilder();
        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                String name = (String) item.get("name");
                long quantity = 1;
                Object qtyObj = item.get("quantity");
                if (qtyObj instanceof Long) quantity = (Long) qtyObj;
                else if (qtyObj instanceof Double) quantity = ((Double) qtyObj).longValue();
                else if (qtyObj instanceof Integer) quantity = (Integer) qtyObj;
                items.append("• ").append(name).append(" x").append(quantity).append("\n");
            }
        }
        tvItems.setText(items.toString());

        // Ghi chú
        String note = order.getOrderNote();
        if (note == null || note.isEmpty()) {
            tvOrderNote.setText("Không có ghi chú");
        } else {
            tvOrderNote.setText(note);
        }

        // Tổng tiền
        tvTotalPrice.setText(f.format(order.getFinalTotal()) + "đ");

        // Đóng dialog
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String getUserId() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            userId = prefs.getString("user_email", "");
        }
        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }
        return userId;
    }

    private void loadOrders() {
        String userId = getUserId();

        if (userId.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvDeliveringOrders.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        rvDeliveringOrders.setVisibility(View.VISIBLE);

        // Lấy đơn hàng pending và delivering
        repository.getOrdersByStatus(userId, "pending", new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orderList.clear();
                if (data != null && !data.isEmpty()) {
                    orderList.addAll(data);
                }

                repository.getOrdersByStatus(userId, "delivering", new FirebaseRepository.OnDataLoaded<List<Order>>() {
                    @Override
                    public void onSuccess(List<Order> deliveringData) {
                        if (deliveringData != null && !deliveringData.isEmpty()) {
                            orderList.addAll(deliveringData);
                        }
                        adapter.notifyDataSetChanged();

                        if (orderList.isEmpty()) {
                            rvDeliveringOrders.setVisibility(View.GONE);
                            layoutEmpty.setVisibility(View.VISIBLE);
                        } else {
                            rvDeliveringOrders.setVisibility(View.VISIBLE);
                            layoutEmpty.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        adapter.notifyDataSetChanged();
                        if (orderList.isEmpty()) {
                            rvDeliveringOrders.setVisibility(View.GONE);
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                layoutEmpty.setVisibility(View.VISIBLE);
                rvDeliveringOrders.setVisibility(View.GONE);
            }
        });
    }

    private void showCancelConfirmDialog(Order order) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #" + order.getOrderCode() + " không?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> cancelOrder(order))
                .setNegativeButton("Quay lại", null)
                .show();
    }

    private void showReceivedConfirmDialog(Order order) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận đã nhận hàng")
                .setMessage("Bạn đã nhận được đơn hàng #" + order.getOrderCode() + " chưa?")
                .setPositiveButton("Đã nhận hàng", (dialog, which) -> confirmReceived(order))
                .setNegativeButton("Chưa", null)
                .show();
    }

    private void cancelOrder(Order order) {
        repository.updateOrderStatus(order.getId(), "cancelled", new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(getContext(), "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                loadOrders();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi hủy đơn: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmReceived(Order order) {
        repository.updateOrderStatus(order.getId(), "delivered", new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(getContext(), "Cảm ơn bạn! Đã nhận hàng thành công", Toast.LENGTH_SHORT).show();
                loadOrders();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
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
    static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private List<Order> orders;
        private OnOrderClickListener clickListener;
        private OnCancelListener cancelListener;
        private OnReceivedListener receivedListener;

        interface OnOrderClickListener { void onOrderClick(Order order); }
        interface OnCancelListener { void onCancel(Order order); }
        interface OnReceivedListener { void onReceived(Order order); }

        OrderAdapter(List<Order> orders, OnOrderClickListener clickListener,
                     OnCancelListener cancelListener, OnReceivedListener receivedListener) {
            this.orders = orders;
            this.clickListener = clickListener;
            this.cancelListener = cancelListener;
            this.receivedListener = receivedListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_delivering_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.bind(order, clickListener, cancelListener, receivedListener);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvStatus, tvOrderDate, tvRestaurantName, tvFoodItems, tvTotalPrice;
            Button btnCancel, btnReceived;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
                tvFoodItems = itemView.findViewById(R.id.tvFoodItems);
                tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
                btnCancel = itemView.findViewById(R.id.btnCancel);
                btnReceived = itemView.findViewById(R.id.btnReceived);
            }

            void bind(Order order, OnOrderClickListener clickListener,
                      OnCancelListener cancelListener, OnReceivedListener receivedListener) {
                NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

                String orderCode = order.getOrderCode();
                if (orderCode == null || orderCode.isEmpty()) {
                    String id = order.getId();
                    orderCode = id != null && id.length() > 6 ? id.substring(0, 6) : "ORD";
                }
                tvOrderId.setText("Mã: #" + orderCode);

                String status = order.getStatus();
                if ("pending".equals(status)) {
                    tvStatus.setText("⏳ Chờ xác nhận");
                    tvStatus.setTextColor(0xFFFF9800);
                } else if ("delivering".equals(status)) {
                    tvStatus.setText("🚚 Đang giao");
                    tvStatus.setTextColor(0xFF2196F3);
                } else if ("delivered".equals(status)) {
                    tvStatus.setText("✅ Đã giao");
                    tvStatus.setTextColor(0xFF4CAF50);
                } else if ("cancelled".equals(status)) {
                    tvStatus.setText("❌ Đã hủy");
                    tvStatus.setTextColor(0xFFF44336);
                }

                // Hiển thị nút
                if ("delivered".equals(status) || "cancelled".equals(status)) {
                    btnCancel.setVisibility(View.GONE);
                    btnReceived.setVisibility(View.GONE);
                } else {
                    btnCancel.setVisibility(View.VISIBLE);
                    btnReceived.setVisibility(View.VISIBLE);
                }

                if (order.getCreatedAt() > 0) {
                    tvOrderDate.setText(sdf.format(new Date(order.getCreatedAt())));
                } else {
                    tvOrderDate.setText("Đang cập nhật");
                }

                tvRestaurantName.setText(order.getRestaurantName() != null ? order.getRestaurantName() : "Nhà hàng");
                tvTotalPrice.setText(f.format(order.getFinalTotal()) + "đ");

                StringBuilder items = new StringBuilder();
                if (order.getItems() != null) {
                    for (Map<String, Object> item : order.getItems()) {
                        String name = (String) item.get("name");
                        long quantity = 1;
                        Object qtyObj = item.get("quantity");
                        if (qtyObj instanceof Long) quantity = (Long) qtyObj;
                        else if (qtyObj instanceof Double) quantity = ((Double) qtyObj).longValue();
                        else if (qtyObj instanceof Integer) quantity = (Integer) qtyObj;
                        items.append("• ").append(name).append(" x").append(quantity).append("\n");
                    }
                }
                tvFoodItems.setText(items.toString());

                btnCancel.setOnClickListener(v -> cancelListener.onCancel(order));
                btnReceived.setOnClickListener(v -> receivedListener.onReceived(order));
                itemView.setOnClickListener(v -> clickListener.onOrderClick(order));
            }
        }
    }
}