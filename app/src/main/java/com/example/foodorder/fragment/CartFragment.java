package com.example.foodorder.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.CheckoutActivity;
import com.example.foodorder.HomeActivity;
import com.example.foodorder.R;
import com.example.foodorder.adapter.CartAdapter;
import com.example.foodorder.adapter.VoucherAdapter;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Voucher;
import com.example.foodorder.repository.FirebaseRepository;
import com.example.foodorder.utils.ShippingFeeHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment {

    private static final int REQUEST_CHECKOUT = 100;
    private static final String TAG = "CART_FRAGMENT";
    public static final String ACTION_REFRESH_NOTIFICATIONS = "REFRESH_NOTIFICATIONS";

    private RecyclerView rvCart;
    private TextView tvTotalPrice, tvDeliveryFee, tvDiscount, tvFinalTotal, tvEmptyCart;
    private TextView tvVoucherApplied, tvVoucherName;
    private Button btnCheckout, btnGoShopping;
    private LinearLayout layoutCartContent, layoutEmpty, layoutVoucher;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private List<Voucher> voucherList;
    private FirebaseRepository repository;
    private FirebaseFirestore db;
    private String userId = "";
    private String userEmail = "";
    private String userName = "";
    private String userPhone = "";
    private String userAddress = "";
    private Voucher selectedVoucher;
    private String selectedVoucherCode;
    private double discount = 0;
    private double deliveryFee = 15000;
    private boolean isApplyingVoucher = false;

    private final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (HomeActivity.ACTION_REFRESH_CART.equals(intent.getAction())) {
                Log.d(TAG, "Received refresh broadcast, reloading cart...");
                refreshData();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(HomeActivity.ACTION_REFRESH_CART);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(refreshReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        initViews(view);
        setupRecyclerView();

        showEmptyLayoutImmediately();
        loadUserId();
        loadCart();
        loadUserInfo();
        return view;
    }

    private void initViews(View view) {
        rvCart = view.findViewById(R.id.rvCart);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        tvDeliveryFee = view.findViewById(R.id.tvDeliveryFee);
        tvDiscount = view.findViewById(R.id.tvDiscount);
        tvFinalTotal = view.findViewById(R.id.tvFinalTotal);
        tvEmptyCart = view.findViewById(R.id.tvEmptyCart);
        tvVoucherApplied = view.findViewById(R.id.tvVoucherApplied);
        tvVoucherName = view.findViewById(R.id.tvVoucherName);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        btnGoShopping = view.findViewById(R.id.btnGoShopping);
        layoutCartContent = view.findViewById(R.id.layoutCartContent);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        layoutVoucher = view.findViewById(R.id.layoutVoucher);

        repository = FirebaseRepository.getInstance();
        db = FirebaseFirestore.getInstance();
        cartItems = new ArrayList<>();
        voucherList = new ArrayList<>();

        layoutVoucher.setOnClickListener(v -> showVoucherDialog());
        btnCheckout.setOnClickListener(v -> showCheckoutDialog());

        btnGoShopping.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToHome();
            }
        });
    }

    /**
     * Lấy userId từ email của user (ví dụ: tung@gmail.com)
     */
    private void loadUserId() {
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);

            // Ưu tiên lấy user_email làm userId
            userEmail = prefs.getString("user_email", "");
            userId = prefs.getString("user_id", "");

            // Nếu userId rỗng hoặc là giá trị mặc định, dùng email làm userId
            if (userId == null || userId.isEmpty() || userId.equals("user123")) {
                userId = userEmail;
            }

            // Nếu vẫn rỗng, dùng email mặc định
            if (userId == null || userId.isEmpty()) {
                userId = "tung@gmail.com";
            }

            Log.d(TAG, "📧 Loaded userId from email: " + userId);
        }

        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }
    }

    /**
     * Lấy userId hiện tại
     */
    private String getCurrentUserId() {
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);
            String email = prefs.getString("user_email", "");
            String id = prefs.getString("user_id", "");

            // Ưu tiên dùng email làm userId
            if (email != null && !email.isEmpty()) {
                return email;
            }
            if (id != null && !id.isEmpty() && !id.equals("user123")) {
                return id;
            }
        }
        return "tung@gmail.com";
    }

    private void showEmptyLayoutImmediately() {
        layoutCartContent.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        rvCart.setVisibility(View.GONE);
    }

    private void loadUserInfo() {
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);
            userName = prefs.getString("user_name", "");
            userPhone = prefs.getString("user_phone", "");
            userAddress = prefs.getString("user_address", "");

            if (userAddress.isEmpty()) {
                userAddress = prefs.getString("delivery_address", "");
            }

            updateShippingFeeBasedOnAddress();
        }

        if (userName.isEmpty() || userPhone.isEmpty()) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            userName = doc.getString("name") != null ? doc.getString("name") : "";
                            userPhone = doc.getString("phone") != null ? doc.getString("phone") : "";
                            userAddress = doc.getString("address") != null ? doc.getString("address") : "";

                            if (getActivity() != null) {
                                SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);
                                prefs.edit().putString("user_name", userName).apply();
                                prefs.edit().putString("user_phone", userPhone).apply();
                                prefs.edit().putString("user_address", userAddress).apply();
                                prefs.edit().putString("delivery_address", userAddress).apply();
                            }

                            updateShippingFeeBasedOnAddress();
                            calculateTotals();
                        }
                    });
        }
    }

    private void updateShippingFeeBasedOnAddress() {
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);
            String address = prefs.getString("delivery_address", "");
            if (address == null || address.isEmpty()) {
                address = prefs.getString("user_address", "");
            }
            if (selectedVoucher == null || !"freeship".equals(selectedVoucher.getDiscountType())) {
                deliveryFee = ShippingFeeHelper.getShippingFee(address);
                Log.d(TAG, "updateShippingFeeBasedOnAddress - deliveryFee: " + deliveryFee);
            }
        }
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                updateCartItem(item);
                calculateTotals();
                clearSelectedVoucher();
            }

            @Override
            public void onItemRemoved(CartItem item, int position) {
                removeFromCart(item.getFoodId(), position);
                calculateTotals();
                clearSelectedVoucher();
            }

            @Override
            public void onNoteChanged(CartItem item, String note) {
                updateCartItem(item);
            }
        });
        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCart.setAdapter(adapter);
    }

    private void loadCart() {
        if (userId == null || userId.isEmpty() || userId.equals("user123")) {
            loadUserId();
        }

        Log.d(TAG, "🛒 Loading cart for userId: " + userId);

        repository.getCart(userId, new FirebaseRepository.OnDataLoaded<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> data) {
                cartItems.clear();
                cartItems.addAll(data);
                adapter.updateList(cartItems);
                updateShippingFeeBasedOnAddress();
                calculateTotals();

                if (cartItems.isEmpty()) {
                    layoutCartContent.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvCart.setVisibility(View.GONE);
                } else {
                    layoutCartContent.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                    rvCart.setVisibility(View.VISIBLE);
                    loadAvailableVouchers();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                layoutCartContent.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                rvCart.setVisibility(View.GONE);
            }
        });
    }

    private void loadAvailableVouchers() {
        if (isApplyingVoucher) return;
        isApplyingVoucher = true;
        voucherList.clear();

        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
        final double finalSubtotal = subtotal;

        db.collection("vouchers")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String code = doc.getString("code");
                        String title = doc.getString("title");
                        String description = doc.getString("description");
                        String discountType = doc.getString("discountType");
                        double discountValue = doc.getDouble("discountValue") != null ? doc.getDouble("discountValue") : 0;
                        double minOrder = doc.getDouble("minOrder") != null ? doc.getDouble("minOrder") : 0;
                        double minFoodPrice = doc.getDouble("minFoodPrice") != null ? doc.getDouble("minFoodPrice") : 0;

                        boolean valid = true;
                        for (CartItem item : cartItems) {
                            if (item.getPrice() < minFoodPrice) {
                                valid = false;
                                break;
                            }
                        }

                        if (valid && finalSubtotal >= minOrder) {
                            Voucher v = new Voucher();
                            v.setCode(code);
                            v.setTitle(title);
                            v.setDescription(description);
                            v.setDiscountType(discountType);
                            v.setDiscountValue(discountValue);
                            v.setMinOrder(minOrder);
                            voucherList.add(v);
                        }
                    }
                    isApplyingVoucher = false;
                })
                .addOnFailureListener(e -> {
                    isApplyingVoucher = false;
                });
    }

    private void showVoucherDialog() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (voucherList.isEmpty()) {
            Toast.makeText(getContext(), "Hiện chưa có voucher khả dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_voucher, null);

        RecyclerView rvVouchers = dialogView.findViewById(R.id.rvVouchers);
        Button btnApplyVoucher = dialogView.findViewById(R.id.btnApplyVoucher);
        Button btnCancelVoucher = dialogView.findViewById(R.id.btnCancelVoucher);

        final Voucher[] tempSelectedVoucher = {null};
        final int[] tempSelectedPosition = {-1};

        VoucherAdapter dialogAdapter = new VoucherAdapter(voucherList, (voucher, position, isSelected) -> {
            if (isSelected) {
                tempSelectedVoucher[0] = voucher;
                tempSelectedPosition[0] = position;
                Toast.makeText(getContext(), "Đã chọn: " + voucher.getTitle(), Toast.LENGTH_SHORT).show();
            } else {
                tempSelectedVoucher[0] = null;
                tempSelectedPosition[0] = -1;
                Toast.makeText(getContext(), "Đã bỏ chọn: " + voucher.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        rvVouchers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVouchers.setAdapter(dialogAdapter);

        AlertDialog dialog = builder.setView(dialogView).setTitle("Chọn voucher").create();
        dialog.show();

        btnApplyVoucher.setOnClickListener(v -> {
            if (tempSelectedVoucher[0] != null) {
                applyVoucher(tempSelectedVoucher[0]);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Vui lòng chọn voucher để áp dụng", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelVoucher.setOnClickListener(v -> dialog.dismiss());
    }

    private void applyVoucher(Voucher voucher) {
        double subtotal = 0;
        for (CartItem item : cartItems) subtotal += item.getTotalPrice();

        if ("percent".equals(voucher.getDiscountType())) {
            discount = subtotal * voucher.getDiscountValue() / 100;
            if (voucher.getDiscountValue() == 50 && discount > 100000) {
                discount = 100000;
            }
            deliveryFee = ShippingFeeHelper.getDefaultFee();
        } else if ("freeship".equals(voucher.getDiscountType())) {
            deliveryFee = 0;
            discount = 0;
        } else {
            discount = voucher.getDiscountValue();
            deliveryFee = ShippingFeeHelper.getDefaultFee();
        }

        if (discount > subtotal) discount = subtotal;
        if (discount < 0) discount = 0;

        selectedVoucher = voucher;
        selectedVoucherCode = voucher.getCode();

        calculateTotals();

        String discountText = "";
        if ("percent".equals(voucher.getDiscountType())) {
            discountText = (int) voucher.getDiscountValue() + "%";
        } else if ("freeship".equals(voucher.getDiscountType())) {
            discountText = "FREESHIP";
        } else {
            discountText = NumberFormat.getInstance(new Locale("vi", "VN")).format(discount) + "đ";
        }

        Toast.makeText(getContext(), "Áp dụng thành công! Giảm " + discountText, Toast.LENGTH_SHORT).show();
    }

    private void clearSelectedVoucher() {
        discount = 0;
        updateShippingFeeBasedOnAddress();
        selectedVoucher = null;
        selectedVoucherCode = null;
        tvVoucherApplied.setVisibility(View.GONE);
        calculateTotals();
        loadAvailableVouchers();
    }

    private void updateCartItem(CartItem item) {
        repository.updateCartItem(userId, item, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override public void onSuccess(Void data) {}
            @Override public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromCart(String foodId, int position) {
        repository.removeFromCart(userId, foodId, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                cartItems.remove(position);
                adapter.updateList(cartItems);
                calculateTotals();
                if (cartItems.isEmpty()) {
                    layoutCartContent.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                    discount = 0;
                    selectedVoucher = null;
                    tvVoucherApplied.setVisibility(View.GONE);
                }
                Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateTotals() {
        double subtotal = 0;
        for (CartItem item : cartItems) subtotal += item.getTotalPrice();

        double finalTotal = subtotal + deliveryFee - discount;
        if (finalTotal < 0) finalTotal = 0;

        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(f.format(subtotal) + "đ");
        tvDeliveryFee.setText(f.format(deliveryFee) + "đ");
        tvDiscount.setText("-" + f.format(discount) + "đ");
        tvFinalTotal.setText(f.format(finalTotal) + "đ");

        if (selectedVoucherCode != null) {
            tvVoucherApplied.setVisibility(View.VISIBLE);

            String discountText = "";
            if (selectedVoucher != null) {
                if ("freeship".equals(selectedVoucher.getDiscountType())) {
                    discountText = "FREESHIP";
                } else if ("percent".equals(selectedVoucher.getDiscountType())) {
                    discountText = (int) selectedVoucher.getDiscountValue() + "%";
                } else {
                    discountText = f.format(discount) + "đ";
                }
            }

            tvVoucherApplied.setText("Đã áp dụng: " + selectedVoucherCode);
            tvVoucherName.setText(selectedVoucherCode + " - Giảm " + discountText);
        } else {
            tvVoucherApplied.setVisibility(View.GONE);
            tvVoucherName.setText("Chưa chọn voucher");
        }
    }

    private void showCheckoutDialog() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isApplyingVoucher) return;

        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }

        updateShippingFeeBasedOnAddress();

        double finalTotal = subtotal + deliveryFee - discount;

        // Đảm bảo userId là email
        String currentUserId = getCurrentUserId();

        Intent intent = new Intent(getActivity(), CheckoutActivity.class);
        intent.putExtra("totalAmount", subtotal);
        intent.putExtra("finalTotal", finalTotal);
        intent.putExtra("discount", discount);
        intent.putExtra("deliveryFee", deliveryFee);
        intent.putExtra("cartItems", new ArrayList<>(cartItems));
        intent.putExtra("userId", currentUserId); // Truyền userId (email) sang CheckoutActivity

        Log.d(TAG, "📤 Starting CheckoutActivity with userId: " + currentUserId);
        startActivityForResult(intent, REQUEST_CHECKOUT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_CHECKOUT && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                boolean refresh = data.getBooleanExtra("refresh", false);
                String orderId = data.getStringExtra("orderId");

                Log.d(TAG, "📦 refresh=" + refresh + ", orderId=" + orderId);

                if (refresh && orderId != null && !orderId.isEmpty()) {
                    // Tạo thông báo cho đơn hàng mới
                    createOrderNotification(orderId);
                } else {
                    Log.w(TAG, "⚠️ OrderId is null or empty, cannot create notification");
                }
                refreshData();
                Toast.makeText(getContext(), "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
            } else {
                // Nếu data null, vẫn refresh
                Log.w(TAG, "⚠️ Result data is null");
                refreshData();
            }
        }
    }

    /**
     * Tạo thông báo khi đặt hàng thành công
     */
    private void createOrderNotification(String orderId) {
        if (getActivity() == null) {
            Log.e(TAG, "❌ Activity is null, cannot create notification");
            return;
        }

        // Đảm bảo userId là email
        String currentUserId = getCurrentUserId();

        Log.d(TAG, "📝 Creating notification for userId: " + currentUserId + ", orderId: " + orderId);

        HashMap<String, Object> notification = new HashMap<>();
        notification.put("userId", currentUserId); // Lưu email làm userId
        notification.put("title", "Đặt hàng thành công 🎉");
        notification.put("message", "Đơn hàng #" + orderId + " đã được đặt thành công. Nhà hàng sẽ xác nhận trong giây lát.");
        notification.put("type", "order");
        notification.put("createdAt", System.currentTimeMillis());
        notification.put("isRead", false);
        notification.put("orderId", orderId);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "✅ Đã tạo thông báo thành công! ID: " + docRef.getId());
                    Log.d(TAG, "📧 userId trong notification: " + currentUserId);

                    // Gửi broadcast để cập nhật NotificationFragment
                    Intent intent = new Intent(ACTION_REFRESH_NOTIFICATIONS);
                    LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);
                    Log.d(TAG, "📡 Đã gửi broadcast REFRESH_NOTIFICATIONS");

                    // Hiển thị Toast thông báo
                    Toast.makeText(getContext(), "📬 Bạn có thông báo mới!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Lỗi tạo thông báo: " + e.getMessage());
                    Toast.makeText(getContext(), "Không thể tạo thông báo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void refreshData() {
        loadCart();
        calculateTotals();
        loadAvailableVouchers();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(refreshReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
    }
}