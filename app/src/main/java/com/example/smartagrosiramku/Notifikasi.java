package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class Notifikasi extends AppCompatActivity {

    // Deklarasi View
    private TextView tabSemua, tabBelumDibaca;
    private TextView tvDashboard, tvHistory, tvControl, tvAccount;
    private ImageView btnBack;

    // Tombol aksi
    private TextView btnTandaiDibaca1, btnHapus1;
    private TextView btnTandaiDibaca2, btnHapus2;
    private TextView btnHapus3;

    // Notifikasi cards
    private LinearLayout notifikasi1, notifikasi2, notifikasi3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifikasi);

        // Inisialisasi Views
        initializeViews();

        // Setup listeners
        setupListeners();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void initializeViews() {
        // Inisialisasi Tab
        tabSemua = findViewById(R.id.tabSemua);
        tabBelumDibaca = findViewById(R.id.tabBelumDibaca);
        
        // Inisialisasi Tombol Kembali
        btnBack = findViewById(R.id.btnBack);

        // Inisialisasi Tombol Aksi
        btnTandaiDibaca1 = findViewById(R.id.btnTandaiDibaca1);
        btnHapus1 = findViewById(R.id.btnHapus1);
        btnTandaiDibaca2 = findViewById(R.id.btnTandaiDibaca2);
        btnHapus2 = findViewById(R.id.btnHapus2);
        btnHapus3 = findViewById(R.id.btnHapus3);

        // Inisialisasi CardView Notifikasi
        notifikasi1 = findViewById(R.id.notifikasi1);
        notifikasi2 = findViewById(R.id.notifikasi2);
        notifikasi3 = findViewById(R.id.notifikasi3);

        // Inisialisasi Bottom Navigation
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory = findViewById(R.id.tvHistory);
        tvControl = findViewById(R.id.tvControl);
        tvAccount = findViewById(R.id.tvAccount);
    }

    private void setupListeners() {
        // Listener untuk Tombol Kembali
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Listener untuk Tab Semua
        if (tabSemua != null) {
            tabSemua.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Ubah style tab
                    tabSemua.setBackgroundColor(ContextCompat.getColor(Notifikasi.this, R.color.light_green));
                    tabSemua.setTextColor(ContextCompat.getColor(Notifikasi.this, R.color.primary_green));
                    if (tabBelumDibaca != null) {
                        tabBelumDibaca.setBackgroundColor(ContextCompat.getColor(Notifikasi.this, android.R.color.white));
                        tabBelumDibaca.setTextColor(ContextCompat.getColor(Notifikasi.this, R.color.text_secondary));
                    }
                    showAllNotifications();
                    showToast("Menampilkan semua notifikasi");
                }
            });
        }

        // Listener untuk Tab Belum Dibaca
        if (tabBelumDibaca != null) {
            tabBelumDibaca.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Ubah style tab
                    tabBelumDibaca.setBackgroundColor(ContextCompat.getColor(Notifikasi.this, R.color.light_green));
                    tabBelumDibaca.setTextColor(ContextCompat.getColor(Notifikasi.this, R.color.primary_green));
                    if (tabSemua != null) {
                        tabSemua.setBackgroundColor(ContextCompat.getColor(Notifikasi.this, android.R.color.white));
                        tabSemua.setTextColor(ContextCompat.getColor(Notifikasi.this, R.color.text_secondary));
                    }
                    showUnreadNotifications();
                    showToast("Menampilkan notifikasi belum dibaca");
                }
            });
        }

        // Setup listeners untuk tombol hapus dan tandai dibaca
        setupActionListeners();
    }

    private void setupActionListeners() {
        if (btnTandaiDibaca1 != null) {
            btnTandaiDibaca1.setOnClickListener(v -> { markAsRead(notifikasi1); showToast("Notifikasi ditandai sudah dibaca"); });
        }
        if (btnHapus1 != null) {
            btnHapus1.setOnClickListener(v -> { deleteNotification(notifikasi1); showToast("Notifikasi dihapus"); });
        }
        if (btnTandaiDibaca2 != null) {
            btnTandaiDibaca2.setOnClickListener(v -> { markAsRead(notifikasi2); showToast("Notifikasi ditandai sudah dibaca"); });
        }
        if (btnHapus2 != null) {
            btnHapus2.setOnClickListener(v -> { deleteNotification(notifikasi2); showToast("Notifikasi dihapus"); });
        }
        if (btnHapus3 != null) {
            btnHapus3.setOnClickListener(v -> { deleteNotification(notifikasi3); showToast("Notifikasi dihapus"); });
        }
    }

    private void setupBottomNavigation() {
        if (tvDashboard != null) {
            tvDashboard.setOnClickListener(v -> {
                startActivity(new Intent(Notifikasi.this, Dashboard.class));
                finish();
            });
        }
        if (tvHistory != null) {
            tvHistory.setOnClickListener(v -> {
                startActivity(new Intent(Notifikasi.this, HistoryActivity.class));
                finish();
            });
        }
        if (tvControl != null) {
            tvControl.setOnClickListener(v -> {
                startActivity(new Intent(Notifikasi.this, ControlActivity.class));
                finish();
            });
        }
        if (tvAccount != null) {
            tvAccount.setOnClickListener(v -> {
                startActivity(new Intent(Notifikasi.this, AccountActivity.class));
                finish();
            });
        }
    }

    private void markAsRead(LinearLayout notification) {
        if (notification != null) {
            notification.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    private void deleteNotification(LinearLayout notification) {
        if (notification != null) {
            notification.setVisibility(View.GONE);
        }
    }

    private void showAllNotifications() {
        if (notifikasi1 != null) notifikasi1.setVisibility(View.VISIBLE);
        if (notifikasi2 != null) notifikasi2.setVisibility(View.VISIBLE);
        if (notifikasi3 != null) notifikasi3.setVisibility(View.VISIBLE);
    }

    private void showUnreadNotifications() {
        if (notifikasi1 != null) notifikasi1.setVisibility(View.VISIBLE);
        if (notifikasi2 != null) notifikasi2.setVisibility(View.VISIBLE);
        if (notifikasi3 != null) notifikasi3.setVisibility(View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
