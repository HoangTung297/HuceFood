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
import com.example.foodorder.model.Rating;
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
    private boolean isLoading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating_list, container, false);
        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        repository = FirebaseRepository.getInstance();
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
        adapter = new RatingOrderAdapter(orderList, order -> showRatingDialog(order));
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        if (isLoading) return;
        isLoading = true;

        repository.getOrdersByStatus(userId, "delivered", new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orderList.clear();
                for (Order order : data) {
                    if (!order.isRated()) orderList.add(order);
                }

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvOrders.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
                isLoading = false;
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                isLoading = false;
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

        tvRestaurantName.setText(order.getRestaurantName());
        AlertDialog dialog = builder.setView(dialogView).setTitle("Đánh giá").create();
        dialog.show();

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating == 0) {
                Toast.makeText(getContext(), "Chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }
            saveRating(order, rating, etComment.getText().toString());
            dialog.dismiss();
        });
    }

    private void saveRating(Order order, double rating, String comment) {
        repository.updateOrderRating(order.getId(), rating, comment, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                loadOrders();
            }
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class RatingOrderAdapter extends RecyclerView.Adapter<RatingOrderAdapter.ViewHolder> {
        private List<Order> orders;
        private OnRateClickListener listener;
        interface OnRateClickListener { void onRateClick(Order order); }
        RatingOrderAdapter(List<Order> orders, OnRateClickListener listener) {
            this.orders = orders;
            this.listener = listener;
        }
        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_for_rating, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(orders.get(position), listener);
        }
        @Override public int getItemCount() { return orders.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvRestaurantName, tvOrderDate;
            Button btnRate;
            ViewHolder(@NonNull View v) {
                super(v);
                tvOrderId = v.findViewById(R.id.tvOrderId);
                tvRestaurantName = v.findViewById(R.id.tvRestaurantName);
                tvOrderDate = v.findViewById(R.id.tvOrderDate);
                btnRate = v.findViewById(R.id.btnRate);
            }
            void bind(Order order, OnRateClickListener listener) {
                tvOrderId.setText("Mã: " + (order.getOrderCode() != null ? order.getOrderCode() : order.getId()));
                tvRestaurantName.setText(order.getRestaurantName());
                tvOrderDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new java.util.Date(order.getCreatedAt())));
                btnRate.setOnClickListener(v -> listener.onRateClick(order));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }
}