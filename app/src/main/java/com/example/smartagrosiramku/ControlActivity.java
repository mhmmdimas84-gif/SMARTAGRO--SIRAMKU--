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
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.button.MaterialButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;

public class ControlActivity extends AppCompatActivity {

    private DatabaseReference pompaAirRef;

    // Bottom Navigation
    private TextView tvDashboard, tvHistory, tvControl, tvAccount;

    // Header
    private ImageButton btnNotif;

    // Tombol Aksi Pompa Air
    private MaterialButton btnNyalakanAir, btnEditAir;

    // Status & Jadwal & Durasi Pompa Air
    private TextView tvStatusAir, tvJadwalAir, tvDurasiAir;

    // Switch Mode Otomatis
    private SwitchCompat switchModeOtomatis;
    private boolean isModeOtomatis = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        initializeViews();
        setupHeaderActions();

        pompaAirRef = FirebaseDatabase.getInstance().getReference("Controls/pompa_air");

        listenToFirebase();
        setupPompaListeners();
        setupBottomNavigation();
    }

    private void listenToFirebase() {

        pompaAirRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean status = snapshot.child("status").getValue(Boolean.class);
                String jadwal = snapshot.child("jadwal").getValue(String.class);
                Integer durasi = snapshot.child("durasi_menit").getValue(Integer.class);
                Boolean modeOtomatis = snapshot.child("mode_otomatis").getValue(Boolean.class);

                if (status != null) {
                    setStatusPompa(tvStatusAir, status ? "Aktif" : "Tidak Aktif", status);
                    btnNyalakanAir.setText(status ? "Matikan" : "Nyalakan");
                    btnNyalakanAir.setBackgroundTintList(getResources().getColorStateList(
                            status ? android.R.color.holo_red_dark : android.R.color.holo_green_dark));
                }
                if (jadwal != null) tvJadwalAir.setText(jadwal);
                if (durasi != null) tvDurasiAir.setText(formatDurasi(durasi));

                // Sinkronisasi state switch mode otomatis dari Firebase
                if (modeOtomatis != null) {
                    isModeOtomatis = modeOtomatis;
                    // Cegah listener switch terpicu saat sinkronisasi dari Firebase
                    switchModeOtomatis.setOnCheckedChangeListener(null);
                    switchModeOtomatis.setChecked(modeOtomatis);
                    setupSwitchListener(); // Pasang ulang listener setelah set value
                    // Nonaktifkan tombol manual saat mode otomatis aktif
                    btnNyalakanAir.setEnabled(!modeOtomatis);
                    btnNyalakanAir.setAlpha(modeOtomatis ? 0.4f : 1.0f);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ControlActivity", "Gagal load pompa air", error.toException());
            }
        });
    }

    private void initializeViews() {
        // Bottom Navigation
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory   = findViewById(R.id.tvHistory);
        tvControl   = findViewById(R.id.tvControl);
        tvAccount   = findViewById(R.id.tvAccount);

        // Header
        btnNotif = findViewById(R.id.btnNotif);

        // Tombol Pompa Air
        btnNyalakanAir = findViewById(R.id.btnNyalakanAir);
        btnEditAir     = findViewById(R.id.btnEditAir);

        // TextView Status & Jadwal
        tvStatusAir      = findViewById(R.id.tvStatusAir);
        tvJadwalAir      = findViewById(R.id.tvJadwalAir);
        tvDurasiAir      = findViewById(R.id.tvDurasiAir);

        // Switch Mode Otomatis
        switchModeOtomatis = findViewById(R.id.switchModeOtomatis);
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

        // ===== POMPA AIR - Tombol Manual =====
        if (btnNyalakanAir != null) {
            btnNyalakanAir.setOnClickListener(v -> {
                if (isModeOtomatis) {
                    Toast.makeText(this, "Matikan mode otomatis terlebih dahulu!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String statusSekarang = tvStatusAir.getText().toString();
                boolean targetStatus = statusSekarang.equals("Tidak Aktif");
                pompaAirRef.child("status").setValue(targetStatus);
                Toast.makeText(this, targetStatus ? "Menyalakan Pompa Air" : "Mematikan Pompa Air", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnEditAir != null) {
            btnEditAir.setOnClickListener(v ->
                    showEditJadwalDialog("Pompa Air", pompaAirRef, tvJadwalAir, tvDurasiAir)
            );
        }

        // ===== SWITCH MODE OTOMATIS =====
        setupSwitchListener();
    }

    private void setupSwitchListener() {
        if (switchModeOtomatis == null) return;
        switchModeOtomatis.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isModeOtomatis = isChecked;
            pompaAirRef.child("mode_otomatis").setValue(isChecked);

            // Jika mode otomatis dimatikan, pastikan pompa juga mati secara manual
            if (!isChecked) {
                pompaAirRef.child("status").setValue(false);
            }

            // Aktifkan/nonaktifkan tombol manual
            btnNyalakanAir.setEnabled(!isChecked);
            btnNyalakanAir.setAlpha(isChecked ? 0.4f : 1.0f);

            Toast.makeText(this,
                    isChecked ? "Mode Otomatis AKTIF — pompa dikendalikan sensor" :
                            "Mode Otomatis MATI — kendali manual aktif",
                    Toast.LENGTH_LONG).show();
        });
    }

    private void showEditJadwalDialog(String namaPompa, DatabaseReference ref, TextView tvJadwalTarget, TextView tvDurasiTarget) {
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

            int durasi = hitungDurasi(mulai, selesai);

            ref.child("jadwal").setValue(baru);
            ref.child("durasi_menit").setValue(durasi);

            Toast.makeText(this, "Jadwal " + namaPompa + " disimpan ke Cloud", Toast.LENGTH_SHORT).show();
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

    private String formatDurasi(int totalMenit) {
        if (totalMenit <= 0) {
            return "0 menit";
        }
        int jam = totalMenit / 60;
        int menit = totalMenit % 60;

        StringBuilder sb = new StringBuilder();
        if (jam > 0) {
            sb.append(jam).append(" jam");
        }
        if (menit > 0) {
            if (jam > 0) {
                sb.append(" ");
            }
            sb.append(menit).append(" menit");
        }
        return sb.toString();
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