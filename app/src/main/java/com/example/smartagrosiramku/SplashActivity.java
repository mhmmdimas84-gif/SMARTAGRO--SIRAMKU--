package com.example.smartagrosiramku;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Menampilkan splash screen selama 2 detik (3000 ms)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Berpindah ke LoginActivity
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Menutup SplashActivity agar tidak bisa kembali ke sini
            }
        }, 2000);
    }
}
