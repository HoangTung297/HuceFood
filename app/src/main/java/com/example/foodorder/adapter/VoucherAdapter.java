package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Voucher;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

    private List<Voucher> voucherList;
    private OnVoucherSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnVoucherSelectedListener {
        void onVoucherSelected(Voucher voucher);
    }

    public VoucherAdapter(List<Voucher> voucherList) {
        this.voucherList = voucherList;
        this.listener = null;
    }

    public VoucherAdapter(List<Voucher> voucherList, OnVoucherSelectedListener listener) {
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.bind(voucher, listener, position);

        // Hiển thị radio button được chọn
        holder.rbSelect.setChecked(position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public void updateList(List<Voucher> newList) {
        this.voucherList = newList;
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDiscount, tvCode;
        RadioButton rbSelect;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvVoucherTitle);
            tvDescription = itemView.findViewById(R.id.tvVoucherDescription);
            tvDiscount = itemView.findViewById(R.id.tvVoucherDiscount);
            tvCode = itemView.findViewById(R.id.tvVoucherCode);
            rbSelect = itemView.findViewById(R.id.rbSelectVoucher);
        }

        void bind(Voucher voucher, OnVoucherSelectedListener listener, int position) {
            NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));

            // Title
            if (voucher.getTitle() != null) {
                tvTitle.setText(voucher.getTitle());
            } else {
                tvTitle.setText(voucher.getCode());
            }

            // Discount
            if ("percent".equals(voucher.getDiscountType())) {
                tvDiscount.setText("-" + (int) voucher.getDiscountValue() + "%");
            } else if ("freeship".equals(voucher.getDiscountType())) {
                tvDiscount.setText("FREE");
            } else {
                tvDiscount.setText("-" + f.format(voucher.getDiscountValue()) + "đ");
            }

            // Description
            if (voucher.getDescription() != null && !voucher.getDescription().isEmpty()) {
                tvDescription.setText(voucher.getDescription());
            } else {
                if ("percent".equals(voucher.getDiscountType())) {
                    tvDescription.setText("Giảm " + (int) voucher.getDiscountValue() + "% cho đơn từ " + f.format(voucher.getMinOrder()) + "đ");
                } else if ("freeship".equals(voucher.getDiscountType())) {
                    tvDescription.setText("Miễn phí ship cho đơn từ " + f.format(voucher.getMinOrder()) + "đ");
                } else {
                    tvDescription.setText("Giảm " + f.format(voucher.getDiscountValue()) + "đ cho đơn từ " + f.format(voucher.getMinOrder()) + "đ");
                }
            }

            // Code
            tvCode.setText("Mã: " + voucher.getCode());

            // Radio button click
            rbSelect.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVoucherSelected(voucher);
                }
            });

            // Item click
            itemView.setOnClickListener(v -> {
                rbSelect.performClick();
            });
        }
    }
}