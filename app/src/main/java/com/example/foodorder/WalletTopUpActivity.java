package com.example.foodorder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodorder.model.BankAccount;
import com.example.foodorder.model.Wallet;
import com.example.foodorder.repository.FirebaseRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletTopUpActivity extends AppCompatActivity {

    private Spinner spinnerBank;
    private EditText etAmount;
    private TextView tvCurrentBalance;
    private Button btnConfirm;
    private FirebaseRepository repository;
    private String userId;
    private List<BankAccount> bankAccounts = new ArrayList<>();
    private BankAccount selectedBank = null;
    private double currentBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_topup);

        spinnerBank = findViewById(R.id.spinnerBank);
        etAmount = findViewById(R.id.etAmount);
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        btnConfirm = findViewById(R.id.btnConfirm);

        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_id", "user123");
        repository = FirebaseRepository.getInstance();

        loadWalletInfo();
        loadBankAccounts();

        btnConfirm.setOnClickListener(v -> performTopUp());
    }

    private void loadWalletInfo() {
        repository.getWallet(userId, new FirebaseRepository.OnDataLoaded<Wallet>() {
            @Override
            public void onSuccess(Wallet wallet) {
                currentBalance = wallet.getBalance();
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvCurrentBalance.setText("Số dư hiện tại: " + formatter.format(currentBalance) + "đ");
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WalletTopUpActivity.this, "Lỗi tải ví", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBankAccounts() {
        repository.getLinkedBankAccounts(userId, new FirebaseRepository.OnDataLoaded<List<BankAccount>>() {
            @Override
            public void onSuccess(List<BankAccount> data) {
                bankAccounts.clear();
                if (data != null) bankAccounts.addAll(data);
                List<String> bankNames = new ArrayList<>();
                for (BankAccount b : bankAccounts) {
                    bankNames.add(b.getBankName() + " - " + b.getAccountNumber());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(WalletTopUpActivity.this,
                        android.R.layout.simple_spinner_item, bankNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBank.setAdapter(adapter);

                spinnerBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedBank = bankAccounts.get(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedBank = null;
                    }
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WalletTopUpActivity.this, "Lỗi tải ngân hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performTopUp() {
        if (selectedBank == null) {
            Toast.makeText(this, "Vui lòng chọn tài khoản ngân hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }
        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Giả lập nạp tiền: cộng vào số dư
        double newBalance = currentBalance + amount;
        repository.updateWalletBalance(userId, newBalance, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(WalletTopUpActivity.this, "Nạp tiền thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WalletTopUpActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}