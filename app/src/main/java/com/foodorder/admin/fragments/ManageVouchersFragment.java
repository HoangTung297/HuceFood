package com.foodorder.admin.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.foodorder.admin.R;
import com.foodorder.admin.model.Voucher;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageVouchersFragment extends Fragment {

    private RecyclerView rvVouchers;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private Button btnAdd;
    private VoucherAdapter adapter;
    private List<Voucher> voucherList;
    private FirebaseFirestore db;
    private NumberFormat currencyFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_vouchers, container, false);

        db = FirebaseFirestore.getInstance();
        voucherList = new ArrayList<>();
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

        rvVouchers = view.findViewById(R.id.rvVouchers);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        btnAdd = view.findViewById(R.id.btnAdd);

        setupRecyclerView();
        loadVouchers();

        btnAdd.setOnClickListener(v -> showAddVoucherDialog());

        return view;
    }

    private void setupRecyclerView() {
        adapter = new VoucherAdapter(voucherList, new VoucherAdapter.OnVoucherActionListener() {
            @Override
            public void onEdit(Voucher voucher) {
                showEditVoucherDialog(voucher);
            }

            @Override
            public void onDelete(Voucher voucher, int position) {
                showDeleteConfirmDialog(voucher, position);
            }
        });
        rvVouchers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVouchers.setAdapter(adapter);
    }

    private void loadVouchers() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("vouchers").get()
                .addOnSuccessListener(query -> {
                    voucherList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Voucher voucher = doc.toObject(Voucher.class);
                        voucher.setId(doc.getId());
                        voucherList.add(voucher);
                    }
                    adapter.updateList(voucherList);
                    progressBar.setVisibility(View.GONE);
                    updateEmptyView();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddVoucherDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_voucher_form, null);

        EditText etCode = dialogView.findViewById(R.id.etCode);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etDiscountValue = dialogView.findViewById(R.id.etDiscountValue);
        EditText etMinOrder = dialogView.findViewById(R.id.etMinOrder);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerDiscountType);

        String[] types = {"percent", "fixed", "freeship"};
        String[] typeNames = {"Giảm theo %", "Giảm theo số tiền", "Free ship"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, typeNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(spinnerAdapter);

        builder.setTitle("Thêm voucher mới")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String code = etCode.getText().toString().trim().toUpperCase();
                    if (code.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập mã", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Voucher voucher = new Voucher();
                    voucher.setCode(code);
                    voucher.setTitle(etTitle.getText().toString().trim());
                    voucher.setDescription(etDescription.getText().toString().trim());
                    voucher.setDiscountType(types[spinnerType.getSelectedItemPosition()]);
                    voucher.setDiscountValue(Double.parseDouble(etDiscountValue.getText().toString().trim()));
                    voucher.setMinOrder(etMinOrder.getText().toString().isEmpty() ? 0 : Double.parseDouble(etMinOrder.getText().toString().trim()));
                    voucher.setActive(true);

                    db.collection("vouchers").add(voucher)
                            .addOnSuccessListener(docRef -> {
                                voucher.setId(docRef.getId());
                                voucherList.add(voucher);
                                adapter.updateList(voucherList);
                                Toast.makeText(getContext(), "Đã thêm voucher", Toast.LENGTH_SHORT).show();
                                updateEmptyView();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditVoucherDialog(Voucher voucher) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_voucher_form, null);

        EditText etCode = dialogView.findViewById(R.id.etCode);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etDiscountValue = dialogView.findViewById(R.id.etDiscountValue);
        EditText etMinOrder = dialogView.findViewById(R.id.etMinOrder);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerDiscountType);

        String[] types = {"percent", "fixed", "freeship"};
        String[] typeNames = {"Giảm theo %", "Giảm theo số tiền", "Free ship"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, typeNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(spinnerAdapter);

        etCode.setText(voucher.getCode());
        etTitle.setText(voucher.getTitle());
        etDescription.setText(voucher.getDescription());
        etDiscountValue.setText(String.valueOf(voucher.getDiscountValue()));
        etMinOrder.setText(String.valueOf(voucher.getMinOrder()));

        for (int i = 0; i < types.length; i++) {
            if (types[i].equals(voucher.getDiscountType())) {
                spinnerType.setSelection(i);
                break;
            }
        }

        builder.setTitle("Sửa voucher")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("code", etCode.getText().toString().trim().toUpperCase());
                    updates.put("title", etTitle.getText().toString().trim());
                    updates.put("description", etDescription.getText().toString().trim());
                    updates.put("discountType", types[spinnerType.getSelectedItemPosition()]);
                    updates.put("discountValue", Double.parseDouble(etDiscountValue.getText().toString().trim()));
                    updates.put("minOrder", etMinOrder.getText().toString().isEmpty() ? 0 : Double.parseDouble(etMinOrder.getText().toString().trim()));

                    db.collection("vouchers").document(voucher.getId()).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                voucher.setCode((String) updates.get("code"));
                                voucher.setTitle((String) updates.get("title"));
                                voucher.setDescription((String) updates.get("description"));
                                voucher.setDiscountType((String) updates.get("discountType"));
                                voucher.setDiscountValue((Double) updates.get("discountValue"));
                                voucher.setMinOrder((Double) updates.get("minOrder"));
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "Đã cập nhật", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteConfirmDialog(Voucher voucher, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa voucher")
                .setMessage("Bạn có chắc muốn xóa " + voucher.getCode() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("vouchers").document(voucher.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                voucherList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(getContext(), "Đã xóa voucher", Toast.LENGTH_SHORT).show();
                                updateEmptyView();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateEmptyView() {
        if (voucherList.isEmpty()) {
            rvVouchers.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvVouchers.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    // ==================== ADAPTER ====================
    static class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {
        private List<Voucher> vouchers;
        private OnVoucherActionListener listener;
        private NumberFormat f;

        interface OnVoucherActionListener {
            void onEdit(Voucher voucher);
            void onDelete(Voucher voucher, int position);
        }

        VoucherAdapter(List<Voucher> vouchers, OnVoucherActionListener listener) {
            this.vouchers = vouchers;
            this.listener = listener;
            this.f = NumberFormat.getInstance(new Locale("vi", "VN"));
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_voucher, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Voucher v = vouchers.get(position);
            holder.tvCode.setText(v.getCode());
            holder.tvTitle.setText(v.getTitle());
            holder.tvDescription.setText(v.getDescription());

            String discount = "";
            if ("percent".equals(v.getDiscountType())) {
                discount = (int) v.getDiscountValue() + "% OFF";
            } else if ("freeship".equals(v.getDiscountType())) {
                discount = "FREE SHIP";
            } else {
                discount = f.format(v.getDiscountValue()) + "đ OFF";
            }
            holder.tvDiscount.setText(discount);
            holder.tvMinOrder.setText(v.getMinOrder() > 0 ? "Đơn tối thiểu: " + f.format(v.getMinOrder()) + "đ" : "Không yêu cầu");

            holder.btnEdit.setOnClickListener(vv -> listener.onEdit(v));
            holder.btnDelete.setOnClickListener(vv -> listener.onDelete(v, position));
        }

        @Override
        public int getItemCount() {
            return vouchers.size();
        }

        void updateList(List<Voucher> newList) {
            this.vouchers = newList;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCode, tvTitle, tvDescription, tvDiscount, tvMinOrder;
            Button btnEdit, btnDelete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCode = itemView.findViewById(R.id.tvCode);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                tvDiscount = itemView.findViewById(R.id.tvDiscount);
                tvMinOrder = itemView.findViewById(R.id.tvMinOrder);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}