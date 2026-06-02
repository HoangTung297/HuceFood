package com.example.foodorder.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RatingFragment extends Fragment {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private RatingOrderAdapter adapter;
    private List<Order> orderList;
    private FirebaseRepository repository;
    private String userId = "user123";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating_list, container, false);

        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        repository = FirebaseRepository.getInstance();
        orderList = new ArrayList<>();

        if (getActivity() != null) {
            userId = getActivity().getSharedPreferences("UserPrefs", 0)
                    .getString("user_id", "user123");
            if (userId == null || userId.isEmpty()) {
                userId = getActivity().getSharedPreferences("UserPrefs", 0)
                        .getString("user_email", "user123");
            }
        }

        setupRecyclerView();
        loadOrders();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new RatingOrderAdapter(orderList, order -> showRatingDialog(order));
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        repository.getOrdersByStatus(userId, "delivered", new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orderList.clear();
                if (data != null) {
                    for (Order order : data) {
                        if (!order.isRated()) {
                            orderList.add(order);
                        }
                    }
                }

                adapter.notifyDataSetChanged();

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvOrders.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                rvOrders.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showRatingDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rating, null);

        TextView tvRestaurantName = dialogView.findViewById(R.id.tvRestaurantName);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        tvRestaurantName.setText(order.getRestaurantName());

        AlertDialog dialog = builder.setView(dialogView).setTitle("Đánh giá nhà hàng").create();
        dialog.show();

        btnSubmit.setOnClickListener(v -> {
            float ratingValue = ratingBar.getRating();
            String comment = etComment.getText().toString();

            if (ratingValue == 0) {
                Toast.makeText(getContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }

            saveRating(order, ratingValue, comment, dialog);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void saveRating(Order order, double ratingValue, String comment, AlertDialog dialog) {
        repository.updateOrderRating(order.getId(), ratingValue, comment, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                // Xóa đơn khỏi danh sách ngay lập tức
                removeOrderFromList(order);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Xóa đơn khỏi danh sách sau khi đánh giá
    private void removeOrderFromList(Order order) {
        int position = -1;
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getId().equals(order.getId())) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            orderList.remove(position);
            adapter.notifyItemRemoved(position);

            // Kiểm tra nếu danh sách rỗng thì hiển thị layout trống
            if (orderList.isEmpty()) {
                rvOrders.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        }
    }

    public void refreshData() {
        loadOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    // ==================== ADAPTER ====================
    static class RatingOrderAdapter extends RecyclerView.Adapter<RatingOrderAdapter.ViewHolder> {
        private List<Order> orders;
        private OnRateClickListener listener;

        interface OnRateClickListener {
            void onRateClick(Order order);
        }

        RatingOrderAdapter(List<Order> orders, OnRateClickListener listener) {
            this.orders = orders;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_for_rating, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.bind(order, listener);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvRestaurantName, tvOrderDate;
            Button btnRate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                btnRate = itemView.findViewById(R.id.btnRate);
            }

            void bind(Order order, OnRateClickListener listener) {
                String orderCode = order.getOrderCode();
                if (orderCode == null || orderCode.isEmpty()) {
                    String id = order.getId();
                    orderCode = id != null && id.length() > 6 ? id.substring(0, 6) : "ORD";
                }
                tvOrderId.setText("Mã: #" + orderCode);
                tvRestaurantName.setText(order.getRestaurantName() != null ? order.getRestaurantName() : "Nhà hàng");

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                if (order.getCreatedAt() > 0) {
                    tvOrderDate.setText(sdf.format(new java.util.Date(order.getCreatedAt())));
                } else {
                    tvOrderDate.setText("Đang cập nhật");
                }

                btnRate.setOnClickListener(v -> listener.onRateClick(order));
            }
        }
    }
}