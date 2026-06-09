package com.foodorder.admin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.foodorder.admin.R;
import com.foodorder.admin.model.Order;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    private List<Order> orderList;
    private OnOrderActionListener listener;
    private NumberFormat currencyFormat;
    private SimpleDateFormat sdf;
    private Context context;

    public interface OnOrderActionListener {
        void onUpdateStatus(Order order, String newStatus);
        void onViewDetail(Order order);
    }

    public AdminOrderAdapter(List<Order> orderList, OnOrderActionListener listener) {
        this.orderList = orderList;
        this.listener = listener;
        this.currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        String orderCode = order.getOrderCode() != null ? order.getOrderCode() : order.getId();
        if (orderCode.length() > 8) {
            orderCode = orderCode.substring(0, 8);
        }
        holder.tvOrderId.setText("#" + orderCode);
        holder.tvCustomer.setText(order.getDeliveryName() != null ? order.getDeliveryName() : "Khách hàng");
        holder.tvTotal.setText(currencyFormat.format(order.getFinalTotal()) + "đ");
        holder.tvDate.setText(order.getCreatedAtMillis() > 0 ? sdf.format(new Date(order.getCreatedAtMillis())) : "Đang cập nhật");
        holder.tvStatus.setText(getStatusText(order.getStatus()));

        holder.btnViewDetail.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetail(order);
        });

        holder.btnUpdateStatus.setOnClickListener(v -> showStatusDialog(order));
    }

    private void showStatusDialog(Order order) {
        String[] statuses = {"pending", "confirmed", "preparing", "delivering", "delivered", "cancelled"};
        String[] statusNames = {"Chờ xác nhận", "Đã xác nhận", "Đang chuẩn bị", "Đang giao", "Đã giao", "Đã hủy"};

        new AlertDialog.Builder(context)
                .setTitle("Cập nhật trạng thái đơn hàng #" + order.getOrderCode())
                .setItems(statusNames, (dialog, which) -> {
                    if (listener != null) {
                        listener.onUpdateStatus(order, statuses[which]);
                    }
                })
                .show();
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "⏳ Chờ xác nhận";
            case "confirmed": return "✅ Đã xác nhận";
            case "preparing": return "🍳 Đang chuẩn bị";
            case "delivering": return "🚚 Đang giao";
            case "delivered": return "📦 Đã giao";
            case "cancelled": return "❌ Đã hủy";
            default: return status;
        }
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
        TextView tvOrderId, tvCustomer, tvTotal, tvDate, tvStatus;
        Button btnViewDetail, btnUpdateStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomer = itemView.findViewById(R.id.tvCustomer);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }
}