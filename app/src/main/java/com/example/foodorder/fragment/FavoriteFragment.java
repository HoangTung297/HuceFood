package com.example.foodorder.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.foodorder.R;

public class FavoriteFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText("Yêu Quán từ món đầu tiên\n\nMón ngon hấp dẫn chiếm trọn trái tim! Thả tim ngay để lưu quán bạn yêu nhé.");

        return view;
    }
}