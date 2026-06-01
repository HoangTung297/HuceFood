package com.example.foodorder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorder.adapter.VoucherAdapter;
import com.example.foodorder.model.Voucher;

import java.util.ArrayList;
import java.util.List;

public class VoucherSelectionActivity extends AppCompatActivity {

    private RecyclerView rvVouchers;
    private EditText etVoucherCode;
    private Button btnCheckVoucher, btnApplyVoucher, btnCancelVoucher;
    private VoucherAdapter adapter;
    private List<Voucher> voucherList = new ArrayList<>();
    private Voucher selectedVoucher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_voucher);

        initViews();
        setupRecyclerView();  // Phải gọi trước loadVouchers
        loadVouchers();

        btnCheckVoucher.setOnClickListener(v -> checkManualCode());
        btnApplyVoucher.setOnClickListener(v -> applySelectedVoucher());
        btnCancelVoucher.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvVouchers = findViewById(R.id.rvVouchers);
        etVoucherCode = findViewById(R.id.etVoucherCode);
        btnCheckVoucher = findViewById(R.id.btnCheckVoucher);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);
        btnCancelVoucher = findViewById(R.id.btnCancelVoucher);
    }

    private void setupRecyclerView() {
        adapter = new VoucherAdapter(voucherList, (voucher, position, isSelected) -> {
            if (isSelected) {
                selectedVoucher = voucher;
            } else {
                selectedVoucher = null;
            }
        });
        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        rvVouchers.setAdapter(adapter);
    }

    private void loadVouchers() {
        voucherList.clear();

        Voucher v1 = new Voucher();
        v1.setId("v1");
        v1.setCode("SALE20");
        v1.setTitle("GIẢM 20%");
        v1.setDescription("Giảm 20% cho đơn từ 100,000đ");
        v1.setDiscountType("percent");
        v1.setDiscountValue(20);
        v1.setMinOrder(100000);
        voucherList.add(v1);

        Voucher v2 = new Voucher();
        v2.setId("v2");
        v2.setCode("SALE50K");
        v2.setTitle("GIẢM 50K");
        v2.setDescription("Giảm 50,000đ cho đơn từ 200,000đ");
        v2.setDiscountType("fixed");
        v2.setDiscountValue(50000);
        v2.setMinOrder(200000);
        voucherList.add(v2);

        Voucher v3 = new Voucher();
        v3.setId("v3");
        v3.setCode("FREESHIP");
        v3.setTitle("FREE SHIP");
        v3.setDescription("Miễn phí giao hàng cho mọi đơn");
        v3.setDiscountType("freeship");
        v3.setDiscountValue(15000);
        v3.setMinOrder(0);
        voucherList.add(v3);

        adapter.updateList(voucherList);
    }

    private void checkManualCode() {
        String code = etVoucherCode.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Voucher v : voucherList) {
            if (v.getCode().equalsIgnoreCase(code)) {
                selectedVoucher = v;
                Toast.makeText(this, "Mã hợp lệ: " + v.getTitle(), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Toast.makeText(this, "Mã voucher không tồn tại", Toast.LENGTH_SHORT).show();
    }

    private void applySelectedVoucher() {
        if (selectedVoucher == null) {
            Toast.makeText(this, "Vui lòng chọn một voucher", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_voucher", selectedVoucher);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}