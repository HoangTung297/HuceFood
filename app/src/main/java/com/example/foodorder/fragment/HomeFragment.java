package com.example.foodorder.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.example.foodorder.adapter.AddressSuggestionAdapter;
import com.example.foodorder.adapter.BannerAdapter;
import com.example.foodorder.adapter.CategoryAdapter;
import com.example.foodorder.adapter.FoodAdapter;
import com.example.foodorder.adapter.VoucherAdapter;
import com.example.foodorder.model.AddressSuggestion;
import com.example.foodorder.model.Banner;
import com.example.foodorder.model.Category;
import com.example.foodorder.model.Food;
import com.example.foodorder.model.Voucher;
import com.example.foodorder.utils.CacheManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    // Views
    private ViewPager2 bannerViewPager;
    private RecyclerView rvFlashSale, rvHotDeals, rvVouchers, rvCategories, rvAllFoods;
    private TextView tvAddress, tvStatus;
    private ImageView ivAvatar, ivClearSearch;
    private EditText etSearch;
    private View layoutAddress;

    // Data
    private List<Food> allFoodsList;
    private List<Voucher> voucherList;
    private List<AddressSuggestion> addressSuggestions;

    private FoodAdapter flashSaleAdapter, hotDealAdapter, allFoodsAdapter;
    private VoucherAdapter voucherAdapter;
    private CategoryAdapter categoryAdapter;
    private BannerAdapter bannerAdapter;
    private AddressSuggestionAdapter addressSuggestionAdapter;

    // Firebase
    private FirebaseFirestore firestore;
    private CacheManager cacheManager;

    // Banner auto slide
    private Handler bannerHandler = new Handler();
    private Runnable bannerRunnable;

    // SharedPreferences
    private SharedPreferences sharedPreferences;
    private static final String KEY_ADDRESS = "delivery_address";
    private static final int REQUEST_FOOD_DETAIL = 200;

    private String currentAddress = "";
    private String currentUserId = "";
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        allFoodsList = new ArrayList<>();
        voucherList = new ArrayList<>();
        addressSuggestions = new ArrayList<>();

        firestore = FirebaseFirestore.getInstance();

        if (getContext() != null) {
            cacheManager = new CacheManager(getContext());
        }

        if (getActivity() != null) {
            sharedPreferences = getActivity().getSharedPreferences("UserPrefs", 0);
            currentAddress = sharedPreferences.getString(KEY_ADDRESS, "Chọn địa chỉ");
            currentUserId = sharedPreferences.getString("user_id", "");
        }

        initViews(view);
        setupBanner();
        setupCategories();
        setupRecyclerViews();
        loadVouchers();
        loadFoods();
        setupSearch();
        setupAddressClick();
        setupAvatarClick();

        return view;
    }

    private void initViews(View view) {
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
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
        ivClearSearch.setVisibility(View.GONE);
    }

    private void setupBanner() {
        List<Banner> bannerList = new ArrayList<>();
        bannerList.add(new Banner(1, "sale_50", "🎉 GIẢM 50%"));
        bannerList.add(new Banner(2, "freeship", "🚚 FREESHIP 0Đ"));
        bannerList.add(new Banner(3, "mua_1_tang_1", "🎁 MUA 1 TẶNG 1"));

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
        LinearLayoutManager flashLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvFlashSale.setLayoutManager(flashLayout);
        flashSaleAdapter = new FoodAdapter(new ArrayList<>(), getOnItemClick(), getOnAddToCart());
        rvFlashSale.setAdapter(flashSaleAdapter);

        LinearLayoutManager hotLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvHotDeals.setLayoutManager(hotLayout);
        hotDealAdapter = new FoodAdapter(new ArrayList<>(), getOnItemClick(), getOnAddToCart());
        rvHotDeals.setAdapter(hotDealAdapter);

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
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã thêm " + food.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void loadVouchers() {
        if (cacheManager != null) {
            List<Voucher> cachedVouchers = cacheManager.getCachedVouchers();
            if (cachedVouchers != null && !cachedVouchers.isEmpty()) {
                voucherList.clear();
                voucherList.addAll(cachedVouchers);
                voucherAdapter = new VoucherAdapter(voucherList, null);
                rvVouchers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                rvVouchers.setAdapter(voucherAdapter);
                return;
            }
        }

        firestore.collection("vouchers")
                .whereEqualTo("isActive", true)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    voucherList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Voucher voucher = new Voucher();
                        voucher.setId(doc.getId());
                        voucher.setCode(doc.getString("code"));
                        voucher.setTitle(doc.getString("title"));
                        voucher.setDescription(doc.getString("description"));
                        voucher.setDiscountType(doc.getString("discountType"));

                        Double discountValue = doc.getDouble("discountValue");
                        voucher.setDiscountValue(discountValue != null ? discountValue : 0);

                        Double minOrder = doc.getDouble("minOrder");
                        voucher.setMinOrder(minOrder != null ? minOrder : 0);

                        voucherList.add(voucher);
                    }

                    if (cacheManager != null) {
                        cacheManager.cacheVouchers(voucherList);
                    }
                    voucherAdapter = new VoucherAdapter(voucherList, null);
                    rvVouchers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    rvVouchers.setAdapter(voucherAdapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Lỗi tải voucher: " + e.getMessage());
                });
    }

    private void loadFoods() {
        if (isLoading) return;

        if (cacheManager != null) {
            List<Food> cachedFoods = cacheManager.getCachedFoods();
            if (cachedFoods != null && !cachedFoods.isEmpty()) {
                allFoodsList.clear();
                allFoodsList.addAll(cachedFoods);
                processAndDisplayData();
                tvStatus.setVisibility(View.GONE);
                return;
            }
        }

        isLoading = true;
        tvStatus.setText("Đang tải món ăn...");
        tvStatus.setVisibility(View.VISIBLE);

        firestore.collection("foods").limit(100).get()
                .addOnSuccessListener(query -> {
                    allFoodsList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String name = doc.getString("name");
                        String desc = doc.getString("description");
                        Double price = doc.getDouble("price");
                        String category = doc.getString("category");
                        String imageUrl = doc.getString("imageUrl");
                        Long soldCount = doc.getLong("soldCount");
                        Double rating = doc.getDouble("rating");
                        String restaurantName = doc.getString("restaurant");

                        Food food = new Food(doc.getId(),
                                name != null ? name : "Món ăn",
                                desc != null ? desc : "",
                                price != null ? price : 0,
                                category != null ? category : "Khác",
                                "");
                        food.setImageUrl(imageUrl != null ? imageUrl : "");
                        food.setSoldCount(soldCount != null ? soldCount.intValue() : 0);
                        food.setRating(rating != null ? rating : 0);
                        food.setRestaurantName(restaurantName != null ? restaurantName : "Nhà hàng");
                        allFoodsList.add(food);
                    }

                    if (cacheManager != null && !allFoodsList.isEmpty()) {
                        cacheManager.cacheFoods(allFoodsList);
                    }

                    processAndDisplayData();
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    tvStatus.setText("Lỗi: " + e.getMessage());
                    isLoading = false;
                });
    }

    private void processAndDisplayData() {
        if (getContext() == null) return;
        if (allFoodsList.isEmpty()) return;

        List<Food> flashList = new ArrayList<>(allFoodsList);
        Collections.sort(flashList, (a, b) -> Integer.compare(b.getSoldCount(), a.getSoldCount()));
        flashSaleAdapter.updateList(flashList.size() > 5 ? flashList.subList(0, 5) : flashList);

        List<Food> hotList = new ArrayList<>(allFoodsList);
        Collections.sort(hotList, (a, b) -> Double.compare(b.getRating(), a.getRating()));
        hotDealAdapter.updateList(hotList.size() > 10 ? hotList.subList(0, 10) : hotList);

        allFoodsAdapter.updateList(allFoodsList);
        tvStatus.setVisibility(View.GONE);
    }

    private void filterAllFoodsByCategory(String category) {
        List<Food> filtered = new ArrayList<>();
        for (Food food : allFoodsList) {
            if (category.equals("Tất cả") || food.getCategory().equals(category)) {
                filtered.add(food);
            }
        }
        allFoodsAdapter.updateList(filtered);
        Toast.makeText(getContext(), category + ": " + filtered.size() + " món", Toast.LENGTH_SHORT).show();
    }

    private void setupSearch() {
        // Chỉ click vào thanh tìm kiếm để mở SearchActivity
        etSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), com.example.foodorder.SearchActivity.class);
            startActivity(intent);
        });

        etSearch.setFocusable(false);
        etSearch.setFocusableInTouchMode(false);
        etSearch.setCursorVisible(false);
    }

    private void setupAddressClick() {
        layoutAddress.setOnClickListener(v -> showAddressDialog());
    }

    private void showAddressDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_address, null);

        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        RecyclerView rvSuggestions = dialogView.findViewById(R.id.rvSuggestions);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        addressSuggestions.clear();
        addressSuggestionAdapter = new AddressSuggestionAdapter(addressSuggestions, address -> {
            etAddress.setText(address.getFullAddress());
            rvSuggestions.setVisibility(View.GONE);
        });
        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuggestions.setAdapter(addressSuggestionAdapter);

        etAddress.setText(currentAddress.equals("Chọn địa chỉ") ? "" : currentAddress);

        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() > 2) {
                    suggestAddresses(query);
                } else {
                    addressSuggestions.clear();
                    addressSuggestionAdapter.updateList(addressSuggestions);
                    rvSuggestions.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        AlertDialog dialog = builder.setView(dialogView).setTitle("Nhập địa chỉ giao hàng").create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            String newAddress = etAddress.getText().toString().trim();
            if (isValidAddress(newAddress)) {
                saveAddress(newAddress);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập địa chỉ đầy đủ (Số nhà, Đường, Phường/Xã, Quận/Huyện, Thành phố)", Toast.LENGTH_LONG).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void suggestAddresses(String query) {
        addressSuggestions.clear();

        if (query.toLowerCase().contains("giải phóng")) {
            addressSuggestions.add(new AddressSuggestion(
                    "Số 50, Đường Giải Phóng, Phương Liệt, Quận Thanh Xuân, Hà Nội",
                    "50", "Đường Giải Phóng", "Phương Liệt", "Quận Thanh Xuân", "Hà Nội"
            ));
            addressSuggestions.add(new AddressSuggestion(
                    "Số 123, Đường Giải Phóng, Đồng Tâm, Quận Hai Bà Trưng, Hà Nội",
                    "123", "Đường Giải Phóng", "Đồng Tâm", "Quận Hai Bà Trưng", "Hà Nội"
            ));
        }

        if (query.toLowerCase().contains("bạch mai")) {
            addressSuggestions.add(new AddressSuggestion(
                    "Số 156, Đường Bạch Mai, Phường Bạch Mai, Quận Hai Bà Trưng, Hà Nội",
                    "156", "Đường Bạch Mai", "Phường Bạch Mai", "Quận Hai Bà Trưng", "Hà Nội"
            ));
        }

        if (query.toLowerCase().contains("cầu giấy")) {
            addressSuggestions.add(new AddressSuggestion(
                    "Số 78, Đường Trần Duy Hưng, Trung Hòa, Quận Cầu Giấy, Hà Nội",
                    "78", "Đường Trần Duy Hưng", "Trung Hòa", "Quận Cầu Giấy", "Hà Nội"
            ));
        }

        if (query.toLowerCase().contains("hàng bông")) {
            addressSuggestions.add(new AddressSuggestion(
                    "Số 82, Phố Hàng Bông, Hàng Bông, Quận Hoàn Kiếm, Hà Nội",
                    "82", "Phố Hàng Bông", "Hàng Bông", "Quận Hoàn Kiếm", "Hà Nội"
            ));
        }

        addressSuggestionAdapter.updateList(addressSuggestions);
        View rvSuggestionsView = getView().findViewById(R.id.rvSuggestions);
        if (rvSuggestionsView != null) {
            rvSuggestionsView.setVisibility(addressSuggestions.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private boolean isValidAddress(String address) {
        if (address == null || address.isEmpty()) return false;

        boolean hasNumber = address.matches(".*\\d+.*");
        boolean hasStreet = address.contains("Đường") || address.contains("Phố") || address.contains("Ngõ") || address.contains("Hẻm");
        boolean hasDistrict = address.contains("Quận") || address.contains("Huyện");
        boolean hasCity = address.contains("Hà Nội") || address.contains("TP.HCM") || address.contains("Đà Nẵng") ||
                address.contains("Hải Phòng") || address.contains("Cần Thơ");

        return hasNumber && hasStreet && hasDistrict && hasCity;
    }

    private void saveAddress(String newAddress) {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);
        prefs.edit().putString(KEY_ADDRESS, newAddress).apply();
        prefs.edit().putString("user_address", newAddress).apply();
        tvAddress.setText(newAddress);
        currentAddress = newAddress;

        Toast.makeText(getContext(), "Đã cập nhật địa chỉ: " + newAddress, Toast.LENGTH_SHORT).show();
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