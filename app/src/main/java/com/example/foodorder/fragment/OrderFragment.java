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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {

    private RecyclerView rvOrders;
    private TextView tvEmptyOrders;
    private OrderHistoryAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        firestore = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();

        initViews(view);
        setupRecyclerView();
        loadOrdersFromFirebase();

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

    private void loadOrdersFromFirebase() {
        // TODO: Load orders from Firebase for current user
        // Tạm thời hiển thị trạng thái trống
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