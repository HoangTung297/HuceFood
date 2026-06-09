package com.foodorder.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.foodorder.admin.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendNotificationFragment extends Fragment {

    private Spinner spinnerUserType;
    private EditText etTitle, etMessage;
    private Button btnSend;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_notification, container, false);

        db = FirebaseFirestore.getInstance();

        spinnerUserType = view.findViewById(R.id.spinnerUserType);
        etTitle = view.findViewById(R.id.etTitle);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        progressBar = view.findViewById(R.id.progressBar);

        // Thiết lập Spinner
        String[] userTypes = {"Tất cả người dùng", "Chỉ User thường", "Chỉ Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserType.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendNotification());

        return view;
    }

    private void sendNotification() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        int typePos = spinnerUserType.getSelectedItemPosition();

        if (title.isEmpty()) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }
        if (message.isEmpty()) {
            etMessage.setError("Vui lòng nhập nội dung");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSend.setEnabled(false);

        // Xác định role cần gửi
        String targetRole = null;
        if (typePos == 1) targetRole = "user";
        else if (typePos == 2) targetRole = "admin";

        // Truy vấn users theo role
        com.google.firebase.firestore.Query query = db.collection("users");
        if (targetRole != null) {
            query = query.whereEqualTo("role", targetRole);
        }

        query.get()
                .addOnSuccessListener(users -> {
                    List<String> userIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : users) {
                        userIds.add(doc.getId());
                    }

                    if (userIds.isEmpty()) {
                        Toast.makeText(getContext(), "Không có người dùng nào để gửi", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        btnSend.setEnabled(true);
                        return;
                    }

                    // Gửi thông báo đến từng user
                    for (String userId : userIds) {
                        Map<String, Object> notification = new HashMap<>();
                        notification.put("userId", userId);
                        notification.put("title", title);
                        notification.put("message", message);
                        notification.put("type", "promotion");
                        notification.put("createdAt", System.currentTimeMillis());
                        notification.put("isRead", false);

                        db.collection("notifications").add(notification);
                    }

                    Toast.makeText(getContext(), "Đã gửi thông báo đến " + userIds.size() + " người dùng", Toast.LENGTH_LONG).show();

                    // Reset form
                    etTitle.setText("");
                    etMessage.setText("");
                    progressBar.setVisibility(View.GONE);
                    btnSend.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSend.setEnabled(true);
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}