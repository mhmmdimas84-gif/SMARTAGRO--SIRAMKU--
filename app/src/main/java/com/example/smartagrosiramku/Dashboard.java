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
import android.util.Log;

public class Dashboard extends AppCompatActivity {

    // Deklarasi View untuk Bottom Navigation
    private TextView tvDashboard, tvHistory, tvControl, tvAccount;
    
    // Deklarasi View untuk Header Notification & Greeting
    private ImageButton btnNotif;
    private TextView tvGreeting;

    // View untuk Chart & Filter
    private LineChart chartLevelAir;
    private TextView tvFilterMinggu, tvFilterBulan;

    // View untuk Sensor Real-time
    private TextView tvTDS, tvLevelAir;
    private android.widget.ProgressBar progressTDS, progressAir;

    // Firebase Listeners for Chart
    private DatabaseReference currentChartRef;
    private ValueEventListener currentChartListener;

    // Logging History
    private long lastLogTime = 0;
    private float currentTds = 0;
    private int currentAir = 0;

    // Notification Cooldowns
    private long lastWaterLevelNotifTime = 0;
    private long lastHighTdsNotifTime = 0;
    private long lastLowTdsNotifTime = 0;
    private static final long NOTIF_COOLDOWN = 30 * 60 * 1000; // 30 menit cooldown

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Inisialisasi Views
        initializeViews();

        // Update Greeting dynamically
        updateGreeting();

        // Setup Bottom Navigation
        setupBottomNavigation();
        
        // Setup Header Actions
        setupHeaderActions();

        // Setup Chart
        setupChart();
        setupChartFilters();
        
        // Load default data (Mingguan)
        loadChartDataMingguan();

        // Load data sensor realtime
        loadRealtimeSensorData();
    }

    private void initializeViews() {
        // Inisialisasi Bottom Navigation
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory = findViewById(R.id.tvHistory);
        tvControl = findViewById(R.id.tvControl);
        tvAccount = findViewById(R.id.tvAccount);
        
        // Inisialisasi Header Notification & Greeting
        btnNotif = findViewById(R.id.btnNotif);
        tvGreeting = findViewById(R.id.tvGreeting);

        // Inisialisasi Chart & Filter
        chartLevelAir = findViewById(R.id.chartLevelAir);
        tvFilterMinggu = findViewById(R.id.tvFilterMinggu);
        tvFilterBulan = findViewById(R.id.tvFilterBulan);

        // Inisialisasi Sensor Real-time
        tvTDS = findViewById(R.id.tvTDS);
        tvLevelAir = findViewById(R.id.tvLevelAir);
        progressTDS = findViewById(R.id.progressTDS);
        progressAir = findViewById(R.id.progressAir);
    }

    private void updateGreeting() {
        if (tvGreeting == null) return;
        
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;
        
        if (hour >= 0 && hour < 11) {
            greeting = "Selamat pagi, Petani!";
        } else if (hour >= 11 && hour < 15) {
            greeting = "Selamat siang, Petani!";
        } else if (hour >= 15 && hour < 18) {
            greeting = "Selamat sore, Petani!";
        } else {
            greeting = "Selamat malam, Petani!";
        }
        
        tvGreeting.setText(greeting);
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

    private void setupChart() {
        if (chartLevelAir == null) return;
        
        chartLevelAir.getDescription().setEnabled(false);
        chartLevelAir.getLegend().setEnabled(false);
        chartLevelAir.setDrawGridBackground(false);
        chartLevelAir.setTouchEnabled(true);
        chartLevelAir.setDragEnabled(true);
        chartLevelAir.setScaleEnabled(false);
        chartLevelAir.setExtraOffsets(0f, 10f, 0f, 10f);
        
        XAxis xAxis = chartLevelAir.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.parseColor("#E0E0E0"));
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setTextColor(Color.parseColor("#9E9E9E"));
        xAxis.setTextSize(11f);

        chartLevelAir.getAxisRight().setEnabled(false);
        
        YAxis yAxis = chartLevelAir.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.parseColor("#F5F5F5"));
        yAxis.setGridLineWidth(1f);
        yAxis.enableGridDashedLine(10f, 10f, 0f);
        yAxis.setDrawAxisLine(false);
        yAxis.setTextColor(Color.parseColor("#9E9E9E"));
        yAxis.setTextSize(11f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
    }

    private void setupChartFilters() {
        if (tvFilterMinggu == null || tvFilterBulan == null) return;

        tvFilterMinggu.setOnClickListener(v -> {
            tvFilterMinggu.setBackgroundResource(R.drawable.bg_filter_active);
            tvFilterMinggu.setTextColor(Color.WHITE);
            tvFilterBulan.setBackgroundResource(android.R.color.transparent);
            tvFilterBulan.setTextColor(Color.parseColor("#1976D2"));
            loadChartDataMingguan();
        });

        tvFilterBulan.setOnClickListener(v -> {
            tvFilterBulan.setBackgroundResource(R.drawable.bg_filter_active);
            tvFilterBulan.setTextColor(Color.WHITE);
            tvFilterMinggu.setBackgroundResource(android.R.color.transparent);
            tvFilterMinggu.setTextColor(Color.parseColor("#1976D2"));
            loadChartDataBulanan();
        });
    }

    private void loadRealtimeSensorData() {
        DatabaseReference sensorRef = FirebaseDatabase.getInstance().getReference("Sensors");

        // Listener untuk TDS
        sensorRef.child("tds_ppm").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && tvTDS != null && progressTDS != null) {
                    float tdsValue = snapshot.getValue(Float.class);
                    currentTds = tdsValue;
                    tvTDS.setText(String.format("%.0f", tdsValue));
                    
                    // Update progress bar (asumsi max TDS 1000 untuk tampilan visual)
                    int progress = (int) ((tdsValue / 1000f) * 100);
                    progressTDS.setProgress(Math.min(progress, 100));

                    checkAndLogHistory();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Dashboard", "Gagal membaca TDS: " + error.getMessage());
            }
        });

        // Listener untuk Water Level
        sensorRef.child("water_level_pct").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && tvLevelAir != null && progressAir != null) {
                    int waterLevel = snapshot.getValue(Integer.class);
                    currentAir = waterLevel;
                    tvLevelAir.setText(String.valueOf(waterLevel));
                    progressAir.setProgress(waterLevel);

                    checkAndLogHistory();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Dashboard", "Gagal membaca Water Level: " + error.getMessage());
            }
        });
    }

    private void checkAndLogHistory() {
        long currentTime = System.currentTimeMillis();
        // Log setiap 1 menit (60000 ms) agar database tidak penuh
        if (currentTime - lastLogTime > 60000) {
            lastLogTime = currentTime;
            DatabaseReference logRef = FirebaseDatabase.getInstance().getReference("Sensors/history_logs");
            String key = logRef.push().getKey();
            if(key != null) {
                HistoryLog log = new HistoryLog(currentTds, currentAir, currentTime);
                logRef.child(key).setValue(log);
            }
        }
        
        // Panggil juga pengecekan notifikasi
        checkAndPushNotifications();
    }

    private void checkAndPushNotifications() {
        long currentTime = System.currentTimeMillis();
        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("Sensors/notifications");

        // 1. Level air rendah ( < 30%)
        if (currentAir > 0 && currentAir < 30 && (currentTime - lastWaterLevelNotifTime > NOTIF_COOLDOWN)) {
            lastWaterLevelNotifTime = currentTime;
            String key = notifRef.push().getKey();
            if (key != null) {
                NotifikasiLog log = new NotifikasiLog("Level Air Rendah", "Level air di bawah 30%, segera isi ulang tangki untuk mencegah kerusakan sistem.", "error", currentTime, false);
                notifRef.child(key).setValue(log);
            }
        }

        // 2. Pemberian nutrisi berlebihan (misal TDS > 1000)
        if (currentTds > 1000 && (currentTime - lastHighTdsNotifTime > NOTIF_COOLDOWN)) {
            lastHighTdsNotifTime = currentTime;
            String key = notifRef.push().getKey();
            if (key != null) {
                NotifikasiLog log = new NotifikasiLog("Nutrisi Berlebih", "Nilai TDS melebihi batas aman (> 1000 ppm). Tambahkan air bersih untuk menormalkan.", "error", currentTime, false);
                notifRef.child(key).setValue(log);
            }
        }

        // 3. Kekurangan nutrisi (misal TDS < 400)
        if (currentTds > 0 && currentTds < 400 && (currentTime - lastLowTdsNotifTime > NOTIF_COOLDOWN)) {
            lastLowTdsNotifTime = currentTime;
            String key = notifRef.push().getKey();
            if (key != null) {
                NotifikasiLog log = new NotifikasiLog("Kekurangan Nutrisi", "Nilai TDS di bawah batas ideal (< 400 ppm). Pompa nutrisi perlu diaktifkan.", "warning", currentTime, false);
                notifRef.child(key).setValue(log);
            }
        }
    }

    private void loadChartDataMingguan() {
        if (chartLevelAir == null) return;
        
        if (currentChartRef != null && currentChartListener != null) {
            currentChartRef.removeEventListener(currentChartListener);
        }
        currentChartRef = FirebaseDatabase.getInstance().getReference("Sensors/history/mingguan");
        currentChartListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Entry> entries = new ArrayList<>();
                int i = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Float val = data.getValue(Float.class);
                    if (val != null) {
                        entries.add(new Entry(i++, val));
                    }
                }
                
                // Jika kosong dari Firebase, gunakan default (opsional)
                if (entries.isEmpty()) {
                    entries.add(new Entry(0, 60f));
                    entries.add(new Entry(1, 65f));
                    entries.add(new Entry(2, 70f));
                    entries.add(new Entry(3, 50f));
                    entries.add(new Entry(4, 75f));
                    entries.add(new Entry(5, 80f));
                    entries.add(new Entry(6, 75f));
                }

                String[] days = new String[]{"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
                chartLevelAir.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(days));
                chartLevelAir.getXAxis().setLabelCount(days.length, true);

                setChartData(entries, "Mingguan");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Dashboard", "Gagal mengambil data mingguan: " + error.getMessage());
            }
        };
        currentChartRef.addValueEventListener(currentChartListener);
    }

    private void loadChartDataBulanan() {
        if (chartLevelAir == null) return;
        
        if (currentChartRef != null && currentChartListener != null) {
            currentChartRef.removeEventListener(currentChartListener);
        }
        currentChartRef = FirebaseDatabase.getInstance().getReference("Sensors/history/bulanan");
        currentChartListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Entry> entries = new ArrayList<>();
                int i = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Float val = data.getValue(Float.class);
                    if (val != null) {
                        entries.add(new Entry(i++, val));
                    }
                }
                
                if (entries.isEmpty()) {
                    entries.add(new Entry(0, 75f));
                    entries.add(new Entry(1, 72f));
                    entries.add(new Entry(2, 78f));
                    entries.add(new Entry(3, 85f));
                    entries.add(new Entry(4, 80f));
                }

                String[] weeks = new String[]{"Mg 1", "Mg 2", "Mg 3", "Mg 4", "Mg 5"};
                chartLevelAir.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(weeks));
                chartLevelAir.getXAxis().setLabelCount(weeks.length, true);

                setChartData(entries, "Bulanan");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Dashboard", "Gagal mengambil data bulanan: " + error.getMessage());
            }
        };
        currentChartRef.addValueEventListener(currentChartListener);
    }

    private void setChartData(List<Entry> entries, String label) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(Color.parseColor("#1976D2"));
        dataSet.setLineWidth(3.5f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#0D47A1"));
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });
        
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.parseColor("#FFFFFF"));
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.parseColor("#1976D2"));
        dataSet.setCircleHoleRadius(3f);

        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.bg_chart_gradient);
        if (drawable != null) {
            dataSet.setFillDrawable(drawable);
        } else {
            dataSet.setFillColor(Color.parseColor("#801976D2"));
        }

        LineData lineData = new LineData(dataSet);
        chartLevelAir.setData(lineData);
        chartLevelAir.animateXY(1000, 1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
        chartLevelAir.invalidate();
    }
}
