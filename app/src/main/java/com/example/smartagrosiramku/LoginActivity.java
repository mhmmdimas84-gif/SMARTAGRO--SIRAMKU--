package com.example.smartagrosiramku;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private CheckBox cbCaptcha;
    private DatabaseHelper db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );
        
        sharedPreferences = getSharedPreferences("SiramkuPrefs", MODE_PRIVATE);
        
        // Cek jika user sudah login sebelumnya
        if (sharedPreferences.getBoolean("is_logged_in", false)) {
            Intent intent = new Intent(LoginActivity.this, Dashboard.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        initViews();
        // setCurrentTime(); // Removed because tvTime is not in layout
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnMasuk);
        tvRegister = findViewById(R.id.tvDaftar);
        cbCaptcha = findViewById(R.id.cbCaptcha);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Masukkan email dan password lalu klik Masuk untuk mendaftar otomatis", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbCaptcha.isChecked()) {
            Toast.makeText(this, "Silakan centang 'Saya bukan robot'", Toast.LENGTH_SHORT).show();
            return;
        }

        // Logic: Jika user belum ada, daftarkan. Jika sudah ada, cek password.
        if (!db.checkUserExists(email)) {
            // Daftarkan sebagai user baru (Gunakan bagian email sebagai nama awal)
            String name = email.split("@")[0];
            db.registerUser(name, email, password);
            Toast.makeText(this, "Akun baru berhasil dibuat!", Toast.LENGTH_SHORT).show();
            performLogin(email);
        } else {
            // Cek Login
            if (db.checkLogin(email, password)) {
                performLogin(email);
            } else {
                Toast.makeText(this, "Password salah!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void performLogin(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_email", email);
        editor.putBoolean("is_logged_in", true);
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, Dashboard.class);
        startActivity(intent);
        finish();
    }
}
