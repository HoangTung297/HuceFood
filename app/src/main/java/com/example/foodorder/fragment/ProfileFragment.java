package com.example.foodorder.fragment;

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
// import com.example.foodorder.OrderHistoryActivity;  // COMMENT DÒNG NÀY LẠI
import com.example.foodorder.R;
import com.example.foodorder.model.Food;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail;
    private Button btnCart, btnOrderHistory, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnCart = view.findViewById(R.id.btnCart);
        btnOrderHistory = view.findViewById(R.id.btnOrderHistory);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Lấy thông tin user từ SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);
        String userName = prefs.getString("user_name", "User");
        String userEmail = prefs.getString("user_email", "user@example.com");

        tvUsername.setText(userName);
        tvEmail.setText(userEmail);

        btnCart.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                HomeActivity homeActivity = (HomeActivity) getActivity();
                Intent intent = new Intent(getActivity(), CartActivity.class);
                intent.putExtra("cart_list", new ArrayList<>(homeActivity.getCartList()));
                startActivityForResult(intent, 100);
            }
        });

        // SỬA: Comment hoặc thay đổi logic của nút OrderHistory
        btnOrderHistory.setOnClickListener(v -> {
            // Tạm thời hiển thị thông báo thay vì mở OrderHistoryActivity
            Toast.makeText(getContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();

            // Sau này khi có OrderHistoryActivity, bỏ comment dòng dưới
            // startActivity(new Intent(getActivity(), OrderHistoryActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            // Xóa SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        return view;
    }
}