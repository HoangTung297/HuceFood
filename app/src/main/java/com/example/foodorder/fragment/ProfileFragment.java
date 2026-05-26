package com.example.foodorder.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.foodorder.CartActivity;
import com.example.foodorder.HomeActivity;
import com.example.foodorder.LoginActivity;
import com.example.foodorder.R;
import com.example.foodorder.repository.FirebaseRepository;
import com.example.foodorder.utils.CacheManager;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail, tvPhone, tvAddress;
    private Button btnCart, btnOrderHistory, btnLogout, btnClearCart;
    private String userId = "user123";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        loadUserInfo();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);
        btnCart = view.findViewById(R.id.btnCart);
        btnOrderHistory = view.findViewById(R.id.btnOrderHistory);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnClearCart = view.findViewById(R.id.btnClearCart);

        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            userId = prefs.getString("user_id", "user123");
        }
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "User");
        String userEmail = prefs.getString("user_email", "user@example.com");
        String userPhone = prefs.getString("user_phone", "Chưa cập nhật");
        String userAddress = prefs.getString("user_address", "Chưa cập nhật");

        tvUsername.setText(userName);
        tvEmail.setText(userEmail);
        tvPhone.setText(userPhone);
        tvAddress.setText(userAddress);
    }

    private void setupClickListeners() {
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CartActivity.class);
            startActivity(intent);
        });

        btnOrderHistory.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToOrderHistory();
            }
        });

        btnClearCart.setOnClickListener(v -> {
            FirebaseRepository.getInstance().clearCart(userId, new FirebaseRepository.OnDataLoaded<Void>() {
                @Override
                public void onSuccess(Void data) {
                    Toast.makeText(getContext(), "Đã xóa giỏ hàng", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void performLogout() {
        // Xóa Firebase Auth (nếu có)
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }

        // Xóa SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Xóa cache
        try {
            CacheManager cacheManager = new CacheManager(getContext());
            cacheManager.clearCache();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Chuyển về màn hình Login và xóa toàn bộ stack
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finishAffinity();
        }
    }
}