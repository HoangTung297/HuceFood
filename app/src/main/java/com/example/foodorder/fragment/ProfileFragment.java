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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.foodorder.HomeActivity;
import com.example.foodorder.LoginActivity;
import com.example.foodorder.R;
import com.example.foodorder.WalletActivity;
import com.example.foodorder.utils.LoginSessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private EditText etProfileName, etProfileEmail, etProfilePhone, etProfileAddress;
    private Button btnSaveProfile, btnLogout;
    private LinearLayout layoutWallet, layoutOrders, layoutOrderHistory;

    private SharedPreferences sharedPreferences;
    private LoginSessionManager sessionManager;
    private FirebaseFirestore db;
    private String userId;
    private String currentEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setupListeners();
        loadUserData();

        return view;
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        etProfileName = view.findViewById(R.id.etProfileName);
        etProfileEmail = view.findViewById(R.id.etProfileEmail);
        etProfilePhone = view.findViewById(R.id.etProfilePhone);
        etProfileAddress = view.findViewById(R.id.etProfileAddress);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        layoutWallet = view.findViewById(R.id.layoutWallet);
        layoutOrders = view.findViewById(R.id.layoutOrders);
        layoutOrderHistory = view.findViewById(R.id.layoutOrderHistory);

        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", 0);
        sessionManager = new LoginSessionManager(getContext());
        db = FirebaseFirestore.getInstance();

        userId = sessionManager.getUserId();
        currentEmail = sessionManager.getUserEmail();

        if (userId == null || userId.isEmpty()) {
            userId = sharedPreferences.getString("user_id", "");
        }
        if (currentEmail == null || currentEmail.isEmpty()) {
            currentEmail = sharedPreferences.getString("user_email", "");
        }
        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }
        if (currentEmail == null || currentEmail.isEmpty()) {
            currentEmail = "tung@gmail.com";
        }
    }

    private void loadUserData() {
        String name = sharedPreferences.getString("user_name", "");
        String phone = sharedPreferences.getString("user_phone", "");
        String address = sharedPreferences.getString("user_address", "");
        String email = sharedPreferences.getString("user_email", "");

        if (name.isEmpty()) {
            name = sharedPreferences.getString("delivery_name", "");
        }
        if (address.isEmpty()) {
            address = sharedPreferences.getString("delivery_address", "");
        }

        tvUserName.setText(name.isEmpty() ? "Người dùng" : name);
        tvUserEmail.setText(currentEmail);
        etProfileName.setText(name);
        etProfileEmail.setText(currentEmail);
        etProfilePhone.setText(phone);
        etProfileAddress.setText(address);

        if (name.isEmpty() || phone.isEmpty()) {
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
                                etProfileEmail.setText(firebaseEmail);
                                tvUserEmail.setText(firebaseEmail);
                                currentEmail = firebaseEmail;
                                saveToSharedPreferences("user_email", firebaseEmail);
                            }
                        }
                    });
        }
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

        // GIỎ HÀNG - Chuyển đến OrderFragment tab Giỏ hàng
        layoutOrders.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToCart();
            }
        });

        // LỊCH SỬ ĐƠN HÀNG - Chuyển đến OrderFragment tab Lịch sử
        layoutOrderHistory.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToOrderHistory();
            }
        });
    }

    private void saveUserInfo() {
        String name = etProfileName.getText().toString().trim();
        String email = etProfileEmail.getText().toString().trim();
        String phone = etProfilePhone.getText().toString().trim();
        String address = etProfileAddress.getText().toString().trim();

        if (name.isEmpty()) {
            etProfileName.setError("Vui lòng nhập họ tên");
            return;
        }
        if (email.isEmpty()) {
            etProfileEmail.setError("Vui lòng nhập email");
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

        boolean emailChanged = !email.equals(currentEmail);

        if (emailChanged) {
            checkEmailExists(email, new EmailCheckCallback() {
                @Override
                public void onResult(boolean exists) {
                    if (exists) {
                        Toast.makeText(getContext(), "Email đã được sử dụng bởi tài khoản khác", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUserInfo(name, email, phone, address, emailChanged);
                    }
                }
            });
        } else {
            updateUserInfo(name, email, phone, address, false);
        }
    }

    private interface EmailCheckCallback {
        void onResult(boolean exists);
    }

    private void checkEmailExists(String email, EmailCheckCallback callback) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(query -> {
                    boolean exists = !query.isEmpty() && !query.getDocuments().get(0).getId().equals(userId);
                    callback.onResult(exists);
                })
                .addOnFailureListener(e -> callback.onResult(false));
    }

    private void updateUserInfo(String name, String email, String phone, String address, boolean emailChanged) {
        sharedPreferences.edit()
                .putString("user_name", name)
                .putString("delivery_name", name)
                .putString("user_email", email)
                .putString("user_phone", phone)
                .putString("user_address", address)
                .putString("delivery_address", address)
                .apply();

        tvUserName.setText(name);
        tvUserEmail.setText(email);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("address", address);

        if (emailChanged) {
            updates.put("email", email);
            createNewUserDocumentWithNewEmail(userId, name, email, phone, address);
        } else {
            db.collection("users").document(userId).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Đã lưu thông tin thành công", Toast.LENGTH_SHORT).show();
                        sessionManager.createLoginSession(userId, email, name, phone, address);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void createNewUserDocumentWithNewEmail(String oldUserId, String name, String newEmail, String phone, String address) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("name", name);
        newUser.put("email", newEmail);
        newUser.put("phone", phone);
        newUser.put("address", address);
        newUser.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(oldUserId).get()
                .addOnSuccessListener(doc -> {
                    String oldPassword = doc.getString("password");
                    newUser.put("password", oldPassword != null ? oldPassword : "123456");

                    db.collection("users").document(newEmail).set(newUser)
                            .addOnSuccessListener(aVoid -> {
                                db.collection("users").document(oldUserId).delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            sessionManager.createLoginSession(newEmail, newEmail, name, phone, address);
                                            currentEmail = newEmail;
                                            userId = newEmail;

                                            sharedPreferences.edit()
                                                    .putString("user_id", newEmail)
                                                    .putString("user_email", newEmail)
                                                    .apply();

                                            Toast.makeText(getContext(), "Đã cập nhật thông tin. Vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();

                                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            getActivity().finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Lỗi xóa tài khoản cũ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi tạo tài khoản mới: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void logout() {
        new AlertDialog.Builder(getContext())
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