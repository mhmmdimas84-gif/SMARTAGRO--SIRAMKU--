package com.example.smartagrosiramku;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

public class AccountActivity extends AppCompatActivity {

    private SwitchCompat switchAutoMode, switchNotifications, switchSoundAlert, switchDarkMode;
    private CardView btnEditProfil;
    private TextView tvEmail, tvNama, tvBergabung;
    private Button btnLogout;

    private SharedPreferences sharedPreferences;
    private DatabaseHelper db;
    private static final String PREF_NAME = "SiramkuPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        db = new DatabaseHelper(this);

        initializeViews();
        setupListeners();
        loadSettings();
        setupBottomNavigation();
    }

    private void initializeViews() {
        switchAutoMode = findViewById(R.id.switchAutoMode);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchSoundAlert = findViewById(R.id.switchSoundAlert);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        btnEditProfil = findViewById(R.id.btnEditProfil);
        tvNama = findViewById(R.id.tvNama);
        tvEmail = findViewById(R.id.tvEmail);
        tvBergabung = findViewById(R.id.tvBergabung);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupListeners() {
        switchAutoMode.setOnCheckedChangeListener((buttonView, isChecked) -> saveSetting("auto_mode", isChecked));
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> saveSetting("notifications", isChecked));
        switchSoundAlert.setOnCheckedChangeListener((buttonView, isChecked) -> saveSetting("sound_alert", isChecked));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> saveSetting("dark_mode", isChecked));

        btnEditProfil.setOnClickListener(v -> Toast.makeText(AccountActivity.this, "Fitur Edit Profil (Coming Soon)", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupBottomNavigation() {
        findViewById(R.id.tvDashboard).setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, Dashboard.class));
            finish();
        });
        findViewById(R.id.tvHistory).setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, HistoryActivity.class));
            finish();
        });
        findViewById(R.id.tvControl).setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, ControlActivity.class));
            finish();
        });
        findViewById(R.id.tvNotifications).setOnClickListener(v -> {
            startActivity(new Intent(AccountActivity.this, Notifikasi.class));
            finish();
        });
    }

    private void loadSettings() {
        switchAutoMode.setChecked(sharedPreferences.getBoolean("auto_mode", true));
        switchNotifications.setChecked(sharedPreferences.getBoolean("notifications", true));
        switchSoundAlert.setChecked(sharedPreferences.getBoolean("sound_alert", true));
        switchDarkMode.setChecked(sharedPreferences.getBoolean("dark_mode", false));
    }

    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void loadUserProfile() {
        String email = sharedPreferences.getString("user_email", "");
        if (!email.isEmpty()) {
            Cursor cursor = db.getUserData(email);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
                tvNama.setText(name);
                tvEmail.setText(email);
                tvBergabung.setText("User Siramku Terverifikasi");
                cursor.close();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }
}
