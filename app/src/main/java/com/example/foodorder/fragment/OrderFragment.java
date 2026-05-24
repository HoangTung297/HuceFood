package com.example.foodorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.foodorder.R;
import com.example.foodorder.adapter.OrderPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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

        return view;
    }

    private void setupViewPager() {
        pagerAdapter = new OrderPagerAdapter(requireActivity());

        pagerAdapter.addFragment(new CartFragment(), "🛒 Giỏ hàng");
        pagerAdapter.addFragment(new DeliveringFragment(), "📦 Đang giao");
        pagerAdapter.addFragment(new OrderReceivedFragment(), "✅ Đã nhận");
        pagerAdapter.addFragment(new RatingFragment(), "⭐ Đánh giá");
        pagerAdapter.addFragment(new OrderHistoryFragment(), "📜 Lịch sử");

        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(pagerAdapter.getTabTitle(position))
        ).attach();
    }

    public void refreshAllTabs() {
        if (pagerAdapter == null) return;
        for (int i = 0; i < pagerAdapter.getItemCount(); i++) {
            Fragment fragment = getChildFragmentManager().findFragmentByTag("f" + i);
            if (fragment instanceof CartFragment) {
                ((CartFragment) fragment).refreshData();
            } else if (fragment instanceof DeliveringFragment) {
                ((DeliveringFragment) fragment).refreshData();
            } else if (fragment instanceof OrderReceivedFragment) {
                ((OrderReceivedFragment) fragment).refreshData();
            } else if (fragment instanceof OrderHistoryFragment) {
                ((OrderHistoryFragment) fragment).refreshData();
            }
        }
    }
}