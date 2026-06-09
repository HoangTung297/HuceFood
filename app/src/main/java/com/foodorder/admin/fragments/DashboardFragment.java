package com.foodorder.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.foodorder.admin.R;
import com.foodorder.admin.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvTotalUsers, tvTotalOrders, tvTodayRevenue, tvTotalRevenue;
    private FirebaseFirestore db;
    private NumberFormat currencyFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        db = FirebaseFirestore.getInstance();
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
        tvTodayRevenue = view.findViewById(R.id.tvTodayRevenue);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);

        loadStatistics();

        return view;
    }

    private void loadStatistics() {
        // Tổng số người dùng
        db.collection("users").get()
                .addOnSuccessListener(users -> {
                    String totalUsers = String.valueOf(users.size());
                    tvTotalUsers.setText(totalUsers);
                });

        // Tổng số đơn hàng và doanh thu
        db.collection("orders").get()
                .addOnSuccessListener(orders -> {
                    int totalOrders = orders.size();
                    tvTotalOrders.setText(String.valueOf(totalOrders));

                    double totalRevenue = 0;
                    double todayRevenue = 0;
                    long todayStart = getStartOfDay();

                    for (QueryDocumentSnapshot doc : orders) {
                        Order order = doc.toObject(Order.class);
                        if ("delivered".equals(order.getStatus())) {
                            totalRevenue += order.getFinalTotal();
                            if (order.getDeliveredAtMillis() >= todayStart) {
                                todayRevenue += order.getFinalTotal();
                            }
                        }
                    }

                    tvTotalRevenue.setText(currencyFormat.format(totalRevenue) + "đ");
                    tvTodayRevenue.setText(currencyFormat.format(todayRevenue) + "đ");
                });
    }

    private long getStartOfDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String todayStr = sdf.format(new Date());
        try {
            Date today = sdf.parse(todayStr);
            if (today != null) {
                return today.getTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis() - (24 * 60 * 60 * 1000);
    }
}