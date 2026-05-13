package com.example.foodorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.adapter.SuggestRestaurantAdapter;
import com.example.foodorder.model.Restaurant;
import java.util.ArrayList;
import java.util.List;

public class DeliveringFragment extends Fragment {

    // ============ KHAI BÁO TẤT CẢ BIẾN ============
    private RecyclerView rvSuggestions;           // ✅ RecyclerView cho gợi ý
    private TextView tvOrderId;                   // ✅ Mã đơn hàng
    private TextView tvRestaurantName;            // ✅ Tên nhà hàng
    private TextView tvFoodItems;                 // ✅ Danh sách món ăn
    private TextView tvTotalPrice;                // ✅ Tổng tiền
    private Button btnTrack;                      // ✅ Nút theo dõi
    private View layoutEmpty;                     // ✅ Layout khi không có đơn hàng
    private View cardOrder;                       // ✅ Card đơn hàng
    private SuggestRestaurantAdapter suggestAdapter;  // ✅ Adapter gợi ý

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivering, container, false);

        initViews(view);
        setupSuggestions();
        loadOrderData();
        loadSuggestions();

        return view;
    }

    private void initViews(View view) {
        // Ánh xạ các view
        rvSuggestions = view.findViewById(R.id.rvSuggestions);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        cardOrder = view.findViewById(R.id.cardOrder);
        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvRestaurantName = view.findViewById(R.id.tvRestaurantName);
        tvFoodItems = view.findViewById(R.id.tvFoodItems);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnTrack = view.findViewById(R.id.btnTrack);
    }

    private void setupSuggestions() {
        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        suggestAdapter = new SuggestRestaurantAdapter(new ArrayList<>());
        rvSuggestions.setAdapter(suggestAdapter);
    }

    private void loadOrderData() {
        // TODO: Thay bằng dữ liệu thật từ database
        boolean hasOrder = false;  // Giả sử chưa có đơn hàng

        if (hasOrder) {
            // Có đơn hàng: hiển thị card, ẩn layout rỗng
            cardOrder.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            // Gán dữ liệu mẫu
            tvOrderId.setText("#ĐƠN2024001");
            tvRestaurantName.setText("Gà Rán KFC");
            tvFoodItems.setText("• Gà rán giòn x2\n• Khoai tây chiên x1\n• Pepsi x1");
            tvTotalPrice.setText("145.000đ");

            btnTrack.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Đang theo dõi đơn hàng...", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Không có đơn hàng: ẩn card, hiển thị layout rỗng
            cardOrder.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void loadSuggestions() {
        List<Restaurant> suggestions = new ArrayList<>();

        // Dữ liệu mẫu cho gợi ý
        suggestions.add(new Restaurant(1, "Bún Bò Huế, Cơm Gà và Cơm Sườn - Gia Huy Quân",
                "Linh Đàm", 4.6, 0.5, "25phút", "Giảm món | Mã giảm 19%", ""));

        suggestions.add(new Restaurant(2, "Trà Chanh Bụi Phố - Linh Đàm",
                "Linh Đàm", 4.2, 0.3, "22phút", "Mã giảm 19%", ""));

        suggestions.add(new Restaurant(3, "Ăn Vật Bon Bon - Thịt Trâu Gác Bếp Tây Bắc",
                "Hoàng Mai", 4.5, 0.8, "30phút", "Giảm 15%", ""));

        suggestions.add(new Restaurant(4, "Pizza Hut - Linh Đàm",
                "Linh Đàm", 4.7, 1.2, "35phút", "Mua 1 tặng 1", ""));

        suggestAdapter.updateList(suggestions);
    }
}