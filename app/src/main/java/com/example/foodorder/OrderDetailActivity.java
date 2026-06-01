package com.example.foodorder;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvStatus, tvOrderId, tvRestaurantName, tvOrderDate, tvPaymentMethod, tvItems, tvOrderNote, tvTotalPrice;
    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_order_detail);

        initViews();

        String orderId = getIntent().getStringExtra("orderId");
        if (orderId != null) {
            loadOrderDetail(orderId);
        }

        btnClose.setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvItems = findViewById(R.id.tvItems);
        tvOrderNote = findViewById(R.id.tvOrderNote);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnClose = findViewById(R.id.btnClose);
    }

    private void loadOrderDetail(String orderId) {
        FirebaseRepository.getInstance().getOrderById(orderId, new FirebaseRepository.OnDataLoaded<Order>() {
            @Override
            public void onSuccess(Order order) {
                displayOrder(order);
            }

            @Override
            public void onError(String error) {
                tvOrderNote.setText("Lỗi tải đơn hàng: " + error);
            }
        });
    }

    private void displayOrder(Order order) {
        NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));

        // Trạng thái
        String status = order.getStatus();
        if ("pending".equals(status)) {
            tvStatus.setText("Chờ xác nhận");
            tvStatus.setBackgroundColor(0xFFFF9800);
        } else if ("delivering".equals(status)) {
            tvStatus.setText("Đang giao");
            tvStatus.setBackgroundColor(0xFF2196F3);
        } else if ("delivered".equals(status)) {
            tvStatus.setText("Đã giao");
            tvStatus.setBackgroundColor(0xFF4CAF50);
        } else if ("cancelled".equals(status)) {
            tvStatus.setText("Đã hủy");
            tvStatus.setBackgroundColor(0xFFF44336);
        }

        tvOrderId.setText(order.getOrderCode() != null ? order.getOrderCode() : order.getId());
        tvRestaurantName.setText(order.getRestaurantName());
        tvOrderDate.setText(sdf.format(new Date(order.getCreatedAt())));

        String paymentText = "COD";
        if ("Banking".equals(order.getPaymentMethod())) paymentText = "Chuyển khoản";
        if ("Wallet".equals(order.getPaymentMethod())) paymentText = "Ví điện tử";
        tvPaymentMethod.setText(paymentText);

        // Danh sách món
        StringBuilder items = new StringBuilder();
        if (order.getItems() != null) {
            for (Map<String, Object> item : order.getItems()) {
                String name = (String) item.get("name");
                long quantity = 1;
                Object qtyObj = item.get("quantity");
                if (qtyObj instanceof Long) quantity = (Long) qtyObj;
                else if (qtyObj instanceof Double) quantity = ((Double) qtyObj).longValue();
                else if (qtyObj instanceof Integer) quantity = (Integer) qtyObj;
                items.append("• ").append(name).append(" x").append(quantity).append("\n");
            }
        }
        tvItems.setText(items.toString());

        // Ghi chú
        String note = order.getOrderNote();
        if (note == null || note.isEmpty()) {
            tvOrderNote.setText("Không có ghi chú");
        } else {
            tvOrderNote.setText(note);
        }

        tvTotalPrice.setText(f.format(order.getFinalTotal()) + "đ");
    }
}