package com.example.foodorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnSave = findViewById(R.id.btnSave);
        ImageView btnBack = findViewById(R.id.btnBack);

        // Load thông tin hiện tại
        loadCurrentInfo();

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentInfo() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        etName.setText(prefs.getString("user_name", ""));
        etPhone.setText(prefs.getString("user_phone", ""));
        etAddress.setText(prefs.getString("user_address", ""));
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập tên");
            return;
        }

        // Lưu vào SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_name", name);
        editor.putString("user_phone", phone);
        editor.putString("user_address", address);
        editor.apply();

        Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
        finish();
    }
}