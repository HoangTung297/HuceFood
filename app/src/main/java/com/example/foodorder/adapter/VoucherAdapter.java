package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public VoucherAdapter(List<Voucher> voucherList) {
        this.voucherList = voucherList;
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
        holder.bind(voucher);
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    public void updateList(List<Voucher> newList) {
        this.voucherList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDiscount, tvDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvVoucherTitle);
            tvDiscount = itemView.findViewById(R.id.tvVoucherDiscount);
            tvDescription = itemView.findViewById(R.id.tvVoucherDescription);
        }

        void bind(Voucher voucher) {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

            // Title
            if (voucher.getTitle() != null) {
                tvTitle.setText(voucher.getTitle());
            } else {
                tvTitle.setText(voucher.getCode() != null ? voucher.getCode() : "Voucher");
            }

            // Discount
            if ("percent".equals(voucher.getDiscountType())) {
                tvDiscount.setText("-" + (int) voucher.getDiscountValue() + "%");
            } else if ("freeship".equals(voucher.getDiscountType())) {
                tvDiscount.setText("FREE SHIP");
            } else {
                tvDiscount.setText("-" + formatter.format(voucher.getDiscountValue()) + "đ");
            }

            // Description
            if (voucher.getDescription() != null && !voucher.getDescription().isEmpty()) {
                tvDescription.setText(voucher.getDescription());
            } else {
                if ("percent".equals(voucher.getDiscountType())) {
                    tvDescription.setText("Giảm " + (int) voucher.getDiscountValue() + "% cho đơn từ " + formatter.format(voucher.getMinOrder()) + "đ");
                } else if ("freeship".equals(voucher.getDiscountType())) {
                    tvDescription.setText("Miễn phí ship cho đơn từ " + formatter.format(voucher.getMinOrder()) + "đ");
                } else {
                    tvDescription.setText("Giảm " + formatter.format(voucher.getDiscountValue()) + "đ cho đơn từ " + formatter.format(voucher.getMinOrder()) + "đ");
                }
            }
        }
    }
}