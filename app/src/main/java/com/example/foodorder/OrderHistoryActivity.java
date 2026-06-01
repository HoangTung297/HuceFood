package com.example.foodorder;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorder.adapter.OrderHistoryAdapter;
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private TextView tvEmptyOrders;
    private ImageView btnBack;
    private OrderHistoryAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_id", "user123");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("");
        }

        btnBack = findViewById(R.id.btnBack);
        rvOrders = findViewById(R.id.rvOrders);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);

        btnBack.setOnClickListener(v -> finish());

        adapter = new OrderHistoryAdapter(orderList,
                order -> {
                    // Xem chi tiết đơn hàng (có thể mở Activity chi tiết sau)
                    Toast.makeText(this, "Xem đơn hàng " + order.getId(), Toast.LENGTH_SHORT).show();
                },
                (order, position) -> {
                    // Xóa đơn hàng
                    deleteOrder(order, position);
                }
        );

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        FirebaseRepository repository = FirebaseRepository.getInstance();
        repository.getUserOrders(userId, new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orderList.clear();
                if (data != null) {
                    orderList.addAll(data);
                }
                adapter.updateList(orderList);
                toggleEmptyView();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(OrderHistoryActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                toggleEmptyView();
            }
        });
    }

    private void deleteOrder(Order order, int position) {
        FirebaseRepository.getInstance().deleteOrder(order.getId(), new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void unused) {
                orderList.remove(position);
                adapter.notifyItemRemoved(position);
                toggleEmptyView();
                Toast.makeText(OrderHistoryActivity.this, "Đã xóa đơn hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(OrderHistoryActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEmptyView() {
        if (orderList.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            tvEmptyOrders.setVisibility(View.VISIBLE);
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            tvEmptyOrders.setVisibility(View.GONE);
        }
    }
}