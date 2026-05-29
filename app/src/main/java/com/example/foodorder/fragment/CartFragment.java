package com.example.foodorder.fragment;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.adapter.CartAdapter;
import com.example.foodorder.adapter.VoucherAdapter;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Order;
import com.example.foodorder.model.Voucher;
import com.example.foodorder.repository.FirebaseRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartFragment extends Fragment {

    private RecyclerView rvCart;
    private TextView tvTotalPrice, tvDeliveryFee, tvDiscount, tvFinalTotal, tvEmptyCart;
    private TextView tvVoucherApplied, tvVoucherName;
    private Button btnCheckout, btnGoShopping;
    private LinearLayout layoutCartContent, layoutEmpty, layoutVoucher;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private List<Voucher> voucherList;
    private VoucherAdapter voucherAdapter;
    private FirebaseRepository repository;
    private FirebaseFirestore db;
    private String userId = "user123";
    private String userName = "";
    private String userPhone = "";
    private String userAddress = "";
    private Voucher selectedVoucher;
    private String selectedVoucherCode;
    private double discount = 0;
    private double deliveryFee = 15000;
    private boolean isApplyingVoucher = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        initViews(view);
        setupRecyclerView();
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

        if (getActivity() != null) {
            userId = getActivity().getSharedPreferences("UserPrefs", 0)
                    .getString("user_id", "user123");
        }

        layoutVoucher.setOnClickListener(v -> showVoucherDialog());
        btnCheckout.setOnClickListener(v -> showCheckoutDialog());
        btnGoShopping.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });
    }

    private void loadUserInfo() {
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);
            userName = prefs.getString("user_name", "");
            userPhone = prefs.getString("user_phone", "");
            userAddress = prefs.getString("user_address", "");
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
                            }
                        }
                    });
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
        repository.getCart(userId, new FirebaseRepository.OnDataLoaded<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> data) {
                cartItems.clear();
                cartItems.addAll(data);
                adapter.updateList(cartItems);
                calculateTotals();

                if (cartItems.isEmpty()) {
                    layoutCartContent.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    layoutCartContent.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                    loadAvailableVouchers();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
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

        // Lưu voucher được chọn tạm thời
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
        } else if ("freeship".equals(voucher.getDiscountType())) {
            deliveryFee = 0;
            discount = 0;
        } else {
            discount = voucher.getDiscountValue();
        }

        if (discount > subtotal) discount = subtotal;
        if (discount < 0) discount = 0;

        selectedVoucher = voucher;
        selectedVoucherCode = voucher.getCode();
        calculateTotals();

        String discountText = "percent".equals(voucher.getDiscountType()) ?
                (int) voucher.getDiscountValue() + "%" :
                NumberFormat.getInstance(new Locale("vi", "VN")).format(discount) + "đ";

        Toast.makeText(getContext(), "Áp dụng thành công! Giảm " + discountText, Toast.LENGTH_SHORT).show();
    }

    private void clearSelectedVoucher() {
        discount = 0;
        deliveryFee = 15000;
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

        if (discount > 0 && selectedVoucherCode != null) {
            tvVoucherApplied.setVisibility(View.VISIBLE);
            tvVoucherApplied.setText("Đã áp dụng: " + selectedVoucherCode);
            tvVoucherName.setText(selectedVoucherCode + " - Giảm " + f.format(discount) + "đ");
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_checkout, null);

        TextView tvOrderItems = dialogView.findViewById(R.id.tvOrderItems);
        TextView tvOrderSubtotal = dialogView.findViewById(R.id.tvOrderSubtotal);
        TextView tvOrderDelivery = dialogView.findViewById(R.id.tvOrderDelivery);
        TextView tvOrderDiscount = dialogView.findViewById(R.id.tvOrderDiscount);
        TextView tvOrderTotal = dialogView.findViewById(R.id.tvOrderTotal);
        EditText etDeliveryName = dialogView.findViewById(R.id.etDeliveryName);
        EditText etDeliveryPhone = dialogView.findViewById(R.id.etDeliveryPhone);
        EditText etDeliveryAddress = dialogView.findViewById(R.id.etDeliveryAddress);
        EditText etOrderNote = dialogView.findViewById(R.id.etOrderNote);
        RadioGroup rgPayment = dialogView.findViewById(R.id.rgPayment);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmOrder);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelOrder);

        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);
        etDeliveryName.setText(prefs.getString("user_name", ""));
        etDeliveryPhone.setText(prefs.getString("user_phone", ""));
        etDeliveryAddress.setText(prefs.getString("user_address", ""));

        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        double subtotal = 0;
        StringBuilder itemsText = new StringBuilder();

        for (CartItem item : cartItems) {
            // Tên món và số lượng
            itemsText.append("• ").append(item.getName()).append(" x").append(item.getQuantity());

            // HIỂN THỊ GHI CHÚ CỦA TỪNG MÓN
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                itemsText.append("\n  📝 ").append(item.getNote());
            }
            itemsText.append("\n");

            subtotal += item.getTotalPrice();
        }

        tvOrderItems.setText(itemsText.toString());
        double finalTotal = subtotal + deliveryFee - discount;
        tvOrderSubtotal.setText(f.format(subtotal) + "đ");
        tvOrderDelivery.setText(f.format(deliveryFee) + "đ");
        tvOrderDiscount.setText("-" + f.format(discount) + "đ");
        tvOrderTotal.setText(f.format(finalTotal) + "đ");

        AlertDialog dialog = builder.setView(dialogView).setTitle("Xác nhận đơn hàng").create();
        dialog.show();

        btnConfirm.setOnClickListener(v -> {
            String deliveryName = etDeliveryName.getText().toString().trim();
            String deliveryPhone = etDeliveryPhone.getText().toString().trim();
            String deliveryAddress = etDeliveryAddress.getText().toString().trim();
            String orderNote = etOrderNote.getText().toString();

            if (deliveryName.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
                return;
            }
            if (deliveryPhone.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }
            if (deliveryAddress.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit().putString("user_name", deliveryName).apply();
            prefs.edit().putString("user_phone", deliveryPhone).apply();
            prefs.edit().putString("user_address", deliveryAddress).apply();

            int selectedId = rgPayment.getCheckedRadioButtonId();
            String paymentMethod = "COD";
            if (selectedId == R.id.rbCOD) paymentMethod = "COD";
            else if (selectedId == R.id.rbMomo) paymentMethod = "Momo";

            createOrder(orderNote, paymentMethod, deliveryName, deliveryPhone, deliveryAddress);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void createOrder(String orderNote, String paymentMethod, String deliveryName, String deliveryPhone, String deliveryAddress) {
        if (isApplyingVoucher) return;
        isApplyingVoucher = true;
        btnCheckout.setEnabled(false);
        btnCheckout.setText("Đang xử lý...");

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderCode("ORD" + System.currentTimeMillis());
        order.setDeliveryName(deliveryName);
        order.setDeliveryPhone(deliveryPhone);
        order.setDeliveryAddress(deliveryAddress);

        if (!cartItems.isEmpty()) {
            order.setRestaurantId(cartItems.get(0).getRestaurantId());
            order.setRestaurantName(cartItems.get(0).getName());
        }

        List<Map<String, Object>> itemsMap = new ArrayList<>();
        double subtotal = 0;
        for (CartItem item : cartItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("foodId", item.getFoodId());
            map.put("name", item.getName());
            map.put("price", item.getPrice());
            map.put("quantity", item.getQuantity());
            map.put("imageUrl", item.getImageUrl());
            map.put("note", item.getNote() != null ? item.getNote() : "");
            itemsMap.add(map);
            subtotal += item.getTotalPrice();
        }

        order.setItems(itemsMap);
        order.setSubtotal(subtotal);
        order.setTotalPrice(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setDiscount(discount);
        order.setFinalTotal(Math.max(0, subtotal + deliveryFee - discount));
        order.setStatus("pending");
        order.setCreatedAt(System.currentTimeMillis());
        order.setPaymentMethod(paymentMethod);
        order.setOrderNote(orderNote);
        if (selectedVoucherCode != null) order.setVoucherCode(selectedVoucherCode);

        repository.createOrder(order, new FirebaseRepository.OnDataLoaded<String>() {
            @Override
            public void onSuccess(String orderId) {
                repository.clearCart(userId, new FirebaseRepository.OnDataLoaded<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        clearSelectedVoucher();
                        loadCart();
                        isApplyingVoucher = false;
                        btnCheckout.setEnabled(true);
                        btnCheckout.setText("THANH TOÁN");
                        String paymentText = paymentMethod.equals("COD") ? "Thanh toán khi nhận hàng" : "Ví MoMo";
                        Toast.makeText(getContext(), "Đặt hàng thành công!\nMã: #" + orderId + "\n" + paymentText, Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onError(String error) {
                        isApplyingVoucher = false;
                        btnCheckout.setEnabled(true);
                        btnCheckout.setText("THANH TOÁN");
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        loadCart();
                    }
                });
            }
            @Override
            public void onError(String error) {
                isApplyingVoucher = false;
                btnCheckout.setEnabled(true);
                btnCheckout.setText("THANH TOÁN");
                Toast.makeText(getContext(), "Đặt hàng thất bại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void refreshData() {
        loadCart();
        calculateTotals();
        loadAvailableVouchers();
    }
}