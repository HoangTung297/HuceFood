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
import com.example.foodorder.WalletTopUpActivity; // sẽ tạo
import com.example.foodorder.adapter.BankAccountAdapter;
import com.example.foodorder.model.BankAccount;
import com.example.foodorder.model.Wallet;
import com.example.foodorder.repository.FirebaseRepository;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletFragment extends Fragment {

    private TextView tvBalance;
    private RecyclerView rvBankAccounts;
    private Button btnTopUp, btnAddBank;
    private FirebaseRepository repository;
    private String userId;
    private List<BankAccount> bankAccounts = new ArrayList<>();
    private BankAccountAdapter bankAdapter;

    public WalletFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        tvBalance = view.findViewById(R.id.tvBalance);
        rvBankAccounts = view.findViewById(R.id.rvBankAccounts);
        btnTopUp = view.findViewById(R.id.btnTopUp);
        btnAddBank = view.findViewById(R.id.btnAddBank);

        userId = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE)
                .getString("user_id", "user123");
        repository = FirebaseRepository.getInstance();

        loadWallet();
        setupBankList();
        loadBankAccounts();

        btnTopUp.setOnClickListener(v -> {
            // Mở màn hình nạp tiền
            Intent intent = new Intent(getActivity(), WalletTopUpActivity.class);
            startActivity(intent);
        });

        btnAddBank.setOnClickListener(v -> {
            // Mở màn hình thêm ngân hàng (có thể tạo AddBankActivity)
            // Ở đây tạm thời hiển thị Toast
            Toast.makeText(getContext(), "Chức năng thêm ngân hàng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void loadWallet() {
        repository.getWallet(userId, new FirebaseRepository.OnDataLoaded<Wallet>() {
            @Override
            public void onSuccess(Wallet wallet) {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvBalance.setText(formatter.format(wallet.getBalance()) + "đ");
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi tải ví: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBankList() {
        bankAdapter = new BankAccountAdapter(bankAccounts, bankAccount -> {
            // Xử lý click item (nếu cần)
        });
        rvBankAccounts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBankAccounts.setAdapter(bankAdapter);
    }

    private void loadBankAccounts() {
        repository.getLinkedBankAccounts(userId, new FirebaseRepository.OnDataLoaded<List<BankAccount>>() {
            @Override
            public void onSuccess(List<BankAccount> data) {
                bankAccounts.clear();
                if (data != null) bankAccounts.addAll(data);
                bankAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi tải ngân hàng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWallet(); // cập nhật số dư khi quay lại
    }
}