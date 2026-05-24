package com.example.foodorder.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.adapter.OrderHistoryAdapter;
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;
import com.example.foodorder.utils.CacheManager;
import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private OrderHistoryAdapter adapter;
    private List<Order> orderList;
    private FirebaseRepository repository;
    private CacheManager cacheManager;
    private String userId = "user123";
    private boolean isLoading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);
        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        progressBar = view.findViewById(R.id.progressBar);

        repository = FirebaseRepository.getInstance();
        if (getContext() != null) {
            cacheManager = new CacheManager(getContext());
        }
        orderList = new ArrayList<>();

        if (getActivity() != null) {
            userId = getActivity().getSharedPreferences("UserPrefs", 0)
                    .getString("user_id", "user123");
        }

        setupRecyclerView();
        loadOrders();
        return view;
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(orderList,
                order -> {
                    Toast.makeText(getContext(), "Đơn: " + order.getOrderCode(), Toast.LENGTH_SHORT).show();
                },
                (order, position) -> {
                    showDeleteConfirmDialog(order, position);
                }
        );
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    }

    private void showDeleteConfirmDialog(Order order, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa đơn hàng")
                .setMessage("Xóa đơn hàng " + order.getOrderCode() + " khỏi lịch sử?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteOrder(order, position);
                })
                .setNegativeButton("Quay lại", null)
                .show();
    }

    private void deleteOrder(Order order, int position) {
        progressBar.setVisibility(View.VISIBLE);
        repository.deleteOrder(order.getId(), new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                orderList.remove(position);
                adapter.updateList(orderList);
                if (cacheManager != null) {
                    cacheManager.cacheOrders(orderList);
                }
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrders() {
        if (isLoading) return;

        // Hiển thị cache trước
        if (cacheManager != null) {
            List<Order> cachedOrders = cacheManager.getCachedOrders();
            if (cachedOrders != null && !cachedOrders.isEmpty()) {
                orderList.clear();
                orderList.addAll(cachedOrders);
                adapter.updateList(orderList);

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvOrders.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                }
            }
        }

        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        repository.getUserOrders(userId, new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orderList.clear();
                orderList.addAll(data);
                adapter.updateList(orderList);
                if (cacheManager != null) {
                    cacheManager.cacheOrders(data);
                }

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvOrders.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                }
                progressBar.setVisibility(View.GONE);
                isLoading = false;
            }

            @Override
            public void onError(String error) {
                if (orderList.isEmpty()) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
                isLoading = false;
            }
        });
    }

    public void refreshData() {
        loadOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }
}