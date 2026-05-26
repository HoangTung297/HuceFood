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
        void onVoucherSelected(Voucher voucher, int position, boolean isSelected);
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
        holder.bind(voucher, position);
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

    public int getSelectedPosition() {
        return selectedPosition;
    }

    // ==================== VIEW HOLDER ====================
    class ViewHolder extends RecyclerView.ViewHolder {
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

        void bind(Voucher voucher, int position) {
            NumberFormat f = NumberFormat.getInstance(new Locale("vi", "VN"));

            tvTitle.setText(voucher.getTitle() != null ? voucher.getTitle() : voucher.getCode());

            if ("percent".equals(voucher.getDiscountType())) {
                tvDiscount.setText("-" + (int) voucher.getDiscountValue() + "%");
            } else if ("freeship".equals(voucher.getDiscountType())) {
                tvDiscount.setText("FREE SHIP");
            } else {
                tvDiscount.setText("-" + f.format(voucher.getDiscountValue()) + "đ");
            }

            tvDescription.setText(voucher.getDescription());
            tvCode.setText("Mã: " + voucher.getCode());

            // Xử lý click - có thể truy cập biến selectedPosition của class bên ngoài
            rbSelect.setOnClickListener(v -> {
                boolean isSelected;
                if (selectedPosition == position) {
                    selectedPosition = -1;
                    isSelected = false;
                } else {
                    selectedPosition = position;
                    isSelected = true;
                }
                notifyDataSetChanged();

                if (listener != null) {
                    listener.onVoucherSelected(voucher, position, isSelected);
                }
            });

            itemView.setOnClickListener(v -> {
                rbSelect.performClick();
            });
        }
    }
}