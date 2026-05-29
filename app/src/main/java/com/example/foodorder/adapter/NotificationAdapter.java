package com.example.foodorder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.foodorder.R;
import com.example.foodorder.model.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;
    private OnNotificationDeleteListener deleteListener;
    private boolean isSelectionMode = false;
    private List<Integer> selectedPositions = new ArrayList<>();

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public interface OnNotificationDeleteListener {
        void onDeleteClick(Notification notification, int position);
        void onDeleteMultiple(List<Notification> notifications);
    }

    public NotificationAdapter(List<Notification> notifications,
                               OnNotificationClickListener listener,
                               OnNotificationDeleteListener deleteListener) {
        this.notifications = notifications;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, position, isSelectionMode, selectedPositions.contains(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateList(List<Notification> newList) {
        this.notifications = newList;
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean enabled) {
        this.isSelectionMode = enabled;
        if (!enabled) {
            selectedPositions.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void toggleSelection(int position) {
        if (selectedPositions.contains(position)) {
            selectedPositions.remove(Integer.valueOf(position));
        } else {
            selectedPositions.add(position);
        }
        notifyItemChanged(position);
    }

    public int getSelectedCount() {
        return selectedPositions.size();
    }

    public List<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardNotification;
        private ImageView ivIcon, ivImage;
        private TextView tvTitle, tvMessage, tvTime, tvTypeIcon;
        private View vSelectionOverlay;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNotification = itemView.findViewById(R.id.cardNotification);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTypeIcon = itemView.findViewById(R.id.tvTypeIcon);
            vSelectionOverlay = itemView.findViewById(R.id.vSelectionOverlay);
        }

        void bind(Notification notification, int position, boolean selectionMode, boolean isSelected) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            tvTime.setText(notification.getTimeAgo());

            tvTypeIcon.setVisibility(View.VISIBLE);
            tvTypeIcon.setText(notification.getTypeIcon());
            ivIcon.setVisibility(View.GONE);

            if (notification.getImageUrl() != null && !notification.getImageUrl().isEmpty()) {
                ivImage.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(notification.getImageUrl())
                        .placeholder(R.drawable.ic_food_default)
                        .into(ivImage);
            } else {
                ivImage.setVisibility(View.GONE);
            }

            if (!notification.isRead()) {
                cardNotification.setCardBackgroundColor(
                        itemView.getContext().getColor(R.color.notification_unread_bg));
                tvTitle.setTypeface(tvTitle.getTypeface(), android.graphics.Typeface.BOLD);
            } else {
                cardNotification.setCardBackgroundColor(
                        itemView.getContext().getColor(android.R.color.white));
                tvTitle.setTypeface(tvTitle.getTypeface(), android.graphics.Typeface.NORMAL);
            }

            if (selectionMode) {
                vSelectionOverlay.setVisibility(View.VISIBLE);
                vSelectionOverlay.setBackgroundColor(isSelected ?
                        itemView.getContext().getColor(R.color.selection_overlay) :
                        itemView.getContext().getColor(android.R.color.transparent));
            } else {
                vSelectionOverlay.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (selectionMode) {
                    toggleSelection(position);
                } else {
                    if (!notification.isRead()) {
                        notification.setRead(true);
                        notifyItemChanged(position);
                    }
                    if (listener != null) {
                        listener.onNotificationClick(notification);
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (!selectionMode) {
                    setSelectionMode(true);
                    toggleSelection(position);
                    return true;
                }
                return false;
            });
        }
    }
}