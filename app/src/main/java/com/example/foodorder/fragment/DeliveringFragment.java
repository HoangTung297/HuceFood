package com.example.foodorder.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorder.R;
import com.example.foodorder.adapter.FoodAdapter;
import com.example.foodorder.model.CartItem;
import com.example.foodorder.model.Food;
import com.example.foodorder.model.Order;
import com.example.foodorder.repository.FirebaseRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeliveringFragment extends Fragment {

    private View cardOrder;
    private TextView tvOrderId, tvRestaurantName, tvFoodItems, tvTotalPrice;
    private Button btnTrack;
    private LinearLayout layoutEmpty;
    private RecyclerView rvSuggestions;

    private String userId;
    private FirebaseRepository repository;
    private FoodAdapter foodAdapter;
    private List<Food> suggestionList = new ArrayList<>();

    public DeliveringFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivering, container, false);

        // Ánh xạ view
        cardOrder = view.findViewById(R.id.cardOrder);
        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvRestaurantName = view.findViewById(R.id.tvRestaurantName);
        tvFoodItems = view.findViewById(R.id.tvFoodItems);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnTrack = view.findViewById(R.id.btnTrack);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        rvSuggestions = view.findViewById(R.id.rvSuggestions);

        // Lấy userId
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
        userId = prefs.getString("user_id", "user123");

        repository = FirebaseRepository.getInstance();

        // Setup gợi ý món ăn
        foodAdapter = new FoodAdapter(suggestionList,
                food -> {
                    Toast.makeText(requireContext(), "Xem chi tiết: " + food.getName(), Toast.LENGTH_SHORT).show();
                },
                food -> {
                    addToCart(food);
                }
        );
        rvSuggestions.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        rvSuggestions.setAdapter(foodAdapter);

        // Tải dữ liệu lần đầu
        refreshData();

        // Nút theo dõi
        btnTrack.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Chức năng theo dõi đơn hàng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    /**
     * Phương thức public để OrderFragment gọi refresh lại dữ liệu.
     */
    public void refreshData() {
        checkDeliveringOrder();
        loadSuggestions();
    }

    private void checkDeliveringOrder() {
        repository.getOrdersByStatus(userId, "delivering", new FirebaseRepository.OnDataLoaded<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                if (data != null && !data.isEmpty()) {
                    showOrderCard(data.get(0));
                } else {
                    cardOrder.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Lỗi tải đơn hàng: " + error, Toast.LENGTH_SHORT).show();
                cardOrder.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showOrderCard(Order order) {
        cardOrder.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        tvOrderId.setText("Mã đơn: " + (order.getOrderCode() != null ? order.getOrderCode() : order.getId()));
        tvRestaurantName.setText(order.getRestaurantName() != null ? order.getRestaurantName() : "Đang cập nhật");

        StringBuilder itemsText = new StringBuilder();
        if (order.getItems() != null) {
            for (java.util.Map<String, Object> item : order.getItems()) {
                String name = (String) item.get("name");
                long quantity = ((Number) item.get("quantity")).longValue();
                itemsText.append("• ").append(name).append(" x").append(quantity).append("\n");
            }
        }
        tvFoodItems.setText(itemsText.toString());

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText(formatter.format(order.getFinalTotal()) + "đ");
    }

    private void loadSuggestions() {
        // TODO: Thay bằng dữ liệu thực từ Firebase
        suggestionList.clear();

        Food f1 = new Food();
        f1.setId("1");
        f1.setName("Gà rán giòn");
        f1.setPrice(50000);
        f1.setImageUrl("");
        suggestionList.add(f1);

        Food f2 = new Food();
        f2.setId("2");
        f2.setName("Trà sữa trân châu");
        f2.setPrice(35000);
        suggestionList.add(f2);

        foodAdapter.notifyDataSetChanged();
    }

    private void addToCart(Food food) {
        CartItem item = new CartItem();
        item.setFoodId(food.getId());
        item.setName(food.getName());
        item.setPrice(food.getPrice());
        item.setQuantity(1);
        item.setImageUrl(food.getImageUrl());

        repository.addToCart(userId, item, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(requireContext(), "Đã thêm " + food.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}