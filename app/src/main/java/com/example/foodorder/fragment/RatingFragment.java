package com.example.foodorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.adapter.RatingAdapter;
import com.example.foodorder.model.RatingItem;
import java.util.ArrayList;
import java.util.List;

public class RatingFragment extends Fragment {

    private RecyclerView rvRating;
    private TextView tvEmpty;
    private RatingAdapter ratingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating, container, false);

        initViews(view);
        setupRecyclerView();
        loadRatingItems();

        return view;
    }

    private void initViews(View view) {
        rvRating = view.findViewById(R.id.rvRating);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void setupRecyclerView() {
        rvRating.setLayoutManager(new LinearLayoutManager(getContext()));
        ratingAdapter = new RatingAdapter(new ArrayList<>());
        rvRating.setAdapter(ratingAdapter);

        ratingAdapter.setOnRatingSubmitListener((item, rating) -> {
            Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá " + item.getFoodName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadRatingItems() {
        List<RatingItem> ratingItems = new ArrayList<>();

        ratingItems.add(new RatingItem(1, "Gà rán KFC", "Đã giao ngày 10/05/2024", false));
        ratingItems.add(new RatingItem(2, "Pizza Hut", "Đã giao ngày 05/05/2024", false));

        if (ratingItems.isEmpty()) {
            rvRating.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Chưa có đơn hàng nào cần đánh giá");
        } else {
            rvRating.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            ratingAdapter.updateList(ratingItems);
        }
    }
}
