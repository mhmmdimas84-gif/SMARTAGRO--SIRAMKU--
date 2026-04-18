package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.widget.Button;
import java.util.Locale;
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

    // Status & Jadwal & Durasi Pompa Nutrisi
    private TextView tvStatusNutrisi, tvJadwalNutrisi, tvDurasiNutrisi;

    // Status & Jadwal & Durasi Pompa Air
    private TextView tvStatusAir, tvJadwalAir, tvDurasiAir;

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
        tvDurasiNutrisi  = findViewById(R.id.tvDurasiNutrisi);
        tvStatusAir      = findViewById(R.id.tvStatusAir);
        tvJadwalAir      = findViewById(R.id.tvJadwalAir);
        tvDurasiAir      = findViewById(R.id.tvDurasiAir);
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
                    btnNyalakanNutrisi.setText("Matikan");
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
                    Toast.makeText(this, "Pompa Nutrisi dimatikan", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnEditNutrisi != null) {
            btnEditNutrisi.setOnClickListener(v ->
                    showEditJadwalDialog("Pompa Nutrisi", tvJadwalNutrisi, tvDurasiNutrisi)
            );
        }

        // ===== POMPA AIR =====
        if (btnNyalakanAir != null) {
            btnNyalakanAir.setOnClickListener(v -> {
                String statusSekarang = tvStatusAir.getText().toString();

                if (statusSekarang.equals("Tidak Aktif")) {
                    setStatusPompa(tvStatusAir, "Aktif", true);
                    btnNyalakanAir.setText("Matikan");
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
                    Toast.makeText(this, "Pompa Air dimatikan", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnEditAir != null) {
            btnEditAir.setOnClickListener(v ->
                    showEditJadwalDialog("Pompa Air", tvJadwalAir, tvDurasiAir)
            );
        }
    }

    private void showEditJadwalDialog(String namaPompa, TextView tvJadwalTarget, TextView tvDurasiTarget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_jadwal, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvWaktuMulai = dialogView.findViewById(R.id.tvWaktuMulai);
        TextView tvWaktuSelesai = dialogView.findViewById(R.id.tvWaktuSelesai);
        Button btnBatal = dialogView.findViewById(R.id.btnBatalJadwal);
        Button btnSimpan = dialogView.findViewById(R.id.btnSimpanJadwal);

        tvTitle.setText("Atur Jadwal " + namaPompa);

        String jadwalSekarang = tvJadwalTarget.getText().toString();
        if (jadwalSekarang.contains("-")) {
            String[] split = jadwalSekarang.split("-");
            if (split.length == 2) {
                tvWaktuMulai.setText(split[0].trim());
                tvWaktuSelesai.setText(split[1].trim());
            }
        }

        tvWaktuMulai.setOnClickListener(v -> showTimePicker(tvWaktuMulai));
        tvWaktuSelesai.setOnClickListener(v -> showTimePicker(tvWaktuSelesai));

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnSimpan.setOnClickListener(v -> {
            String mulai = tvWaktuMulai.getText().toString();
            String selesai = tvWaktuSelesai.getText().toString();
            String baru = mulai + " - " + selesai;
            tvJadwalTarget.setText(baru);
            
            if (tvDurasiTarget != null) {
                int durasi = hitungDurasi(mulai, selesai);
                tvDurasiTarget.setText(durasi + " menit");
            }
            
            Toast.makeText(this, "Jadwal " + namaPompa + " diperbarui", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showTimePicker(TextView targetTextView) {
        String currentTime = targetTextView.getText().toString();
        int hour = 8;
        int minute = 0;
        if (currentTime.contains(":")) {
            String[] split = currentTime.split(":");
            if (split.length == 2) {
                try {
                    hour = Integer.parseInt(split[0]);
                    minute = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    // Abaikan dan gunakan default
                }
            }
        }

        // Menggunakan THEME_HOLO_LIGHT agar tampilannya spinner yang mudah diatur
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                android.app.AlertDialog.THEME_HOLO_LIGHT,
                (view, hourOfDay, minuteOfHour) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    targetTextView.setText(time);
                }, hour, minute, true);
        
        // Agar window tidak transparan yang bikin tulisan susah dibaca, kasih background putih
        if (timePickerDialog.getWindow() != null) {
            timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        }
        timePickerDialog.show();
    }

    private int hitungDurasi(String jamMulai, String jamSelesai) {
        try {
            String[] mSplit = jamMulai.split(":");
            String[] sSplit = jamSelesai.split(":");
            int mHour = Integer.parseInt(mSplit[0]);
            int mMin = Integer.parseInt(mSplit[1]);
            int sHour = Integer.parseInt(sSplit[0]);
            int sMin = Integer.parseInt(sSplit[1]);

            int totalMulai = mHour * 60 + mMin;
            int totalSelesai = sHour * 60 + sMin;
            
            if (totalSelesai < totalMulai) {
                totalSelesai += 24 * 60;
            }
            return totalSelesai - totalMulai;
        } catch (Exception e) {
            return 0;
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