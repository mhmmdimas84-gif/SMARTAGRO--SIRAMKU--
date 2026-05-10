package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import android.graphics.Color;

public class HistoryActivity extends AppCompatActivity {

    private TextView tabSemua, tabHariIni;
    private TextView tvDashboard, tvHistory, tvControl, tvAccount;
    private ImageButton btnNotif;

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private List<HistoryLog> historyList;

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

        // Load History Data
        loadHistoryData();
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
        
        // Inisialisasi Header Notification - Fixed ID mismatch
        btnNotif = findViewById(R.id.btnNotif);

        // Inisialisasi RecyclerView
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();
        adapter = new HistoryAdapter(historyList);
        rvHistory.setAdapter(adapter);
    }

    private void setupHeaderActions() {
        if (btnNotif != null) {
            btnNotif.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HistoryActivity.this, Notifikasi.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void loadHistoryData() {
        DatabaseReference logRef = FirebaseDatabase.getInstance().getReference("Sensors/history_logs");
        logRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    HistoryLog log = data.getValue(HistoryLog.class);
                    if (log != null) {
                        historyList.add(log);
                    }
                }
                // Reverse to show newest first
                Collections.reverse(historyList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HistoryActivity.this, "Gagal memuat histori", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Listener untuk Tab Semua
        if (tabSemua != null) {
            tabSemua.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tabSemua.setBackgroundResource(R.drawable.bg_tab_active);
                    tabSemua.setTextColor(Color.WHITE);
                    if (tabHariIni != null) {
                        tabHariIni.setBackgroundResource(android.R.color.transparent);
                        tabHariIni.setTextColor(Color.parseColor("#757575"));
                    }
                    Toast.makeText(HistoryActivity.this, "Menampilkan semua histori", Toast.LENGTH_SHORT).show();
                    loadHistoryData();
                }
            });
        }

        // Listener untuk Tab Hari Ini
        if (tabHariIni != null) {
            tabHariIni.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tabHariIni.setBackgroundResource(R.drawable.bg_tab_active);
                    tabHariIni.setTextColor(Color.WHITE);
                    if (tabSemua != null) {
                        tabSemua.setBackgroundResource(android.R.color.transparent);
                        tabSemua.setTextColor(Color.parseColor("#757575"));
                    }
                    Toast.makeText(HistoryActivity.this, "Menampilkan histori hari ini", Toast.LENGTH_SHORT).show();
                    loadHistoryDataHariIni();
                }
            });
        }
    }

    private void loadHistoryDataHariIni() {
        DatabaseReference logRef = FirebaseDatabase.getInstance().getReference("Sensors/history_logs");
        logRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                historyList.clear();
                
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                long startOfDay = cal.getTimeInMillis();

                for (DataSnapshot data : snapshot.getChildren()) {
                    HistoryLog log = data.getValue(HistoryLog.class);
                    if (log != null && log.timestamp >= startOfDay) {
                        historyList.add(log);
                    }
                }
                Collections.reverse(historyList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HistoryActivity.this, "Gagal memuat histori", Toast.LENGTH_SHORT).show();
            }
        });
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
