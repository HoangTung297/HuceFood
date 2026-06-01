package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.BankAccount;
import java.util.List;

public class BankAccountAdapter extends RecyclerView.Adapter<BankAccountAdapter.ViewHolder> {

    private List<BankAccount> accounts;
    private OnBankAccountClickListener listener;

    public interface OnBankAccountClickListener {
        void onBankAccountClick(BankAccount account);
        void onDeleteClick(BankAccount account);
    }

    public BankAccountAdapter(List<BankAccount> accounts, OnBankAccountClickListener listener) {
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
        holder.ivDelete.setOnClickListener(v -> listener.onDeleteClick(account));
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBankName, tvAccountNumber, tvAccountHolder;
        ImageView ivDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBankName = itemView.findViewById(R.id.tvBankName);
            tvAccountNumber = itemView.findViewById(R.id.tvAccountNumber);
            tvAccountHolder = itemView.findViewById(R.id.tvAccountHolder);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}