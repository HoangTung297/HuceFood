package com.foodorder.admin.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.foodorder.admin.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE);

        if (prefs.getBoolean("is_admin_logged_in", false)) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> login());
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        db.collection("admins")
                .whereEqualTo("email", "admin@gmail.com")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Map<String, Object> admin = new HashMap<>();
                        admin.put("email", "admin@gmail.com");
                        admin.put("password", "admin123");
                        admin.put("name", "Administrator");
                        admin.put("role", "super_admin");
                        admin.put("createdAt", System.currentTimeMillis());
                        db.collection("admins").add(admin);
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

        progressBar.setVisibility(ProgressBar.VISIBLE);
        btnLogin.setEnabled(false);

        db.collection("admins")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(query -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnLogin.setEnabled(true);

                    if (query.isEmpty()) {
                        Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : query) {
                        prefs.edit()
                                .putBoolean("is_admin_logged_in", true)
                                .putString("admin_name", doc.getString("name"))
                                .putString("admin_email", email)
                                .apply();
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}