package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Voucher;
import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<Voucher> voucherList;

    public VoucherAdapter(List<Voucher> voucherList) {
        this.voucherList = voucherList;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.tvDiscount.setText(voucher.getDiscount());
        holder.tvTitle.setText(voucher.getTitle());
        holder.tvDescription.setText(voucher.getDescription());
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvDiscount, tvTitle, tvDescription;

        VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDiscount = itemView.findViewById(R.id.tvVoucherDiscount);
            tvTitle = itemView.findViewById(R.id.tvVoucherTitle);
            tvDescription = itemView.findViewById(R.id.tvVoucherDescription);
        }
    }
}