//package com.example.foodorder;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.TextView;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.foodorder.adapter.OrderHistoryAdapter;
//import com.example.foodorder.database.DatabaseHelper;
//import com.example.foodorder.model.Order;
//import java.util.ArrayList;
//import java.util.List;
//
//public class OrderHistoryActivity extends AppCompatActivity {
//
//    private RecyclerView rvOrders;
//    private OrderHistoryAdapter orderAdapter;
//    private DatabaseHelper databaseHelper;
//    private List<Order> orderList;
//    private TextView tvEmptyOrders;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_order_history);
//
//        databaseHelper = new DatabaseHelper(this);
//        orderList = new ArrayList<>();
//
//        initViews();
//        setupRecyclerView();
//        loadOrderHistory();
//    }
//
//    private void initViews() {
//        rvOrders = findViewById(R.id.rvOrders);
//        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
//
//        // Nút back
//        View btnBack = findViewById(R.id.btnBack);
//        if (btnBack != null) {
//            btnBack.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    finish();
//                }
//            });
//        }
//    }
//
//    private void setupRecyclerView() {
//        orderAdapter = new OrderHistoryAdapter(orderList);
//        rvOrders.setLayoutManager(new LinearLayoutManager(this));
//        rvOrders.setAdapter(orderAdapter);
//    }
//
//    private void loadOrderHistory() {
//        // TODO: Load orders from database
//        if (orderList.isEmpty()) {
//            tvEmptyOrders.setVisibility(View.VISIBLE);
//            rvOrders.setVisibility(View.GONE);
//        } else {
//            tvEmptyOrders.setVisibility(View.GONE);
//            rvOrders.setVisibility(View.VISIBLE);
//            orderAdapter.updateList(orderList);
//        }
//    }
//}