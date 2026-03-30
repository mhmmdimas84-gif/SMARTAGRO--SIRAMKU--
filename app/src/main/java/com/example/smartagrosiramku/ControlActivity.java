package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ControlActivity extends AppCompatActivity {

    // Deklarasi View untuk Bottom Navigation
    private TextView tvDashboard, tvHistory, tvControl, tvNotifications, tvAccount;

    // Deklarasi View untuk Tombol Aksi
    private CardView btnMulaiSemua, btnHentikanSemua, btnReset, btnTimer;
    private TextView tvWaktu;

    // Deklarasi View untuk Status Pompa
    private TextView statusPompaNutrisi, jadwalPompaNutrisi, modePompaNutrisi;
    private TextView statusPompaAir, jadwalPompaAir, modePompaAir;
    private TextView statusPompaPhUp, modePompaPhUp;
    private TextView statusPompaPhDown, modePompaPhDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // Inisialisasi Views
        initializeViews();

        // Update waktu
        updateWaktu();

        // Setup listeners
        setupListeners();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void initializeViews() {
        // Inisialisasi Bottom Navigation
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory = findViewById(R.id.tvHistory);
        tvControl = findViewById(R.id.tvControl);
        tvNotifications = findViewById(R.id.tvNotifications);
        tvAccount = findViewById(R.id.tvAccount);

        // Inisialisasi Tombol Aksi
        btnMulaiSemua = findViewById(R.id.btnMulaiSemua);
        btnHentikanSemua = findViewById(R.id.btnHentikanSemua);
        btnReset = findViewById(R.id.btnReset);
        btnTimer = findViewById(R.id.btnTimer);
        tvWaktu = findViewById(R.id.tvWaktu);

        // Inisialisasi Status Pompa Nutrisi
        statusPompaNutrisi = findViewById(R.id.statusPompaNutrisi);
        jadwalPompaNutrisi = findViewById(R.id.jadwalPompaNutrisi);
        modePompaNutrisi = findViewById(R.id.modePompaNutrisi);

        // Inisialisasi Status Pompa Air
        statusPompaAir = findViewById(R.id.statusPompaAir);
        jadwalPompaAir = findViewById(R.id.jadwalPompaAir);
        modePompaAir = findViewById(R.id.modePompaAir);

        // Inisialisasi Status Pompa pH Up
        statusPompaPhUp = findViewById(R.id.statusPompaPhUp);
        modePompaPhUp = findViewById(R.id.modePompaPhUp);

        // Inisialisasi Status Pompa pH Down
        statusPompaPhDown = findViewById(R.id.statusPompaPhDown);
        modePompaPhDown = findViewById(R.id.modePompaPhDown);
    }

    private void updateWaktu() {
        // Update waktu saat ini
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm", Locale.getDefault());
        String waktuSekarang = sdf.format(new Date());
        if (tvWaktu != null) {
            tvWaktu.setText(waktuSekarang);
        }
    }

    private void setupListeners() {
        // Tombol Mulai Semua
        btnMulaiSemua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ubah status semua pompa menjadi Aktif
                statusPompaNutrisi.setText("Aktif");
                statusPompaNutrisi.setTextColor(getResources().getColor(R.color.primary_green));
                statusPompaNutrisi.setBackgroundColor(getResources().getColor(R.color.light_green));

                statusPompaAir.setText("Aktif");
                statusPompaAir.setTextColor(getResources().getColor(R.color.primary_green));
                statusPompaAir.setBackgroundColor(getResources().getColor(R.color.light_green));

                statusPompaPhUp.setText("Aktif");
                statusPompaPhUp.setTextColor(getResources().getColor(R.color.primary_green));
                statusPompaPhUp.setBackgroundColor(getResources().getColor(R.color.light_green));

                if (statusPompaPhDown != null) {
                    statusPompaPhDown.setText("Aktif");
                    statusPompaPhDown.setTextColor(getResources().getColor(R.color.primary_green));
                    statusPompaPhDown.setBackgroundColor(getResources().getColor(R.color.light_green));
                }

                Toast.makeText(ControlActivity.this, "Semua pompa diaktifkan", Toast.LENGTH_SHORT).show();
            }
        });

        // Tombol Hentikan Semua
        btnHentikanSemua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ubah status semua pompa menjadi Tidak Aktif
                statusPompaNutrisi.setText("Tidak Aktif");
                statusPompaNutrisi.setTextColor(getResources().getColor(R.color.status_inactive));
                statusPompaNutrisi.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                statusPompaAir.setText("Tidak Aktif");
                statusPompaAir.setTextColor(getResources().getColor(R.color.status_inactive));
                statusPompaAir.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                statusPompaPhUp.setText("Tidak Aktif");
                statusPompaPhUp.setTextColor(getResources().getColor(R.color.status_inactive));
                statusPompaPhUp.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                if (statusPompaPhDown != null) {
                    statusPompaPhDown.setText("Tidak Aktif");
                    statusPompaPhDown.setTextColor(getResources().getColor(R.color.status_inactive));
                    statusPompaPhDown.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                }

                Toast.makeText(ControlActivity.this, "Semua pompa dihentikan", Toast.LENGTH_SHORT).show();
            }
        });

        // Tombol Reset
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset ke pengaturan default
                statusPompaNutrisi.setText("Tidak Aktif");
                statusPompaNutrisi.setTextColor(getResources().getColor(R.color.status_inactive));
                statusPompaNutrisi.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                statusPompaAir.setText("Tidak Aktif");
                statusPompaAir.setTextColor(getResources().getColor(R.color.status_inactive));
                statusPompaAir.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                statusPompaPhUp.setText("Tidak Aktif");
                statusPompaPhUp.setTextColor(getResources().getColor(R.color.status_inactive));
                statusPompaPhUp.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                modePompaNutrisi.setText("Otomatis");
                modePompaAir.setText("Otomatis");
                modePompaPhUp.setText("Manual");

                jadwalPompaNutrisi.setText("08:00 - 08:15");
                jadwalPompaAir.setText("12:00 - 12:20");

                Toast.makeText(ControlActivity.this, "Reset ke pengaturan default", Toast.LENGTH_SHORT).show();
            }
        });

        // Tombol Timer
        btnTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ControlActivity.this, "Fitur Timer (Coming Soon)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        // Dashboard
        tvDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ControlActivity.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        });

        // Histori
        tvHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ControlActivity.this, HistoryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Kontrol (current activity)
        tvControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ControlActivity.this, "Anda sedang di halaman Kontrol", Toast.LENGTH_SHORT).show();
            }
        });

        // Notifikasi
        tvNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ControlActivity.this, Notifikasi.class);
                startActivity(intent);
                finish();
            }
        });

        // Akun
        tvAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ControlActivity.this, AccountActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
