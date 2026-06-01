package com.example.foodorder;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.adapter.BankAccountAdapter;
import com.example.foodorder.model.BankAccount;
import com.example.foodorder.model.Wallet;
import com.example.foodorder.repository.FirebaseRepository;
import com.example.foodorder.utils.LoginSessionManager;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletActivity extends AppCompatActivity {

    private TextView tvBalance, tvWalletId;
    private RecyclerView rvBankAccounts;
    private Button btnTopUp, btnWithdraw, btnAddBank;
    private Toolbar toolbar;

    private FirebaseRepository repository;
    private LoginSessionManager sessionManager;
    private Wallet wallet;
    private List<BankAccount> bankAccounts;
    private BankAccountAdapter bankAdapter;
    private NumberFormat currencyFormat;
    private static final String TAG = "WalletActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        repository = FirebaseRepository.getInstance();
        sessionManager = new LoginSessionManager(this);
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        bankAccounts = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadWalletInfo();
        loadBankAccounts();

        btnTopUp.setOnClickListener(v -> {
            if (bankAccounts.isEmpty()) {
                Toast.makeText(this, "Vui lòng thêm tài khoản ngân hàng trước khi nạp tiền", Toast.LENGTH_LONG).show();
                showAddBankDialog();
            } else {
                showTopUpDialog();
            }
        });

        btnWithdraw.setOnClickListener(v -> {
            if (bankAccounts.isEmpty()) {
                Toast.makeText(this, "Vui lòng thêm tài khoản ngân hàng trước khi rút tiền", Toast.LENGTH_LONG).show();
                showAddBankDialog();
            } else if (wallet == null || wallet.getBalance() <= 0) {
                Toast.makeText(this, "Số dư trong ví không đủ để rút", Toast.LENGTH_SHORT).show();
            } else {
                showWithdrawDialog();
            }
        });

        btnAddBank.setOnClickListener(v -> showAddBankDialog());
    }

    private void initViews() {
        tvBalance = findViewById(R.id.tvBalance);
        tvWalletId = findViewById(R.id.tvWalletId);
        rvBankAccounts = findViewById(R.id.rvBankAccounts);
        btnTopUp = findViewById(R.id.btnTopUp);
        btnWithdraw = findViewById(R.id.btnWithdraw);
        btnAddBank = findViewById(R.id.btnAddBank);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        bankAdapter = new BankAccountAdapter(bankAccounts, new BankAccountAdapter.OnBankAccountClickListener() {
            @Override
            public void onBankAccountClick(BankAccount account) {
                showTopUpWithBankDialog(account);
            }

            @Override
            public void onDeleteClick(BankAccount account) {
                confirmDeleteBankAccount(account);
            }
        });
        rvBankAccounts.setLayoutManager(new LinearLayoutManager(this));
        rvBankAccounts.setAdapter(bankAdapter);
    }

    private String getCurrentUserId() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userId = prefs.getString("user_email", "");
        }
        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }
        return userId;
    }

    private void loadWalletInfo() {
        String userId = getCurrentUserId();
        repository.getWallet(userId, new FirebaseRepository.OnDataLoaded<Wallet>() {
            @Override
            public void onSuccess(Wallet data) {
                wallet = data;
                updateWalletDisplay();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading wallet: " + error);
            }
        });
    }

    private void updateWalletDisplay() {
        if (wallet != null) {
            tvBalance.setText(currencyFormat.format(wallet.getBalance()) + "đ");
            String walletId = wallet.getId();
            if (walletId != null && !walletId.isEmpty()) {
                tvWalletId.setText("ID: " + (walletId.length() > 8 ? walletId.substring(0, 8) : walletId));
            }
        }
    }

    private void loadBankAccounts() {
        String userId = getCurrentUserId();
        repository.getLinkedBankAccounts(userId, new FirebaseRepository.OnDataLoaded<List<BankAccount>>() {
            @Override
            public void onSuccess(List<BankAccount> data) {
                bankAccounts.clear();
                if (data != null && !data.isEmpty()) {
                    bankAccounts.addAll(data);
                }
                bankAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading bank accounts: " + error);
            }
        });
    }

    // ==================== NẠP TIỀN ====================
    private void showAddBankDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_bank, null);

        EditText edtBankName = dialogView.findViewById(R.id.edtBankName);
        EditText edtAccountNumber = dialogView.findViewById(R.id.edtAccountNumber);
        EditText edtAccountHolder = dialogView.findViewById(R.id.edtAccountHolder);

        builder.setTitle("Thêm tài khoản ngân hàng")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String bankName = edtBankName.getText().toString().trim();
                    String accountNumber = edtAccountNumber.getText().toString().trim();
                    String accountHolder = edtAccountHolder.getText().toString().trim();

                    if (bankName.isEmpty() || accountNumber.isEmpty() || accountHolder.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BankAccount bankAccount = new BankAccount();
                    bankAccount.setUserId(getCurrentUserId());
                    bankAccount.setBankName(bankName);
                    bankAccount.setAccountNumber(accountNumber);
                    bankAccount.setAccountHolder(accountHolder);
                    bankAccount.setLinked(true);

                    repository.addBankAccount(bankAccount, new FirebaseRepository.OnDataLoaded<String>() {
                        @Override
                        public void onSuccess(String data) {
                            Toast.makeText(WalletActivity.this, "Thêm tài khoản thành công", Toast.LENGTH_SHORT).show();
                            loadBankAccounts();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(WalletActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showTopUpDialog() {
        String[] bankNames = new String[bankAccounts.size()];
        for (int i = 0; i < bankAccounts.size(); i++) {
            bankNames[i] = bankAccounts.get(i).getBankName() + " - " + bankAccounts.get(i).getAccountNumber();
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_topup, null);
        EditText edtAmount = dialogView.findViewById(R.id.edtAmount);
        Spinner spinnerBank = dialogView.findViewById(R.id.spinnerBank);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Nạp tiền vào ví")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String amountStr = edtAmount.getText().toString();
                    if (amountStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double amount = Double.parseDouble(amountStr);
                    BankAccount selectedBank = bankAccounts.get(spinnerBank.getSelectedItemPosition());
                    performTopUp(amount, selectedBank);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showTopUpWithBankDialog(BankAccount bankAccount) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_topup, null);
        EditText edtAmount = dialogView.findViewById(R.id.edtAmount);
        Spinner spinnerBank = dialogView.findViewById(R.id.spinnerBank);
        spinnerBank.setVisibility(View.GONE);

        TextView tvBankInfo = new TextView(this);
        tvBankInfo.setText("Ngân hàng: " + bankAccount.getBankName() + "\nSố TK: " + bankAccount.getAccountNumber());
        tvBankInfo.setPadding(16, 16, 16, 16);
        ((LinearLayout) dialogView).addView(tvBankInfo, 1);

        new AlertDialog.Builder(this)
                .setTitle("Nạp tiền từ " + bankAccount.getBankName())
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String amountStr = edtAmount.getText().toString();
                    if (amountStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double amount = Double.parseDouble(amountStr);
                    performTopUp(amount, bankAccount);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performTopUp(double amount, BankAccount bankAccount) {
        if (wallet == null) return;
        double newBalance = wallet.getBalance() + amount;
        repository.updateWalletBalance(wallet.getUserId(), newBalance, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                wallet.setBalance(newBalance);
                updateWalletDisplay();
                Toast.makeText(WalletActivity.this, "Nạp thành công " + currencyFormat.format(amount) + "đ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WalletActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==================== RÚT TIỀN ====================
    private void showWithdrawDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_withdraw, null);
        EditText edtAmount = dialogView.findViewById(R.id.edtWithdrawAmount);
        Spinner spinnerBank = dialogView.findViewById(R.id.spinnerWithdrawBank);
        TextView tvBalanceInfo = dialogView.findViewById(R.id.tvWalletBalanceInfo);

        // Hiển thị số dư hiện tại
        tvBalanceInfo.setText("Số dư hiện tại: " + currencyFormat.format(wallet.getBalance()) + "đ");

        // Tạo danh sách tài khoản ngân hàng
        String[] bankNames = new String[bankAccounts.size()];
        for (int i = 0; i < bankAccounts.size(); i++) {
            bankNames[i] = bankAccounts.get(i).getBankName() + " - " + bankAccounts.get(i).getAccountNumber();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Rút tiền từ ví")
                .setView(dialogView)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String amountStr = edtAmount.getText().toString();
                    if (amountStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (amount > wallet.getBalance()) {
                        Toast.makeText(this, "Số dư không đủ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    BankAccount selectedBank = bankAccounts.get(spinnerBank.getSelectedItemPosition());
                    performWithdraw(amount, selectedBank);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performWithdraw(double amount, BankAccount bankAccount) {
        double newBalance = wallet.getBalance() - amount;
        repository.updateWalletBalance(wallet.getUserId(), newBalance, new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                wallet.setBalance(newBalance);
                updateWalletDisplay();
                Toast.makeText(WalletActivity.this,
                        "Rút thành công " + currencyFormat.format(amount) + "đ về " + bankAccount.getBankName(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WalletActivity.this, "Lỗi rút tiền: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteBankAccount(BankAccount bankAccount) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản " + bankAccount.getBankName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteBankAccount(bankAccount))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteBankAccount(BankAccount bankAccount) {
        repository.deleteBankAccount(bankAccount.getId(), new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(WalletActivity.this, "Đã xóa tài khoản", Toast.LENGTH_SHORT).show();
                loadBankAccounts();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WalletActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}