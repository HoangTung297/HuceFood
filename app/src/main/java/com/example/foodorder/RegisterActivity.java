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

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etAddress, etPassword, etConfirmPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView tvLogin;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> register());
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    private boolean isValidPhone(String phone) {
        return phone.matches("^(0|\\+84)[0-9]{9,10}$");
    }

    private void register() {
        // 1. Lấy dữ liệu từ EditText
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 2. Kiểm tra HỌ TÊN
        if (TextUtils.isEmpty(name)) {
            etName.setError("Vui lòng nhập họ tên");
            etName.requestFocus();
            return;
        }

        // 3. Kiểm tra EMAIL
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }
        if (!isValidEmail(email)) {
            etEmail.setError("Email không đúng định dạng ");
            etEmail.requestFocus();
            return;
        }

        // 4. Kiểm tra SỐ ĐIỆN THOẠI
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }
        if (!isValidPhone(phone)) {
            etPhone.setError("Số điện thoại không hợp lệ ");
            etPhone.requestFocus();
            return;
        }

        // 5. Kiểm tra MẬT KHẨU
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        // 6. Kiểm tra XÁC NHẬN MẬT KHẨU
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        // 7. Hiển thị loading
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // 8. Kiểm tra email đã tồn tại chưa
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(query -> {
                    // 8a. Nếu email đã tồn tại
                    if (!query.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setEnabled(true);
                        etEmail.setError("Email đã tồn tại");
                        etEmail.requestFocus();
                        return;
                    }

                    // 8b. Nếu email chưa tồn tại → Tạo user mới
                    createNewUser(name, email, phone, address, password);
                })
                .addOnFailureListener(e -> {
                    // 8c. Lỗi truy vấn Firebase
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                            "Lỗi kết nối: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Tạo user mới và lưu vào Firestore
     */
    private void createNewUser(String name, String email, String phone,
                               String address, String password) {
        // 1. Tạo đối tượng User
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setAddress(address);
        newUser.setPassword(password);
        newUser.setCreatedAt(System.currentTimeMillis());

        // 2. Lưu vào Firestore với Document ID = email
        db.collection("users")
                .document(email)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    // 3. Đăng ký thành công
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this,
                            "✅ Đăng ký thành công!",
                            Toast.LENGTH_LONG).show();

                    // 4. Lưu session (để người dùng đăng nhập tự động)
                    saveUserSession(email, name, email, phone, address);

                    // 5. Chuyển sang LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // 6. Lỗi lưu Firestore
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this,
                            "❌ Lỗi đăng ký: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Lưu thông tin người dùng vào SharedPreferences
     */
    private void saveUserSession(String userId, String userName, String userEmail,
                                 String userPhone, String userAddress) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_id", userId);
        editor.putString("user_name", userName);
        editor.putString("user_email", userEmail);
        editor.putString("user_phone", userPhone);
        editor.putString("user_address", userAddress);
        editor.putString("delivery_name", userName);
        editor.putString("delivery_address", userAddress);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }
}