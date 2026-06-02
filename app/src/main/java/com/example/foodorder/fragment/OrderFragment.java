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

    public static final int TAB_CART = 0;
    public static final int TAB_DELIVERING = 1;
    public static final int TAB_RECEIVED = 2;
    public static final int TAB_RATING = 3;
    public static final int TAB_HISTORY = 4;

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

    public void switchToCartTab() {
        if (viewPager != null) {
            viewPager.setCurrentItem(TAB_CART, true);
        }
    }

    public void switchToDeliveringTab() {
        if (viewPager != null) {
            viewPager.setCurrentItem(TAB_DELIVERING, true);
        }
    }

    public void switchToHistoryTab() {
        if (viewPager != null) {
            viewPager.setCurrentItem(TAB_HISTORY, true);
        }
    }

    public void refreshCartFragment() {
        Fragment cartFragment = getChildFragmentManager().findFragmentByTag("f0");
        if (cartFragment instanceof CartFragment) {
            ((CartFragment) cartFragment).refreshData();
        }
    }
}