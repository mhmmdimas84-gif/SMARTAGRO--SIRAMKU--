package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class ControlActivity extends AppCompatActivity {

    // Bottom Navigation
    private TextView tvDashboard, tvHistory, tvControl, tvAccount;

    // Header
    private ImageButton btnNotif;

    // Tombol Aksi Pompa Nutrisi
    private MaterialButton btnNyalakanNutrisi, btnEditNutrisi;

    // Tombol Aksi Pompa Air
    private MaterialButton btnNyalakanAir, btnEditAir;

    // Status & Jadwal Pompa Nutrisi
    private TextView tvStatusNutrisi, tvJadwalNutrisi;

    // Status & Jadwal Pompa Air
    private TextView tvStatusAir, tvJadwalAir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        initializeViews();
        setupHeaderActions();
        setupPompaListeners();
        setupBottomNavigation();
    }

    private void initializeViews() {
        // Bottom Navigation
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory   = findViewById(R.id.tvHistory);
        tvControl   = findViewById(R.id.tvControl);
        tvAccount   = findViewById(R.id.tvAccount);

        // Header
        btnNotif = findViewById(R.id.btnNotif);

        // Tombol Pompa Nutrisi
        btnNyalakanNutrisi = findViewById(R.id.btnNyalakanNutrisi);
        btnEditNutrisi     = findViewById(R.id.btnEditNutrisi);

        // Tombol Pompa Air
        btnNyalakanAir = findViewById(R.id.btnNyalakanAir);
        btnEditAir     = findViewById(R.id.btnEditAir);

        // TextView Status & Jadwal
        tvStatusNutrisi  = findViewById(R.id.tvStatusNutrisi);
        tvJadwalNutrisi  = findViewById(R.id.tvJadwalNutrisi);
        tvStatusAir      = findViewById(R.id.tvStatusAir);
        tvJadwalAir      = findViewById(R.id.tvJadwalAir);
    }

    private void setupHeaderActions() {
        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> {
                Intent intent = new Intent(ControlActivity.this, Notifikasi.class);
                startActivity(intent);
            });
        }
    }

    private void setupPompaListeners() {

        // ===== POMPA NUTRISI =====
        if (btnNyalakanNutrisi != null) {
            btnNyalakanNutrisi.setOnClickListener(v -> {
                String statusSekarang = tvStatusNutrisi.getText().toString();

                if (statusSekarang.equals("Tidak Aktif")) {
                    // Nyalakan pompa nutrisi
                    setStatusPompa(tvStatusNutrisi, "Aktif", true);
                    btnNyalakanNutrisi.setText("Hentikan");
                    btnNyalakanNutrisi.setBackgroundTintList(
                            getResources().getColorStateList(android.R.color.holo_red_dark)
                    );
                    Toast.makeText(this, "Pompa Nutrisi dinyalakan", Toast.LENGTH_SHORT).show();
                } else {
                    // Matikan pompa nutrisi
                    setStatusPompa(tvStatusNutrisi, "Tidak Aktif", false);
                    btnNyalakanNutrisi.setText("Nyalakan");
                    btnNyalakanNutrisi.setBackgroundTintList(
                            getResources().getColorStateList(android.R.color.holo_green_dark)
                    );
                    Toast.makeText(this, "Pompa Nutrisi dihentikan", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnEditNutrisi != null) {
            btnEditNutrisi.setOnClickListener(v ->
                    Toast.makeText(this, "Edit Jadwal Pompa Nutrisi (Coming Soon)", Toast.LENGTH_SHORT).show()
            );
        }

        // ===== POMPA AIR =====
        if (btnNyalakanAir != null) {
            btnNyalakanAir.setOnClickListener(v -> {
                String statusSekarang = tvStatusAir.getText().toString();

                if (statusSekarang.equals("Tidak Aktif")) {
                    setStatusPompa(tvStatusAir, "Aktif", true);
                    btnNyalakanAir.setText("Hentikan");
                    btnNyalakanAir.setBackgroundTintList(
                            getResources().getColorStateList(android.R.color.holo_red_dark)
                    );
                    Toast.makeText(this, "Pompa Air dinyalakan", Toast.LENGTH_SHORT).show();
                } else {
                    setStatusPompa(tvStatusAir, "Tidak Aktif", false);
                    btnNyalakanAir.setText("Nyalakan");
                    btnNyalakanAir.setBackgroundTintList(
                            getResources().getColorStateList(android.R.color.holo_green_dark) // <-- Ganti ini
                    );
                    Toast.makeText(this, "Pompa Air dihentikan", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnEditAir != null) {
            btnEditAir.setOnClickListener(v ->
                    Toast.makeText(this, "Edit Jadwal Pompa Air (Coming Soon)", Toast.LENGTH_SHORT).show()
            );
        }
    }

    /**
     * Helper untuk mengubah tampilan badge status pompa.
     * @param tvStatus  TextView badge yang ingin diubah
     * @param teks      Teks baru ("Aktif" / "Tidak Aktif")
     * @param isAktif   true = warna hijau, false = warna merah
     */
    private void setStatusPompa(TextView tvStatus, String teks, boolean isAktif) {
        if (tvStatus == null) return;
        tvStatus.setText(teks);
        if (isAktif) {
            tvStatus.setTextColor(getResources().getColor(R.color.status_aktif_text));
            tvStatus.setBackgroundResource(R.drawable.bg_badge_active);
        } else {
            tvStatus.setTextColor(getResources().getColor(R.color.status_inactive_text));
            tvStatus.setBackgroundResource(R.drawable.bg_badge_inactive);
        }
    }

    private void setupBottomNavigation() {
        if (tvDashboard != null) {
            tvDashboard.setOnClickListener(v -> {
                startActivity(new Intent(ControlActivity.this, Dashboard.class));
                finish();
            });
        }

        if (tvHistory != null) {
            tvHistory.setOnClickListener(v -> {
                startActivity(new Intent(ControlActivity.this, HistoryActivity.class));
                finish();
            });
        }

        if (tvControl != null) {
            tvControl.setOnClickListener(v ->
                    Toast.makeText(this, "Anda sedang di halaman Kontrol", Toast.LENGTH_SHORT).show()
            );
        }

        if (tvAccount != null) {
            tvAccount.setOnClickListener(v -> {
                startActivity(new Intent(ControlActivity.this, AccountActivity.class));
                finish();
            });
        }
    }
}