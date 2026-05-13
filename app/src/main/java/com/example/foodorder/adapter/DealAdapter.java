package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Deal;
import java.util.List;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.ViewHolder> {

    private List<Deal> dealList;

    public DealAdapter(List<Deal> dealList) {
        this.dealList = dealList;
    }

    public void updateList(List<Deal> newList) {
        this.dealList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Deal deal = dealList.get(position);
        holder.tvDealName.setText(deal.getName());
        holder.tvDiscount.setText(deal.getDiscount());
        holder.tvCount.setText(deal.getCount());
        holder.tvDate.setText(deal.getDate());
    }

    @Override
    public int getItemCount() {
        return dealList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDealName, tvDiscount, tvCount, tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDealName = itemView.findViewById(R.id.tvDealName);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}