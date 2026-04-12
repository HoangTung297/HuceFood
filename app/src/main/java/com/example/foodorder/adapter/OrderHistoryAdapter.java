package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Order;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<Order> orderList;

    public OrderHistoryAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateList(List<Order> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal, tvItemCount;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
        }

        void bind(Order order) {
            tvOrderId.setText("Mã đơn: #" + order.getId());
            tvOrderDate.setText("Ngày: " + order.getOrderDate());
            tvOrderStatus.setText("Trạng thái: " + order.getStatus());

            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvOrderTotal.setText("Tổng: " + formatter.format(order.getTotalPrice()) + " VNĐ");

            int itemCount = order.getFoodItems() != null ? order.getFoodItems().size() : 0;
            tvItemCount.setText("Số món: " + itemCount);
        }
    }
}