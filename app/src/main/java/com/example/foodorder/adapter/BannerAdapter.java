package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.model.Banner;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<Banner> bannerList;

    public BannerAdapter(List<Banner> bannerList) {
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = bannerList.get(position);

        // Hiển thị tiêu đề
        holder.tvTitle.setText(banner.getTitle());

        // Hiển thị ảnh theo imageUrl
        String imageUrl = banner.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Thử tìm ảnh trong drawable theo tên
            int resId = holder.itemView.getContext().getResources()
                    .getIdentifier(imageUrl, "drawable", holder.itemView.getContext().getPackageName());

            if (resId != 0) {
                holder.ivBanner.setImageResource(resId);
            } else {
                // Ảnh mặc định nếu không tìm thấy
                holder.ivBanner.setImageResource(R.drawable.ic_food_default);
            }
        } else {
            holder.ivBanner.setImageResource(R.drawable.ic_food_default);
        }
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        TextView tvTitle;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivBanner);
            tvTitle = itemView.findViewById(R.id.tvBannerTitle);
        }
    }
}