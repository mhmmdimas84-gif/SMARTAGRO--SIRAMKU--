package com.example.smartagrosiramku;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

public class AccountActivity extends AppCompatActivity {

    // Deklarasi View
    private SwitchCompat switchAutoMode, switchNotifications, switchSoundAlert, switchDarkMode;
    private CardView btnEditProfil;
    private TextView tvTelepon, tvLokasi, tvEmail, tvNama, tvBergabung;

    // SharedPreferences untuk menyimpan pengaturan
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "SiramkuPrefs";
    private static final String KEY_AUTO_MODE = "auto_mode";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_SOUND_ALERT = "sound_alert";
    private static final String KEY_DARK_MODE = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Inisialisasi Views
        initializeViews();

        // Setup listeners
        setupListeners();

        // Load saved settings
        loadSettings();

        // Setup click listeners untuk bottom navigation
        setupBottomNavigation();
    }

    private void initializeViews() {
        // Inisialisasi Switch
        switchAutoMode = findViewById(R.id.switchAutoMode);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchSoundAlert = findViewById(R.id.switchSoundAlert);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Inisialisasi Button
        btnEditProfil = findViewById(R.id.btnEditProfil);

        // Inisialisasi TextViews untuk info profil
        tvNama = findViewById(R.id.tvNama);
        tvEmail = findViewById(R.id.tvEmail);
        tvBergabung = findViewById(R.id.tvBergabung);
        tvTelepon = findViewById(R.id.tvTelepon);
        tvLokasi = findViewById(R.id.tvLokasi);
    }

    private void setupListeners() {
        // Listener untuk Switch Mode Otomatis
        switchAutoMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSetting(KEY_AUTO_MODE, isChecked);
                showToast("Mode Otomatis: " + (isChecked ? "Aktif" : "Nonaktif"));

                // Kirim broadcast atau panggil method untuk mengubah mode sistem
                updateAutoMode(isChecked);
            }
        });

        // Listener untuk Switch Notifikasi
        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSetting(KEY_NOTIFICATIONS, isChecked);
                showToast("Notifikasi: " + (isChecked ? "Aktif" : "Nonaktif"));

                // Update pengaturan notifikasi
                updateNotificationSettings(isChecked);
            }
        });

        // Listener untuk Switch Suara Alert
        switchSoundAlert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSetting(KEY_SOUND_ALERT, isChecked);
                showToast("Suara Alert: " + (isChecked ? "Aktif" : "Nonaktif"));

                // Update pengaturan suara
                updateSoundSettings(isChecked);
            }
        });

        // Listener untuk Switch Mode Gelap
        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSetting(KEY_DARK_MODE, isChecked);
                showToast("Mode Gelap: " + (isChecked ? "Aktif" : "Nonaktif"));

                // Terapkan mode gelap
                applyDarkMode(isChecked);
            }
        });

        // Listener untuk Button Edit Profil
        btnEditProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buka halaman edit profil
                openEditProfile();
            }
        });
    }

    private void setupBottomNavigation() {
        // Dashboard
        findViewById(R.id.tvDashboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        });

        // Histori
        findViewById(R.id.tvHistory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent ke HistoryActivity
                Intent intent = new Intent(AccountActivity.this, HistoryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Kontrol
        findViewById(R.id.tvControl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent ke ControlActivity
                Intent intent = new Intent(AccountActivity.this, ControlActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Notifikasi
        findViewById(R.id.tvNotifications).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent ke NotificationsActivity
                Intent intent = new Intent(AccountActivity.this, Notifikasi.class);
                startActivity(intent);
                finish();
            }
        });

        // Akun (sudah di halaman ini)
        findViewById(R.id.tvAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sudah di halaman akun
                showToast("Anda sedang di halaman Akun");
            }
        });
    }

    private void loadSettings() {
        // Load semua pengaturan dari SharedPreferences
        switchAutoMode.setChecked(sharedPreferences.getBoolean(KEY_AUTO_MODE, true));
        switchNotifications.setChecked(sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true));
        switchSoundAlert.setChecked(sharedPreferences.getBoolean(KEY_SOUND_ALERT, true));
        switchDarkMode.setChecked(sharedPreferences.getBoolean(KEY_DARK_MODE, false));
    }

    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void updateAutoMode(boolean isActive) {
        // Implementasi logic untuk mengubah mode otomatis sistem
    }

    private void updateNotificationSettings(boolean isActive) {
        // Implementasi logic untuk mengubah pengaturan notifikasi
    }

    private void updateSoundSettings(boolean isActive) {
        // Implementasi logic untuk mengubah pengaturan suara
    }

    private void applyDarkMode(boolean isDarkMode) {
        // Implementasi logic untuk menerapkan mode gelap
    }

    private void openEditProfile() {
        // Intent ke EditProfileActivity
        showToast("Fitur Edit Profil (Coming Soon)");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Method untuk update data profil dari server/database
    private void loadUserProfile() {
        // Contoh data statis
        tvNama.setText("Budi Santoso");
        tvEmail.setText("budi.santoso@email.com");
        tvBergabung.setText("Bergabung sejak 15 Januari 2024");
        tvTelepon.setText("+62 812-3456-7890");
        tvLokasi.setText("Jakarta, Indonesia");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data profil setiap kali halaman dibuka
        loadUserProfile();
    }
}
