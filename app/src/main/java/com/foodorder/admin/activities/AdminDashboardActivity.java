package com.foodorder.admin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.foodorder.admin.R;
import com.foodorder.admin.fragments.DashboardFragment;
import com.foodorder.admin.fragments.ManageFoodsFragment;
import com.foodorder.admin.fragments.ManageOrdersFragment;
import com.foodorder.admin.fragments.ManageRestaurantsFragment;
import com.foodorder.admin.fragments.ManageUsersFragment;
import com.foodorder.admin.fragments.ManageVouchersFragment;
import com.google.android.material.navigation.NavigationView;
import com.foodorder.admin.fragments.SendNotificationFragment;
public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_drawer);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Mặc định hiển thị Dashboard
        loadFragment(new DashboardFragment());
        navigationView.setCheckedItem(R.id.nav_dashboard);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                Fragment fragment = null;

                if (id == R.id.nav_dashboard) {
                    fragment = new DashboardFragment();
                } else if (id == R.id.nav_users) {
                    fragment = new ManageUsersFragment();  // ← ĐÃ SỬA
                } else if (id == R.id.nav_restaurants) {
                    fragment = new ManageRestaurantsFragment();
                } else if (id == R.id.nav_notifications) {
                    fragment = new SendNotificationFragment();
                }
                else if (id == R.id.nav_foods) {
                    fragment = new ManageFoodsFragment();
                } else if (id == R.id.nav_orders) {
                    fragment = new ManageOrdersFragment();
                } else if (id == R.id.nav_vouchers) {
                    fragment = new ManageVouchersFragment();
                } else if (id == R.id.nav_notifications) {
                    fragment = new DashboardFragment(); // Tạm thời
                } else if (id == R.id.nav_logout) {
                    showLogoutDialog();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }

                if (fragment != null) {
                    loadFragment(fragment);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameContainer, fragment);
        transaction.commit();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    getSharedPreferences("AdminPrefs", MODE_PRIVATE).edit().clear().apply();
                    startActivity(new Intent(this, AdminLoginActivity.class));
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}