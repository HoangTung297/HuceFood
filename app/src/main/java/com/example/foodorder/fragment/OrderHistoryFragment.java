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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    // Map ánh xạ ID nhà hàng sang tên thật
    private static final Map<String, String> RESTAURANT_NAME_MAP = new LinkedHashMap<>();
    static {
        RESTAURANT_NAME_MAP.put("pho_thin", "Phở Thìn");
        RESTAURANT_NAME_MAP.put("pho_thin_quan", "Phở Thìn");
        RESTAURANT_NAME_MAP.put("kfc", "KFC");
        RESTAURANT_NAME_MAP.put("cong_ca_phe", "Cộng Cà Phê");
        RESTAURANT_NAME_MAP.put("com_tam", "Cơm Tấm Ba Ghiền");
        RESTAURANT_NAME_MAP.put("pizza_hut", "Pizza Hut");
        RESTAURANT_NAME_MAP.put("lotteria", "Lotteria");
        RESTAURANT_NAME_MAP.put("ding_tea", "Ding Tea");
        RESTAURANT_NAME_MAP.put("mcdonalds", "McDonald's");
        RESTAURANT_NAME_MAP.put("bo_to_quan", "Bò Tơ Quán");
        RESTAURANT_NAME_MAP.put("pizza_pepperoni", "Domino's Pizza");
        RESTAURANT_NAME_MAP.put("dominos", "Domino's Pizza");
        RESTAURANT_NAME_MAP.put("domino", "Domino's Pizza");
        RESTAURANT_NAME_MAP.put("domino's", "Domino's Pizza");
        RESTAURANT_NAME_MAP.put("dominos_pizza", "Domino's Pizza");
        RESTAURANT_NAME_MAP.put("pho_xao_chien", "Phở Xào Chiến");
        RESTAURANT_NAME_MAP.put("pho_xao", "Phở Xào Chiến");
    }

    private static final String[] FOOD_KEYWORDS = {"Phở", "Cà phê", "Cơm", "Pizza", "Burger",
            "Gà", "Trà", "Sữa", "Bánh", "Cháo", "Bún", "Mì", "Khoai", "Coca", "Spaghetti", "Pepsi",
            "Pepperoni", "Bạc xíu", "Khoai tây"};

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
                order -> showOrderDetail(order),
                (order, position) -> {
                    String status = order.getStatus();
                    if ("delivered".equals(status) || "cancelled".equals(status)) {
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
                .setPositiveButton("Xóa", (dialog, which) -> deleteOrder(order, position))
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

    // Format tên nhà hàng từ ID
    private String formatRestaurantName(String id) {
        if (id == null) return "Nhà hàng";
        String[] parts = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1).toLowerCase());
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    // Lấy tên nhà hàng đúng từ item
    private String getCorrectRestaurantNameFromItem(Map<String, Object> item) {
        // 1. ƯU TIÊN LẤY TỪ RESTAURANT ID
        String restaurantId = (String) item.get("restaurantId");
        if (restaurantId != null && !restaurantId.isEmpty()) {
            String mappedName = RESTAURANT_NAME_MAP.get(restaurantId.toLowerCase());
            if (mappedName != null) {
                return mappedName;
            }
            // Nếu không có trong map, trả về restaurantId đã format
            return formatRestaurantName(restaurantId);
        }

        // 2. LẤY TỪ RESTAURANT NAME
        String name = (String) item.get("restaurantName");
        if (name != null && !name.isEmpty() && !isFoodName(name)) {
            return name;
        }

        // 3. FALLBACK: Lấy từ tên món
        String itemName = (String) item.get("name");
        if (itemName != null && !itemName.isEmpty()) {
            itemName = itemName.replaceAll("\\s+x\\d+$", "").trim();

            // Xử lý đặc biệt cho Pizza
            if (itemName.toLowerCase().contains("pizza") || itemName.toLowerCase().contains("pepperoni")) {
                return "Domino's Pizza";
            }

            // Xử lý đặc biệt cho Cà phê
            if (itemName.toLowerCase().contains("cà phê") || itemName.toLowerCase().contains("bạc xíu")) {
                return "Cộng Cà Phê";
            }

            // Xử lý đặc biệt cho Phở
            if (itemName.toLowerCase().contains("phở") || itemName.toLowerCase().contains("pho")) {
                return "Phở Thìn";
            }

            // Xử lý đặc biệt cho Gà rán
            if (itemName.toLowerCase().contains("gà") || itemName.toLowerCase().contains("kfc")) {
                return "KFC";
            }

            for (String keyword : FOOD_KEYWORDS) {
                if (itemName.contains(keyword)) {
                    return itemName;
                }
            }
        }

        return "Nhà hàng";
    }

    // Lấy danh sách tên nhà hàng duy nhất từ order
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
                    itemsText.append("- ").append(itemRestaurant).append(":\n");
                    currentRestaurant = itemRestaurant;
                }
                itemsText.append("  - ").append(name).append(" x").append(quantity).append("\n");

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