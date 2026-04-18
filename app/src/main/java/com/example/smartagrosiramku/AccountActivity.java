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
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity {

    private CardView btnEditProfil;
    private TextView tvEmail, tvNama;
    private TextView tvMerekPerangkat, tvSeriPerangkat, tvSistemOperasi;
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
        setupBottomNavigation();
    }

    private void initializeViews() {
        btnEditProfil = findViewById(R.id.btnEditProfil);
        tvNama = findViewById(R.id.tvNama);
        tvEmail = findViewById(R.id.tvEmail);
        tvMerekPerangkat = findViewById(R.id.tvMerekPerangkat);
        tvSeriPerangkat = findViewById(R.id.tvSeriPerangkat);
        tvSistemOperasi = findViewById(R.id.tvSistemOperasi);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupListeners() {

        btnEditProfil.setOnClickListener(v -> Toast.makeText(AccountActivity.this, "Fitur Edit Profil (Coming Soon)", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            
            // Logout dari Firebase jika login
            FirebaseAuth.getInstance().signOut();
            
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
        // tvNotifications was removed from bottom navigation layout
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        
        if (user != null) {
            // Data dari Akun Google / Firebase
            String name = user.getDisplayName();
            tvNama.setText(name != null && !name.isEmpty() ? name : "User Aplikasi");
            tvEmail.setText(user.getEmail());
        } else {
            // Data lama dari Database Lokal (Bukan Google)
            String email = sharedPreferences.getString("user_email", "");
            if (!email.isEmpty()) {
                Cursor cursor = db.getUserData(email);
                if (cursor != null && cursor.moveToFirst()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
                    tvNama.setText(name);
                    tvEmail.setText(email);
                    cursor.close();
                }
            }
        }
        
        // Membaca informasi detail perangkat
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer != null && manufacturer.length() > 0) {
            manufacturer = manufacturer.substring(0, 1).toUpperCase() + manufacturer.substring(1);
        }
        
        if (tvMerekPerangkat != null) tvMerekPerangkat.setText(manufacturer);
        if (tvSeriPerangkat != null) tvSeriPerangkat.setText(Build.MODEL);
        if (tvSistemOperasi != null) tvSistemOperasi.setText("Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }
}
