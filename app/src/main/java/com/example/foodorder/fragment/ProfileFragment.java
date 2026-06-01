package com.example.foodorder.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodorder.CartActivity;
import com.example.foodorder.HomeActivity;
import com.example.foodorder.LoginActivity;
import com.example.foodorder.OrderHistoryActivity;
import com.example.foodorder.R;
import com.example.foodorder.utils.CacheManager;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail, tvPhone, tvAddress;
    private EditText etUsername, etEmail, etPhone, etAddress;
    private Button btnCart, btnOrderHistory, btnEditProfile, btnLogout;
    private Button btnSaveProfile, btnCancelEdit;
    private View layoutSaveCancel;

    private String userId = "user123";
    private boolean isEditing = false;

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

        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etAddress = view.findViewById(R.id.etAddress);

        btnCart = view.findViewById(R.id.btnCart);
        btnOrderHistory = view.findViewById(R.id.btnOrderHistory);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnCancelEdit = view.findViewById(R.id.btnCancelEdit);

        layoutSaveCancel = view.findViewById(R.id.layoutSaveCancel);

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

        etUsername.setText(userName);
        etEmail.setText(userEmail);
        etPhone.setText(userPhone);
        etAddress.setText(userAddress);
    }

    private void setupClickListeners() {
        btnCart.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đã bấm giỏ hàng", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), CartActivity.class);
            startActivity(intent);
        });

        // Nút Lịch sử đơn hàng – mở trực tiếp OrderHistoryActivity
        btnOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> toggleEditMode(true));

        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());

        btnCancelEdit.setOnClickListener(v -> {
            loadUserInfo();
            toggleEditMode(false);
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

    private void toggleEditMode(boolean enable) {
        isEditing = enable;
        if (enable) {
            tvUsername.setVisibility(View.GONE);
            tvEmail.setVisibility(View.GONE);
            tvPhone.setVisibility(View.GONE);
            tvAddress.setVisibility(View.GONE);

            etUsername.setVisibility(View.VISIBLE);
            etEmail.setVisibility(View.VISIBLE);
            etPhone.setVisibility(View.VISIBLE);
            etAddress.setVisibility(View.VISIBLE);

            btnEditProfile.setVisibility(View.GONE);
            layoutSaveCancel.setVisibility(View.VISIBLE);
        } else {
            tvUsername.setVisibility(View.VISIBLE);
            tvEmail.setVisibility(View.VISIBLE);
            tvPhone.setVisibility(View.VISIBLE);
            tvAddress.setVisibility(View.VISIBLE);

            etUsername.setVisibility(View.GONE);
            etEmail.setVisibility(View.GONE);
            etPhone.setVisibility(View.GONE);
            etAddress.setVisibility(View.GONE);

            btnEditProfile.setVisibility(View.VISIBLE);
            layoutSaveCancel.setVisibility(View.GONE);
        }
    }

    private void saveProfileChanges() {
        String name = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên");
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            return;
        }

        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_name", name);
        editor.putString("user_email", email);
        editor.putString("user_phone", phone);
        editor.putString("user_address", address);
        editor.apply();

        tvUsername.setText(name);
        tvEmail.setText(email);
        tvPhone.setText(phone);
        tvAddress.setText(address);

        Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
        toggleEditMode(false);
    }

    private void performLogout() {
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }

        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        try {
            CacheManager cacheManager = new CacheManager(getContext());
            cacheManager.clearCache();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finishAffinity();
        }
    }
}