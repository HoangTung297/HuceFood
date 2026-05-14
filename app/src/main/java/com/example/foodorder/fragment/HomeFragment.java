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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    // ============ VIEWS ============
    private ViewPager2 bannerViewPager;
    private TabLayout tabLayout;
    private RecyclerView rvFlashSale, rvHotDeals, rvVouchers, rvCategories, rvAllFoods;
    private TextView tvAddress, tvStatus;
    private ImageView ivAvatar, ivClearSearch;
    private EditText etSearch;
    private View layoutAddress;

    // ============ DATA ============
    private List<Food> allFoodsList;
    private List<Voucher> voucherList;

    // ============ ADAPTERS ============
    private FoodAdapter flashSaleAdapter, hotDealAdapter, allFoodsAdapter;
    private VoucherAdapter voucherAdapter;
    private CategoryAdapter categoryAdapter;
    private BannerAdapter bannerAdapter;

    // ============ FIREBASE & BANNER ============
    private FirebaseFirestore firestore;
    private Handler bannerHandler = new Handler();
    private Runnable bannerRunnable;

    // ============ SHARED PREFERENCES ============
    private SharedPreferences sharedPreferences;
    private static final String KEY_ADDRESS = "delivery_address";
    private static final int REQUEST_FOOD_DETAIL = 200;

    private String currentAddress = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        allFoodsList = new ArrayList<>();
        voucherList = new ArrayList<>();

        firestore = FirebaseFirestore.getInstance();

        if (getActivity() != null) {
            sharedPreferences = getActivity().getSharedPreferences("UserPrefs", 0);
            currentAddress = sharedPreferences.getString(KEY_ADDRESS, "Chọn địa chỉ");
        }

        initViews(view);
        setupBanner();
        setupVouchers();
        setupCategories();
        setupRecyclerViews();
        loadFoodsFromFirebase();
        setupSearch();
        setupAddressClick();
        setupAvatarClick();

        return view;
    }

    private void initViews(View view) {
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        rvFlashSale = view.findViewById(R.id.rvFlashSale);
        rvHotDeals = view.findViewById(R.id.rvHotDeals);
        rvVouchers = view.findViewById(R.id.rvVouchers);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvAllFoods = view.findViewById(R.id.rvAllFoods);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvStatus = view.findViewById(R.id.tvStatus);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        etSearch = view.findViewById(R.id.etSearch);
        ivClearSearch = view.findViewById(R.id.ivClearSearch);
        layoutAddress = view.findViewById(R.id.layoutAddress);

        tvAddress.setText(currentAddress);
        tvStatus.setVisibility(View.VISIBLE);
    }

    // ✅ TỪ AN: Setup banner với TabLayout
    private void setupBanner() {
        List<Banner> bannerList = new ArrayList<>();
        bannerList.add(new Banner(1, "sale_50", "🎉 GIẢM 50% - Món ngon giá sốc"));
        bannerList.add(new Banner(2, "freeship", "🚚 FREESHIP 0Đ - Đơn từ 50K"));
        bannerList.add(new Banner(3, "mua_1_tang_1", "🎁 MUA 1 TẶNG 1"));
        bannerList.add(new Banner(4, "new_mem", "👤 NGƯỜI MỚI - Giảm thêm 50K"));

        bannerAdapter = new BannerAdapter(bannerList);
        bannerViewPager.setAdapter(bannerAdapter);


        bannerRunnable = () -> {
            int current = bannerViewPager.getCurrentItem();
            if (current < bannerList.size() - 1) {
                bannerViewPager.setCurrentItem(current + 1);
            } else {
                bannerViewPager.setCurrentItem(0);
            }
            bannerHandler.postDelayed(bannerRunnable, 3000);
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
        voucherList.add(new Voucher(1, "Gà Rán KFC", "-50%", "1 BÁNH TRƯNG 9K"));
        voucherList.add(new Voucher(2, "Gà Rán Popeyes", "-50%", "1 MIẾNG GÀ"));
        voucherList.add(new Voucher(3, "Combo 2 Gà", "-35%", "64K"));

        voucherAdapter = new VoucherAdapter(voucherList);
        rvVouchers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
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

        categoryAdapter = new CategoryAdapter(categoryList, (category, position) -> {
            filterAllFoodsByCategory(category.getName());
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupRecyclerViews() {
        // Flash Sale - Dọc
        LinearLayoutManager flashLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvFlashSale.setLayoutManager(flashLayout);
        flashSaleAdapter = new FoodAdapter(new ArrayList<>(), getOnItemClick(), getOnAddToCart());
        rvFlashSale.setAdapter(flashSaleAdapter);

        // Hot Deals - Dọc
        LinearLayoutManager hotLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvHotDeals.setLayoutManager(hotLayout);
        hotDealAdapter = new FoodAdapter(new ArrayList<>(), getOnItemClick(), getOnAddToCart());
        rvHotDeals.setAdapter(hotDealAdapter);

        // All Foods - Dọc
        LinearLayoutManager allLayout = new LinearLayoutManager(getContext());
        rvAllFoods.setLayoutManager(allLayout);
        allFoodsAdapter = new FoodAdapter(new ArrayList<>(), getOnItemClick(), getOnAddToCart());
        rvAllFoods.setAdapter(allFoodsAdapter);
    }

    private FoodAdapter.OnItemClickListener getOnItemClick() {
        return food -> {
            Intent intent = new Intent(getContext(), FoodDetailActivity.class);
            intent.putExtra("food", food);
            startActivityForResult(intent, REQUEST_FOOD_DETAIL);
        };
    }

    private FoodAdapter.OnAddToCartClickListener getOnAddToCart() {
        return food -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).addToCart(food);
                Toast.makeText(getContext(), "Đã thêm " + food.getName(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    // ✅ TỪ BẠN: Load dữ liệu từ Firebase
    private void loadFoodsFromFirebase() {
        tvStatus.setText("Đang tải món ăn...");

        firestore.collection("foods").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allFoodsList.clear();

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                if (name == null) name = "Món ăn";

                String description = doc.getString("description");
                if (description == null) description = "";

                Double price = doc.getDouble("price");
                if (price == null) price = 0.0;

                String category = doc.getString("category");
                if (category == null) category = "Khác";

                String imageUrl = doc.getString("imageUrl");
                if (imageUrl == null) imageUrl = "";

                Long soldCount = doc.getLong("soldCount");
                if (soldCount == null) soldCount = 0L;

                Double rating = doc.getDouble("rating");
                if (rating == null) rating = 0.0;

                String restaurantName = doc.getString("restaurant");
                if (restaurantName == null) restaurantName = "Nhà hàng";

                Food food = new Food(doc.getId(), name, description, price, category, "");
                food.setImageUrl(imageUrl);
                food.setSoldCount(soldCount.intValue());
                food.setRating(rating);
                food.setRestaurantName(restaurantName);
                allFoodsList.add(food);
            }

            processAndDisplayData();

        }).addOnFailureListener(e -> {
            tvStatus.setText("Lỗi: " + e.getMessage());
            Log.e("HomeFragment", "Firebase error: " + e.getMessage());
        });
    }

    // ✅ KẾT HỢP: Flash Sale top 5 bán chạy + Hot Deal top 10 rating
    private void processAndDisplayData() {
        if (allFoodsList.isEmpty()) {
            tvStatus.setText("Không có dữ liệu món ăn!");
            return;
        }

        Log.d("HomeFragment", "========== PROCESSING DATA ==========");
        Log.d("HomeFragment", "Total foods: " + allFoodsList.size());

        // 1. FLASH SALE: Top 5 bán chạy nhất (soldCount cao nhất)
        List<Food> flashList = new ArrayList<>(allFoodsList);
        Collections.sort(flashList, (a, b) -> Integer.compare(b.getSoldCount(), a.getSoldCount()));
        List<Food> flashLimited = new ArrayList<>();
        for (int i = 0; i < Math.min(5, flashList.size()); i++) {
            flashLimited.add(flashList.get(i));
            Log.d("HomeFragment", "Flash Sale: " + flashList.get(i).getName() + " (sold: " + flashList.get(i).getSoldCount() + ")");
        }
        flashSaleAdapter.updateList(flashLimited);

        // 2. HOT DEAL: Top 10 rating cao nhất
        List<Food> hotList = new ArrayList<>(allFoodsList);
        Collections.sort(hotList, (a, b) -> Double.compare(b.getRating(), a.getRating()));
        List<Food> hotLimited = new ArrayList<>();
        for (int i = 0; i < Math.min(10, hotList.size()); i++) {
            hotLimited.add(hotList.get(i));
            Log.d("HomeFragment", "Hot Deal: " + hotList.get(i).getName() + " (rating: " + hotList.get(i).getRating() + ")");
        }
        hotDealAdapter.updateList(hotLimited);

        // 3. TẤT CẢ MÓN ĂN
        allFoodsAdapter.updateList(allFoodsList);

        tvStatus.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Loaded " + allFoodsList.size() + " items", Toast.LENGTH_SHORT).show();
    }

    // ✅ TỪ AN: Lọc danh sách theo category
    private void filterAllFoodsByCategory(String category) {
        List<Food> filtered = new ArrayList<>();
        for (Food food : allFoodsList) {
            if (category.equals("Tất cả") || food.getCategory().equals(category)) {
                filtered.add(food);
            }
        }
        allFoodsAdapter.updateList(filtered);
        String icon = getCategoryIcon(category);
        Toast.makeText(getContext(), icon + " " + category + ": " + filtered.size() + " món", Toast.LENGTH_SHORT).show();
        Log.d("HomeFragment", "Filter by " + category + ": " + filtered.size() + " items");
    }

    private String getCategoryIcon(String category) {
        switch (category) {
            case "Fast Food": return "🍔";
            case "Món Việt": return "🍜";
            case "Đồ uống": return "☕";
            case "Tráng miệng": return "🍰";
            case "Hải sản": return "🦞";
            default: return "🍕";
        }
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
                    allFoodsAdapter.updateList(allFoodsList);
                } else {
                    ivClearSearch.setVisibility(View.VISIBLE);
                    List<Food> filtered = new ArrayList<>();
                    for (Food food : allFoodsList) {
                        if (food.getName().toLowerCase().contains(query)) {
                            filtered.add(food);
                        }
                    }
                    allFoodsAdapter.updateList(filtered);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            allFoodsAdapter.updateList(allFoodsList);
        });
    }

    private void setupAddressClick() {
        layoutAddress.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Nhập địa chỉ giao hàng");
            final EditText input = new EditText(getContext());
            input.setHint("Ví dụ: Số 123, Đường ABC, Phường XYZ");
            input.setText(currentAddress.equals("Chọn địa chỉ") ? "" : currentAddress);
            builder.setView(input);
            builder.setPositiveButton("Lưu", (dialog, which) -> {
                String newAddress = input.getText().toString().trim();
                if (!newAddress.isEmpty()) {
                    sharedPreferences.edit().putString(KEY_ADDRESS, newAddress).apply();
                    tvAddress.setText(newAddress);
                    currentAddress = newAddress;
                    Toast.makeText(getContext(), "Đã cập nhật địa chỉ: " + newAddress, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
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
    public void onDestroyView() {
        super.onDestroyView();
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }
}