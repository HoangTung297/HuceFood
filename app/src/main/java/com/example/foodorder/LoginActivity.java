package com.example.foodorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorder.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupClickListeners();

        // Tạo tài khoản mẫu nếu chưa có
        createSampleAccount();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> login());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Vui lòng liên hệ admin để đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
        });
    }

    private void createSampleAccount() {
        db.collection("users")
                .whereEqualTo("email", "tung@gmail.com")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        User sampleUser = new User();
                        sampleUser.setName("Nguyễn Văn Tùng");
                        sampleUser.setEmail("tung@gmail.com");
                        sampleUser.setPhone("0987654321");
                        sampleUser.setAddress("Hà Nội");
                        sampleUser.setPassword("123456");
                        sampleUser.setCreatedAt(System.currentTimeMillis());

                        db.collection("users").document("tung@gmail.com").set(sampleUser)
                                .addOnSuccessListener(aVoid -> {
                                    System.out.println("✅ Đã tạo tài khoản mẫu: tung@gmail.com / 123456");
                                });
                    }
                });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Email không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String storedPassword = doc.getString("password");
                        if (storedPassword != null && storedPassword.equals(password)) {
                            String userId = doc.getId();
                            String userName = doc.getString("name");
                            String userEmail = doc.getString("email");
                            String userPhone = doc.getString("phone") != null ? doc.getString("phone") : "";
                            String userAddress = doc.getString("address") != null ? doc.getString("address") : "";

                            saveUserSession(userId, userName, userEmail, userPhone, userAddress);

                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserSession(String userId, String userName, String userEmail, String userPhone, String userAddress) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_id", userId);
        editor.putString("user_name", userName);
        editor.putString("user_email", userEmail);
        editor.putString("user_phone", userPhone);
        editor.putString("user_address", userAddress);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }
}