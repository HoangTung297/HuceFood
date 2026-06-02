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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DeliveringFragment extends Fragment {

    private RecyclerView rvDeliveringOrders;
    private LinearLayout layoutEmpty;
    private OrderAdapter adapter;
    private List<Order> orderList;
    private FirebaseRepository repository;
    private LoginSessionManager sessionManager;
    private static final String TAG = "DeliveringFragment";

    // Map ánh xạ ID nhà hàng -> tên
    private static final Map<String, String> RESTAURANT_MAP = new java.util.HashMap<>();
    static {
        RESTAURANT_MAP.put("pho_thin", "Phở Thìn");
        RESTAURANT_MAP.put("kfc", "KFC");
        RESTAURANT_MAP.put("cong_ca_phe", "Cộng Cà Phê");
        RESTAURANT_MAP.put("com_tam", "Cơm Tấm Ba Ghiền");
        RESTAURANT_MAP.put("pizza_hut", "Pizza Hut");
        RESTAURANT_MAP.put("lotteria", "Lotteria");
        RESTAURANT_MAP.put("ding_tea", "Ding Tea");
        RESTAURANT_MAP.put("mcdonalds", "McDonald's");
        RESTAURANT_MAP.put("bo_to_quan", "Bò Tơ Quán");
    }

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

    // Lấy danh sách tên nhà hàng duy nhất từ order
    private String getUniqueRestaurantNames(Order order) {
        Set<String> restaurantSet = new LinkedHashSet<>();

        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                String restaurantName = getRestaurantNameFromItem(item);
                if (restaurantName != null && !restaurantName.isEmpty()) {
                    restaurantSet.add(restaurantName);
                }
            }
        }

        // Nếu không có trong items, lấy từ order
        if (restaurantSet.isEmpty()) {
            String orderRestaurant = order.getRestaurantName();
            if (orderRestaurant != null && !orderRestaurant.isEmpty()) {
                return orderRestaurant;
            }
            return "Nhà hàng";
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String name : restaurantSet) {
            if (index > 0) sb.append(", ");
            sb.append(name);
            index++;
        }
        return sb.toString();
    }

    private String getRestaurantNameFromItem(Map<String, Object> item) {
        // Lấy từ restaurantName trong item
        String name = (String) item.get("restaurantName");
        if (name != null && !name.isEmpty()) {
            return name;
        }

        // Lấy từ restaurantId
        String restaurantId = (String) item.get("restaurantId");
        if (restaurantId != null && !restaurantId.isEmpty()) {
            String mappedName = RESTAURANT_MAP.get(restaurantId.toLowerCase());
            if (mappedName != null) {
                return mappedName;
            }
        }

        // Lấy từ tên món (fallback)
        String itemName = (String) item.get("name");
        if (itemName != null && !itemName.isEmpty()) {
            itemName = itemName.replaceAll("\\s+x\\d+$", "").trim();
            return itemName;
        }

        return "Nhà hàng";
    }

    private void showOrderDetailDialog(Order order) {
        try {
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.dialog_order_detail);

            TextView tvOrderId = dialog.findViewById(R.id.tvOrderId);
            TextView tvRestaurantName = dialog.findViewById(R.id.tvRestaurantName);
            TextView tvOrderDate = dialog.findViewById(R.id.tvOrderDate);
            TextView tvPaymentMethod = dialog.findViewById(R.id.tvPaymentMethod);
            TextView tvItems = dialog.findViewById(R.id.tvItems);
            TextView tvOrderNote = dialog.findViewById(R.id.tvOrderNote);
            TextView tvTotalPrice = dialog.findViewById(R.id.tvTotalPrice);
            TextView tvStatus = dialog.findViewById(R.id.tvStatus);
            Button btnClose = dialog.findViewById(R.id.btnClose);

            NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

            String orderCode = order.getOrderCode();
            if (orderCode == null || orderCode.isEmpty()) {
                String id = order.getId();
                orderCode = id != null && id.length() > 8 ? id.substring(0, 8) : id;
            }
            tvOrderId.setText(orderCode);

            // Hiển thị tất cả nhà hàng
            String restaurantNames = getUniqueRestaurantNames(order);
            tvRestaurantName.setText(restaurantNames);

            if (order.getCreatedAt() > 0) {
                tvOrderDate.setText(sdf.format(new Date(order.getCreatedAt())));
            }

            String paymentMethod = order.getPaymentMethod();
            if ("COD".equals(paymentMethod)) {
                tvPaymentMethod.setText("💵 Thanh toán khi nhận hàng");
            } else if ("Wallet".equals(paymentMethod)) {
                tvPaymentMethod.setText("💳 Ví điện tử");
            } else {
                tvPaymentMethod.setText(paymentMethod);
            }

            tvTotalPrice.setText(f.format(order.getFinalTotal()) + "đ");

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

            // Hiển thị danh sách món theo nhà hàng
            StringBuilder items = new StringBuilder();
            if (order.getItems() != null) {
                String currentRestaurant = "";
                for (Map<String, Object> item : order.getItems()) {
                    String name = (String) item.get("name");
                    long quantity = 1;
                    Object qtyObj = item.get("quantity");
                    if (qtyObj instanceof Long) quantity = (Long) qtyObj;
                    else if (qtyObj instanceof Double) quantity = ((Double) qtyObj).longValue();

                    String itemRestaurant = getRestaurantNameFromItem(item);

                    if (!itemRestaurant.equals(currentRestaurant)) {
                        if (!currentRestaurant.isEmpty()) {
                            items.append("\n");
                        }
                        items.append("🏠 ").append(itemRestaurant).append(":\n");
                        currentRestaurant = itemRestaurant;
                    }
                    items.append("  • ").append(name).append(" x").append(quantity).append("\n");

                    String note = (String) item.get("note");
                    if (note != null && !note.isEmpty()) {
                        items.append("    📝 ").append(note).append("\n");
                    }
                }
            }
            tvItems.setText(items.toString());

            String note = order.getOrderNote();
            tvOrderNote.setText(note != null && !note.isEmpty() ? note : "Không có ghi chú");

            btnClose.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
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
                removeOrderFromList(order);
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
                removeOrderFromList(order);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeOrderFromList(Order order) {
        int position = -1;
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getId().equals(order.getId())) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            orderList.remove(position);
            adapter.notifyItemRemoved(position);
        }

        if (orderList.isEmpty()) {
            rvDeliveringOrders.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
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
                    btnCancel.setVisibility(View.VISIBLE);
                    btnReceived.setVisibility(View.VISIBLE);
                } else if ("delivering".equals(status)) {
                    tvStatus.setText("🚚 Đang giao");
                    tvStatus.setTextColor(0xFF2196F3);
                    btnCancel.setVisibility(View.VISIBLE);
                    btnReceived.setVisibility(View.VISIBLE);
                } else if ("delivered".equals(status)) {
                    tvStatus.setText("✅ Đã giao");
                    tvStatus.setTextColor(0xFF4CAF50);
                    btnCancel.setVisibility(View.GONE);
                    btnReceived.setVisibility(View.GONE);
                } else if ("cancelled".equals(status)) {
                    tvStatus.setText("❌ Đã hủy");
                    tvStatus.setTextColor(0xFFF44336);
                    btnCancel.setVisibility(View.GONE);
                    btnReceived.setVisibility(View.GONE);
                }

                if (order.getCreatedAt() > 0) {
                    tvOrderDate.setText(sdf.format(new Date(order.getCreatedAt())));
                } else {
                    tvOrderDate.setText("Đang cập nhật");
                }

                // Hiển thị tất cả nhà hàng
                String restaurantNames = getUniqueRestaurantNames(order);
                tvRestaurantName.setText(restaurantNames);
                tvTotalPrice.setText(f.format(order.getFinalTotal()) + "đ");

                // Hiển thị danh sách món theo nhà hàng
                StringBuilder items = new StringBuilder();
                if (order.getItems() != null) {
                    String currentRestaurant = "";
                    for (Map<String, Object> item : order.getItems()) {
                        String name = (String) item.get("name");
                        long quantity = 1;
                        Object qtyObj = item.get("quantity");
                        if (qtyObj instanceof Long) quantity = (Long) qtyObj;
                        else if (qtyObj instanceof Double) quantity = ((Double) qtyObj).longValue();

                        String itemRestaurant = getRestaurantNameFromItem(item);

                        if (!itemRestaurant.equals(currentRestaurant)) {
                            if (!currentRestaurant.isEmpty()) {
                                items.append("\n");
                            }
                            items.append("🏠 ").append(itemRestaurant).append(":\n");
                            currentRestaurant = itemRestaurant;
                        }
                        items.append("  • ").append(name).append(" x").append(quantity).append("\n");

                        String note = (String) item.get("note");
                        if (note != null && !note.isEmpty()) {
                            items.append("    📝 ").append(note).append("\n");
                        }
                    }
                }
                tvFoodItems.setText(items.toString());

                btnCancel.setOnClickListener(v -> cancelListener.onCancel(order));
                btnReceived.setOnClickListener(v -> receivedListener.onReceived(order));
                itemView.setOnClickListener(v -> clickListener.onOrderClick(order));
            }

            private String getRestaurantNameFromItem(Map<String, Object> item) {
                String name = (String) item.get("restaurantName");
                if (name != null && !name.isEmpty()) {
                    return name;
                }

                String restaurantId = (String) item.get("restaurantId");
                if (restaurantId != null && !restaurantId.isEmpty()) {
                    String mappedName = RESTAURANT_MAP.get(restaurantId.toLowerCase());
                    if (mappedName != null) {
                        return mappedName;
                    }
                }

                String itemName = (String) item.get("name");
                if (itemName != null && !itemName.isEmpty()) {
                    itemName = itemName.replaceAll("\\s+x\\d+$", "").trim();
                    return itemName;
                }

                return "Nhà hàng";
            }

            private String getUniqueRestaurantNames(Order order) {
                Set<String> restaurantSet = new LinkedHashSet<>();

                if (order.getItems() != null) {
                    for (Map<String, Object> item : order.getItems()) {
                        String restaurantName = getRestaurantNameFromItem(item);
                        if (restaurantName != null && !restaurantName.isEmpty()) {
                            restaurantSet.add(restaurantName);
                        }
                    }
                }

                if (restaurantSet.isEmpty()) {
                    String orderRestaurant = order.getRestaurantName();
                    if (orderRestaurant != null && !orderRestaurant.isEmpty()) {
                        return orderRestaurant;
                    }
                    return "Nhà hàng";
                }

                StringBuilder sb = new StringBuilder();
                int index = 0;
                for (String name : restaurantSet) {
                    if (index > 0) sb.append(", ");
                    sb.append(name);
                    index++;
                }
                return sb.toString();
            }
        }
    }
}