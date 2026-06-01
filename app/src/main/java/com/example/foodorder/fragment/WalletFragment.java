package com.example.foodorder.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.WalletActivity;
import com.example.foodorder.model.BankAccount;
import com.example.foodorder.model.Wallet;
import com.example.foodorder.repository.FirebaseRepository;
import com.example.foodorder.utils.LoginSessionManager;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletFragment extends Fragment {

    private TextView tvBalance, tvWalletId;
    private RecyclerView rvBankAccounts;
    private Button btnTopUp, btnAddBank;

    private FirebaseRepository repository;
    private LoginSessionManager sessionManager;
    private Wallet wallet;
    private List<BankAccount> bankAccounts;
    private BankAccountAdapter bankAdapter;
    private NumberFormat currencyFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_wallet, container, false);

        repository = FirebaseRepository.getInstance();
        sessionManager = new LoginSessionManager(getContext());
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        bankAccounts = new ArrayList<>();

        initViews(view);
        setupRecyclerView(view);
        loadWalletInfo();
        loadBankAccounts();

        btnTopUp.setOnClickListener(v -> {
            // Mở WalletActivity thay vì WalletTopUpActivity
            Intent intent = new Intent(getActivity(), WalletActivity.class);
            startActivity(intent);
        });

        btnAddBank.setOnClickListener(v -> showAddBankDialog());

        return view;
    }

    private void initViews(View view) {
        tvBalance = view.findViewById(R.id.tvBalance);
        tvWalletId = view.findViewById(R.id.tvWalletId);
        rvBankAccounts = view.findViewById(R.id.rvBankAccounts);
        btnTopUp = view.findViewById(R.id.btnTopUp);
        btnAddBank = view.findViewById(R.id.btnAddBank);
    }

    private void setupRecyclerView(View view) {
        bankAdapter = new BankAccountAdapter(bankAccounts, account -> {
            showBankOptions(account);
        });
        rvBankAccounts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBankAccounts.setAdapter(bankAdapter);
    }

    private void loadWalletInfo() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }

        repository.getWallet(userId, new FirebaseRepository.OnDataLoaded<Wallet>() {
            @Override
            public void onSuccess(Wallet data) {
                wallet = data;
                updateWalletDisplay();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi tải ví: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWalletDisplay() {
        if (wallet != null) {
            tvBalance.setText(currencyFormat.format(wallet.getBalance()) + "đ");
            String walletId = wallet.getId();
            if (walletId != null && walletId.length() > 8) {
                tvWalletId.setText("ID: " + walletId.substring(0, 8));
            } else {
                tvWalletId.setText("ID: " + walletId);
            }
        }
    }

    private void loadBankAccounts() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }

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
                Toast.makeText(getContext(), "Lỗi tải tài khoản: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddBankDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_bank, null);

        android.widget.EditText edtBankName = dialogView.findViewById(R.id.edtBankName);
        android.widget.EditText edtAccountNumber = dialogView.findViewById(R.id.edtAccountNumber);
        android.widget.EditText edtAccountHolder = dialogView.findViewById(R.id.edtAccountHolder);

        builder.setTitle("Thêm tài khoản ngân hàng")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String bankName = edtBankName.getText().toString().trim();
                    String accountNumber = edtAccountNumber.getText().toString().trim();
                    String accountHolder = edtAccountHolder.getText().toString().trim();

                    if (bankName.isEmpty() || accountNumber.isEmpty() || accountHolder.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    BankAccount bankAccount = new BankAccount();
                    bankAccount.setUserId(sessionManager.getUserId());
                    bankAccount.setBankName(bankName);
                    bankAccount.setAccountNumber(accountNumber);
                    bankAccount.setAccountHolder(accountHolder);
                    bankAccount.setLinked(true);

                    repository.addBankAccount(bankAccount, new FirebaseRepository.OnDataLoaded<String>() {
                        @Override
                        public void onSuccess(String data) {
                            Toast.makeText(getContext(), "Thêm tài khoản thành công", Toast.LENGTH_SHORT).show();
                            loadBankAccounts();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showBankOptions(BankAccount bankAccount) {
        String[] options = {"Xóa tài khoản"};

        new android.app.AlertDialog.Builder(getContext())
                .setTitle(bankAccount.getBankName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        deleteBankAccount(bankAccount);
                    }
                })
                .show();
    }

    private void deleteBankAccount(BankAccount bankAccount) {
        repository.deleteBankAccount(bankAccount.getId(), new FirebaseRepository.OnDataLoaded<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(getContext(), "Đã xóa tài khoản", Toast.LENGTH_SHORT).show();
                loadBankAccounts();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter inner class
    static class BankAccountAdapter extends RecyclerView.Adapter<BankAccountAdapter.ViewHolder> {
        private List<BankAccount> accounts;
        private OnBankAccountClickListener listener;

        interface OnBankAccountClickListener {
            void onBankAccountClick(BankAccount account);
        }

        BankAccountAdapter(List<BankAccount> accounts, OnBankAccountClickListener listener) {
            this.accounts = accounts;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bank_account, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BankAccount account = accounts.get(position);
            holder.tvBankName.setText(account.getBankName());
            holder.tvAccountNumber.setText(account.getAccountNumber());
            holder.tvAccountHolder.setText(account.getAccountHolder());

            holder.itemView.setOnClickListener(v -> listener.onBankAccountClick(account));
        }

        @Override
        public int getItemCount() {
            return accounts.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvBankName, tvAccountNumber, tvAccountHolder;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvBankName = itemView.findViewById(R.id.tvBankName);
                tvAccountNumber = itemView.findViewById(R.id.tvAccountNumber);
                tvAccountHolder = itemView.findViewById(R.id.tvAccountHolder);
            }
        }
    }
}