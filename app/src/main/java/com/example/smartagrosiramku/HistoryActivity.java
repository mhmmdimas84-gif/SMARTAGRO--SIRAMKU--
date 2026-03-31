package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class HistoryActivity extends AppCompatActivity {

    private TextView tabSemua, tabHariIni;
    private TextView tvDashboard, tvHistory, tvControl, tvAccount;
    private FrameLayout btnNotificationsHeader;

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
        
        // Setup Header Actions
        setupHeaderActions();
    }

    private void initializeViews() {
        // Inisialisasi Tab
        tabSemua = findViewById(R.id.tabSemua);
        tabHariIni = findViewById(R.id.tabHariIni);

        // Inisialisasi Bottom Navigation
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory = findViewById(R.id.tvHistory);
        tvControl = findViewById(R.id.tvControl);
        tvAccount = findViewById(R.id.tvAccount);
        
        // Inisialisasi Header Notification
        btnNotificationsHeader = findViewById(R.id.btnNotificationsHeader);
    }

    private void setupHeaderActions() {
        if (btnNotificationsHeader != null) {
            btnNotificationsHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HistoryActivity.this, Notifikasi.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void setupListeners() {
        // Listener untuk Tab Semua
        if (tabSemua != null) {
            tabSemua.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tabSemua.setBackgroundColor(ContextCompat.getColor(HistoryActivity.this, R.color.light_green));
                    tabSemua.setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.primary_green));
                    if (tabHariIni != null) {
                        tabHariIni.setBackgroundColor(ContextCompat.getColor(HistoryActivity.this, android.R.color.white));
                        tabHariIni.setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.text_secondary));
                    }
                    Toast.makeText(HistoryActivity.this, "Menampilkan semua histori", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Listener untuk Tab Hari Ini
        if (tabHariIni != null) {
            tabHariIni.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tabHariIni.setBackgroundColor(ContextCompat.getColor(HistoryActivity.this, R.color.light_green));
                    tabHariIni.setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.primary_green));
                    if (tabSemua != null) {
                        tabSemua.setBackgroundColor(ContextCompat.getColor(HistoryActivity.this, android.R.color.white));
                        tabSemua.setTextColor(ContextCompat.getColor(HistoryActivity.this, R.color.text_secondary));
                    }
                    Toast.makeText(HistoryActivity.this, "Menampilkan histori hari ini", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupBottomNavigation() {
        if (tvDashboard != null) {
            tvDashboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(HistoryActivity.this, Dashboard.class));
                    finish();
                }
            });
        }

        if (tvHistory != null) {
            tvHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(HistoryActivity.this, "Anda sedang di halaman Histori", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (tvControl != null) {
            tvControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(HistoryActivity.this, ControlActivity.class));
                    finish();
                }
            });
        }

        if (tvAccount != null) {
            tvAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(HistoryActivity.this, AccountActivity.class));
                    finish();
                }
            });
        }
    }
}
