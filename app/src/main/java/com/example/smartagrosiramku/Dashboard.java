package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Dashboard extends AppCompatActivity {

    // Deklarasi View untuk Bottom Navigation
    private TextView tvDashboard, tvHistory, tvControl, tvAccount;
    
    // Deklarasi View untuk Header Notification
    private ImageButton btnNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Inisialisasi Views
        initializeViews();

        // Setup Bottom Navigation
        setupBottomNavigation();
        
        // Setup Header Actions
        setupHeaderActions();
    }

    private void initializeViews() {
        // Inisialisasi Bottom Navigation
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory = findViewById(R.id.tvHistory);
        tvControl = findViewById(R.id.tvControl);
        tvAccount = findViewById(R.id.tvAccount);
        
        // Inisialisasi Header Notification
        btnNotif = findViewById(R.id.btnNotif);
    }

    private void setupHeaderActions() {
        if (btnNotif != null) {
            btnNotif.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Dashboard.this, Notifikasi.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void setupBottomNavigation() {
        // Dashboard (current activity)
        if (tvDashboard != null) {
            tvDashboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Dashboard.this, "Anda sedang di Dashboard", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Histori
        if (tvHistory != null) {
            tvHistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Dashboard.this, HistoryActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Kontrol
        if (tvControl != null) {
            tvControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Dashboard.this, ControlActivity.class);
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
}
