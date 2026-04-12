package com.example.foodorder.fragment;

import android.content.Intent;
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
import com.example.foodorder.OrderHistoryActivity;
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

        tvUsername.setText("hoangtung2907");
        tvEmail.setText("hoangtung@example.com");

        // Nút giỏ hàng
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof HomeActivity) {
                    HomeActivity homeActivity = (HomeActivity) getActivity();
                    List<Food> cartList = homeActivity.getCartList();

                    Intent intent = new Intent(getActivity(), CartActivity.class);
                    intent.putExtra("cart_list", new ArrayList<>(cartList));
                    startActivityForResult(intent, 100);
                } else {
                    Toast.makeText(getActivity(), "Lỗi: Không thể lấy giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Nút lịch sử đơn hàng
        btnOrderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
                startActivity(intent);
            }
        });

        // Nút đăng xuất
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == -1) {
            if (getActivity() instanceof HomeActivity) {
                HomeActivity homeActivity = (HomeActivity) getActivity();
                homeActivity.clearCart();
                Toast.makeText(getActivity(), "Đã thanh toán thành công!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}