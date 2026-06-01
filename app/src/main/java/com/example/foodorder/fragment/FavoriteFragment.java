package com.example.foodorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.foodorder.R;
import com.example.foodorder.adapter.FavoritePagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FavoriteFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FavoritePagerAdapter pagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_tab, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        setupViewPager();

        // Refresh khi chuyển tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Fragment fragment = getChildFragmentManager().findFragmentByTag("f" + position);
                if (fragment instanceof FavoriteFoodFragment) {
                    ((FavoriteFoodFragment) fragment).refreshData();
                } else if (fragment instanceof FavoriteRestaurantFragment) {
                    ((FavoriteRestaurantFragment) fragment).refreshData();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        return view;
    }

    private void setupViewPager() {
        pagerAdapter = new FavoritePagerAdapter(requireActivity());

        pagerAdapter.addFragment(new FavoriteFoodFragment(), "🍕 Món ăn");
        pagerAdapter.addFragment(new FavoriteRestaurantFragment(), "🏠 Nhà hàng");

        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(pagerAdapter.getTabTitle(position))
        ).attach();
    }
}