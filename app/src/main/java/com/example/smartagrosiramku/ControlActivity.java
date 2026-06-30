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
    private DatabaseReference sensorRef;

    private TextView tvDashboard, tvHistory, tvControl, tvAccount;
    private ImageButton btnNotif;
    private MaterialButton btnNyalakanAir, btnEditAir;
    private TextView tvStatusAir, tvJadwalAir, tvDurasiAir;
    private SwitchCompat switchModeOtomatis;
    
    private boolean isModeOtomatis = false;
    private int currentWaterLevel = 100;
    private Boolean lastPumpStatus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        initializeViews();
        setupHeaderActions();
        
        pompaAirRef = FirebaseDatabase.getInstance().getReference("Controls/pompa_air");
        sensorRef = FirebaseDatabase.getInstance().getReference("Sensors/water_level_pct");

        listenToFirebase();
        listenToSensorForAutomation(); // Tambahkan otomatisasi di sini
        setupPompaListeners();
        setupBottomNavigation();
    }

    private void listenToFirebase() {
        pompaAirRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Boolean status = snapshot.child("status").getValue(Boolean.class);
                String jadwal = snapshot.child("jadwal").getValue(String.class);
                
                Number durasiVal = snapshot.child("durasi_menit").getValue(Number.class);
                Integer durasi = durasiVal != null ? durasiVal.intValue() : 0;
                
                Boolean modeOto = snapshot.child("mode_otomatis").getValue(Boolean.class);

                if (status != null) {
                    lastPumpStatus = status;
                    setStatusPompa(tvStatusAir, status ? "Aktif" : "Tidak Aktif", status);
                    btnNyalakanAir.setText(status ? "Matikan" : "Nyalakan");
                    btnNyalakanAir.setBackgroundTintList(getResources().getColorStateList(
                            status ? android.R.color.holo_red_dark : android.R.color.holo_green_dark));
                }
                if (jadwal != null) tvJadwalAir.setText(jadwal);
                tvDurasiAir.setText(formatDurasi(durasi));

                if (modeOto != null) {
                    isModeOtomatis = modeOto;
                    switchModeOtomatis.setOnCheckedChangeListener(null);
                    switchModeOtomatis.setChecked(isModeOtomatis);
                    setupSwitchListener();
                    btnNyalakanAir.setEnabled(!isModeOtomatis);
                    btnNyalakanAir.setAlpha(isModeOtomatis ? 0.4f : 1.0f);
                    
                    runAutomationLogic(); 
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void listenToSensorForAutomation() {
        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    currentWaterLevel = ((Number) snapshot.getValue()).intValue();
                    runAutomationLogic();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void runAutomationLogic() {
        if (isModeOtomatis) {
            if (currentWaterLevel < 20) {
                if (lastPumpStatus == null || !lastPumpStatus) {
                    pompaAirRef.child("status").setValue(true);
                    Toast.makeText(this, "Otomatis: Air < 20%, Pompa dinyalakan", Toast.LENGTH_SHORT).show();
                }
            } else if (currentWaterLevel >= 95) {
                if (lastPumpStatus == null || lastPumpStatus) {
                    pompaAirRef.child("status").setValue(false);
                    Toast.makeText(this, "Otomatis: Air Penuh, Pompa dimatikan", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initializeViews() {
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory   = findViewById(R.id.tvHistory);
        tvControl   = findViewById(R.id.tvControl);
        tvAccount   = findViewById(R.id.tvAccount);
        btnNotif = findViewById(R.id.btnNotif);
        btnNyalakanAir = findViewById(R.id.btnNyalakanAir);
        btnEditAir     = findViewById(R.id.btnEditAir);
        tvStatusAir      = findViewById(R.id.tvStatusAir);
        tvJadwalAir      = findViewById(R.id.tvJadwalAir);
        tvDurasiAir      = findViewById(R.id.tvDurasiAir);
        switchModeOtomatis = findViewById(R.id.switchModeOtomatis);
    }

    private void setupHeaderActions() {
        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> startActivity(new Intent(this, Notifikasi.class)));
        }
    }

    private void setupPompaListeners() {
        btnNyalakanAir.setOnClickListener(v -> {
            if (isModeOtomatis) {
                Toast.makeText(this, "Matikan mode otomatis untuk kendali manual", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean target = tvStatusAir.getText().toString().equals("Tidak Aktif");
            pompaAirRef.child("status").setValue(target);
        });

        btnEditAir.setOnClickListener(v -> showEditJadwalDialog("Pompa Air", pompaAirRef, tvJadwalAir, tvDurasiAir));
        setupSwitchListener();
    }

    private void setupSwitchListener() {
        if (switchModeOtomatis == null) return;
        switchModeOtomatis.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isModeOtomatis = isChecked;
            pompaAirRef.child("mode_otomatis").setValue(isChecked);
            if (!isChecked) pompaAirRef.child("status").setValue(false);
            btnNyalakanAir.setEnabled(!isChecked);
            btnNyalakanAir.setAlpha(isChecked ? 0.4f : 1.0f);
        });
    }

    private void showEditJadwalDialog(String nama, DatabaseReference ref, TextView tvJ, TextView tvD) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_jadwal, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        TextView tvM = view.findViewById(R.id.tvWaktuMulai);
        TextView tvS = view.findViewById(R.id.tvWaktuSelesai);
        Button btnSimpan = view.findViewById(R.id.btnSimpanJadwal);

        tvM.setOnClickListener(v -> showTimePicker(tvM));
        tvS.setOnClickListener(v -> showTimePicker(tvS));

        btnSimpan.setOnClickListener(v -> {
            String baru = tvM.getText().toString() + " - " + tvS.getText().toString();
            ref.child("jadwal").setValue(baru);
            ref.child("durasi_menit").setValue(hitungDurasi(tvM.getText().toString(), tvS.getText().toString()));
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showTimePicker(TextView target) {
        new TimePickerDialog(this, android.app.AlertDialog.THEME_HOLO_LIGHT, (view, h, m) -> {
            target.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
        }, 8, 0, true).show();
    }

    private int hitungDurasi(String m, String s) {
        try {
            String[] mS = m.split(":"), sS = s.split(":");
            int mTot = Integer.parseInt(mS[0]) * 60 + Integer.parseInt(mS[1]);
            int sTot = Integer.parseInt(sS[0]) * 60 + Integer.parseInt(sS[1]);
            return (sTot < mTot) ? (sTot + 1440 - mTot) : (sTot - mTot);
        } catch (Exception e) { return 0; }
    }

    private String formatDurasi(int t) {
        return (t/60 > 0 ? t/60 + " jam " : "") + (t%60 + " menit");
    }

    private void setStatusPompa(TextView tv, String teks, boolean aktif) {
        if (tv == null) return;
        tv.setText(teks);
        tv.setBackgroundResource(aktif ? R.drawable.bg_badge_active : R.drawable.bg_badge_inactive);
    }

    private void setupBottomNavigation() {
        tvDashboard.setOnClickListener(v -> { startActivity(new Intent(this, Dashboard.class)); finish(); });
        tvHistory.setOnClickListener(v -> { startActivity(new Intent(this, HistoryActivity.class)); finish(); });
        tvAccount.setOnClickListener(v -> { startActivity(new Intent(this, AccountActivity.class)); finish(); });
    }
}
