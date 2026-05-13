package com.example.foodorder;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.adapter.CartGroupAdapter;
import com.example.foodorder.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private CartGroupAdapter cartAdapter;
    private List<CartItem> cartItems;
    private TextView tvTotalPrice, tvDeliveryFee, tvVoucherDiscount, tvFinalTotal, tvEmptyCart;
    private Button btnCheckout, btnContinue, btnSelectVoucher;
    private Toolbar toolbar;

    private double deliveryFee = 15000;
    private double voucherDiscount = 0;
    private String selectedVoucher = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartItems = (List<CartItem>) getIntent().getSerializableExtra("cart_items");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        updateTotalPrice();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvCart = findViewById(R.id.rvCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvVoucherDiscount = findViewById(R.id.tvVoucherDiscount);
        tvFinalTotal = findViewById(R.id.tvFinalTotal);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnContinue = findViewById(R.id.btnContinue);
        btnSelectVoucher = findViewById(R.id.btnSelectVoucher);

        if (tvDeliveryFee != null) {
            tvDeliveryFee.setText(String.format("%,.0fđ", deliveryFee));
        }

        if (btnSelectVoucher != null) {
            btnSelectVoucher.setOnClickListener(v -> showVoucherDialog());
        }

        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(v -> checkout());
        }

        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> finish());
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Giỏ hàng");
        }
    }

    private void setupRecyclerView() {
        cartAdapter = new CartGroupAdapter(cartItems, new CartGroupAdapter.OnCartChangeListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                if (newQuantity <= 0) {
                    cartItems.remove(item);
                } else {
                    item.setQuantity(newQuantity);
                }
                cartAdapter.updateData(cartItems);
                updateTotalPrice();

                if (cartItems.isEmpty() && tvEmptyCart != null) {
                    tvEmptyCart.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onItemDeleted(CartItem item) {
                cartItems.remove(item);
                cartAdapter.updateData(cartItems);
                updateTotalPrice();

                if (cartItems.isEmpty() && tvEmptyCart != null) {
                    tvEmptyCart.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNoteChanged(CartItem item, String note) {
                item.setNote(note);
                cartAdapter.updateData(cartItems);
            }
        });

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(cartAdapter);

        if (cartItems.isEmpty() && tvEmptyCart != null) {
            tvEmptyCart.setVisibility(View.VISIBLE);
        } else if (tvEmptyCart != null) {
            tvEmptyCart.setVisibility(View.GONE);
        }
    }

    private double getSubTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    private void updateTotalPrice() {
        double subTotal = getSubTotal();
        double finalTotal = subTotal + deliveryFee - voucherDiscount;

        if (tvTotalPrice != null) {
            tvTotalPrice.setText(String.format("%,.0fđ", subTotal));
        }
        if (tvFinalTotal != null) {
            tvFinalTotal.setText(String.format("%,.0fđ", finalTotal));
        }

        if (tvVoucherDiscount != null) {
            if (voucherDiscount > 0) {
                tvVoucherDiscount.setText(String.format("- %,.0fđ", voucherDiscount));
            } else {
                tvVoucherDiscount.setText("0đ");
            }
        }
    }

    private void showVoucherDialog() {
        String[] vouchers = {
                "Giảm 20.000đ cho đơn từ 100.000đ",
                "Giảm 50.000đ cho đơn từ 200.000đ",
                "Miễn phí vận chuyển",
                "Không dùng voucher"
        };

        new AlertDialog.Builder(this)
                .setTitle("Chọn voucher")
                .setItems(vouchers, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            if (getSubTotal() >= 100000) {
                                voucherDiscount = 20000;
                                selectedVoucher = "Giảm 20.000đ";
                                deliveryFee = 15000;
                                if (tvDeliveryFee != null) {
                                    tvDeliveryFee.setText(String.format("%,.0fđ", deliveryFee));
                                }
                            } else {
                                Toast.makeText(this, "Đơn hàng tối thiểu 100.000đ", Toast.LENGTH_SHORT).show();
                                voucherDiscount = 0;
                            }
                            break;
                        case 1:
                            if (getSubTotal() >= 200000) {
                                voucherDiscount = 50000;
                                selectedVoucher = "Giảm 50.000đ";
                                deliveryFee = 15000;
                                if (tvDeliveryFee != null) {
                                    tvDeliveryFee.setText(String.format("%,.0fđ", deliveryFee));
                                }
                            } else {
                                Toast.makeText(this, "Đơn hàng tối thiểu 200.000đ", Toast.LENGTH_SHORT).show();
                                voucherDiscount = 0;
                            }
                            break;
                        case 2:
                            deliveryFee = 0;
                            voucherDiscount = 0;
                            selectedVoucher = "Miễn phí vận chuyển";
                            if (tvDeliveryFee != null) {
                                tvDeliveryFee.setText("0đ");
                            }
                            break;
                        case 3:
                            voucherDiscount = 0;
                            deliveryFee = 15000;
                            selectedVoucher = "";
                            if (tvDeliveryFee != null) {
                                tvDeliveryFee.setText("15.000đ");
                            }
                            break;
                    }
                    updateTotalPrice();
                    Toast.makeText(this, "Đã áp dụng: " + selectedVoucher, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        double finalTotal = getSubTotal() + deliveryFee - voucherDiscount;

        StringBuilder orderDetail = new StringBuilder();
        orderDetail.append("🛒 ĐƠN HÀNG CỦA BẠN\n\n");

        List<CartGroupAdapter.CartGroup> groupList = cartAdapter.getGroupList();
        if (groupList != null) {
            for (CartGroupAdapter.CartGroup group : groupList) {
                orderDetail.append("📌 ").append(group.restaurantName).append(":\n");
                for (CartItem item : group.items) {
                    orderDetail.append("   • ").append(item.getFood().getName())
                            .append(" x").append(item.getQuantity())
                            .append(" - ").append(String.format("%,.0fđ", item.getTotalPrice()))
                            .append("\n");
                    if (!item.getNote().isEmpty()) {
                        orderDetail.append("     📝 ").append(item.getNote()).append("\n");
                    }
                }
                orderDetail.append("\n");
            }
        }

        orderDetail.append("\n💰 Tạm tính: ").append(String.format("%,.0fđ", getSubTotal()))
                .append("\n🚚 Phí ship: ").append(String.format("%,.0fđ", deliveryFee))
                .append("\n🎁 Giảm giá: ").append(String.format("%,.0fđ", voucherDiscount))
                .append("\n────────────────")
                .append("\n💵 Tổng cộng: ").append(String.format("%,.0fđ", finalTotal));

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt hàng")
                .setMessage(orderDetail.toString())
                .setPositiveButton("ĐẶT HÀNG", (dialog, which) -> {
                    Toast.makeText(this, "Đặt hàng thành công!\n" +
                            String.format("Tổng tiền: %,.0fđ", finalTotal), Toast.LENGTH_LONG).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("cart_cleared", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}