package com.example.foodorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etDeliveryName, etDeliveryPhone, etDeliveryAddress;
    private TextView tvOrderItems, tvOrderSubtotal, tvOrderDelivery, tvOrderDiscount, tvOrderTotal;
    private RadioGroup rgPayment;
    private RadioButton rbCOD, rbMomo;
    private EditText etOrderNote;
    private Button btnCancelOrder, btnConfirmOrder;

    private List<CartItem> cartItems = new ArrayList<>();
    private double deliveryFee = 15000;
    private double discount = 0;
    private String userId = "user123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_checkout);

        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_id", "user123");

        // Nhận giỏ hàng và discount
        if (getIntent().hasExtra("cart_list")) {
            cartItems = (List<CartItem>) getIntent().getSerializableExtra("cart_list");
        }
        if (getIntent().hasExtra("discount")) {
            discount = getIntent().getDoubleExtra("discount", 0);
        }

        // Ánh xạ view
        etDeliveryName = findViewById(R.id.etDeliveryName);
        etDeliveryPhone = findViewById(R.id.etDeliveryPhone);
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        tvOrderItems = findViewById(R.id.tvOrderItems);
        tvOrderSubtotal = findViewById(R.id.tvOrderSubtotal);
        tvOrderDelivery = findViewById(R.id.tvOrderDelivery);
        tvOrderDiscount = findViewById(R.id.tvOrderDiscount);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        rgPayment = findViewById(R.id.rgPayment);
        rbCOD = findViewById(R.id.rbCOD);
        rbMomo = findViewById(R.id.rbMomo);
        etOrderNote = findViewById(R.id.etOrderNote);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        displayOrderSummary();
        loadDeliveryInfo();

        btnCancelOrder.setOnClickListener(v -> {
            Toast.makeText(this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
    }

    private void displayOrderSummary() {
        if (cartItems == null || cartItems.isEmpty()) {
            tvOrderItems.setText("Không có món nào");
            tvOrderSubtotal.setText("0đ");
            tvOrderDelivery.setText("0đ");
            tvOrderDiscount.setText("-0đ");
            tvOrderTotal.setText("0đ");
            return;
        }

        StringBuilder itemsText = new StringBuilder();
        double subtotal = 0;
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        for (CartItem item : cartItems) {
            itemsText.append("• ").append(item.getName())
                    .append(" x").append(item.getQuantity())
                    .append(" - ").append(formatter.format(item.getTotalPrice())).append("đ\n");
            subtotal += item.getTotalPrice();
        }

        double total = subtotal + deliveryFee - discount;

        tvOrderItems.setText(itemsText.toString().trim());
        tvOrderSubtotal.setText(formatter.format(subtotal) + "đ");
        tvOrderDelivery.setText(formatter.format(deliveryFee) + "đ");
        tvOrderDiscount.setText("-" + formatter.format(discount) + "đ");
        tvOrderTotal.setText(formatter.format(total) + "đ");
    }

    private void loadDeliveryInfo() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        etDeliveryName.setText(prefs.getString("user_name", ""));
        etDeliveryPhone.setText(prefs.getString("user_phone", ""));
        etDeliveryAddress.setText(prefs.getString("user_address", ""));
    }

    private void confirmOrder() {
        String name = etDeliveryName.getText().toString().trim();
        String phone = etDeliveryPhone.getText().toString().trim();
        String address = etDeliveryAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Order
        Order order = new Order();
        order.setUserId(userId);
        order.setDeliveryName(name);
        order.setDeliveryPhone(phone);
        order.setDeliveryAddress(address);
        order.setOrderNote(etOrderNote.getText().toString().trim());
        order.setPaymentMethod(rbCOD.isChecked() ? "COD" : "MoMo");

        // Chuyển CartItem sang Map để lưu vào Firestore
        List<Map<String, Object>> itemsList = new ArrayList<>();
        double subtotal = 0;
        for (CartItem item : cartItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("foodId", item.getFoodId());
            map.put("name", item.getName());
            map.put("price", item.getPrice());
            map.put("quantity", item.getQuantity());
            map.put("imageUrl", item.getImageUrl());
            map.put("restaurantId", item.getRestaurantId());
            itemsList.add(map);
            subtotal += item.getTotalPrice();
        }
        order.setItems(itemsList);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setDiscount(discount);
        order.setFinalTotal(subtotal + deliveryFee - discount);

        // Nếu có voucher
        if (getIntent().hasExtra("voucher_code")) {
            order.setVoucherCode(getIntent().getStringExtra("voucher_code"));
            order.setVoucherDiscount(discount);
        }

        // Lưu lên Firebase
        FirebaseRepository repository = FirebaseRepository.getInstance();
        repository.createOrder(order, new FirebaseRepository.OnDataLoaded<String>() {
            @Override
            public void onSuccess(String orderId) {
                Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                // Xóa giỏ hàng
                repository.clearCart(userId, new FirebaseRepository.OnDataLoaded<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        // không cần làm gì
                    }

                    @Override
                    public void onError(String error) {
                        // không cần làm gì
                    }
                });
                // Quay về trang chủ
                Intent intent = new Intent(CheckoutActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CheckoutActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}