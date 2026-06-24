package com.example.foodorder.fragment;

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
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.example.foodorder.LoginActivity;
import com.example.foodorder.R;
import com.example.foodorder.WalletActivity;
import com.example.foodorder.utils.CacheManager;
import com.example.foodorder.utils.LoginSessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private EditText etProfileName, etProfileEmail, etProfilePhone, etProfileAddress;
    private Button btnSaveProfile, btnLogout;
    private CardView layoutWallet;

    private SharedPreferences sharedPreferences;
    private LoginSessionManager sessionManager;
    private CacheManager cacheManager;
    private FirebaseFirestore db;
    private String userId;
    private String currentEmail;

    private boolean isDataLoaded = false;
    private boolean isLoading = false;
    private boolean isViewDestroyed = false;

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

        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", 0);
        sessionManager = new LoginSessionManager(getContext());
        cacheManager = new CacheManager(getContext());
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
        if (isDataLoaded) {
            // Đã có dữ liệu, chỉ refresh UI từ cache
            refreshUIFromCache();
            return;
        }

        String name = "";
        String phone = "";
        String address = "";
        String email = currentEmail;

        // 1. LẤY TỪ CACHE
        Map<String, String> cachedUser = cacheManager.getCachedUserData();
        if (cachedUser != null && !cachedUser.isEmpty()) {
            name = cachedUser.get("name") != null ? cachedUser.get("name") : "";
            email = cachedUser.get("email") != null ? cachedUser.get("email") : currentEmail;
            phone = cachedUser.get("phone") != null ? cachedUser.get("phone") : "";
            address = cachedUser.get("address") != null ? cachedUser.get("address") : "";
        }

        // 2. NẾU CACHE RỖNG, LẤY TỪ SHAREDPREFERENCES
        if (name.isEmpty()) {
            name = sharedPreferences.getString("user_name", "");
            if (name.isEmpty()) {
                name = sharedPreferences.getString("delivery_name", "");
            }
        }
        if (phone.isEmpty()) {
            phone = sharedPreferences.getString("user_phone", "");
        }
        if (address.isEmpty()) {
            address = sharedPreferences.getString("user_address", "");
            if (address.isEmpty()) {
                address = sharedPreferences.getString("delivery_address", "");
            }
        }
        if (email.isEmpty() || email.equals(currentEmail)) {
            email = sharedPreferences.getString("user_email", currentEmail);
        }

        // 3. HIỂN THỊ NGAY LẬP TỨC
        updateUI(name, email, phone, address);
        isDataLoaded = true;

        // 4. LOAD TỪ FIREBASE NẾU CHƯA CÓ DỮ LIỆU
        if (name.isEmpty() || phone.isEmpty()) {
            loadFromFirebase();
        }
    }

    private void loadFromFirebase() {
        if (isLoading || isViewDestroyed) return;
        isLoading = true;

        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (isViewDestroyed) return;
                    isLoading = false;

                    if (doc.exists()) {
                        String firebaseName = doc.getString("name");
                        String firebasePhone = doc.getString("phone");
                        String firebaseAddress = doc.getString("address");
                        String firebaseEmail = doc.getString("email");

                        String name = firebaseName != null ? firebaseName : "";
                        String phone = firebasePhone != null ? firebasePhone : "";
                        String address = firebaseAddress != null ? firebaseAddress : "";
                        String email = firebaseEmail != null ? firebaseEmail : currentEmail;

                        // Lưu vào Cache
                        cacheManager.cacheUserData(name, email, phone, address);

                        // Lưu vào SharedPreferences
                        if (!name.isEmpty()) {
                            saveToSharedPreferences("user_name", name);
                            saveToSharedPreferences("delivery_name", name);
                        }
                        if (!phone.isEmpty()) {
                            saveToSharedPreferences("user_phone", phone);
                        }
                        if (!address.isEmpty()) {
                            saveToSharedPreferences("user_address", address);
                            saveToSharedPreferences("delivery_address", address);
                        }
                        if (!email.isEmpty() && !email.equals(currentEmail)) {
                            saveToSharedPreferences("user_email", email);
                            currentEmail = email;
                        }

                        // Cập nhật UI nếu Fragment còn hiển thị
                        if (!isViewDestroyed) {
                            updateUI(name, email, phone, address);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading = false;
                });
    }

    private void refreshUIFromCache() {
        if (isViewDestroyed) return;

        Map<String, String> cachedUser = cacheManager.getCachedUserData();
        if (cachedUser != null && !cachedUser.isEmpty()) {
            String name = cachedUser.get("name") != null ? cachedUser.get("name") : "";
            String email = cachedUser.get("email") != null ? cachedUser.get("email") : currentEmail;
            String phone = cachedUser.get("phone") != null ? cachedUser.get("phone") : "";
            String address = cachedUser.get("address") != null ? cachedUser.get("address") : "";
            updateUI(name, email, phone, address);
        }
    }

    private void updateUI(String name, String email, String phone, String address) {
        if (getActivity() == null || isViewDestroyed) return;

        tvUserName.setText(name.isEmpty() ? "Người dùng" : name);
        tvUserEmail.setText(email.isEmpty() ? currentEmail : email);
        etProfileName.setText(name);
        etProfileEmail.setText(email.isEmpty() ? currentEmail : email);
        etProfilePhone.setText(phone != null ? phone : "");
        etProfileAddress.setText(address != null ? address : "");
    }

    private void saveToSharedPreferences(String key, String value) {
        if (value == null) return;
        sharedPreferences.edit().putString(key, value).apply();
    }

    private void setupListeners() {
        btnSaveProfile.setOnClickListener(v -> saveUserInfo());
        btnLogout.setOnClickListener(v -> logout());

        layoutWallet.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), WalletActivity.class);
            startActivity(intent);
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

        // Lưu vào Cache và SharedPreferences ngay lập tức
        cacheManager.cacheUserData(name, email, phone, address);
        saveToSharedPreferences("user_name", name);
        saveToSharedPreferences("delivery_name", name);
        saveToSharedPreferences("user_email", email);
        saveToSharedPreferences("user_phone", phone);
        saveToSharedPreferences("user_address", address);
        saveToSharedPreferences("delivery_address", address);

        updateUI(name, email, phone, address);

        boolean emailChanged = !email.equals(currentEmail);

        if (emailChanged) {
            checkEmailExists(email, new EmailCheckCallback() {
                @Override
                public void onResult(boolean exists) {
                    if (exists) {
                        Toast.makeText(getContext(), "Email đã được sử dụng bởi tài khoản khác", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUserInfo(name, email, phone, address, true);
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
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(), "Đã lưu thông tin thành công", Toast.LENGTH_SHORT).show();
                        sessionManager.createLoginSession(userId, email, name, phone, address);
                        cacheManager.cacheUserData(name, email, phone, address);
                    })
                    .addOnFailureListener(e -> {
                        if (getActivity() == null) return;
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
                    if (isViewDestroyed) return;

                    String oldPassword = doc.getString("password");
                    newUser.put("password", oldPassword != null ? oldPassword : "123456");

                    db.collection("users").document(newEmail).set(newUser)
                            .addOnSuccessListener(aVoid -> {
                                if (isViewDestroyed) return;

                                db.collection("users").document(oldUserId).delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            if (isViewDestroyed) return;

                                            sessionManager.createLoginSession(newEmail, newEmail, name, phone, address);
                                            currentEmail = newEmail;
                                            userId = newEmail;

                                            sharedPreferences.edit()
                                                    .putString("user_id", newEmail)
                                                    .putString("user_email", newEmail)
                                                    .apply();

                                            cacheManager.cacheUserData(name, newEmail, phone, address);

                                            Toast.makeText(getContext(), "Đã cập nhật thông tin. Vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();

                                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            getActivity().finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (isViewDestroyed) return;
                                            Toast.makeText(getContext(), "Lỗi xóa tài khoản cũ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                if (isViewDestroyed) return;
                                Toast.makeText(getContext(), "Lỗi tạo tài khoản mới: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void logout() {
        new AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    cacheManager.clearUserCache();
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

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewDestroyed = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isViewDestroyed = false;
        if (isDataLoaded) {
            refreshUIFromCache();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && isDataLoaded) {
            refreshUIFromCache();
        }
    }
}