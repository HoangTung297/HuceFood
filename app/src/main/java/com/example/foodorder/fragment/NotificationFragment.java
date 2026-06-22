package com.example.foodorder.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorder.R;
import com.example.foodorder.adapter.NotificationAdapter;
import com.example.foodorder.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationFragment";
    public static final String ACTION_REFRESH_NOTIFICATIONS = "REFRESH_NOTIFICATIONS";

    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private TextView tvEmptyMessage;
    private Button btnMarkAllRead, btnDeleteSelected, btnCancelSelection;
    private LinearLayout layoutSelectionBar;

    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private String userId = "";
    private String currentFilter = "all";
    private View rootView;

    // Broadcast receiver để refresh khi có thông báo mới
    private final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_REFRESH_NOTIFICATIONS.equals(intent.getAction())) {
                Log.d(TAG, "📡 Received refresh broadcast, reloading notifications...");
                loadNotifications();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notification, container, false);

        // Đăng ký BroadcastReceiver
        IntentFilter filter = new IntentFilter(ACTION_REFRESH_NOTIFICATIONS);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(refreshReceiver, filter);
        Log.d(TAG, "📡 Registered BroadcastReceiver for " + ACTION_REFRESH_NOTIFICATIONS);

        loadUserId();
        initViews(rootView);
        setupRecyclerView();
        setupFilterTabs(rootView);
        loadNotifications();

        return rootView;
    }

    /**
     * Lấy userId từ email của user
     */
    private void loadUserId() {
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);

            // Ưu tiên lấy user_email làm userId
            String email = prefs.getString("user_email", "");
            String id = prefs.getString("user_id", "");

            if (email != null && !email.isEmpty()) {
                userId = email;
            } else if (id != null && !id.isEmpty() && !id.equals("user123")) {
                userId = id;
            } else {
                userId = "tung@gmail.com";
            }

            Log.d(TAG, "📧 Loaded userId from email: " + userId);
        }

        if (userId == null || userId.isEmpty()) {
            userId = "tung@gmail.com";
        }
    }

    /**
     * Lấy userId hiện tại (email)
     */
    private String getCurrentUserId() {
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", 0);
            String email = prefs.getString("user_email", "");

            // Ưu tiên dùng email làm userId
            if (email != null && !email.isEmpty()) {
                return email;
            }

            String id = prefs.getString("user_id", "");
            if (id != null && !id.isEmpty() && !id.equals("user123")) {
                return id;
            }
        }
        return "tung@gmail.com";
    }

    private void initViews(View view) {
        rvNotifications = view.findViewById(R.id.rvNotifications);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);
        btnDeleteSelected = view.findViewById(R.id.btnDeleteSelected);
        btnCancelSelection = view.findViewById(R.id.btnCancelSelection);
        layoutSelectionBar = view.findViewById(R.id.layoutSelectionBar);

        db = FirebaseFirestore.getInstance();
        notificationList = new ArrayList<>();

        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
        btnDeleteSelected.setOnClickListener(v -> deleteSelectedNotifications());
        btnCancelSelection.setOnClickListener(v -> disableSelectionMode());
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notificationList,
                this::onNotificationClick,
                new NotificationAdapter.OnNotificationDeleteListener() {
                    @Override
                    public void onDeleteClick(Notification notification, int position) {
                        deleteNotification(notification, position);
                    }

                    @Override
                    public void onDeleteMultiple(List<Notification> notifications) {
                        deleteMultipleNotifications(notifications);
                    }
                });
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(adapter);
    }

    private void setupFilterTabs(View view) {
        Button btnAll = view.findViewById(R.id.btnFilterAll);
        Button btnOrder = view.findViewById(R.id.btnFilterOrder);
        Button btnPromotion = view.findViewById(R.id.btnFilterPromotion);

        if (btnAll == null || btnOrder == null || btnPromotion == null) {
            return;
        }

        View.OnClickListener filterListener = v -> {
            currentFilter = (String) v.getTag();
            loadNotifications();
            updateFilterButtonStyle(v);
        };

        btnAll.setOnClickListener(filterListener);
        btnOrder.setOnClickListener(filterListener);
        btnPromotion.setOnClickListener(filterListener);

        updateFilterButtonStyle(btnAll);
    }

    private void updateFilterButtonStyle(View selectedButton) {
        if (rootView == null || getContext() == null) return;

        Button btnAll = rootView.findViewById(R.id.btnFilterAll);
        Button btnOrder = rootView.findViewById(R.id.btnFilterOrder);
        Button btnPromotion = rootView.findViewById(R.id.btnFilterPromotion);

        Button[] buttons = {btnAll, btnOrder, btnPromotion};

        for (Button btn : buttons) {
            if (btn != null) {
                btn.setBackgroundResource(R.drawable.bg_filter_button);
                btn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            }
        }

        if (selectedButton != null && selectedButton instanceof Button) {
            Button selectedBtn = (Button) selectedButton;
            selectedBtn.setBackgroundResource(R.drawable.bg_filter_button_selected);
            selectedBtn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        }
    }

    /**
     * Load thông báo - KHÔNG dùng orderBy để tránh lỗi index
     */
    private void loadNotifications() {
        String currentUserId = getCurrentUserId();

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "❌ userId is null or empty!");
            Toast.makeText(getContext(), "Chưa có thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "📧 Loading notifications for userId: " + currentUserId);

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Tạo query với userId là email
        Query query = db.collection("notifications")
                .whereEqualTo("userId", currentUserId);

        if (!"all".equals(currentFilter)) {
            query = query.whereEqualTo("type", currentFilter);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();

                    Log.d(TAG, "📦 Found " + queryDocumentSnapshots.size() + " notifications for user: " + currentUserId);

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = new Notification();
                        notification.setId(doc.getId());
                        notification.setUserId(doc.getString("userId"));
                        notification.setTitle(doc.getString("title"));
                        notification.setMessage(doc.getString("message"));
                        notification.setType(doc.getString("type"));

                        Long createdAt = doc.getLong("createdAt");
                        notification.setCreatedAt(createdAt != null ? createdAt : System.currentTimeMillis());

                        notification.setRead(doc.getBoolean("isRead") != null && doc.getBoolean("isRead"));
                        notification.setOrderId(doc.getString("orderId"));
                        notification.setImageUrl(doc.getString("imageUrl"));
                        notificationList.add(notification);

                        Log.d(TAG, "📄 Notification: " + notification.getTitle() + " | userId: " + notification.getUserId());
                    }

                    // Sắp xếp thủ công theo createdAt (mới nhất lên đầu)
                    Collections.sort(notificationList, (o1, o2) ->
                            Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));

                    Log.d(TAG, "✅ After sorting, " + notificationList.size() + " notifications loaded");

                    updateUI();
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.e(TAG, "❌ Error loading notifications: " + e.getMessage());

                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("FAILED_PRECONDITION")) {
                        Toast.makeText(getContext(),
                                "Lỗi: Cần tạo index trên Firebase. Vui lòng liên hệ quản trị viên.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi tải thông báo: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        int unreadCount = 0;
        for (Notification n : notificationList) {
            if (!n.isRead()) unreadCount++;
        }

        Log.d(TAG, "📊 updateUI: " + notificationList.size() + " notifications, " + unreadCount + " unread");

        if (btnMarkAllRead != null) {
            btnMarkAllRead.setText(unreadCount > 0 ?
                    "Đánh dấu đã đọc (" + unreadCount + ")" : "Đánh dấu đã đọc");
            btnMarkAllRead.setEnabled(unreadCount > 0);
        }

        if (notificationList.isEmpty()) {
            if (rvNotifications != null) rvNotifications.setVisibility(View.GONE);
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);

            String message = "";
            switch (currentFilter) {
                case "order": message = "Chưa có thông báo về đơn hàng"; break;
                case "promotion": message = "Chưa có thông báo khuyến mãi"; break;
                default: message = "Bạn chưa có thông báo nào";
            }
            if (tvEmptyMessage != null) tvEmptyMessage.setText(message);
        } else {
            if (rvNotifications != null) rvNotifications.setVisibility(View.VISIBLE);
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
            if (adapter != null) {
                adapter.updateList(notificationList);
                Log.d(TAG, "🔄 Updated adapter with " + notificationList.size() + " items");
            }
        }
    }

    private void onNotificationClick(Notification notification) {
        if (getContext() == null) return;

        // Đánh dấu là đã đọc khi click
        if (!notification.isRead()) {
            db.collection("notifications").document(notification.getId())
                    .update("isRead", true)
                    .addOnSuccessListener(aVoid -> {
                        notification.setRead(true);
                        if (adapter != null) adapter.updateList(notificationList);
                        updateUI();
                    });
        }

        if (notification.getType().equals("order") && notification.getOrderId() != null) {
            Toast.makeText(getContext(), "Đơn hàng: " + notification.getOrderId(), Toast.LENGTH_LONG).show();
        } else if (notification.getType().equals("promotion")) {
            Toast.makeText(getContext(), notification.getTitle(), Toast.LENGTH_LONG).show();
        }
    }

    private void markAllAsRead() {
        if (getContext() == null) return;

        for (Notification notification : notificationList) {
            if (!notification.isRead()) {
                db.collection("notifications").document(notification.getId())
                        .update("isRead", true);
            }
        }

        for (Notification n : notificationList) {
            n.setRead(true);
        }
        if (adapter != null) adapter.updateList(notificationList);
        Toast.makeText(getContext(), "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show();
        updateUI();
    }

    private void enableSelectionMode() {
        if (adapter != null) adapter.setSelectionMode(true);
        if (layoutSelectionBar != null) layoutSelectionBar.setVisibility(View.VISIBLE);
        updateSelectionCount();
    }

    private void disableSelectionMode() {
        if (adapter != null) adapter.setSelectionMode(false);
        if (layoutSelectionBar != null) layoutSelectionBar.setVisibility(View.GONE);
    }

    private void updateSelectionCount() {
        if (rootView == null || adapter == null) return;
        TextView tvSelectionCount = rootView.findViewById(R.id.tvSelectionCount);
        if (tvSelectionCount != null) {
            tvSelectionCount.setText("Đã chọn " + adapter.getSelectedCount() + " thông báo");
        }
    }

    private void deleteSelectedNotifications() {
        if (getContext() == null || adapter == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc muốn xóa " + adapter.getSelectedCount() + " thông báo?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    for (int pos : adapter.getSelectedPositions()) {
                        Notification notification = notificationList.get(pos);
                        db.collection("notifications").document(notification.getId()).delete();
                    }

                    List<Notification> newList = new ArrayList<>();
                    for (int i = 0; i < notificationList.size(); i++) {
                        if (!adapter.getSelectedPositions().contains(i)) {
                            newList.add(notificationList.get(i));
                        }
                    }
                    notificationList = newList;
                    if (adapter != null) adapter.updateList(notificationList);
                    disableSelectionMode();
                    updateUI();
                    Toast.makeText(getContext(), "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteNotification(Notification notification, int position) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc muốn xóa thông báo này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("notifications").document(notification.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                notificationList.remove(position);
                                if (adapter != null) adapter.updateList(notificationList);
                                updateUI();
                                Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteMultipleNotifications(List<Notification> toDelete) {
        for (Notification n : toDelete) {
            db.collection("notifications").document(n.getId()).delete();
        }
        loadNotifications();
        disableSelectionMode();
        if (getContext() != null) {
            Toast.makeText(getContext(), "Đã xóa " + toDelete.size() + " thông báo", Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshData() {
        loadNotifications();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(refreshReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
    }
}