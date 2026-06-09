package com.foodorder.admin.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.foodorder.admin.R;
import com.foodorder.admin.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageUsersFragment extends Fragment {

    private RecyclerView rvUsers;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private UserAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_users, container, false);

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        rvUsers = view.findViewById(R.id.rvUsers);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);

        setupRecyclerView();
        loadUsers();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(userList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onResetPassword(User user) {
                showResetPasswordDialog(user);
            }

            @Override
            public void onDeleteUser(User user, int position) {
                showDeleteConfirmDialog(user, position);
            }
        });
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsers.setAdapter(adapter);
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").get()
                .addOnSuccessListener(query -> {
                    userList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        User user = doc.toObject(User.class);
                        user.setId(doc.getId());
                        userList.add(user);
                    }
                    adapter.updateList(userList);
                    progressBar.setVisibility(View.GONE);
                    updateEmptyView();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showResetPasswordDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reset_password, null);

        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);

        builder.setTitle("Đặt lại mật khẩu cho " + user.getName())
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newPassword = etNewPassword.getText().toString().trim();
                    if (newPassword.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPassword.length() < 6) {
                        Toast.makeText(getContext(), "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    resetPassword(user, newPassword);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void resetPassword(User user, String newPassword) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("password", newPassword);

        db.collection("users").document(user.getId()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã đặt lại mật khẩu cho " + user.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmDialog(User user, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa người dùng")
                .setMessage("Bạn có chắc muốn xóa người dùng " + user.getName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("users").document(user.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                userList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(getContext(), "Đã xóa người dùng", Toast.LENGTH_SHORT).show();
                                updateEmptyView();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateEmptyView() {
        if (userList.isEmpty()) {
            rvUsers.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvUsers.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    // ==================== ADAPTER ====================
    static class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private List<User> users;
        private OnUserActionListener listener;

        interface OnUserActionListener {
            void onResetPassword(User user);
            void onDeleteUser(User user, int position);
        }

        UserAdapter(List<User> users, OnUserActionListener listener) {
            this.users = users;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = users.get(position);
            holder.tvName.setText(user.getName());
            holder.tvEmail.setText(user.getEmail());
            holder.tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa cập nhật");

            // Hiển thị role: admin hoặc user
            if ("admin".equals(user.getRole())) {
                holder.tvRole.setText("👑 Admin");
            } else {
                holder.tvRole.setText("👤 User");
            }

            holder.btnResetPassword.setOnClickListener(v -> listener.onResetPassword(user));
            holder.btnDelete.setOnClickListener(v -> listener.onDeleteUser(user, position));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        void updateList(List<User> newList) {
            this.users = newList;
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvPhone, tvRole;
            Button btnResetPassword, btnDelete;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvRole = itemView.findViewById(R.id.tvRole);
                btnResetPassword = itemView.findViewById(R.id.btnResetPassword);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}