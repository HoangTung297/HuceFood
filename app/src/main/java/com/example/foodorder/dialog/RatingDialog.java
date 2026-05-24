package com.example.foodorder.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import com.example.foodorder.R;
import com.example.foodorder.model.Order;

public class RatingDialog {

    public interface RatingCallback {
        void onRatingSubmitted(float rating, String comment);
    }

    public static void show(Context context, Order order, String userId, RatingCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText etComment = view.findViewById(R.id.etComment);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);
        // Bỏ btnCancel vì có thể layout không có

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true); // Cho phép bấm outside để đóng

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString();

            if (rating == 0) {
                Toast.makeText(context, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                return;
            }

            if (callback != null) {
                callback.onRatingSubmitted(rating, comment);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}