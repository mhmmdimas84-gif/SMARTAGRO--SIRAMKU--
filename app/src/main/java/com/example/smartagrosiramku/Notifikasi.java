package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Notifikasi extends AppCompatActivity {

    // Deklarasi View
    private TextView tabSemua, tabBelumDibaca;
    private TextView tvDashboard, tvHistory, tvControl, tvNotifications, tvAccount;

    // Tombol aksi
    private TextView btnTandaiDibaca1, btnHapus1;
    private TextView btnTandaiDibaca2, btnHapus2;
    private TextView btnHapus3;

    // CardView notifikasi
    private CardView notifikasi1, notifikasi2, notifikasi3;

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
    }

    private void initializeViews() {
        // Inisialisasi Tab
        tabSemua = findViewById(R.id.tabSemua);
        tabBelumDibaca = findViewById(R.id.tabBelumDibaca);

        // Inisialisasi Tombol Aksi
        btnTandaiDibaca1 = findViewById(R.id.btnTandaiDibaca1);
        btnHapus1 = findViewById(R.id.btnHapus1);
        btnTandaiDibaca2 = findViewById(R.id.btnTandaiDibaca2);
        btnHapus2 = findViewById(R.id.btnHapus2);
        btnHapus3 = findViewById(R.id.btnHapus3);

        // Inisialisasi CardView Notifikasi
        notifikasi1 = findViewById(R.id.notifikasi1);
        notifikasi2 = findViewById(R.id.notifikasi2);
        notifikasi3 = findViewById(R.id.notifikasi3);

        // Inisialisasi Bottom Navigation
        tvDashboard = findViewById(R.id.tvDashboard);
        tvHistory = findViewById(R.id.tvHistory);
        tvControl = findViewById(R.id.tvControl);
        tvNotifications = findViewById(R.id.tvNotifications);
        tvAccount = findViewById(R.id.tvAccount);
    }

    private void setupListeners() {
        // Listener untuk Tab Semua
        tabSemua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ubah style tab
                tabSemua.setBackgroundColor(getColor(R.color.light_green));
                tabSemua.setTextColor(getColor(R.color.primary_green));
                tabBelumDibaca.setBackgroundColor(getColor(android.R.color.white));
                tabBelumDibaca.setTextColor(getColor(R.color.text_secondary));

                // Tampilkan semua notifikasi
                showAllNotifications();

                showToast("Menampilkan semua notifikasi");
            }
        });

        // Listener untuk Tab Belum Dibaca
        tabBelumDibaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ubah style tab
                tabBelumDibaca.setBackgroundColor(getColor(R.color.light_green));
                tabBelumDibaca.setTextColor(getColor(R.color.primary_green));
                tabSemua.setBackgroundColor(getColor(android.R.color.white));
                tabSemua.setTextColor(getColor(R.color.text_secondary));

                // Tampilkan hanya notifikasi yang belum dibaca
                showUnreadNotifications();

                showToast("Menampilkan notifikasi belum dibaca");
            }
        });

        // Listener untuk Tombol Tandai Dibaca (Notifikasi 1)
        if (btnTandaiDibaca1 != null) {
            btnTandaiDibaca1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    markAsRead(notifikasi1);
                    showToast("Notifikasi ditandai sudah dibaca");
                }
            });
        }

        // Listener untuk Tombol Hapus (Notifikasi 1)
        if (btnHapus1 != null) {
            btnHapus1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNotification(notifikasi1);
                    showToast("Notifikasi dihapus");
                }
            });
        }

        // Listener untuk Tombol Tandai Dibaca (Notifikasi 2)
        if (btnTandaiDibaca2 != null) {
            btnTandaiDibaca2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    markAsRead(notifikasi2);
                    showToast("Notifikasi ditandai sudah dibaca");
                }
            });
        }

        // Listener untuk Tombol Hapus (Notifikasi 2)
        if (btnHapus2 != null) {
            btnHapus2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNotification(notifikasi2);
                    showToast("Notifikasi dihapus");
                }
            });
        }

        // Listener untuk Tombol Hapus (Notifikasi 3)
        if (btnHapus3 != null) {
            btnHapus3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNotification(notifikasi3);
                    showToast("Notifikasi dihapus");
                }
            });
        }
    }

    private void setupBottomNavigation() {
        // Dashboard
        tvDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Notifikasi.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        });

        // Histori
        tvHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Notifikasi.this, HistoryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Kontrol
        tvControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Notifikasi.this, ControlActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Notifikasi (current activity)
        tvNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Anda sedang di halaman Notifikasi");
            }
        });

        // Akun
        tvAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Notifikasi.this, AccountActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void markAsRead(CardView notification) {
        if (notification != null) {
            // Ubah background menjadi putih (tanda sudah dibaca)
            notification.setCardBackgroundColor(getColor(android.R.color.white));
        }
    }

    private void deleteNotification(CardView notification) {
        if (notification != null) {
            notification.setVisibility(View.GONE);
        }
    }

    private void showAllNotifications() {
        // Tampilkan semua notifikasi
        if (notifikasi1 != null) notifikasi1.setVisibility(View.VISIBLE);
        if (notifikasi2 != null) notifikasi2.setVisibility(View.VISIBLE);
        if (notifikasi3 != null) notifikasi3.setVisibility(View.VISIBLE);
    }

    private void showUnreadNotifications() {
        // Tampilkan hanya notifikasi yang belum dibaca
        if (notifikasi1 != null) notifikasi1.setVisibility(View.VISIBLE);
        if (notifikasi2 != null) notifikasi2.setVisibility(View.VISIBLE);
        if (notifikasi3 != null) notifikasi3.setVisibility(View.GONE); // Notifikasi 3 sudah dibaca
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
