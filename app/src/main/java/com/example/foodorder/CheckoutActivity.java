package com.example.foodorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private static final String TAG = "CHECKOUT";

    private TextView tvOrderTotal, tvWalletBalance;
    private RadioGroup rgPayment;
    private RadioButton rbCOD, rbWallet;
    private LinearLayout layoutWalletInfo;
    private EditText etDeliveryName, etDeliveryPhone, etDeliveryAddress, etOrderNote;
    private Button btnConfirmOrder, btnCancelOrder;

    private FirebaseRepository repository;
    private LoginSessionManager sessionManager;
    private double walletBalance = 0;
    private double totalAmount = 0;
    private double subtotalAmount = 0;
    private double deliveryFee = 0;
    private double discountAmount = 0;
    private List<CartItem> cartItems;
    private String selectedPaymentMethod = "COD";
    private boolean isProcessing = false;
    private String mOrderId = ""; // Lưu orderId để trả về

    // Map ánh xạ restaurantId -> tên nhà hàng
    private static final Map<String, String> RESTAURANT_MAP = new HashMap<>();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_checkout);

        repository = FirebaseRepository.getInstance();
        sessionManager = new LoginSessionManager(this);

        initViews();
        loadData();
        loadUserInfo();
        loadWalletFromFirestore();
        setupListeners();
        setupPaymentMethodListener();
        updateOrderTotalDisplay();
    }

    private void initViews() {
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        rgPayment = findViewById(R.id.rgPayment);
        rbCOD = findViewById(R.id.rbCOD);
        rbWallet = findViewById(R.id.rbWallet);
        layoutWalletInfo = findViewById(R.id.layoutWalletInfo);
        etDeliveryName = findViewById(R.id.etDeliveryName);
        etDeliveryPhone = findViewById(R.id.etDeliveryPhone);
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        etOrderNote = findViewById(R.id.etOrderNote);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
    }

    private void loadData() {
        subtotalAmount = getIntent().getDoubleExtra("totalAmount", 0);
        discountAmount = getIntent().getDoubleExtra("discount", 0);
        deliveryFee = getIntent().getDoubleExtra("deliveryFee", 0);
        cartItems = (List<CartItem>) getIntent().getSerializableExtra("cartItems");

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        totalAmount = subtotalAmount + deliveryFee - discountAmount;

        updateOrderItemsDisplay();
    }

    private void updateOrderItemsDisplay() {
        TextView tvOrderItems = findViewById(R.id.tvOrderItems);
        TextView tvOrderSubtotal = findViewById(R.id.tvOrderSubtotal);
        TextView tvOrderDelivery = findViewById(R.id.tvOrderDelivery);
        TextView tvOrderDiscount = findViewById(R.id.tvOrderDiscount);

        if (tvOrderItems != null) {
            StringBuilder itemsText = new StringBuilder();
            for (CartItem item : cartItems) {
                itemsText.append("• ").append(item.getName()).append(" x").append(item.getQuantity());
                if (item.getNote() != null && !item.getNote().isEmpty()) {
                    itemsText.append("\n  📝 ").append(item.getNote());
                }
                itemsText.append("\n");
            }
            tvOrderItems.setText(itemsText.toString());
        }

        if (tvOrderSubtotal != null) {
            tvOrderSubtotal.setText(formatCurrency(subtotalAmount));
        }
        if (tvOrderDelivery != null) {
            tvOrderDelivery.setText(formatCurrency(deliveryFee));
        }
        if (tvOrderDiscount != null) {
            tvOrderDiscount.setText("-" + formatCurrency(discountAmount));
        }
    }

    private void updateOrderTotalDisplay() {
        tvOrderTotal.setText(formatCurrency(totalAmount));
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String savedName = prefs.getString("user_name", "");
        String savedPhone = prefs.getString("user_phone", "");
        String savedAddress = prefs.getString("user_address", "");

        if (savedAddress.isEmpty()) {
            savedAddress = prefs.getString("delivery_address", "");
        }

        if (!savedName.isEmpty()) etDeliveryName.setText(savedName);
        if (!savedPhone.isEmpty()) etDeliveryPhone.setText(savedPhone);
        if (!savedAddress.isEmpty()) etDeliveryAddress.setText(savedAddress);
    }

    private void loadWalletFromFirestore() {
        String userId = getCurrentUserId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("wallets").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double balance = documentSnapshot.getDouble("balance");
                        walletBalance = (balance != null) ? balance : 0;
                    } else {
                        walletBalance = 0;
                        Map<String, Object> newWallet = new HashMap<>();
                        newWallet.put("userId", userId);
                        newWallet.put("balance", 0);
                        newWallet.put("createdAt", System.currentTimeMillis());
                        db.collection("wallets").document(userId).set(newWallet);
                    }
                    updateWalletUI();
                })
                .addOnFailureListener(e -> {
                    walletBalance = 0;
                    updateWalletUI();
                });
    }

    private void updateWalletUI() {
        String balanceText = formatCurrency(walletBalance);
        tvWalletBalance.setText("Số dư: " + balanceText);
        rbWallet.setText("💳 Thanh toán bằng ví");
    }

    private String getCurrentUserId() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userId = prefs.getString("user_email", "");
        }
        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }
        return userId;
    }

    private void setupListeners() {
        btnConfirmOrder.setOnClickListener(v -> {
            if (isProcessing) {
                Toast.makeText(this, "Đang xử lý...", Toast.LENGTH_SHORT).show();
                return;
            }
            processOrder();
        });
        btnCancelOrder.setOnClickListener(v -> finish());
    }

    private void setupPaymentMethodListener() {
        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCOD) {
                selectedPaymentMethod = "COD";
                layoutWalletInfo.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbWallet) {
                selectedPaymentMethod = "wallet";
                layoutWalletInfo.setVisibility(View.VISIBLE);

                if (walletBalance < totalAmount) {
                    Toast.makeText(this, "Số dư không đủ (" + formatCurrency(walletBalance) + ")", Toast.LENGTH_LONG).show();
                    rbCOD.setChecked(true);
                }
            }
        });
    }

    private void saveUserInfo(String name, String phone, String address) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("user_name", name)
                .putString("delivery_name", name)
                .putString("user_phone", phone)
                .putString("user_address", address)
                .putString("delivery_address", address)
                .apply();
    }

    private String getRestaurantNameFromId(String restaurantId) {
        if (restaurantId == null) return null;
        String name = RESTAURANT_MAP.get(restaurantId.toLowerCase());
        if (name != null) return name;
        return null;
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

        saveUserInfo(name, phone, address);

        isProcessing = true;
        btnConfirmOrder.setEnabled(false);

        String paymentMethod = getPaymentMethodCode();

        if (selectedPaymentMethod.equals("wallet")) {
            if (walletBalance < totalAmount) {
                Toast.makeText(this, "Số dư không đủ", Toast.LENGTH_SHORT).show();
                isProcessing = false;
                btnConfirmOrder.setEnabled(true);
                return;
            }

            double newBalance = walletBalance - totalAmount;
            String userId = getCurrentUserId();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("wallets").document(userId)
                    .update("balance", newBalance)
                    .addOnSuccessListener(aVoid -> {
                        walletBalance = newBalance;
                        updateWalletUI();
                        createOrder(name, phone, address, note, paymentMethod, true);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        isProcessing = false;
                        btnConfirmOrder.setEnabled(true);
                    });
        } else {
            createOrder(name, phone, address, note, paymentMethod, false);
        }
    }

    private String getPaymentMethodCode() {
        if (selectedPaymentMethod.equals("COD")) return "COD";
        return "Wallet";
    }

    private String getPaymentMethodText() {
        if (selectedPaymentMethod.equals("COD")) return "Tiền mặt khi nhận hàng";
        return "Ví điện tử";
    }

    private void createOrder(String name, String phone, String address, String note, String paymentMethod, boolean isPaid) {
        String userId = getCurrentUserId();

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderCode("ORD" + System.currentTimeMillis());

        // Lấy thông tin từ cart item đầu tiên
        String firstRestaurantId = null;
        String firstRestaurantName = "Nhà hàng";

        if (cartItems != null && !cartItems.isEmpty()) {
            CartItem firstItem = cartItems.get(0);
            firstRestaurantId = firstItem.getRestaurantId();
            String nameFromId = getRestaurantNameFromId(firstRestaurantId);
            if (nameFromId != null) {
                firstRestaurantName = nameFromId;
            } else {
                firstRestaurantName = firstItem.getName();
                if (firstRestaurantName == null || firstRestaurantName.isEmpty()) {
                    firstRestaurantName = "Nhà hàng";
                }
            }
        }

        order.setRestaurantId(firstRestaurantId);
        order.setRestaurantName(firstRestaurantName);
        order.setDeliveryName(name);
        order.setDeliveryPhone(phone);
        order.setDeliveryAddress(address);

        String finalNote = note;
        if (isPaid) {
            finalNote = "[ĐÃ THANH TOÁN QUA VÍ - " + formatCurrency(totalAmount) + "] " + note;
        }
        order.setOrderNote(finalNote);

        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(isPaid ? "paid" : "pending");

        order.setSubtotal(subtotalAmount);
        order.setDeliveryFee(deliveryFee);
        order.setDiscount(discountAmount);
        order.setFinalTotal(totalAmount);

        order.setStatus("pending");
        order.setCreatedAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());

        // Tạo danh sách items với đầy đủ restaurantId
        List<Map<String, Object>> items = new ArrayList<>();
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                Map<String, Object> map = new HashMap<>();
                map.put("foodId", item.getFoodId());
                map.put("name", item.getName());
                map.put("price", item.getPrice());
                map.put("quantity", item.getQuantity());
                map.put("imageUrl", item.getImageUrl() != null ? item.getImageUrl() : "");
                map.put("note", item.getNote() != null ? item.getNote() : "");

                if (item.getRestaurantId() != null && !item.getRestaurantId().isEmpty()) {
                    map.put("restaurantId", item.getRestaurantId());
                    String itemRestaurantName = getRestaurantNameFromId(item.getRestaurantId());
                    map.put("restaurantName", itemRestaurantName != null ? itemRestaurantName : item.getName());
                } else {
                    map.put("restaurantId", "");
                    map.put("restaurantName", item.getName());
                }

                items.add(map);
            }
        }
        order.setItems(items);

        Log.d(TAG, "===== TẠO ĐƠN HÀNG =====");
        Log.d(TAG, "restaurantId: " + firstRestaurantId);
        Log.d(TAG, "restaurantName: " + firstRestaurantName);
        Log.d(TAG, "Số lượng items: " + items.size());

        // Gọi repository để tạo đơn hàng
        repository.createOrder(order, new FirebaseRepository.OnDataLoaded<String>() {
            @Override
            public void onSuccess(String orderId) {
                // LƯU ORDER ID ĐỂ TRẢ VỀ
                mOrderId = orderId;
                Log.d(TAG, "✅✅✅ ĐÃ TẠO ĐƠN HÀNG THÀNH CÔNG! orderId: " + orderId);

                String message = "Đặt hàng thành công!\n" +
                        "Tổng thanh toán: " + formatCurrency(totalAmount) + "\n" +
                        "Phương thức: " + getPaymentMethodText();

                if (isPaid) {
                    message = "✅ Thanh toán thành công!\n" +
                            "Đã trừ " + formatCurrency(totalAmount) + " từ ví.\n" +
                            message;
                }
                Toast.makeText(CheckoutActivity.this, message, Toast.LENGTH_LONG).show();

                clearCart();

                // 🔥 QUAN TRỌNG: Trả về orderId cho CartFragment
                Intent resultIntent = new Intent();
                resultIntent.putExtra("refresh", true);
                resultIntent.putExtra("orderId", orderId); // <-- THÊM DÒNG NÀY
                Log.d(TAG, "📤 Trả về orderId cho CartFragment: " + orderId);
                setResult(RESULT_OK, resultIntent);

                isProcessing = false;
                finish();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Lỗi tạo đơn: " + error);
                Toast.makeText(CheckoutActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                isProcessing = false;
                btnConfirmOrder.setEnabled(true);
            }
        });
    }

    private void clearCart() {
        String userId = getCurrentUserId();
        repository.clearCart(userId, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override public void onSuccess(Void data) {
                Log.d(TAG, "🛒 Đã xóa giỏ hàng");
            }
            @Override public void onError(String error) {
                Log.e(TAG, "Lỗi xóa giỏ hàng: " + error);
            }
        });
    }

    private String formatCurrency(double amount) {
        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        return f.format(amount) + "đ";
    }
}