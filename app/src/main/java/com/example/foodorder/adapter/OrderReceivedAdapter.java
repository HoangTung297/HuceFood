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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderReceivedAdapter extends RecyclerView.Adapter<OrderReceivedAdapter.ViewHolder> {

    private List<Order> orderList;
    private OnReorderListener listener;

    public interface OnReorderListener {
        void onReorder(Order order);
    }

    public OrderReceivedAdapter(List<Order> orderList, OnReorderListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_received_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order, listener);
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
        TextView tvOrderId, tvOrderDate, tvRestaurantName, tvFoodItems, tvTotalPrice;
        Button btnReorder;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvFoodItems = itemView.findViewById(R.id.tvFoodItems);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnReorder = itemView.findViewById(R.id.btnReorder);
        }

        void bind(Order order, OnReorderListener listener) {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            tvOrderId.setText("Mã đơn: " + (order.getOrderCode() != null ? order.getOrderCode() : order.getId()));

            // ===== SỬA: Kiểm tra null và dùng trực tiếp Date =====
            if (order.getCreatedAt() != null) {
                tvOrderDate.setText(sdf.format(order.getCreatedAt()));
            } else {
                tvOrderDate.setText("Đang cập nhật");
            }
            // ===================================================

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

            btnReorder.setOnClickListener(v -> listener.onReorder(order));
        }
    }
}