package com.example.foodorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.adapter.OrderHistoryAdapter;
import com.example.foodorder.model.Order;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrderHistory;
    private TextView tvEmpty;
    private OrderHistoryAdapter orderHistoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        initViews(view);
        setupRecyclerView();
        loadOrderHistory();

        return view;
    }

    private void initViews(View view) {
        rvOrderHistory = view.findViewById(R.id.rvOrderHistory);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void setupRecyclerView() {
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        orderHistoryAdapter = new OrderHistoryAdapter(new ArrayList<>());
        rvOrderHistory.setAdapter(orderHistoryAdapter);
    }

    private void loadOrderHistory() {
        List<Order> orders = new ArrayList<>();

        Order order1 = new Order(1, 1, "10/05/2024", "Đã giao");
        order1.setTotalPrice(145000);

        Order order2 = new Order(2, 1, "05/05/2024", "Đã giao");
        order2.setTotalPrice(89000);

        orders.add(order1);
        orders.add(order2);

        if (orders.isEmpty()) {
            rvOrderHistory.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvOrderHistory.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            orderHistoryAdapter.updateList(orders);
        }
    }
}
