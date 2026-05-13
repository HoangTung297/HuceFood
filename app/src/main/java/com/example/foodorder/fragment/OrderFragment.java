package com.example.foodorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.adapter.SuggestRestaurantAdapter;
import com.example.foodorder.model.Restaurant;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private OrderPagerAdapter pagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        setupViewPager();
        setupTabLayout();

        return view;
    }

    private void setupViewPager() {
        pagerAdapter = new OrderPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Đang đến");
                    break;
                case 1:
                    tab.setText("Deal đã mua");
                    break;
                case 2:
                    tab.setText("Lịch sử");
                    break;
                case 3:
                    tab.setText("Đánh giá");
                    break;
                case 4:
                    tab.setText("Đơn nhận");
                    break;
            }
        }).attach();
    }

    // Adapter cho ViewPager
    class OrderPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
        public OrderPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new DeliveringFragment();
                case 1:
                    return new DealBoughtFragment();
                case 2:
                    return new OrderHistoryFragment();
                case 3:
                    return new RatingFragment();
                case 4:
                    return new OrderReceivedFragment();
                default:
                    return new DeliveringFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 5;
        }
    }
}