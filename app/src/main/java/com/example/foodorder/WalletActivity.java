package com.example.foodorder;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorder.fragment.WalletFragment;

public class WalletActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.wallet_container, new WalletFragment())
                    .commit();
        }
    }
}