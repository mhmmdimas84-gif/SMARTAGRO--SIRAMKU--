package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Dashboard extends AppCompatActivity {

    // Deklarasi View untuk Bottom Navigation
    private TextView tvDashboard, tvHistory, tvControl, tvNotifications, tvAccount;

    // Deklarasi View untuk Kontrol Pompa (jika ada)
    private CardView btnPompaNutrisi, btnPompaAir, btnPhUp, btnPhDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Inisialisasi Views
        initializeViews();

        // Setup Bottom Navigation
        setupBottomNavigation();

        // Setup Kontrol Pompa (jika ada)
        setupPumpControls();
    }

    private void initializeViews() {
        // Inisialisasi Bottom Navigation
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory = findViewById(R.id.tvHistory);
        tvControl = findViewById(R.id.tvControl);
        tvNotifications = findViewById(R.id.tvNotifications);
        tvAccount = findViewById(R.id.tvAccount);

        // Inisialisasi Kontrol Pompa (sesuaikan dengan ID di layout)
        // btnPompaNutrisi = findViewById(R.id.btnPompaNutrisi);
        // btnPompaAir = findViewById(R.id.btnPompaAir);
        // btnPhUp = findViewById(R.id.btnPhUp);
        // btnPhDown = findViewById(R.id.btnPhDown);
    }

    private void setupBottomNavigation() {
        // Dashboard (current activity)
        if (tvDashboard != null) {
            tvDashboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Sudah di halaman dashboard
                    Toast.makeText(Dashboard.this, "Anda sedang di Dashboard", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Histori
        if (tvHistory != null) {
            tvHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Cek apakah HistoryActivity sudah ada
                    try {
                        Intent intent = new Intent(Dashboard.this, HistoryActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(Dashboard.this, "Fitur Histori (Coming Soon)", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Kontrol
        if (tvControl != null) {
            tvControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Dashboard.this, ControlActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(Dashboard.this, "Fitur Kontrol (Coming Soon)", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Notifikasi
        if (tvNotifications != null) {
            tvNotifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Dashboard.this, Notifikasi.class);
                    startActivity(intent);
                }
            });
        }

        // Akun
        if (tvAccount != null) {
            tvAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Dashboard.this, AccountActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void setupPumpControls() {
        // Setup listener untuk kontrol pompa (jika ada)
    }

    // Method untuk update data sensor (dipanggil dari Firebase atau sumber data lain)
    private void updateSensorData() {
        // Update data sensor di UI
    }
}
