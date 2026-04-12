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
import com.example.foodorder.database.DatabaseHelper;
import com.example.foodorder.model.Order;
import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {

    private RecyclerView rvOrders;
    private OrderHistoryAdapter orderAdapter;
    private DatabaseHelper databaseHelper;
    private List<Order> orderList;
    private TextView tvEmptyOrders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        databaseHelper = new DatabaseHelper(getActivity());
        orderList = new ArrayList<>();

        initViews(view);
        setupRecyclerView();
        loadOrders();

        return view;
    }

    private void initViews(View view) {
        rvOrders = view.findViewById(R.id.rvOrders);
        tvEmptyOrders = view.findViewById(R.id.tvEmptyOrders);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderHistoryAdapter(orderList);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        // TODO: Load orders from database
        if (orderList.isEmpty()) {
            tvEmptyOrders.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            tvEmptyOrders.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
            orderAdapter.updateList(orderList);
        }
    }
}