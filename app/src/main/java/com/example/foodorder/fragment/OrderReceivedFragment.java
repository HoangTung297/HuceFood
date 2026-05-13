package com.example.foodorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.adapter.OrderReceivedAdapter;
import com.example.foodorder.model.Order;
import java.util.ArrayList;
import java.util.List;

public class OrderReceivedFragment extends Fragment {

    private RecyclerView rvReceived;
    private TextView tvEmpty;
    private OrderReceivedAdapter receivedAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_received, container, false);

        initViews(view);
        setupRecyclerView();
        loadReceivedOrders();

        return view;
    }

    private void initViews(View view) {
        rvReceived = view.findViewById(R.id.rvReceived);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void setupRecyclerView() {
        rvReceived.setLayoutManager(new LinearLayoutManager(getContext()));
        receivedAdapter = new OrderReceivedAdapter(new ArrayList<>());
        rvReceived.setAdapter(receivedAdapter);

        receivedAdapter.setOnReorderClickListener(order -> {
            Toast.makeText(getContext(), "Đã thêm lại đơn hàng #" + order.getId() + " vào giỏ", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadReceivedOrders() {
        List<Order> receivedOrders = new ArrayList<>();

        Order order1 = new Order(1, 1, "10/05/2024", "Đã nhận hàng");
        order1.setTotalPrice(145000);

        Order order2 = new Order(2, 1, "05/05/2024", "Đã nhận hàng");
        order2.setTotalPrice(89000);

        receivedOrders.add(order1);
        receivedOrders.add(order2);

        if (receivedOrders.isEmpty()) {
            rvReceived.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvReceived.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            receivedAdapter.updateList(receivedOrders);
        }
    }
}
