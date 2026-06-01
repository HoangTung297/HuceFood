package com.example.foodorder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorder.adapter.ProductItemAdapter;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Voucher;
import com.example.foodorder.repository.FirebaseRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvTotalPrice, tvDeliveryFee, tvVoucherDiscount, tvFinalTotal, tvEmptyCart;
    private TextView tvAppliedVoucher;
    private Button btnCheckout, btnSelectVoucher, btnContinue;
    private ProductItemAdapter adapter;
    private List<CartItem> cartItems;
    private Toolbar toolbar;
    private FirebaseRepository repository;
    private String userId = "user123";
    private double deliveryFee = 15000;
    private double discount = 0;
    private Voucher appliedVoucher = null;

    ActivityResultLauncher<Intent> voucherLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    appliedVoucher = (Voucher) result.getData().getSerializableExtra("selected_voucher");
                    if (appliedVoucher != null) {
                        // Lưu tạm voucher trước khi apply để kiểm tra kết quả
                        Voucher tempVoucher = appliedVoucher;
                        applyVoucherDiscount(appliedVoucher);
                        // Sau khi apply, kiểm tra lại: nếu không đủ điều kiện, appliedVoucher đã bị set null
                        if (appliedVoucher != null) {
                            Toast.makeText(this, "Đã áp dụng: " + appliedVoucher.getTitle(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Voucher không hợp lệ hoặc không đủ điều kiện", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Người dùng hủy chọn voucher
                        discount = 0;
                        tvAppliedVoucher.setText("");
                        calculateTotals();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        repository = FirebaseRepository.getInstance();
        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("user_id", "user123");

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadCartItems();
        calculateTotals();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvCart = findViewById(R.id.rvCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvVoucherDiscount = findViewById(R.id.tvVoucherDiscount);
        tvFinalTotal = findViewById(R.id.tvFinalTotal);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        tvAppliedVoucher = findViewById(R.id.tvAppliedVoucher);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnSelectVoucher = findViewById(R.id.btnSelectVoucher);
        btnContinue = findViewById(R.id.btnContinue);

        btnSelectVoucher.setOnClickListener(v -> selectVoucher());
        btnContinue.setOnClickListener(v -> continueShopping());
        btnCheckout.setOnClickListener(v -> checkout());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Giỏ hàng");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        cartItems = new ArrayList<>();
        adapter = new ProductItemAdapter(cartItems,
                item -> {},
                (item, newQuantity) -> {
                    updateCartItem(item);
                    calculateTotals();
                    if (appliedVoucher != null) {
                        applyVoucherDiscount(appliedVoucher);
                    }
                },
                (item, position) -> removeFromCart(item, position)
        );
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);
    }

    private void loadCartItems() {
        if (getIntent().hasExtra("cart_list")) {
            ArrayList<CartItem> receivedList = (ArrayList<CartItem>) getIntent().getSerializableExtra("cart_list");
            if (receivedList != null) {
                cartItems.clear();
                cartItems.addAll(receivedList);
                adapter.updateList(cartItems);
            }
        } else {
            repository.getCart(userId, new FirebaseRepository.OnDataLoaded<List<CartItem>>() {
                @Override
                public void onSuccess(List<CartItem> data) {
                    cartItems.clear();
                    if (data != null) cartItems.addAll(data);
                    adapter.updateList(cartItems);
                    calculateTotals();
                    updateCartVisibility();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(CartActivity.this, "Lỗi tải giỏ hàng: " + error, Toast.LENGTH_SHORT).show();
                    updateCartVisibility();
                }
            });
        }
        updateCartVisibility();
    }

    private void updateCartVisibility() {
        if (cartItems.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
            rvCart.setVisibility(View.VISIBLE);
        }
    }

    private void updateCartItem(CartItem item) {
        repository.updateCartItem(userId, item, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {}
            @Override
            public void onError(String error) {
                Toast.makeText(CartActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromCart(CartItem item, int position) {
        cartItems.remove(position);
        adapter.updateList(cartItems);
        calculateTotals();
        updateCartVisibility();
        if (cartItems.isEmpty()) {
            appliedVoucher = null;
            discount = 0;
            tvAppliedVoucher.setText("");
        } else if (appliedVoucher != null) {
            applyVoucherDiscount(appliedVoucher);
        }

        repository.removeFromCart(userId, item.getFoodId(), new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {}
            @Override
            public void onError(String error) {
                Toast.makeText(CartActivity.this, "Không đồng bộ được: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        Toast.makeText(this, "Đã xóa món", Toast.LENGTH_SHORT).show();
    }

    private void calculateTotals() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
        double finalTotal = subtotal + deliveryFee - discount;
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvTotalPrice.setText(formatter.format(subtotal) + "đ");
        tvDeliveryFee.setText(formatter.format(deliveryFee) + "đ");
        tvVoucherDiscount.setText("-" + formatter.format(discount) + "đ");
        tvFinalTotal.setText(formatter.format(finalTotal) + "đ");
    }

    private void selectVoucher() {
        Intent intent = new Intent(this, VoucherSelectionActivity.class);
        voucherLauncher.launch(intent);
    }

    private void applyVoucherDiscount(Voucher voucher) {
        if (voucher == null) return;

        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }

        if (subtotal < voucher.getMinOrder()) {
            Toast.makeText(this, "Đơn hàng chưa đạt tối thiểu " + formatCurrency(voucher.getMinOrder()), Toast.LENGTH_SHORT).show();
            appliedVoucher = null;
            discount = 0;
            tvAppliedVoucher.setText("");
        } else {
            if ("percent".equals(voucher.getDiscountType())) {
                discount = subtotal * voucher.getDiscountValue() / 100.0;
            } else if ("freeship".equals(voucher.getDiscountType())) {
                discount = deliveryFee;
            } else {
                discount = voucher.getDiscountValue();
            }
            if (discount > subtotal) {
                discount = subtotal;
            }
            tvAppliedVoucher.setText("Đang áp dụng: " + voucher.getTitle());
            // Giữ nguyên appliedVoucher (không set null)
        }
        calculateTotals();
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + "đ";
    }

    private void continueShopping() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("cart_list", new ArrayList<>(cartItems));
        if (appliedVoucher != null) {
            intent.putExtra("voucher", appliedVoucher);
            intent.putExtra("discount", discount);
            intent.putExtra("voucher_code", appliedVoucher.getCode());
        }
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}