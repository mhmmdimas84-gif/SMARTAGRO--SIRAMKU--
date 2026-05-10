package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Notifikasi extends AppCompatActivity {

    // Deklarasi View
    private TextView tabSemua, tabBelumDibaca;
    private TextView tvDashboard, tvHistory, tvControl, tvAccount;
    private ImageView btnBack;

    // RecyclerView & Adapter
    private RecyclerView rvNotifikasi;
    private NotifikasiAdapter adapter;
    private List<NotifikasiLog> allNotifList;
    private List<NotifikasiLog> filteredNotifList;
    private boolean showingAll = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifikasi);

        // Inisialisasi Views
        initializeViews();

        // Setup listeners
        setupListeners();

        // Setup bottom navigation
        setupBottomNavigation();

        // Load data from Firebase
        loadNotifications();
    }

    private void initializeViews() {
        // Inisialisasi Tab
        tabSemua = findViewById(R.id.tabSemua);
        tabBelumDibaca = findViewById(R.id.tabBelumDibaca);
        
        // Inisialisasi Tombol Kembali
        btnBack = findViewById(R.id.btnBack);

        // Inisialisasi RecyclerView
        rvNotifikasi = findViewById(R.id.rvNotifikasi);
        rvNotifikasi.setLayoutManager(new LinearLayoutManager(this));
        allNotifList = new ArrayList<>();
        filteredNotifList = new ArrayList<>();
        adapter = new NotifikasiAdapter(filteredNotifList);
        rvNotifikasi.setAdapter(adapter);

        // Inisialisasi Bottom Navigation
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory = findViewById(R.id.tvHistory);
        tvControl = findViewById(R.id.tvControl);
        tvAccount = findViewById(R.id.tvAccount);
    }

    private void setupListeners() {
        // Listener untuk Tombol Kembali
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Listener untuk Tab Semua
        if (tabSemua != null) {
            tabSemua.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Ubah style tab
                    tabSemua.setBackgroundResource(R.drawable.bg_tab_active);
                    tabSemua.setTextColor(Color.WHITE);
                    if (tabBelumDibaca != null) {
                        tabBelumDibaca.setBackgroundResource(android.R.color.transparent);
                        tabBelumDibaca.setTextColor(Color.parseColor("#757575"));
                    }
                    showingAll = true;
                    applyFilter();
                }
            });
        }

        // Listener untuk Tab Belum Dibaca
        if (tabBelumDibaca != null) {
            tabBelumDibaca.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Ubah style tab
                    tabBelumDibaca.setBackgroundResource(R.drawable.bg_tab_active);
                    tabBelumDibaca.setTextColor(Color.WHITE);
                    if (tabSemua != null) {
                        tabSemua.setBackgroundResource(android.R.color.transparent);
                        tabSemua.setTextColor(Color.parseColor("#757575"));
                    }
                    showingAll = false;
                    applyFilter();
                }
            });
        }
    }

    private void loadNotifications() {
        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("Sensors/notifications");
        notifRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allNotifList.clear();
                int unreadCount = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    NotifikasiLog log = data.getValue(NotifikasiLog.class);
                    if (log != null) {
                        log.id = data.getKey();
                        allNotifList.add(log);
                        if (!log.isRead) {
                            unreadCount++;
                        }
                    }
                }
                Collections.reverse(allNotifList);
                
                // Update text tabs with count
                if (tabSemua != null) tabSemua.setText("Semua (" + allNotifList.size() + ")");
                if (tabBelumDibaca != null) tabBelumDibaca.setText("Belum Dibaca (" + unreadCount + ")");
                
                applyFilter();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                showToast("Gagal memuat notifikasi");
            }
        });
    }

    private void applyFilter() {
        filteredNotifList.clear();
        for (NotifikasiLog log : allNotifList) {
            if (showingAll || !log.isRead) {
                filteredNotifList.add(log);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupBottomNavigation() {
        if (tvDashboard != null) {
            tvDashboard.setOnClickListener(v -> {
                startActivity(new Intent(Notifikasi.this, Dashboard.class));
                finish();
            });
        }
        if (tvHistory != null) {
            tvHistory.setOnClickListener(v -> {
                startActivity(new Intent(Notifikasi.this, HistoryActivity.class));
                finish();
            });
        }
        if (tvControl != null) {
            tvControl.setOnClickListener(v -> {
                startActivity(new Intent(Notifikasi.this, ControlActivity.class));
                finish();
            });
        }
        if (tvAccount != null) {
            tvAccount.setOnClickListener(v -> {
                startActivity(new Intent(Notifikasi.this, AccountActivity.class));
                finish();
            });
        }
    }



    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
