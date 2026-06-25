package com.example.foodorder.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
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

public class RatingFragment extends Fragment {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private RatingOrderAdapter adapter;
    private List<Order> orderList;
    private FirebaseRepository repository;
    private LoginSessionManager sessionManager;
    private String userId = "user123";

    // Map ánh xạ ID nhà hàng sang tên thật
    private static final Map<String, String> RESTAURANT_NAME_MAP = new java.util.HashMap<>();
    static {
        RESTAURANT_NAME_MAP.put("pho_thin", "Phở Thìn");
        RESTAURANT_NAME_MAP.put("kfc", "KFC");
        RESTAURANT_NAME_MAP.put("cong_ca_phe", "Cộng Cà Phê");
        RESTAURANT_NAME_MAP.put("com_tam", "Cơm Tấm Ba Ghiền");
        RESTAURANT_NAME_MAP.put("pizza_hut", "Pizza Hut");
        RESTAURANT_NAME_MAP.put("lotteria", "Lotteria");
        RESTAURANT_NAME_MAP.put("ding_tea", "Ding Tea");
        RESTAURANT_NAME_MAP.put("mcdonalds", "McDonald's");
        RESTAURANT_NAME_MAP.put("bo_to_quan", "Bò Tơ Quán");
        RESTAURANT_NAME_MAP.put("pizza_pepperoni", "Domino's Pizza");
    }

    private static final String[] FOOD_KEYWORDS = {"Phở", "Cà phê", "Cơm", "Pizza", "Burger",
            "Gà", "Trà", "Sữa", "Bánh", "Cháo", "Bún", "Mì", "Khoai", "Coca", "Spaghetti", "Pepsi",
            "Pepperoni", "Bạc xíu"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating_list, container, false);

        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        repository = FirebaseRepository.getInstance();
        sessionManager = new LoginSessionManager(getContext());
        orderList = new ArrayList<>();

        userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            if (getActivity() != null) {
                userId = getActivity().getSharedPreferences("UserPrefs", 0)
                        .getString("user_email", "user123");
            }
        }

        setupRecyclerView();
        loadOrders();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new RatingOrderAdapter(orderList,
                order -> showOrderDetail(order),
                order -> showRatingDialog(order)
        );
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        repository.getOrdersByStatus(userId, "delivered", new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orderList.clear();
                for (Order order : data) {
                    if (!order.isRated()) {
                        orderList.add(order);
                    }
                }

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvOrders.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ============ CÁC HÀM XỬ LÝ NHÀ HÀNG ============

    private String getCorrectRestaurantNameFromItem(Map<String, Object> item) {
        String name = (String) item.get("restaurantName");
        if (name != null && !name.isEmpty() && !isFoodName(name)) {
            return name;
        }

        String restaurantId = (String) item.get("restaurantId");
        if (restaurantId != null && !restaurantId.isEmpty()) {
            String mappedName = RESTAURANT_NAME_MAP.get(restaurantId.toLowerCase());
            if (mappedName != null) {
                return mappedName;
            }
        }

        String itemName = (String) item.get("name");
        if (itemName != null && !itemName.isEmpty()) {
            itemName = itemName.replaceAll("\\s+x\\d+$", "").trim();
            if (itemName.contains("Pepperoni")) {
                return "Domino's Pizza";
            }
            if (itemName.contains("Bạc xíu")) {
                return "Cộng Cà Phê";
            }
            for (String keyword : FOOD_KEYWORDS) {
                if (itemName.contains(keyword)) {
                    return itemName + " (Quán)";
                }
            }
        }

        return "Nhà hàng";
    }

    private String getUniqueRestaurantNames(Order order) {
        Set<String> restaurantSet = new LinkedHashSet<>();

        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                String restaurantName = getCorrectRestaurantNameFromItem(item);
                restaurantSet.add(restaurantName);
            }
        }

        if (restaurantSet.isEmpty()) {
            return order.getRestaurantName() != null ? order.getRestaurantName() : "Nhà hàng";
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

    private boolean isFoodName(String name) {
        if (name == null) return true;
        for (String keyword : FOOD_KEYWORDS) {
            if (name.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // ============ HIỂN THỊ CHI TIẾT ĐƠN HÀNG ============

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

        String restaurantNames = getUniqueRestaurantNames(order);
        tvRestaurantName.setText(restaurantNames);

        if (order.getCreatedAt() != null) {
            tvOrderDate.setText(sdf.format(order.getCreatedAt()));
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

        String status = order.getStatus();
        if ("delivered".equals(status)) {
            tvStatus.setText("✅ Đã giao thành công");
            tvStatus.setTextColor(0xFF4CAF50);
        } else if ("cancelled".equals(status)) {
            tvStatus.setText("❌ Đã hủy");
            tvStatus.setTextColor(0xFFF44336);
        } else if ("pending".equals(status)) {
            tvStatus.setText("⏳ Chờ xác nhận");
            tvStatus.setTextColor(0xFFFF9800);
        } else if ("delivering".equals(status)) {
            tvStatus.setText("🚚 Đang giao");
            tvStatus.setTextColor(0xFF2196F3);
        }

        // Danh sách món kèm nhà hàng
        StringBuilder itemsText = new StringBuilder();
        if (order.getItems() != null) {
            String currentRestaurant = "";
            for (Map<String, Object> item : order.getItems()) {
                String name = (String) item.get("name");
                long quantity = 1;
                Object qtyObj = item.get("quantity");
                if (qtyObj instanceof Long) quantity = (Long) qtyObj;
                else if (qtyObj instanceof Double) quantity = ((Double) qtyObj).longValue();

                String itemRestaurant = getCorrectRestaurantNameFromItem(item);

                if (!itemRestaurant.equals(currentRestaurant)) {
                    if (!currentRestaurant.isEmpty()) {
                        itemsText.append("\n");
                    }
                    itemsText.append("🏠 ").append(itemRestaurant).append(":\n");
                    currentRestaurant = itemRestaurant;
                }
                itemsText.append("  • ").append(name).append(" x").append(quantity).append("\n");

                String note = (String) item.get("note");
                if (note != null && !note.isEmpty()) {
                    itemsText.append("    📝 ").append(note).append("\n");
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

    private void showRatingDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rating, null);

        TextView tvRestaurantName = dialogView.findViewById(R.id.tvRestaurantName);
        TextView tvOrderInfo = dialogView.findViewById(R.id.tvOrderInfo);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        tvRestaurantName.setText(getUniqueRestaurantNames(order));

        String dateString = "Đang cập nhật";
        if (order.getCreatedAt() != null) {
            dateString = sdf.format(order.getCreatedAt());
        }

        String orderInfo = "Mã đơn: " + order.getOrderCode() + "\n" +
                "Ngày đặt: " + dateString + "\n" +
                "Tổng tiền: " + f.format(order.getFinalTotal()) + "đ";
        tvOrderInfo.setText(orderInfo);

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Đánh giá nhà hàng")
                .setCancelable(false)
                .create();
        dialog.show();

        btnSubmit.setOnClickListener(v -> {
            float ratingValue = ratingBar.getRating();
            String comment = etComment.getText().toString();

            if (ratingValue == 0) {
                Toast.makeText(getContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }

            saveRating(order, ratingValue, comment, dialog);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void saveRating(Order order, double ratingValue, String comment, AlertDialog dialog) {
        repository.updateOrderRating(order.getId(), ratingValue, comment, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
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
            if (orderList.isEmpty()) {
                rvOrders.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
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

    // ==================== ADAPTER ====================
    static class RatingOrderAdapter extends RecyclerView.Adapter<RatingOrderAdapter.ViewHolder> {
        private List<Order> orders;
        private OnItemClickListener itemClickListener;
        private OnRateClickListener rateClickListener;

        interface OnItemClickListener { void onItemClick(Order order); }
        interface OnRateClickListener { void onRateClick(Order order); }

        RatingOrderAdapter(List<Order> orders, OnItemClickListener itemClickListener, OnRateClickListener rateClickListener) {
            this.orders = orders;
            this.itemClickListener = itemClickListener;
            this.rateClickListener = rateClickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_for_rating, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.bind(order, itemClickListener, rateClickListener);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvOrderDate, tvRestaurantName, tvFoodItems, tvTotalPrice;
            Button btnRate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
                tvFoodItems = itemView.findViewById(R.id.tvFoodItems);
                tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
                btnRate = itemView.findViewById(R.id.btnRate);
            }

            void bind(Order order, OnItemClickListener itemClickListener, OnRateClickListener rateClickListener) {
                NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                String orderCode = order.getOrderCode();
                if (orderCode == null || orderCode.isEmpty()) {
                    String id = order.getId();
                    orderCode = id != null && id.length() > 6 ? id.substring(0, 6) : "ORD";
                }
                tvOrderId.setText("Mã: #" + orderCode);

                if (order.getCreatedAt() != null) {
                    tvOrderDate.setText(sdf.format(order.getCreatedAt()));
                } else {
                    tvOrderDate.setText("Đang cập nhật");
                }

                // ===== SỬA: Thay thế RestaurantHelper bằng hàm lấy tên nhà hàng =====
                String restaurantName = getRestaurantNameFromOrder(order);
                tvRestaurantName.setText(restaurantName);
                // ================================================================

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

                btnRate.setOnClickListener(v -> rateClickListener.onRateClick(order));
                itemView.setOnClickListener(v -> itemClickListener.onItemClick(order));
            }

            // ===== HÀM LẤY TÊN NHÀ HÀNG TỪ ORDER =====
            private String getRestaurantNameFromOrder(Order order) {
                if (order.getItems() == null || order.getItems().isEmpty()) {
                    return order.getRestaurantName() != null ? order.getRestaurantName() : "Nhà hàng";
                }

                Map<String, Object> firstItem = order.getItems().get(0);
                String name = (String) firstItem.get("restaurantName");
                if (name != null && !name.isEmpty()) {
                    return name;
                }

                String restaurantId = (String) firstItem.get("restaurantId");
                if (restaurantId != null && !restaurantId.isEmpty()) {
                    String mappedName = RESTAURANT_NAME_MAP.get(restaurantId.toLowerCase());
                    if (mappedName != null) {
                        return mappedName;
                    }
                    return restaurantId;
                }

                return order.getRestaurantName() != null ? order.getRestaurantName() : "Nhà hàng";
            }
            // =========================================
        }
    }
}