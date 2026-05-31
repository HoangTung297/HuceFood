package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.AddressSuggestion;
import java.util.List;

public class AddressSuggestionAdapter extends RecyclerView.Adapter<AddressSuggestionAdapter.ViewHolder> {

    private List<AddressSuggestion> suggestions;
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(AddressSuggestion address);
    }

    public AddressSuggestionAdapter(List<AddressSuggestion> suggestions, OnAddressSelectedListener listener) {
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AddressSuggestion address = suggestions.get(position);
        holder.tvAddress.setText(address.getFullAddress());
        holder.tvShortAddress.setText(address.getShortAddress());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddressSelected(address);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void updateList(List<AddressSuggestion> newList) {
        this.suggestions = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddress, tvShortAddress;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvShortAddress = itemView.findViewById(R.id.tvShortAddress);
        }
    }
}