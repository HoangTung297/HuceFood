package com.example.foodorder.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.foodorder.LoginActivity;
import com.example.foodorder.R;
import com.example.foodorder.WalletActivity;
import com.example.foodorder.utils.LoginSessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvProfileEmail;
    private EditText etProfileName, etProfilePhone, etProfileAddress;
    private Button btnSaveProfile, btnLogout;
    private LinearLayout layoutWallet, layoutOrders, layoutOrderHistory;

    private SharedPreferences sharedPreferences;
    private LoginSessionManager sessionManager;
    private FirebaseFirestore db;
    private String userId;
    private String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        initData();
        loadUserData();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        etProfileName = view.findViewById(R.id.etProfileName);
        etProfilePhone = view.findViewById(R.id.etProfilePhone);
        etProfileAddress = view.findViewById(R.id.etProfileAddress);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        layoutWallet = view.findViewById(R.id.layoutWallet);
        layoutOrders = view.findViewById(R.id.layoutOrders);
        layoutOrderHistory = view.findViewById(R.id.layoutOrderHistory);
    }

    private void initData() {
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", 0);
        sessionManager = new LoginSessionManager(getContext());
        db = FirebaseFirestore.getInstance();

        // Ưu tiên lấy từ SessionManager
        userId = sessionManager.getUserId();
        userEmail = sessionManager.getUserEmail();

        // Nếu session rỗng, lấy từ SharedPreferences
        if (userId == null || userId.isEmpty()) {
            userId = sharedPreferences.getString("user_id", "");
        }
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = sharedPreferences.getString("user_email", "");
        }

        // Fallback cuối cùng
        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "tung@gmail.com";
        }

        // Log để kiểm tra
        android.util.Log.d("ProfileFragment", "UserId: " + userId);
        android.util.Log.d("ProfileFragment", "UserEmail: " + userEmail);
    }

    private void loadUserData() {
        // Load từ SharedPreferences
        String name = sharedPreferences.getString("user_name", "");
        String phone = sharedPreferences.getString("user_phone", "");
        String address = sharedPreferences.getString("user_address", "");

        if (name.isEmpty()) {
            name = sharedPreferences.getString("delivery_name", "");
        }
        if (address.isEmpty()) {
            address = sharedPreferences.getString("delivery_address", "");
        }

        // HIỂN THỊ EMAIL - CẢ 2 TEXTVIEW
        if (tvUserEmail != null) {
            tvUserEmail.setText(userEmail);
        }
        if (tvProfileEmail != null) {
            tvProfileEmail.setText(userEmail);
        }

        // HIỂN THỊ TÊN
        if (tvUserName != null) {
            tvUserName.setText(name.isEmpty() ? "Người dùng" : name);
        }

        // HIỂN THỊ THÔNG TIN TRONG EDIT TEXT
        if (etProfileName != null) {
            etProfileName.setText(name);
        }
        if (etProfilePhone != null) {
            etProfilePhone.setText(phone);
        }
        if (etProfileAddress != null) {
            etProfileAddress.setText(address);
        }

        // Load từ Firebase để cập nhật nếu có thay đổi
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String firebaseName = doc.getString("name");
                        String firebasePhone = doc.getString("phone");
                        String firebaseAddress = doc.getString("address");
                        String firebaseEmail = doc.getString("email");

                        if (firebaseName != null && !firebaseName.isEmpty()) {
                            etProfileName.setText(firebaseName);
                            tvUserName.setText(firebaseName);
                            saveToSharedPreferences("user_name", firebaseName);
                            saveToSharedPreferences("delivery_name", firebaseName);
                        }
                        if (firebasePhone != null && !firebasePhone.isEmpty()) {
                            etProfilePhone.setText(firebasePhone);
                            saveToSharedPreferences("user_phone", firebasePhone);
                        }
                        if (firebaseAddress != null && !firebaseAddress.isEmpty()) {
                            etProfileAddress.setText(firebaseAddress);
                            saveToSharedPreferences("user_address", firebaseAddress);
                            saveToSharedPreferences("delivery_address", firebaseAddress);
                        }
                        if (firebaseEmail != null && !firebaseEmail.isEmpty()) {
                            // Cập nhật cả 2 TextView email
                            if (tvUserEmail != null) {
                                tvUserEmail.setText(firebaseEmail);
                            }
                            if (tvProfileEmail != null) {
                                tvProfileEmail.setText(firebaseEmail);
                            }
                            userEmail = firebaseEmail;
                        }
                    }
                });
    }

    private void saveToSharedPreferences(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    private void setupListeners() {
        btnSaveProfile.setOnClickListener(v -> saveUserInfo());
        btnLogout.setOnClickListener(v -> logout());

        layoutWallet.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), WalletActivity.class);
            startActivity(intent);
        });

        layoutOrders.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CartFragment())
                        .commit();
            }
        });

        layoutOrderHistory.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new OrderHistoryFragment())
                        .commit();
            }
        });
    }

    private void saveUserInfo() {
        String name = etProfileName.getText().toString().trim();
        String phone = etProfilePhone.getText().toString().trim();
        String address = etProfileAddress.getText().toString().trim();

        if (name.isEmpty()) {
            etProfileName.setError("Vui lòng nhập họ tên");
            return;
        }
        if (phone.isEmpty()) {
            etProfilePhone.setError("Vui lòng nhập số điện thoại");
            return;
        }
        if (address.isEmpty()) {
            etProfileAddress.setError("Vui lòng nhập địa chỉ");
            return;
        }

        // Lưu vào SharedPreferences
        sharedPreferences.edit()
                .putString("user_name", name)
                .putString("delivery_name", name)
                .putString("user_phone", phone)
                .putString("user_address", address)
                .putString("delivery_address", address)
                .apply();

        // Cập nhật UI
        tvUserName.setText(name);

        // Lưu lên Firebase
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("address", address);

        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã lưu thông tin thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    sessionManager.logout();
                    sharedPreferences.edit().clear().apply();

                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}