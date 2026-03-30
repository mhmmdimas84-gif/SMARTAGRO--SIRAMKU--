package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class HistoryActivity extends AppCompatActivity {

    private TextView tabSemua, tabHariIni;
    private TextView tvDashboard, tvHistory, tvControl, tvNotifications, tvAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

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
        tabHariIni = findViewById(R.id.tabHariIni);

        // Inisialisasi Bottom Navigation
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory = findViewById(R.id.tvHistory);
        tvControl = findViewById(R.id.tvControl);
        tvNotifications = findViewById(R.id.tvNotifications);
        tvAccount = findViewById(R.id.tvAccount);
    }

    private void setupListeners() {
        // Listener untuk Tab Semua
        tabSemua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ubah style tab Semua
                tabSemua.setBackgroundColor(ContextCompat.getColor(HistoryActivity.this, R.color.light_green));
                tabSemua.setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.primary_green));

                // Ubah style tab Hari Ini
                tabHariIni.setBackgroundColor(ContextCompat.getColor(HistoryActivity.this, android.R.color.white));
                tabHariIni.setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.text_secondary));

                Toast.makeText(HistoryActivity.this, "Menampilkan semua histori", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener untuk Tab Hari Ini
        tabHariIni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ubah style tab Hari Ini
                tabHariIni.setBackgroundColor(ContextCompat.getColor(HistoryActivity.this, R.color.light_green));
                tabHariIni.setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.primary_green));

                // Ubah style tab Semua
                tabSemua.setBackgroundColor(ContextCompat.getColor(HistoryActivity.this, android.R.color.white));
                tabSemua.setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.text_secondary));

                Toast.makeText(HistoryActivity.this, "Menampilkan histori hari ini", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        // Dashboard
        tvDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        });

        // Histori (current activity)
        tvHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HistoryActivity.this, "Anda sedang di halaman Histori", Toast.LENGTH_SHORT).show();
            }
        });

        // Kontrol
        tvControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, ControlActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Notifikasi
        tvNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, Notifikasi.class);
                startActivity(intent);
                finish();
            }
        });

        // Akun
        tvAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, AccountActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
