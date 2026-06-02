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
import com.example.foodorder.adapter.OrderHistoryAdapter;
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;
import com.example.foodorder.utils.CacheManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private OrderHistoryAdapter adapter;
    private List<Order> orderList;
    private FirebaseRepository repository;
    private CacheManager cacheManager;
    private String userId = "user123";
    private boolean isLoading = false;

    // Danh sách từ khóa tên món ăn
    private static final String[] FOOD_KEYWORDS = {"Phở", "Cà phê", "Cơm", "Pizza", "Burger",
            "Gà", "Trà", "Sữa", "Bánh", "Cháo", "Bún", "Mì", "Xôi", "Nem", "Chả"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);
        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        progressBar = view.findViewById(R.id.progressBar);

        repository = FirebaseRepository.getInstance();
        if (getContext() != null) {
            cacheManager = new CacheManager(getContext());
        }
        orderList = new ArrayList<>();

        if (getActivity() != null) {
            userId = getActivity().getSharedPreferences("UserPrefs", 0)
                    .getString("user_id", "user123");
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
        adapter = new OrderHistoryAdapter(orderList,
                order -> {
                    showOrderDetail(order);
                },
                (order, position) -> {
                    String status = order.getStatus();
                    if ("cancelled".equals(status) || "delivered".equals(status)) {
                        showDeleteConfirmDialog(order, position);
                    } else {
                        Toast.makeText(getContext(), "Chỉ có thể xóa đơn hàng đã giao hoặc đã hủy", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    }

    private void showDeleteConfirmDialog(Order order, int position) {
        String statusText = "delivered".equals(order.getStatus()) ? "đã giao" : "đã hủy";
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa đơn hàng")
                .setMessage("Bạn có chắc chắn muốn xóa đơn hàng " + statusText + " #" + order.getOrderCode() + " khỏi lịch sử?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteOrder(order, position);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteOrder(Order order, int position) {
        progressBar.setVisibility(View.VISIBLE);
        repository.deleteOrder(order.getId(), new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                orderList.remove(position);
                adapter.updateList(orderList);

                if (cacheManager != null) {
                    cacheManager.cacheOrders(orderList);
                }

                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Đã xóa đơn hàng #" + order.getOrderCode(), Toast.LENGTH_SHORT).show();

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi xóa: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lấy tên nhà hàng từ order
    private String getRestaurantNameFromOrder(Order order) {
        // 1. Kiểm tra restaurantName có hợp lệ không (không phải tên món)
        String restaurantName = order.getRestaurantName();
        if (restaurantName != null && !restaurantName.isEmpty() && !isFoodName(restaurantName)) {
            return restaurantName;
        }

        // 2. Kiểm tra restaurantId
        String restaurantId = order.getRestaurantId();
        if (restaurantId != null && !restaurantId.isEmpty() && !isFoodName(restaurantId)) {
            return restaurantId;
        }

        // 3. Lấy từ items (tên nhà hàng có thể lưu trong item)
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            Map<String, Object> firstItem = order.getItems().get(0);
            String itemRestaurant = (String) firstItem.get("restaurantName");
            if (itemRestaurant != null && !itemRestaurant.isEmpty() && !isFoodName(itemRestaurant)) {
                return itemRestaurant;
            }
            String itemRestaurantId = (String) firstItem.get("restaurantId");
            if (itemRestaurantId != null && !itemRestaurantId.isEmpty() && !isFoodName(itemRestaurantId)) {
                return itemRestaurantId;
            }
        }

        // 4. Nếu vẫn không có, lấy tên món đầu tiên và bỏ quantity
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            Map<String, Object> firstItem = order.getItems().get(0);
            String itemName = (String) firstItem.get("name");
            if (itemName != null) {
                // Loại bỏ " x1", " x2" ở cuối
                itemName = itemName.replaceAll("\\s+x\\d+$", "").trim();
                return itemName + " (Quán)";
            }
        }

        return "Nhà hàng";
    }

    // Kiểm tra xem có phải tên món ăn không
    private boolean isFoodName(String name) {
        if (name == null) return true;
        for (String keyword : FOOD_KEYWORDS) {
            if (name.contains(keyword)) {
                return true;
            }
        }
        return false;
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

        // Mã đơn
        String orderCode = order.getOrderCode();
        if (orderCode == null || orderCode.isEmpty()) {
            String id = order.getId();
            orderCode = id != null && id.length() > 8 ? id.substring(0, 8) : id;
        }
        tvOrderId.setText(orderCode);

        // Tên nhà hàng
        String restaurantName = order.getRestaurantName();
        if (restaurantName == null || restaurantName.isEmpty()) {
            restaurantName = "Nhà hàng";
        }
        tvRestaurantName.setText(restaurantName);

        // Ngày đặt
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        if (order.getCreatedAt() > 0) {
            tvOrderDate.setText(sdf.format(new java.util.Date(order.getCreatedAt())));
        } else {
            tvOrderDate.setText("Đang cập nhật");
        }

        // Phương thức thanh toán
        String paymentMethod = order.getPaymentMethod();
        if ("COD".equals(paymentMethod)) {
            tvPaymentMethod.setText("💵 Thanh toán khi nhận hàng");
        } else if ("Banking".equals(paymentMethod)) {
            tvPaymentMethod.setText("🏦 Chuyển khoản ngân hàng");
        } else if ("Wallet".equals(paymentMethod)) {
            tvPaymentMethod.setText("💳 Ví điện tử");
        } else {
            tvPaymentMethod.setText(paymentMethod != null ? paymentMethod : "COD");
        }

        // Tổng tiền
        tvTotalPrice.setText(String.format("%,.0fđ", order.getFinalTotal()));

        // TRẠNG THÁI - SỬA "pending" THÀNH "Chờ xác nhận"
        String status = order.getStatus();
        if ("pending".equals(status)) {
            tvStatus.setText("⏳ Chờ xác nhận");
            tvStatus.setTextColor(0xFFFF9800);
        } else if ("delivered".equals(status)) {
            tvStatus.setText("✅ Đã giao thành công");
            tvStatus.setTextColor(0xFF4CAF50);
        } else if ("cancelled".equals(status)) {
            tvStatus.setText("❌ Đã hủy");
            tvStatus.setTextColor(0xFFF44336);
        } else if ("delivering".equals(status)) {
            tvStatus.setText("🚚 Đang giao");
            tvStatus.setTextColor(0xFF2196F3);
        } else {
            tvStatus.setText(status != null ? status : "Đang xử lý");
        }

        // Danh sách món
        StringBuilder itemsText = new StringBuilder();
        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                String name = (String) item.get("name");
                long quantity = 1;
                Object qtyObj = item.get("quantity");
                if (qtyObj instanceof Long) {
                    quantity = (Long) qtyObj;
                } else if (qtyObj instanceof Double) {
                    quantity = ((Double) qtyObj).longValue();
                } else if (qtyObj instanceof Integer) {
                    quantity = (Integer) qtyObj;
                }
                itemsText.append("• ").append(name).append(" x").append(quantity);

                String note = (String) item.get("note");
                if (note != null && !note.isEmpty()) {
                    itemsText.append("\n  📝 ").append(note);
                }
                itemsText.append("\n");
            }
        }
        tvItems.setText(itemsText.toString());

        // Ghi chú đơn hàng
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

    private void loadOrders() {
        if (isLoading) return;

        if (cacheManager != null) {
            List<Order> cachedOrders = cacheManager.getCachedOrders();
            if (cachedOrders != null && !cachedOrders.isEmpty()) {
                orderList.clear();
                orderList.addAll(cachedOrders);
                adapter.updateList(orderList);

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvOrders.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                }
            }
        }

        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        repository.getUserOrders(userId, new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orderList.clear();
                if (data != null && !data.isEmpty()) {
                    orderList.addAll(data);
                }
                adapter.updateList(orderList);

                if (cacheManager != null) {
                    cacheManager.cacheOrders(orderList);
                }

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
                if (orderList.isEmpty()) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
                isLoading = false;
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
}