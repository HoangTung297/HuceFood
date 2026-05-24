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
import com.example.foodorder.R;
import com.example.foodorder.repository.FirebaseRepository;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail, tvPhone;
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
        btnCart = view.findViewById(R.id.btnCart);
        btnOrderHistory = view.findViewById(R.id.btnOrderHistory);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnClearCart = view.findViewById(R.id.btnClearCart);

        // Lấy userId từ SharedPreferences
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            userId = prefs.getString("user_id", "user123");
        }
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "User " + userId);
        String userEmail = prefs.getString("user_email", "user@example.com");
        String userPhone = prefs.getString("user_phone", "Chưa cập nhật");

        tvUsername.setText(userName);
        tvEmail.setText(userEmail);
        tvPhone.setText(userPhone);
    }

    private void setupClickListeners() {
        // Nút giỏ hàng
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CartActivity.class);
            startActivity(intent);
        });

        // Nút lịch sử đơn hàng
        btnOrderHistory.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToOrderHistory();
            } else {
                Toast.makeText(getContext(), "Vui lòng quay lại màn hình chính", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút xóa giỏ hàng
        btnClearCart.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).clearCart();
            } else {
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
            }
        });

        // Nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
}