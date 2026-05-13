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
import java.util.List;

public class OrderReceivedAdapter extends RecyclerView.Adapter<OrderReceivedAdapter.ViewHolder> {

    private List<Order> orders;
    private OnReorderClickListener listener;

    public interface OnReorderClickListener {
        void onReorderClick(Order order);
    }

    public OrderReceivedAdapter(List<Order> orders) {
        this.orders = orders;
    }

    public void setOnReorderClickListener(OnReorderClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Order> newList) {
        this.orders = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_received, parent, false);
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
        TextView tvOrderId, tvOrderDate, tvTotalPrice;
        Button btnReorder;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnReorder = itemView.findViewById(R.id.btnReorder);
        }

        void bind(Order order, OnReorderClickListener listener) {
            tvOrderId.setText("#ĐƠN" + order.getId());
            tvOrderDate.setText(order.getOrderDate());
            tvTotalPrice.setText(String.format("%,.0fđ", order.getTotalPrice()));

            if (listener != null) {
                btnReorder.setOnClickListener(v -> listener.onReorderClick(order));
            }
        }
    }
}