package com.example.foodorder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.adapter.CartAdapter;
import com.example.foodorder.model.Food;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private CartAdapter cartAdapter;
    private List<Food> cartList;
    private TextView tvTotalPrice, tvEmptyCart;
    private Button btnCheckout, btnContinue;
    private double totalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Nhận dữ liệu từ Intent
        cartList = (List<Food>) getIntent().getSerializableExtra("cart_list");
        if (cartList == null) {
            cartList = new ArrayList<>();
        }

        initViews();
        setupRecyclerView();
        updateTotalPrice();
    }

    private void initViews() {
        rvCart = findViewById(R.id.rvCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnContinue = findViewById(R.id.btnContinue);

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkout();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartList, new CartAdapter.OnItemRemoveListener() {
            @Override
            public void onRemoveClick(Food food) {
                cartList.remove(food);
                cartAdapter.notifyDataSetChanged();
                updateTotalPrice();

                if (cartList.isEmpty()) {
                    tvEmptyCart.setVisibility(View.VISIBLE);
                }
            }
        });

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(cartAdapter);

        if (cartList.isEmpty()) {
            tvEmptyCart.setVisibility(View.VISIBLE);
        } else {
            tvEmptyCart.setVisibility(View.GONE);
        }
    }

    private void updateTotalPrice() {
        totalPrice = 0;
        for (Food food : cartList) {
            totalPrice += food.getPrice();
        }
        tvTotalPrice.setText(String.format("Tổng cộng: %,.0f VNĐ", totalPrice));
    }

    private void checkout() {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đặt hàng thành công!\nTổng tiền: " +
                String.format("%,.0f VNĐ", totalPrice), Toast.LENGTH_LONG).show();

        // Trả kết quả về ProfileFragment
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}