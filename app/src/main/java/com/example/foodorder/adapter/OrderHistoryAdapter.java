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
import com.example.foodorder.utils.RestaurantHelper;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private List<Order> orders;
    private OnItemClickListener itemClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnItemClickListener {
        void onItemClick(Order order);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Order order, int position);
    }

    public OrderHistoryAdapter(List<Order> orders, OnItemClickListener itemClickListener,
                               OnDeleteClickListener deleteClickListener) {
        this.orders = orders;
        this.itemClickListener = itemClickListener;
        this.deleteClickListener = deleteClickListener;
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
        Order order = orders.get(position);
        holder.bind(order, position, itemClickListener, deleteClickListener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateList(List<Order> newList) {
        this.orders = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvRestaurantName, tvFoodItems, tvStatus, tvTotalPrice;
        Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvRestaurantName = itemView.findViewById(R.id.tvRestaurantName);
            tvFoodItems = itemView.findViewById(R.id.tvFoodItems);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Order order, int position, OnItemClickListener clickListener,
                  OnDeleteClickListener deleteListener) {
            NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

            String orderCode = order.getOrderCode();
            if (orderCode == null || orderCode.isEmpty()) {
                String id = order.getId();
                orderCode = id != null && id.length() > 6 ? id.substring(0, 6) : "ORD";
            }
            tvOrderId.setText("Mã: #" + orderCode);

            // ===== SỬA: Kiểm tra null thay vì > 0 =====
            if (order.getCreatedAt() != null) {
                tvOrderDate.setText(sdf.format(order.getCreatedAt()));
            } else {
                tvOrderDate.setText("Đang cập nhật");
            }
            // =========================================

            tvRestaurantName.setText(RestaurantHelper.getRestaurantNameFromOrder(order));
            tvTotalPrice.setText(f.format(order.getFinalTotal()) + "đ");

            // Hiển thị danh sách món
            StringBuilder items = new StringBuilder();
            if (order.getItems() != null) {
                for (Map<String, Object> item : order.getItems()) {
                    String name = (String) item.get("name");
                    long quantity = 1;
                    Object qtyObj = item.get("quantity");
                    if (qtyObj instanceof Long) quantity = (Long) qtyObj;
                    else if (qtyObj instanceof Double) quantity = ((Double) qtyObj).longValue();
                    items.append("• ").append(name).append(" x").append(quantity).append("\n");

                    String note = (String) item.get("note");
                    if (note != null && !note.isEmpty()) {
                        items.append("  📝 ").append(note).append("\n");
                    }
                }
            }
            tvFoodItems.setText(items.toString());

            String status = order.getStatus();
            if ("delivered".equals(status)) {
                tvStatus.setText("✅ Đã giao thành công");
                tvStatus.setTextColor(0xFF4CAF50);
                btnDelete.setVisibility(View.VISIBLE);
            } else if ("cancelled".equals(status)) {
                tvStatus.setText("❌ Đã hủy");
                tvStatus.setTextColor(0xFFF44336);
                btnDelete.setVisibility(View.VISIBLE);
            } else if ("pending".equals(status)) {
                tvStatus.setText("⏳ Chờ xác nhận");
                tvStatus.setTextColor(0xFFFF9800);
                btnDelete.setVisibility(View.GONE);
            } else if ("delivering".equals(status)) {
                tvStatus.setText("🚚 Đang giao");
                tvStatus.setTextColor(0xFF2196F3);
                btnDelete.setVisibility(View.GONE);
            } else {
                tvStatus.setText(status);
                btnDelete.setVisibility(View.GONE);
            }

            btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(order, position));
            itemView.setOnClickListener(v -> clickListener.onItemClick(order));
        }
    }
}