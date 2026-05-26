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
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private FirebaseRepository repository;
    private FirebaseFirestore db;
    private String userId = "user123";
    private double discount = 0;
    private double deliveryFee = 15000;
    private String selectedVoucherCode = null;
    private boolean isCreatingOrder = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        initViews(view);
        setupRecyclerView();
        loadCart();
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

    private void setupRecyclerView() {
        adapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                updateCartItem(item);
                calculateTotals();
            }

            @Override
            public void onItemRemoved(CartItem item, int position) {
                // Xóa trực tiếp khỏi danh sách trước khi gọi API
                cartItems.remove(position);
                adapter.updateList(cartItems);
                calculateTotals();

                // Sau đó mới gọi API xóa trên server
                removeFromCart(item.getFoodId());

                if (cartItems.isEmpty()) {
                    layoutCartContent.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                    discount = 0;
                    selectedVoucherCode = null;
                    tvVoucherApplied.setVisibility(View.GONE);
                }
                Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
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
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartItem(CartItem item) {
        repository.updateCartItem(userId, item, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {}
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromCart(String foodId) {
        repository.removeFromCart(userId, foodId, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {}
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                loadCart(); // Reload nếu lỗi
            }
        });
    }

    private void calculateTotals() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
        double finalTotal = subtotal + deliveryFee - discount;
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

    private void showVoucherDialog() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_voucher, null);
        EditText etVoucherCode = dialogView.findViewById(R.id.etVoucherCode);
        Button btnApply = dialogView.findViewById(R.id.btnApplyVoucher);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelVoucher);

        AlertDialog dialog = builder.setView(dialogView).setTitle("Nhập mã voucher").create();
        dialog.show();

        btnApply.setOnClickListener(v -> {
            String code = etVoucherCode.getText().toString().trim().toUpperCase();
            if (!code.isEmpty()) {
                checkAndApplyVoucher(code);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Nhập mã voucher", Toast.LENGTH_SHORT).show();
            }
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void checkAndApplyVoucher(String code) {
        double subtotal = 0;
        for (CartItem item : cartItems) subtotal += item.getTotalPrice();
        final double finalSubtotal = subtotal;

        db.collection("vouchers")
                .whereEqualTo("code", code)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Map<String, Object> data = query.getDocuments().get(0).getData();
                        String discountType = (String) data.get("discountType");
                        double discountValue = ((Number) data.get("discountValue")).doubleValue();
                        double minOrder = ((Number) data.get("minOrder")).doubleValue();

                        if (finalSubtotal >= minOrder) {
                            if ("percent".equals(discountType)) {
                                discount = finalSubtotal * discountValue / 100;
                            } else {
                                discount = discountValue;
                            }
                            if (discount > finalSubtotal) discount = finalSubtotal;
                            selectedVoucherCode = code;
                            calculateTotals();
                            Toast.makeText(getContext(), "Áp dụng thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
                            Toast.makeText(getContext(), "Đơn tối thiểu " + f.format(minOrder) + "đ", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Mã không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCheckoutDialog() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isCreatingOrder) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_checkout, null);

        TextView tvOrderItems = dialogView.findViewById(R.id.tvOrderItems);
        TextView tvOrderSubtotal = dialogView.findViewById(R.id.tvOrderSubtotal);
        TextView tvOrderDelivery = dialogView.findViewById(R.id.tvOrderDelivery);
        TextView tvOrderDiscount = dialogView.findViewById(R.id.tvOrderDiscount);
        TextView tvOrderTotal = dialogView.findViewById(R.id.tvOrderTotal);
        RadioGroup rgPayment = dialogView.findViewById(R.id.rgPayment);
        EditText etOrderNote = dialogView.findViewById(R.id.etOrderNote);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmOrder);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelOrder);

        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        double subtotal = 0;
        StringBuilder itemsText = new StringBuilder();
        for (CartItem item : cartItems) {
            itemsText.append("• ").append(item.getName()).append(" x").append(item.getQuantity()).append("\n");
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
            int selectedId = rgPayment.getCheckedRadioButtonId();
            String paymentMethod = "COD";
            if (selectedId == R.id.rbCOD) paymentMethod = "COD";
            else if (selectedId == R.id.rbMomo) paymentMethod = "Momo";
            createOrder(etOrderNote.getText().toString(), paymentMethod);
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void createOrder(String orderNote, String paymentMethod) {
        isCreatingOrder = true;
        btnCheckout.setEnabled(false);
        btnCheckout.setText("Đang xử lý...");

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderCode("ORD" + System.currentTimeMillis());
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
                        discount = 0;
                        selectedVoucherCode = null;
                        loadCart();
                        isCreatingOrder = false;
                        btnCheckout.setEnabled(true);
                        btnCheckout.setText("THANH TOÁN");
                        String paymentText = paymentMethod.equals("COD") ? "Thanh toán khi nhận hàng" : "Ví MoMo";
                        Toast.makeText(getContext(), "Đặt hàng thành công!\nMã: #" + orderId + "\n" + paymentText, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String error) {
                        isCreatingOrder = false;
                        btnCheckout.setEnabled(true);
                        btnCheckout.setText("THANH TOÁN");
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        loadCart();
                    }
                });
            }

            @Override
            public void onError(String error) {
                isCreatingOrder = false;
                btnCheckout.setEnabled(true);
                btnCheckout.setText("THANH TOÁN");
                Toast.makeText(getContext(), "Đặt hàng thất bại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void refreshData() {
        loadCart();
        calculateTotals();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
        calculateTotals();
    }
}