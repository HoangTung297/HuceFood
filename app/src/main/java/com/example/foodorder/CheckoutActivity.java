package com.example.foodorder;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;
import com.example.foodorder.utils.LoginSessionManager;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private TextView tvOrderTotal;
    private RadioGroup rgPayment;
    private RadioButton rbCOD, rbMomo;
    private EditText etDeliveryName, etDeliveryPhone, etDeliveryAddress, etOrderNote;
    private Button btnConfirmOrder, btnCancelOrder;

    private LoginSessionManager sessionManager;
    private FirebaseRepository repository;
    private double totalAmount = 0;
    private List<CartItem> cartItems;
    private static final String TAG = "CheckoutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_checkout);

        sessionManager = new LoginSessionManager(this);
        repository = FirebaseRepository.getInstance();

        initViews();
        loadData();
        setupListeners();
    }

    private void initViews() {
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        rgPayment = findViewById(R.id.rgPayment);
        rbCOD = findViewById(R.id.rbCOD);
        rbMomo = findViewById(R.id.rbMomo);
        etDeliveryName = findViewById(R.id.etDeliveryName);
        etDeliveryPhone = findViewById(R.id.etDeliveryPhone);
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        etOrderNote = findViewById(R.id.etOrderNote);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
    }

    private void loadData() {
        totalAmount = getIntent().getDoubleExtra("totalAmount", 0);
        cartItems = (List<CartItem>) getIntent().getSerializableExtra("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        tvOrderTotal.setText(formatCurrency(totalAmount));
    }

    private void setupListeners() {
        btnConfirmOrder.setOnClickListener(v -> processOrder());
        btnCancelOrder.setOnClickListener(v -> finish());
    }

    private void processOrder() {
        String name = etDeliveryName.getText().toString().trim();
        String phone = etDeliveryPhone.getText().toString().trim();
        String address = etDeliveryAddress.getText().toString().trim();
        String note = etOrderNote.getText().toString().trim();

        if (name.isEmpty()) {
            etDeliveryName.setError("Vui lòng nhập họ tên");
            return;
        }
        if (phone.isEmpty()) {
            etDeliveryPhone.setError("Vui lòng nhập số điện thoại");
            return;
        }
        if (address.isEmpty()) {
            etDeliveryAddress.setError("Vui lòng nhập địa chỉ");
            return;
        }

        String paymentMethod = rbCOD.isChecked() ? "COD" : "Momo";

        if (paymentMethod.equals("Momo")) {
            showMomoPaymentDialog(name, phone, address, note, paymentMethod);
        } else {
            createOrder(name, phone, address, note, paymentMethod);
        }
    }

    private void showMomoPaymentDialog(String name, String phone, String address, String note, String paymentMethod) {
        new AlertDialog.Builder(this)
                .setTitle("Thanh toán Momo")
                .setMessage("Số tiền: " + formatCurrency(totalAmount) + "\n\nBạn có muốn thanh toán qua ví Momo không?")
                .setPositiveButton("Thanh toán", (dialog, which) -> {
                    createOrder(name, phone, address, note, paymentMethod);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createOrder(String name, String phone, String address, String note, String paymentMethod) {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderCode("ORD" + System.currentTimeMillis());

        String restaurantName = "Nhà hàng";
        if (cartItems != null && !cartItems.isEmpty() && cartItems.get(0) != null) {
            restaurantName = cartItems.get(0).getName();
            if (restaurantName == null || restaurantName.isEmpty()) {
                restaurantName = "Nhà hàng";
            }
        }
        order.setRestaurantName(restaurantName);
        order.setDeliveryName(name);
        order.setDeliveryPhone(phone);
        order.setDeliveryAddress(address);
        order.setOrderNote(note);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("paid");
        order.setSubtotal(totalAmount);
        order.setDeliveryFee(15000);
        order.setDiscount(0);
        order.setFinalTotal(totalAmount + 15000);

        order.setStatus("pending");
        order.setCreatedAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());

        List<Map<String, Object>> items = new ArrayList<>();
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                Map<String, Object> map = new HashMap<>();
                map.put("foodId", item.getFoodId());
                map.put("name", item.getName());
                map.put("price", item.getPrice());
                map.put("quantity", item.getQuantity());
                map.put("imageUrl", item.getImageUrl());
                items.add(map);
            }
        }
        order.setItems(items);

        Log.d(TAG, "Creating order with status: pending");

        repository.createOrder(order, new FirebaseRepository.OnDataLoaded<String>() {
            @Override
            public void onSuccess(String orderId) {
                Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                clearCart();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
                Toast.makeText(CheckoutActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearCart() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }
        repository.clearCart(userId, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {}
            @Override
            public void onError(String error) {}
        });
    }

    private String formatCurrency(double amount) {
        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        return f.format(amount) + "đ";
    }
}