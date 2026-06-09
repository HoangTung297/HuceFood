package com.foodorder.admin.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.foodorder.admin.R;
import com.foodorder.admin.adapters.AdminOrderAdapter;
import com.foodorder.admin.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageOrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private AdminOrderAdapter adapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private NumberFormat currencyFormat;
    private SimpleDateFormat sdf;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_orders, container, false);

        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        rvOrders = view.findViewById(R.id.rvOrders);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        setupRecyclerView();
        loadOrders();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(orderList, new AdminOrderAdapter.OnOrderActionListener() {
            @Override
            public void onUpdateStatus(Order order, String newStatus) {
                updateOrderStatus(order, newStatus);
            }

            @Override
            public void onViewDetail(Order order) {
                showOrderDetailDialog(order);
            }
        });
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("orders")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Order order = doc.toObject(Order.class);
                        order.setId(doc.getId());
                        orderList.add(order);
                    }
                    adapter.updateList(orderList);
                    progressBar.setVisibility(View.GONE);
                    updateEmptyView();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateOrderStatus(Order order, String newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("updatedAt", System.currentTimeMillis());

        if ("delivered".equals(newStatus)) {
            updates.put("deliveredAt", System.currentTimeMillis());
        }
        if ("cancelled".equals(newStatus)) {
            updates.put("cancelledAt", System.currentTimeMillis());
        }

        db.collection("orders").document(order.getId()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    order.setStatus(newStatus);
                    int pos = orderList.indexOf(order);
                    if (pos >= 0) {
                        adapter.notifyItemChanged(pos);
                    }
                    Toast.makeText(getContext(), "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showOrderDetailDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);

        // Mã đơn hàng
        TextView tvOrderId = new TextView(getContext());
        tvOrderId.setTextSize(16);
        tvOrderId.setTypeface(null, android.graphics.Typeface.BOLD);
        tvOrderId.setText("Mã đơn: " + (order.getOrderCode() != null ? order.getOrderCode() : order.getId()));
        layout.addView(tvOrderId);

        // Ngày đặt
        TextView tvDate = new TextView(getContext());
        tvDate.setText("Ngày đặt: " + (order.getCreatedAtMillis() > 0 ? sdf.format(new Date(order.getCreatedAtMillis())) : "Đang cập nhật"));
        layout.addView(tvDate);

        addDivider(layout);

        // Thông tin khách hàng
        TextView tvCustomerTitle = new TextView(getContext());
        tvCustomerTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvCustomerTitle.setText("👤 Thông tin khách hàng");
        layout.addView(tvCustomerTitle);

        layout.addView(createTextView("Tên: " + (order.getDeliveryName() != null ? order.getDeliveryName() : "Khách hàng")));
        layout.addView(createTextView("SĐT: " + (order.getDeliveryPhone() != null ? order.getDeliveryPhone() : "Chưa cập nhật")));
        layout.addView(createTextView("Địa chỉ: " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Chưa cập nhật")));

        addDivider(layout);

        // Thông tin đơn hàng
        TextView tvOrderTitle = new TextView(getContext());
        tvOrderTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvOrderTitle.setText("🍽️ Thông tin đơn hàng");
        layout.addView(tvOrderTitle);

        layout.addView(createTextView("Nhà hàng: " + (order.getRestaurantName() != null ? order.getRestaurantName() : "Nhà hàng")));

        StringBuilder items = new StringBuilder();
        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                String name = (String) item.get("name");
                long qty = 1;
                Object qtyObj = item.get("quantity");
                if (qtyObj instanceof Long) qty = (Long) qtyObj;
                else if (qtyObj instanceof Double) qty = ((Double) qtyObj).longValue();
                items.append("• ").append(name).append(" x").append(qty).append("\n");
                String note = (String) item.get("note");
                if (note != null && !note.isEmpty()) {
                    items.append("  📝 ").append(note).append("\n");
                }
            }
        }
        layout.addView(createTextView(items.toString()));

        addDivider(layout);

        // Phương thức thanh toán
        String payment = "COD";
        if ("Wallet".equals(order.getPaymentMethod())) payment = "Ví điện tử";
        if ("Banking".equals(order.getPaymentMethod())) payment = "Chuyển khoản";
        layout.addView(createTextView("Phương thức: " + payment));

        // Trạng thái
        String status = order.getStatus();
        String statusText = getStatusText(status);
        layout.addView(createTextView("Trạng thái: " + statusText));

        // Tổng tiền
        TextView tvTotal = new TextView(getContext());
        tvTotal.setTextSize(16);
        tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTotal.setText("Tổng tiền: " + currencyFormat.format(order.getFinalTotal()) + "đ");
        layout.addView(tvTotal);

        builder.setTitle("Chi tiết đơn hàng")
                .setView(layout)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setPadding(0, 4, 0, 4);
        return tv;
    }

    private void addDivider(LinearLayout layout) {
        View divider = new View(getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(0xFFCCCCCC);
        int margin = 16;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) divider.getLayoutParams();
        params.setMargins(0, margin, 0, margin);
        divider.setLayoutParams(params);
        layout.addView(divider);
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "⏳ Chờ xác nhận";
            case "confirmed": return "✅ Đã xác nhận";
            case "preparing": return "🍳 Đang chuẩn bị";
            case "delivering": return "🚚 Đang giao";
            case "delivered": return "📦 Đã giao";
            case "cancelled": return "❌ Đã hủy";
            default: return status;
        }
    }

    private void updateEmptyView() {
        if (orderList.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}