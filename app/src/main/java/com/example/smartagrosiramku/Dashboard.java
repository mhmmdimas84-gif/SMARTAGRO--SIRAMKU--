package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Dashboard extends AppCompatActivity {

    private TextView tvDashboard, tvHistory, tvControl, tvAccount;
    private ImageButton btnNotif;
    private TextView tvGreeting;
    private LineChart chartLevelAir;
    private TextView tvFilterMinggu, tvFilterBulan;
    private TextView tvTDS, tvLevelAir;
    private android.widget.ProgressBar progressAir, progressTDS;

    private DatabaseReference currentChartRef;
    private ValueEventListener currentChartListener;

    private long lastLogTime = 0;
    private float currentTds = 0;
    private int currentAir = 100; 
    private boolean isModeOtomatis = false;
    private Boolean lastPumpStatus = null; 

    private long lastWaterLevelNotifTime = 0;
    private static final long NOTIF_COOLDOWN = 30 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        initializeViews();
        updateGreeting();
        setupBottomNavigation();
        setupHeaderActions();
        setupChart();
        setupChartFilters();
        
        loadChartDataMingguan();
        loadRealtimeSensorData();
        listenToControlMode();
        listenToPumpStatus(); 
    }

    private void initializeViews() {
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory = findViewById(R.id.tvHistory);
        tvControl = findViewById(R.id.tvControl);
        tvAccount = findViewById(R.id.tvAccount);
        btnNotif = findViewById(R.id.btnNotif);
        tvGreeting = findViewById(R.id.tvGreeting);
        chartLevelAir = findViewById(R.id.chartLevelAir);
        tvFilterMinggu = findViewById(R.id.tvFilterMinggu);
        tvFilterBulan = findViewById(R.id.tvFilterBulan);
        tvTDS = findViewById(R.id.tvTDS);
        tvLevelAir = findViewById(R.id.tvLevelAir);
        progressAir = findViewById(R.id.progressAir);
        progressTDS = findViewById(R.id.progressTDS);
    }

    private boolean getSafeBoolean(DataSnapshot snapshot) {
        if (!snapshot.exists() || snapshot.getValue() == null) return false;
        Object val = snapshot.getValue();
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        return false;
    }

    private void listenToControlMode() {
        FirebaseDatabase.getInstance().getReference("Controls/pompa_air/mode_otomatis")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    isModeOtomatis = getSafeBoolean(snapshot);
                    runAutomationLogic(); 
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
    }

    private void listenToPumpStatus() {
        FirebaseDatabase.getInstance().getReference("Controls/pompa_air/status")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        lastPumpStatus = getSafeBoolean(snapshot);
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
    }

    private void loadRealtimeSensorData() {
        DatabaseReference sensorRef = FirebaseDatabase.getInstance().getReference("Sensors");

        sensorRef.child("tds_ppm").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    currentTds = ((Number) snapshot.getValue()).floatValue();
                    tvTDS.setText(String.format("%.0f", currentTds));
                    if (progressTDS != null) {
                        progressTDS.setProgress(Math.min((int)(currentTds/10), 100));
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        sensorRef.child("water_level_pct").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    currentAir = ((Number) snapshot.getValue()).intValue();
                    tvLevelAir.setText(String.valueOf(currentAir));
                    if (progressAir != null) {
                        progressAir.setProgress(currentAir);
                    }
                    
                    runAutomationLogic();
                    checkAndLogHistory();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void runAutomationLogic() {
        if (isModeOtomatis) {
            DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("Controls/pompa_air/status");
            if (currentAir <= 20) {
                if (lastPumpStatus == null || !lastPumpStatus) {
                    statusRef.setValue(true);
                    Toast.makeText(this, "Otomatis: Air <= 20%, Pompa Menyala", Toast.LENGTH_SHORT).show();
                    Log.d("Siramku", "Automation: Water low (" + currentAir + "%), pump ON");
                }
            } else if (currentAir >= 90) {
                if (lastPumpStatus == null || lastPumpStatus) {
                    statusRef.setValue(false);
                    Toast.makeText(this, "Otomatis: Air Cukup, Pompa Mati", Toast.LENGTH_SHORT).show();
                    Log.d("Siramku", "Automation: Water full (" + currentAir + "%), pump OFF");
                }
            }
        }
    }

    private void checkAndLogHistory() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLogTime > 60000) {
            lastLogTime = currentTime;
            DatabaseReference logRef = FirebaseDatabase.getInstance().getReference("Sensors/history_logs");
            String key = logRef.push().getKey();
            if(key != null) {
                HistoryLog log = new HistoryLog(currentTds, currentAir, currentTime);
                logRef.child(key).setValue(log);
            }
        }
        
        if (currentAir <= 20 && (currentTime - lastWaterLevelNotifTime > NOTIF_COOLDOWN)) {
            lastWaterLevelNotifTime = currentTime;
            DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("Sensors/notifications");
            String k = notifRef.push().getKey();
            if (k != null) {
                NotifikasiLog n = new NotifikasiLog("Level Air Kritis", "Air <= 20%. Pompa telah diaktifkan otomatis.", "warning", currentTime, false);
                notifRef.child(k).setValue(n);
            }
        }
    }

    private void updateGreeting() {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        String g = (h < 11) ? "Selamat pagi!" : (h < 15) ? "Selamat siang!" : (h < 18) ? "Selamat sore!" : "Selamat malam!";
        tvGreeting.setText(g + " Petani");
    }

    private void setupHeaderActions() {
        if (btnNotif != null) btnNotif.setOnClickListener(v -> startActivity(new Intent(this, Notifikasi.class)));
    }

    private void setupBottomNavigation() {
        tvDashboard.setOnClickListener(v -> Toast.makeText(this, "Di Dashboard", Toast.LENGTH_SHORT).show());
        tvHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        tvControl.setOnClickListener(v -> startActivity(new Intent(this, ControlActivity.class)));
        tvAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
    }

    private void setupChart() {
        if (chartLevelAir == null) return;
        chartLevelAir.getDescription().setEnabled(false);
        chartLevelAir.getLegend().setEnabled(false);
        XAxis xAxis = chartLevelAir.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        chartLevelAir.getAxisRight().setEnabled(false);
        YAxis yAxis = chartLevelAir.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
    }

    private void setupChartFilters() {
        tvFilterMinggu.setOnClickListener(v -> loadChartDataMingguan());
        tvFilterBulan.setOnClickListener(v -> loadChartDataBulanan());
    }

    private void loadChartDataMingguan() {
        if (currentChartRef != null && currentChartListener != null) currentChartRef.removeEventListener(currentChartListener);
        currentChartRef = FirebaseDatabase.getInstance().getReference("Sensors/history/mingguan");
        currentChartListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Entry> entries = new ArrayList<>();
                int i = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Number v = (Number) data.getValue();
                    if (v != null) entries.add(new Entry(i++, v.floatValue()));
                }
                setChartData(entries, "Mingguan");
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        };
        currentChartRef.addValueEventListener(currentChartListener);
    }

    private void loadChartDataBulanan() {
        if (currentChartRef != null && currentChartListener != null) currentChartRef.removeEventListener(currentChartListener);
        currentChartRef = FirebaseDatabase.getInstance().getReference("Sensors/history/bulanan");
        currentChartListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Entry> entries = new ArrayList<>();
                int i = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Number v = (Number) data.getValue();
                    if (v != null) entries.add(new Entry(i++, v.floatValue()));
                }
                setChartData(entries, "Bulanan");
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        };
        currentChartRef.addValueEventListener(currentChartListener);
    }

    private void setChartData(List<Entry> entries, String label) {
        if (entries.isEmpty()) return;
        LineDataSet ds = new LineDataSet(entries, label);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ds.setColor(Color.parseColor("#1976D2"));
        ds.setLineWidth(3f);
        LineData ld = new LineData(ds);
        chartLevelAir.setData(ld);
        chartLevelAir.invalidate();
    }
}
