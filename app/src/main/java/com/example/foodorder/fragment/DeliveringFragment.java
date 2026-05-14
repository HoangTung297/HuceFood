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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DeliveringFragment extends Fragment {

    // ============ KHAI BÁO VIEW ============
    private RecyclerView rvSuggestions;
    private TextView tvOrderId;
    private TextView tvRestaurantName;
    private TextView tvFoodItems;
    private TextView tvTotalPrice;
    private Button btnTrack;
    private View layoutEmpty;
    private View cardOrder;

    // ============ ADAPTER & DATA ============
    private SuggestRestaurantAdapter suggestAdapter;
    private List<Restaurant> suggestionList;

    // ============ FIREBASE ============
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivering, container, false);

        // Khởi tạo Firebase
        firestore = FirebaseFirestore.getInstance();
        suggestionList = new ArrayList<>();

        initViews(view);
        setupSuggestions();
        loadOrderDataFromFirebase();
        loadSuggestionsFromFirebase();

        return view;
    }

    private void initViews(View view) {
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
        suggestAdapter = new SuggestRestaurantAdapter(suggestionList);
        rvSuggestions.setAdapter(suggestAdapter);

        // Xử lý click vào nhà hàng gợi ý
        suggestAdapter.setOnItemClickListener(restaurant -> {
            Toast.makeText(getContext(), "Đã chọn: " + restaurant.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Mở chi tiết nhà hàng
        });
    }

    private void loadOrderDataFromFirebase() {
        // TODO: Load đơn hàng đang giao của user hiện tại từ Firebase
        // Hiện tại tạm thời hiển thị trạng thái không có đơn hàng
        boolean hasOrder = false;  // Sẽ thay bằng kiểm tra từ Firebase

        if (hasOrder) {
            cardOrder.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            // Gán dữ liệu từ Firebase
            btnTrack.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Đang theo dõi đơn hàng...", Toast.LENGTH_SHORT).show();
            });
        } else {
            cardOrder.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void loadSuggestionsFromFirebase() {
        // Load danh sách nhà hàng gợi ý từ Firestore
        firestore.collection("restaurants")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    suggestionList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        String address = doc.getString("address");
                        Double rating = doc.getDouble("rating");
                        Double distance = doc.getDouble("distance");
                        String deliveryTime = doc.getString("deliveryTime");
                        String discount = doc.getString("discount");
                        String imageUrl = doc.getString("imageUrl");

                        if (name == null) name = "Nhà hàng";
                        if (address == null) address = "Đang cập nhật";
                        if (rating == null) rating = 4.0;
                        if (distance == null) distance = 1.0;
                        if (deliveryTime == null) deliveryTime = "30phút";
                        if (discount == null) discount = "Giảm 10%";
                        if (imageUrl == null) imageUrl = "";

                        Restaurant restaurant = new Restaurant(id, name, address, rating,
                                distance, deliveryTime, discount, imageUrl);
                        suggestionList.add(restaurant);
                    }
                    suggestAdapter.updateList(suggestionList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải gợi ý: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}