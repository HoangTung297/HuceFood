package com.example.foodorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.adapter.DealAdapter;
import com.example.foodorder.model.Deal;
import java.util.ArrayList;
import java.util.List;

public class DealBoughtFragment extends Fragment {

    private RecyclerView rvDeals;
    private TextView tvEmpty;
    private DealAdapter dealAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deal_bought, container, false);

        initViews(view);
        setupRecyclerView();
        loadDeals();

        return view;
    }

    private void initViews(View view) {
        rvDeals = view.findViewById(R.id.rvDeals);
        tvEmpty = view.findViewById(R.id.tvEmpty);
    }

    private void setupRecyclerView() {
        rvDeals.setLayoutManager(new LinearLayoutManager(getContext()));
        dealAdapter = new DealAdapter(new ArrayList<>());
        rvDeals.setAdapter(dealAdapter);
    }

    private void loadDeals() {
        List<Deal> deals = new ArrayList<>();

        deals.add(new Deal(1, "Gà rán KFC", "Giảm 50%", "Đã mua 2 lần", "12/05/2024"));
        deals.add(new Deal(2, "Pizza Hut", "Mua 1 tặng 1", "Đã mua 1 lần", "10/05/2024"));
        deals.add(new Deal(3, "Trà sữa Gong Cha", "Giảm 40%", "Đã mua 3 lần", "08/05/2024"));

        if (deals.isEmpty()) {
            rvDeals.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvDeals.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            dealAdapter.updateList(deals);
        }
    }
}