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
import com.example.foodorder.utils.LoginSessionManager;
import com.example.foodorder.utils.RestaurantHelper;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RatingFragment extends Fragment {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private RatingOrderAdapter adapter;
    private List<Order> orderList;
    private FirebaseRepository repository;
    private LoginSessionManager sessionManager;
    private String userId = "user123";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating_list, container, false);

        rvOrders = view.findViewById(R.id.rvOrders);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        repository = FirebaseRepository.getInstance();
        sessionManager = new LoginSessionManager(getContext());
        orderList = new ArrayList<>();

        userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            if (getActivity() != null) {
                userId = getActivity().getSharedPreferences("UserPrefs", 0)
                        .getString("user_email", "user123");
            }
        }

        setupRecyclerView();
        loadOrders();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new RatingOrderAdapter(orderList,
                order -> showOrderDetail(order),
                order -> showRatingDialog(order)
        );
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        repository.getOrdersByStatus(userId, "delivered", new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orderList.clear();
                for (Order order : data) {
                    if (!order.isRated()) {
                        orderList.add(order);
                    }
                }

                if (orderList.isEmpty()) {
                    rvOrders.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvOrders.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOrderDetail(Order order) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_order_detail, null);

        TextView tvOrderId = dialogView.findViewById(R.id.tvOrderId);
        TextView tvRestaurantName = dialogView.findViewById(R.id.tvRestaurantName);
        TextView tvOrderDate = dialogView.findViewById(R.id.tvOrderDate);
        TextView tvPaymentMethod = dialogView.findViewById(R.id.tvPaymentMethod);
        TextView tvItems = dialogView.findViewById(R.id.tvItems);
        TextView tvOrderNote = dialogView.findViewById(R.id.tvOrderNote);
        TextView tvTotalPrice = dialogView.findViewById(R.id.tvTotalPrice);
        TextView tvStatus = dialogView.findViewById(R.id.tvStatus);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        String orderCode = order.getOrderCode();
        if (orderCode == null || orderCode.isEmpty()) {
            String id = order.getId();
            orderCode = id != null && id.length() > 8 ? id.substring(0, 8) : id;
        }
        tvOrderId.setText(orderCode);

        tvRestaurantName.setText(RestaurantHelper.getRestaurantNameFromOrder(order));

        if (order.getCreatedAt() > 0) {
            tvOrderDate.setText(sdf.format(new Date(order.getCreatedAt())));
        } else {
            tvOrderDate.setText("Đang cập nhật");
        }

        String paymentMethod = order.getPaymentMethod();
        if ("COD".equals(paymentMethod)) {
            tvPaymentMethod.setText("💵 Thanh toán khi nhận hàng");
        } else if ("Wallet".equals(paymentMethod)) {
            tvPaymentMethod.setText("💳 Ví điện tử");
        } else {
            tvPaymentMethod.setText(paymentMethod != null ? paymentMethod : "COD");
        }

        tvTotalPrice.setText(f.format(order.getFinalTotal()) + "đ");
        tvStatus.setText("✅ Đã giao thành công");
        tvStatus.setTextColor(0xFF4CAF50);

        StringBuilder itemsText = new StringBuilder();
        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                String name = (String) item.get("name");
                long quantity = 1;
                Object qtyObj = item.get("quantity");
                if (qtyObj instanceof Long) quantity = (Long) qtyObj;
                else if (qtyObj instanceof Double) quantity = ((Double) qtyObj).longValue();
                itemsText.append("• ").append(name).append(" x").append(quantity).append("\n");

                String note = (String) item.get("note");
                if (note != null && !note.isEmpty()) {
                    itemsText.append("  📝 ").append(note).append("\n");
                }
            }
        }
        tvItems.setText(itemsText.toString());

        String orderNote = order.getOrderNote();
        if (orderNote != null && !orderNote.isEmpty()) {
            tvOrderNote.setText(orderNote);
        } else {
            tvOrderNote.setText("Không có ghi chú");
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        dialog.show();

        btnClose.setOnClickListener(v -> dialog.dismiss());
    }

    private void showRatingDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rating, null);

        TextView tvRestaurantName = dialogView.findViewById(R.id.tvRestaurantName);
        TextView tvOrderInfo = dialogView.findViewById(R.id.tvOrderInfo);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        tvRestaurantName.setText(RestaurantHelper.getRestaurantNameFromOrder(order));

        String orderInfo = "Mã đơn: " + order.getOrderCode() + "\n" +
                "Ngày đặt: " + sdf.format(new Date(order.getCreatedAt())) + "\n" +
                "Tổng tiền: " + f.format(order.getFinalTotal()) + "đ";
        tvOrderInfo.setText(orderInfo);

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Đánh giá nhà hàng")
                .setCancelable(false)
                .create();
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
                removeOrderFromList(order);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

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
        private OnItemClickListener itemClickListener;
        private OnRateClickListener rateClickListener;

        interface OnItemClickListener { void onItemClick(Order order); }
        interface OnRateClickListener { void onRateClick(Order order); }

        RatingOrderAdapter(List<Order> orders, OnItemClickListener itemClickListener, OnRateClickListener rateClickListener) {
            this.orders = orders;
            this.itemClickListener = itemClickListener;
            this.rateClickListener = rateClickListener;
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
            holder.bind(order, itemClickListener, rateClickListener);
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvOrderDate, tvRestaurantName, tvFoodItems, tvTotalPrice;
            Button btnRate;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrderId = itemView.findViewById(R.id.tvOrderId);
                tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
                tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
                tvFoodItems = itemView.findViewById(R.id.tvFoodItems);
                tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
                btnRate = itemView.findViewById(R.id.btnRate);
            }

            void bind(Order order, OnItemClickListener itemClickListener, OnRateClickListener rateClickListener) {
                NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                String orderCode = order.getOrderCode();
                if (orderCode == null || orderCode.isEmpty()) {
                    String id = order.getId();
                    orderCode = id != null && id.length() > 6 ? id.substring(0, 6) : "ORD";
                }
                tvOrderId.setText("Mã: #" + orderCode);

                if (order.getCreatedAt() > 0) {
                    tvOrderDate.setText(sdf.format(new Date(order.getCreatedAt())));
                } else {
                    tvOrderDate.setText("Đang cập nhật");
                }

                tvRestaurantName.setText(RestaurantHelper.getRestaurantNameFromOrder(order));
                tvTotalPrice.setText(f.format(order.getFinalTotal()) + "đ");

                StringBuilder itemsText = new StringBuilder();
                if (order.getItems() != null) {
                    for (Map<String, Object> item : order.getItems()) {
                        String name = (String) item.get("name");
                        long quantity = 1;
                        Object qtyObj = item.get("quantity");
                        if (qtyObj instanceof Long) quantity = (Long) qtyObj;
                        else if (qtyObj instanceof Double) quantity = ((Double) qtyObj).longValue();
                        itemsText.append("• ").append(name).append(" x").append(quantity).append("\n");

                        String note = (String) item.get("note");
                        if (note != null && !note.isEmpty()) {
                            itemsText.append("  📝 ").append(note).append("\n");
                        }
                    }
                }
                tvFoodItems.setText(itemsText.toString());

                btnRate.setOnClickListener(v -> rateClickListener.onRateClick(order));
                itemView.setOnClickListener(v -> itemClickListener.onItemClick(order));
            }
        }
    }
}