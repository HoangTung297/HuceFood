package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Order;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private List<Order> orderList;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(Order order);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Order order, int position);
    }

    public OrderHistoryAdapter(List<Order> orderList, OnItemClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    public OrderHistoryAdapter(List<Order> orderList, OnItemClickListener listener, OnDeleteClickListener deleteListener) {
        this.orderList = orderList;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order, listener, deleteListener, position);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateList(List<Order> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvRestaurantName, tvFoodItems, tvTotalPrice, tvStatus;
        Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvFoodItems = itemView.findViewById(R.id.tvFoodItems);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Order order, OnItemClickListener listener, OnDeleteClickListener deleteListener, int position) {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

            tvOrderId.setText("Mã: " + (order.getOrderCode() != null ? order.getOrderCode() : order.getId()));

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvOrderDate.setText(sdf.format(new java.util.Date(order.getCreatedAt())));

            tvRestaurantName.setText(order.getRestaurantName());

            StringBuilder itemsText = new StringBuilder();
            if (order.getItems() != null) {
                for (Map<String, Object> item : order.getItems()) {
                    String name = (String) item.get("name");
                    long quantity = ((Number) item.get("quantity")).longValue();
                    itemsText.append("• ").append(name).append(" x").append(quantity).append("\n");
                }
            }
            tvFoodItems.setText(itemsText.toString());
            tvTotalPrice.setText(formatter.format(order.getFinalTotal()) + "đ");
            tvStatus.setText(order.getStatusText());

            // Nút xóa (chỉ hiển thị nếu đơn đã giao và có thể xóa)
            if (btnDelete != null && "delivered".equals(order.getStatus())) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteClick(order, position);
                    }
                });
            } else if (btnDelete != null) {
                btnDelete.setVisibility(View.GONE);
            }

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onItemClick(order));
            }
        }
    }
}