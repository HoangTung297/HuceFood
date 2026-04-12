package com.example.foodorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.foodorder.R;
import com.example.foodorder.adapter.BannerAdapter;
import com.example.foodorder.adapter.RestaurantAdapter;
import com.example.foodorder.adapter.VoucherAdapter;
import com.example.foodorder.model.Banner;
import com.example.foodorder.model.Restaurant;
import com.example.foodorder.model.Voucher;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 bannerViewPager;
    private TabLayout tabLayout;
    private RecyclerView rvVouchers, rvRestaurants;
    private TextView tvAddress;
    private ImageView ivAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupBanner();
        setupVouchers();
        setupRestaurants();

        return view;
    }

    private void initViews(View view) {
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        rvVouchers = view.findViewById(R.id.rvVouchers);
        rvRestaurants = view.findViewById(R.id.rvRestaurants);
        tvAddress = view.findViewById(R.id.tvAddress);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        tvAddress.setText("📍 ");
    }

    private void setupBanner() {
        List<Banner> bannerList = new ArrayList<>();
        bannerList.add(new Banner(1, "", "🎉 Giảm 50%"));
        bannerList.add(new Banner(2, "", "🚚 Freeship 0Đ"));
        bannerList.add(new Banner(3, "", "🎁 Mua 1 tặng 1"));

        BannerAdapter bannerAdapter = new BannerAdapter(bannerList);
        bannerViewPager.setAdapter(bannerAdapter);

        new TabLayoutMediator(tabLayout, bannerViewPager, (tab, position) -> {}).attach();
    }

    private void setupVouchers() {
        List<Voucher> voucherList = new ArrayList<>();
        voucherList.add(new Voucher(1, "Gà Rán KFC", "-50%", "1 BÁNH TRƯNG 9.000₫"));
        voucherList.add(new Voucher(2, "Gà Rán Popeyes", "-50%", "1 MIẾNG GÀ RÁN GIÒN"));
        voucherList.add(new Voucher(3, "Combo 2 Gà", "-35%", "64.000₫"));
        voucherList.add(new Voucher(4, "Trà Sữa", "-40%", "19.000₫"));

        rvVouchers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        VoucherAdapter voucherAdapter = new VoucherAdapter(voucherList);
        rvVouchers.setAdapter(voucherAdapter);
    }

    private void setupRestaurants() {
        List<Restaurant> restaurantList = new ArrayList<>();
        restaurantList.add(new Restaurant(1, "GAETA PIZZA & STEAK", "KHU ĐÔ THỊ VIỆT HƯNG",
                4.7, 0.6, "30phút", "⭐ Mã giảm 19%", ""));
        restaurantList.add(new Restaurant(2, "Ăn Vặt Nhà Dâu Bột", "KĐT Việt Hưng",
                4.5, 0.7, "41phút", "⭐ Mã giảm 19%", ""));
        restaurantList.add(new Restaurant(3, "Highlands Coffee", "Phan Văn Đáng",
                4.4, 0.8, "25phút", "⭐ Giảm 50%", ""));
        restaurantList.add(new Restaurant(4, "KFC", "Nguyễn Văn Linh",
                4.6, 1.2, "35phút", "⭐ Giảm 30%", ""));
        restaurantList.add(new Restaurant(5, "Lotteria", "Hùng Vương",
                4.3, 1.5, "40phút", "⭐ Mua 1 tặng 1", ""));

        rvRestaurants.setLayoutManager(new LinearLayoutManager(getContext()));
        RestaurantAdapter restaurantAdapter = new RestaurantAdapter(restaurantList);
        rvRestaurants.setAdapter(restaurantAdapter);

        restaurantAdapter.setOnItemClickListener(restaurant -> {
            // Xử lý click vào nhà hàng
        });
    }
}