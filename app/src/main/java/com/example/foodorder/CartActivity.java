package com.example.foodorder;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.adapter.ProductItemAdapter;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.repository.FirebaseRepository;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvTotalPrice, tvDeliveryFee, tvVoucherDiscount, tvFinalTotal, tvEmptyCart;
    private Button btnCheckout;
    private ProductItemAdapter adapter;
    private List<CartItem> cartItems;
    private Toolbar toolbar;
    private FirebaseRepository repository;
    private String userId = "user123";
    private double deliveryFee = 15000;
    private double discount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        repository = FirebaseRepository.getInstance();

        // Lấy userId từ SharedPreferences
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
        btnCheckout = findViewById(R.id.btnCheckout);

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
                },
                (item, position) -> {
                    removeFromCart(item.getFoodId(), position);
                }
        );
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);
    }

    private void loadCartItems() {
        // Nhận danh sách giỏ hàng từ Intent nếu có
        if (getIntent().hasExtra("cart_list")) {
            ArrayList<CartItem> receivedList = (ArrayList<CartItem>) getIntent().getSerializableExtra("cart_list");
            if (receivedList != null) {
                cartItems.clear();
                cartItems.addAll(receivedList);
                adapter.updateList(cartItems);
            }
        } else {
            // Nếu không có thì load từ Firebase
            repository.getCart(userId, new FirebaseRepository.OnDataLoaded<List<CartItem>>() {
                @Override
                public void onSuccess(List<CartItem> data) {
                    cartItems.clear();
                    cartItems.addAll(data);
                    adapter.updateList(cartItems);
                    calculateTotals();

                    if (cartItems.isEmpty()) {
                        tvEmptyCart.setVisibility(View.VISIBLE);
                        rvCart.setVisibility(View.GONE);
                    } else {
                        tvEmptyCart.setVisibility(View.GONE);
                        rvCart.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(CartActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }

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

    private void removeFromCart(String foodId, int position) {
        repository.removeFromCart(userId, foodId, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                cartItems.remove(position);
                adapter.updateList(cartItems);
                calculateTotals();

                if (cartItems.isEmpty()) {
                    tvEmptyCart.setVisibility(View.VISIBLE);
                    rvCart.setVisibility(View.GONE);
                }
                Toast.makeText(CartActivity.this, "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CartActivity.this, "Lỗi xóa: " + error, Toast.LENGTH_SHORT).show();
            }
        });
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
        tvVoucherDiscount.setText(formatter.format(discount) + "đ");
        tvFinalTotal.setText(formatter.format(finalTotal) + "đ");
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Xử lý thanh toán, tạo đơn hàng
        Toast.makeText(this, "Chức năng thanh toán đang phát triển", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}