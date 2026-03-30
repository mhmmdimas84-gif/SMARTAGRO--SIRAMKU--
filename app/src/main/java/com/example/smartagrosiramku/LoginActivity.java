package com.example.smartagrosiramku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvTime, tvForgotPassword, tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setCurrentTime();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvTime = findViewById(R.id.tvTime);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvTime.setText(currentTime);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,
                        "Fitur lupa password akan segera tersedia",
                        Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,
                        "Silakan hubungi admin untuk registrasi",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email tidak boleh kosong");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password tidak boleh kosong");
            return;
        }

        // Cek kredensial demo
        if (email.equals("admin@siramku.id") && password.equals("siramku123")) {
            // Login berhasil
            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();

            // Simulasi proses login
            btnLogin.setEnabled(false);
            btnLogin.setText("Memuat...");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Pindah ke activity Dashboard
                    Intent intent = new Intent(LoginActivity.this, Dashboard.class);
                    startActivity(intent);
                    finish();
                }
            }, 1500);

        } else {
            // Login gagal
            Toast.makeText(this,
                    "Email atau password salah! Gunakan akun demo.",
                    Toast.LENGTH_LONG).show();

            // Isi otomatis akun demo untuk memudahkan
            etEmail.setText("admin@siramku.id");
            etPassword.setText("siramku123");
        }
    }
}
