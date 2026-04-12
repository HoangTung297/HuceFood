package com.example.foodorder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorder.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra email có tồn tại không
        if (!databaseHelper.isEmailExists(email)) {
            Toast.makeText(this, "Email không tồn tại. Vui lòng đăng ký!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra đăng nhập
        if (databaseHelper.checkUser(email, password)) {
            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

            // In ra log để kiểm tra
            databaseHelper.printAllUsers();

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("user_email", email);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
        }
    }
}