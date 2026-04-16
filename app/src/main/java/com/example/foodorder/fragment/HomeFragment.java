package com.example.foodorder.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.foodorder.FoodDetailActivity;
import com.example.foodorder.HomeActivity;
import com.example.foodorder.R;
import com.example.foodorder.adapter.BannerAdapter;
import com.example.foodorder.adapter.CategoryAdapter;
import com.example.foodorder.adapter.FoodAdapter;
import com.example.foodorder.adapter.VoucherAdapter;
import com.example.foodorder.model.Banner;
import com.example.foodorder.model.Category;
import com.example.foodorder.model.Food;
import com.example.foodorder.model.Voucher;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // Views
    private ViewPager2 bannerViewPager;
    private TabLayout tabLayout;
    private RecyclerView rvVouchers, rvCategories, rvBestSellers, rvNearby, rvTopRated, rvAllFoods;
    private TextView tvAddress;
    private ImageView ivAvatar, ivClearSearch;
    private EditText etSearch;
    private View layoutAddress;

    // Data gốc
    private List<Food> allFoods;
    private List<Food> originalBestSellers;
    private List<Food> originalNearbyFoods;
    private List<Food> originalTopRatedFoods;
    private List<Voucher> voucherList;

    // Adapters
    private FoodAdapter bestSellerAdapter, nearbyAdapter, topRatedAdapter, allFoodsAdapter;
    private CategoryAdapter categoryAdapter;
    private VoucherAdapter voucherAdapter;

    // Banner auto slide
    private Handler bannerHandler = new Handler();
    private Runnable bannerRunnable;

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_ADDRESS = "delivery_address";
    private static final int REQUEST_FOOD_DETAIL = 200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        allFoods = new ArrayList<>();
        originalBestSellers = new ArrayList<>();
        originalNearbyFoods = new ArrayList<>();
        originalTopRatedFoods = new ArrayList<>();
        voucherList = new ArrayList<>();

        if (getActivity() != null) {
            sharedPreferences = getActivity().getSharedPreferences(PREF_NAME, 0);
        }

        initViews(view);
        setupBannerInfinite();
        setupVouchers();
        setupCategories();
        loadAllData();
        setupSearch();
        setupAddressClick();
        setupAvatarClick();

        return view;
    }

    private void initViews(View view) {
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        rvVouchers = view.findViewById(R.id.rvVouchers);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvBestSellers = view.findViewById(R.id.rvBestSellers);
        rvNearby = view.findViewById(R.id.rvNearby);
        rvTopRated = view.findViewById(R.id.rvTopRated);
        rvAllFoods = view.findViewById(R.id.rvAllFoods);
        tvAddress = view.findViewById(R.id.tvAddress);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        etSearch = view.findViewById(R.id.etSearch);
        ivClearSearch = view.findViewById(R.id.ivClearSearch);
        layoutAddress = view.findViewById(R.id.layoutAddress);

        if (sharedPreferences != null) {
            String savedAddress = sharedPreferences.getString(KEY_ADDRESS, "Chọn địa chỉ giao hàng");
            tvAddress.setText(savedAddress);
        } else {
            tvAddress.setText("Chọn địa chỉ giao hàng");
        }
    }

    private void setupBannerInfinite() {
        List<Banner> bannerList = new ArrayList<>();
        bannerList.add(new Banner(1, "sale_50", "🎉 GIẢM 50% - Món ngon giá sốc"));
        bannerList.add(new Banner(2, "freeship", "🚚 FREESHIP 0Đ - Đơn từ 50K"));
        bannerList.add(new Banner(3, "mua_1_tang_1", "🎁 MUA 1 TẶNG 1"));
        bannerList.add(new Banner(4, "new_mem", "👤 NGƯỜI MỚI - Giảm thêm 50K"));

        BannerAdapter bannerAdapter = new BannerAdapter(bannerList);
        bannerViewPager.setAdapter(bannerAdapter);
        new TabLayoutMediator(tabLayout, bannerViewPager, (tab, position) -> {}).attach();

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = bannerViewPager.getCurrentItem();
                if (currentItem < bannerList.size() - 1) {
                    bannerViewPager.setCurrentItem(currentItem + 1);
                } else {
                    bannerViewPager.setCurrentItem(0);
                }
                bannerHandler.postDelayed(this, 3000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);

        bannerViewPager.setOnTouchListener((v, event) -> {
            bannerHandler.removeCallbacks(bannerRunnable);
            bannerHandler.postDelayed(bannerRunnable, 3000);
            return false;
        });
    }

    private void setupVouchers() {
        voucherList = new ArrayList<>();
        voucherList.add(new Voucher(1, "Gà Rán KFC", "-50%", "1 BÁNH TRƯNG 9.000₫"));
        voucherList.add(new Voucher(2, "Gà Rán Popeyes", "-50%", "1 MIẾNG GÀ RÁN GIÒN"));
        voucherList.add(new Voucher(3, "Combo 2 Gà", "-35%", "64.000₫"));
        voucherList.add(new Voucher(4, "Trà Sữa Gong Cha", "-40%", "19.000₫"));
        voucherList.add(new Voucher(5, "Pizza Hut", "-30%", "Pizza size L 99K"));

        rvVouchers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        voucherAdapter = new VoucherAdapter(voucherList);
        rvVouchers.setAdapter(voucherAdapter);
    }

    private void setupCategories() {
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category(1, "Tất cả", "🍕", 0));
        categoryList.add(new Category(2, "Fast Food", "🍔", 0));
        categoryList.add(new Category(3, "Món Việt", "🍜", 0));
        categoryList.add(new Category(4, "Đồ uống", "🥤", 0));
        categoryList.add(new Category(5, "Tráng miệng", "🍰", 0));
        categoryList.add(new Category(6, "Hải sản", "🦞", 0));

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            // Lọc TẤT CẢ các danh sách theo danh mục
            filterAllListsByCategory(category.getName());
        });
        rvCategories.setAdapter(categoryAdapter);
    }

    private void loadAllData() {
        allFoods = createAccurateFoods();

        // Tạo dữ liệu gốc cho 3 danh mục
        originalBestSellers = new ArrayList<>();
        originalNearbyFoods = new ArrayList<>();
        originalTopRatedFoods = new ArrayList<>();

        for (Food food : allFoods) {
            // Bán chạy nhất: soldCount > 100
            if (food.getSoldCount() > 100) {
                originalBestSellers.add(food);
            }
            // Gần tôi: distance < 1.2km
            if (food.getDistance() < 1.2) {
                originalNearbyFoods.add(food);
            }
            // Đánh giá cao: rating > 4.6
            if (food.getRating() > 4.6) {
                originalTopRatedFoods.add(food);
            }
        }

        Log.d("HOMEFRAGMENT", "📊 Tổng số món: " + allFoods.size());
        Log.d("HOMEFRAGMENT", "🔥 Bán chạy nhất gốc: " + originalBestSellers.size());
        Log.d("HOMEFRAGMENT", "📍 Gần tôi gốc: " + originalNearbyFoods.size());
        Log.d("HOMEFRAGMENT", "⭐ Đánh giá cao gốc: " + originalTopRatedFoods.size());

        // Click listener
        FoodAdapter.OnItemClickListener onItemClick = food -> {
            Intent intent = new Intent(getContext(), FoodDetailActivity.class);
            intent.putExtra("food", food);
            startActivityForResult(intent, REQUEST_FOOD_DETAIL);
        };

        FoodAdapter.OnAddToCartClickListener onAddToCart = food -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).addToCart(food);
                Toast.makeText(getContext(), "Đã thêm " + food.getName(), Toast.LENGTH_SHORT).show();
            }
        };

        bestSellerAdapter = new FoodAdapter(originalBestSellers, onItemClick, onAddToCart);
        nearbyAdapter = new FoodAdapter(originalNearbyFoods, onItemClick, onAddToCart);
        topRatedAdapter = new FoodAdapter(originalTopRatedFoods, onItemClick, onAddToCart);
        allFoodsAdapter = new FoodAdapter(allFoods, onItemClick, onAddToCart);

        rvBestSellers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvBestSellers.setAdapter(bestSellerAdapter);

        rvNearby.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvNearby.setAdapter(nearbyAdapter);

        rvTopRated.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTopRated.setAdapter(topRatedAdapter);

        rvAllFoods.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAllFoods.setAdapter(allFoodsAdapter);
    }

    // Hàm lọc TẤT CẢ các danh sách theo danh mục
    private void filterAllListsByCategory(String category) {
        List<Food> filteredAll = new ArrayList<>();
        List<Food> filteredBestSellers = new ArrayList<>();
        List<Food> filteredNearby = new ArrayList<>();
        List<Food> filteredTopRated = new ArrayList<>();

        if (category.equals("Tất cả")) {
            // Hiển thị toàn bộ dữ liệu gốc
            filteredAll.addAll(allFoods);
            filteredBestSellers.addAll(originalBestSellers);
            filteredNearby.addAll(originalNearbyFoods);
            filteredTopRated.addAll(originalTopRatedFoods);
        } else {
            // Lọc theo danh mục
            for (Food food : allFoods) {
                if (food.getCategory().equals(category)) {
                    filteredAll.add(food);
                }
            }
            for (Food food : originalBestSellers) {
                if (food.getCategory().equals(category)) {
                    filteredBestSellers.add(food);
                }
            }
            for (Food food : originalNearbyFoods) {
                if (food.getCategory().equals(category)) {
                    filteredNearby.add(food);
                }
            }
            for (Food food : originalTopRatedFoods) {
                if (food.getCategory().equals(category)) {
                    filteredTopRated.add(food);
                }
            }
        }

        // Cập nhật tất cả adapters
        allFoodsAdapter.updateList(filteredAll);
        bestSellerAdapter.updateList(filteredBestSellers);
        nearbyAdapter.updateList(filteredNearby);
        topRatedAdapter.updateList(filteredTopRated);

        // Hiển thị thông báo
        String icon = "";
        switch (category) {
            case "Fast Food": icon = "🍔"; break;
            case "Món Việt": icon = "🍜"; break;
            case "Đồ uống": icon = "☕"; break;
            case "Tráng miệng": icon = "🍰"; break;
            case "Hải sản": icon = "🦞"; break;
            default: icon = "🍕";
        }

        Toast.makeText(getContext(), icon + " " + category + ": " +
                filteredBestSellers.size() + " món bán chạy, " +
                filteredNearby.size() + " món gần, " +
                filteredTopRated.size() + " món đánh giá cao", Toast.LENGTH_LONG).show();

        Log.d("FILTER", "Category: " + category);
        Log.d("FILTER", "  - Best sellers: " + filteredBestSellers.size());
        Log.d("FILTER", "  - Nearby: " + filteredNearby.size());
        Log.d("FILTER", "  - Top rated: " + filteredTopRated.size());
        Log.d("FILTER", "  - All foods: " + filteredAll.size());
    }

    private List<Food> createAccurateFoods() {
        List<Food> foods = new ArrayList<>();
        int id = 1;

        // ========== FAST FOOD ==========
        foods.add(createFood(id++, "🍔 Gà rán giòn KFC", "KFC", "Fast Food", 45000, 4.7, 250, 0.8));
        foods.add(createFood(id++, "🍔 Burger Zinger", "KFC", "Fast Food", 55000, 4.6, 200, 0.8));
        foods.add(createFood(id++, "🍟 Khoai tây chiên", "KFC", "Fast Food", 25000, 4.5, 180, 0.8));
        foods.add(createFood(id++, "🍔 Lotteria Burger", "Lotteria", "Fast Food", 49000, 4.5, 150, 1.2));
        foods.add(createFood(id++, "🍚 Cơm gà rán", "Lotteria", "Fast Food", 59000, 4.4, 120, 1.2));
        foods.add(createFood(id++, "🍕 Pizza hải sản", "Pizza Hut", "Fast Food", 129000, 4.8, 300, 1.5));
        foods.add(createFood(id++, "🍕 Pizza bò viên", "Pizza Hut", "Fast Food", 119000, 4.7, 280, 1.5));
        foods.add(createFood(id++, "🍕 Pizza gà cay", "Pizza Hut", "Fast Food", 109000, 4.6, 250, 1.5));
        foods.add(createFood(id++, "🍕 Pizza phô mai", "Domino's", "Fast Food", 99000, 4.6, 200, 2.0));

        // ========== MÓN VIỆT ==========
        foods.add(createFood(id++, "🍜 Phở bò tái", "Phở Thìn", "Món Việt", 55000, 4.9, 500, 1.0));
        foods.add(createFood(id++, "🍜 Phở gà", "Phở Thìn", "Món Việt", 55000, 4.8, 450, 1.0));
        foods.add(createFood(id++, "🍜 Phở tái nạm", "Phở Thìn", "Món Việt", 60000, 4.9, 480, 1.0));
        foods.add(createFood(id++, "🍚 Cơm tấm sườn", "Cơm Tấm Ba Ghiền", "Món Việt", 45000, 4.7, 350, 1.3));
        foods.add(createFood(id++, "🍚 Cơm tấm sườn bì", "Cơm Tấm Ba Ghiền", "Món Việt", 50000, 4.6, 320, 1.3));
        foods.add(createFood(id++, "🍜 Bún chả Hà Nội", "Bún Chả Hương", "Món Việt", 55000, 4.7, 280, 1.8));

        // ========== ĐỒ UỐNG ==========
        foods.add(createFood(id++, "☕ Cà phê sữa đá", "Highlands Coffee", "Đồ uống", 39000, 4.7, 400, 0.5));
        foods.add(createFood(id++, "☕ Cà phê đen", "Highlands Coffee", "Đồ uống", 35000, 4.6, 380, 0.5));
        foods.add(createFood(id++, "🍵 Trà xanh", "Highlands Coffee", "Đồ uống", 39000, 4.5, 350, 0.5));
        foods.add(createFood(id++, "☕ Caramel Macchiato", "Starbucks", "Đồ uống", 79000, 4.8, 300, 2.2));
        foods.add(createFood(id++, "☕ Cà phê đen", "Starbucks", "Đồ uống", 59000, 4.7, 280, 2.2));
        foods.add(createFood(id++, "🧋 Trà sữa trân châu", "Gong Cha", "Đồ uống", 49000, 4.6, 320, 1.5));
        foods.add(createFood(id++, "🧋 Trà sữa matcha", "Gong Cha", "Đồ uống", 55000, 4.7, 300, 1.5));

        // ========== TRÁNG MIỆNG ==========
        foods.add(createFood(id++, "🍮 Bánh flan", "Cơm Tấm Ba Ghiền", "Tráng miệng", 15000, 4.5, 200, 1.3));
        foods.add(createFood(id++, "🍦 Kem ốc quế", "Lotteria", "Tráng miệng", 12000, 4.4, 180, 1.2));
        foods.add(createFood(id++, "🍰 Bánh ngọt", "Highlands Coffee", "Tráng miệng", 25000, 4.5, 150, 0.5));

        // ========== HẢI SẢN ==========
        foods.add(createFood(id++, "🦐 Tôm hấp bia", "Hải sản Hoàng Gia", "Hải sản", 150000, 4.6, 120, 3.0));
        foods.add(createFood(id++, "🐚 Sò điệp nướng", "Hải sản Hoàng Gia", "Hải sản", 120000, 4.7, 100, 3.0));
        foods.add(createFood(id++, "🦑 Mực chiên giòn", "Hải sản Hoàng Gia", "Hải sản", 110000, 4.5, 90, 3.0));

        return foods;
    }

    private Food createFood(int id, String name, String restaurant, String category,
                            double price, double rating, int soldCount, double distance) {
        Food food = new Food(id, name, "🏠 " + restaurant, price, 0, category);
        food.setRestaurantName(restaurant);
        food.setRestaurantId(id);
        food.setRating(rating);
        food.setSoldCount(soldCount);
        food.setBestSeller(soldCount > 150);
        food.setDistance(distance);
        return food;
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                if (query.isEmpty()) {
                    ivClearSearch.setVisibility(View.GONE);
                    // Reset về dữ liệu gốc
                    allFoodsAdapter.updateList(allFoods);
                    bestSellerAdapter.updateList(originalBestSellers);
                    nearbyAdapter.updateList(originalNearbyFoods);
                    topRatedAdapter.updateList(originalTopRatedFoods);
                } else {
                    ivClearSearch.setVisibility(View.VISIBLE);
                    // Tìm kiếm trong tất cả danh sách
                    List<Food> filteredAll = new ArrayList<>();
                    List<Food> filteredBest = new ArrayList<>();
                    List<Food> filteredNearby = new ArrayList<>();
                    List<Food> filteredTop = new ArrayList<>();

                    for (Food food : allFoods) {
                        if (food.getName().toLowerCase().contains(query) ||
                                food.getRestaurantName().toLowerCase().contains(query)) {
                            filteredAll.add(food);
                        }
                    }
                    for (Food food : originalBestSellers) {
                        if (food.getName().toLowerCase().contains(query) ||
                                food.getRestaurantName().toLowerCase().contains(query)) {
                            filteredBest.add(food);
                        }
                    }
                    for (Food food : originalNearbyFoods) {
                        if (food.getName().toLowerCase().contains(query) ||
                                food.getRestaurantName().toLowerCase().contains(query)) {
                            filteredNearby.add(food);
                        }
                    }
                    for (Food food : originalTopRatedFoods) {
                        if (food.getName().toLowerCase().contains(query) ||
                                food.getRestaurantName().toLowerCase().contains(query)) {
                            filteredTop.add(food);
                        }
                    }

                    allFoodsAdapter.updateList(filteredAll);
                    bestSellerAdapter.updateList(filteredBest);
                    nearbyAdapter.updateList(filteredNearby);
                    topRatedAdapter.updateList(filteredTop);

                    Toast.makeText(getContext(), "🔍 Tìm thấy " + filteredAll.size() + " món", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            allFoodsAdapter.updateList(allFoods);
            bestSellerAdapter.updateList(originalBestSellers);
            nearbyAdapter.updateList(originalNearbyFoods);
            topRatedAdapter.updateList(originalTopRatedFoods);
        });
    }

    private void setupAddressClick() {
        layoutAddress.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Chọn địa chỉ giao hàng");
            final EditText input = new EditText(getContext());
            input.setHint("Nhập địa chỉ mới");
            input.setText(tvAddress.getText().toString());
            builder.setView(input);
            builder.setPositiveButton("Lưu", (dialog, which) -> {
                String newAddress = input.getText().toString();
                if (!newAddress.isEmpty() && sharedPreferences != null) {
                    sharedPreferences.edit().putString(KEY_ADDRESS, newAddress).apply();
                    tvAddress.setText(newAddress);
                    Toast.makeText(getContext(), "Đã cập nhật địa chỉ", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Hủy", null);
            builder.show();
        });
    }

    private void setupAvatarClick() {
        ivAvatar.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToProfile();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOOD_DETAIL && resultCode == getActivity().RESULT_OK && data != null) {
            Food addedFood = (Food) data.getSerializableExtra("added_food");
            if (addedFood != null && getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).addToCart(addedFood);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }
}