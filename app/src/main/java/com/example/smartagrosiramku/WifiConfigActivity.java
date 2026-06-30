package com.example.smartagrosiramku;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WifiConfigActivity extends AppCompatActivity {

    private static final String ESP_AP_IP = "192.168.4.1";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT    = 5000;

    private TextView tvStatusBadge, tvDeviceName, tvDeviceStatus;
    private TextView tvCurrentSSID, tvIPAddress;
    private TextInputEditText etSSID, etPassword;
    private Button btnConnect;
    private LinearLayout layoutLoading;
    private TextView tvLoadingText;
    private CardView cardStatusResult;
    private View viewStatusDot;
    private TextView tvStatusResult;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_config);

        initializeViews();
        setupListeners();

        // Memaksa Android menggunakan jalur WiFi meskipun tidak ada internet
        forceRouteOverWifi();

        // Otomatis cek status ESP32
        checkESP32Status();
    }

    /**
     * Memaksa aplikasi untuk berkomunikasi melalui WiFi jika terhubung ke AP ESP32.
     * Ini sangat penting untuk Android 10 ke atas agar tidak "lari" ke Mobile Data.
     */
    private void forceRouteOverWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // Ikat proses aplikasi ke jaringan WiFi ini
                connectivityManager.bindProcessToNetwork(network);
                mainHandler.post(() -> {
                    Toast.makeText(WifiConfigActivity.this, "Terhubung ke jalur WiFi ESP32", Toast.LENGTH_SHORT).show();
                    checkESP32Status(); // Cek ulang status setelah jaringan siap
                });
            }

            @Override
            public void onLost(Network network) {
                connectivityManager.bindProcessToNetwork(null);
            }
        });
    }

    private void initializeViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setImageResource(android.R.drawable.ic_menu_revert);

        tvStatusBadge  = findViewById(R.id.tvStatusBadge);
        tvDeviceName   = findViewById(R.id.tvDeviceName);
        tvDeviceStatus = findViewById(R.id.tvDeviceStatus);
        tvCurrentSSID  = findViewById(R.id.tvCurrentSSID);
        tvIPAddress    = findViewById(R.id.tvIPAddress);
        etSSID      = findViewById(R.id.etSSID);
        etPassword  = findViewById(R.id.etPassword);
        btnConnect  = findViewById(R.id.btnConnect);
        layoutLoading = findViewById(R.id.layoutLoading);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        cardStatusResult = findViewById(R.id.cardStatusResult);
        viewStatusDot    = findViewById(R.id.viewStatusDot);
        tvStatusResult   = findViewById(R.id.tvStatusResult);
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnConnect.setOnClickListener(v -> {
            String ssid = etSSID.getText() != null ? etSSID.getText().toString().trim() : "";
            String pass = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            if (ssid.isEmpty()) {
                etSSID.setError("SSID tidak boleh kosong");
                return;
            }
            sendWifiConfig(ssid, pass);
        });
    }

    private void checkESP32Status() {
        mainHandler.post(() -> {
            tvStatusBadge.setText("Mengecek...");
            tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_inactive);
        });

        executor.execute(() -> {
            try {
                URL url = new URL("http://" + ESP_AP_IP + "/status");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String response = reader.readLine();
                    reader.close();

                    String[] parts = response.split("\\|");
                    if (parts.length >= 4) {
                        mainHandler.post(() -> updateUIStatus(parts[0], parts[1], parts[2], parts[3]));
                    }
                } else {
                    mainHandler.post(this::setStatusOffline);
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(this::setStatusOffline);
            }
        });
    }

    private void updateUIStatus(String name, String status, String ssid, String ip) {
        tvDeviceName.setText(name);
        tvCurrentSSID.setText(ssid);
        tvIPAddress.setText(ip);

        if ("Online".equalsIgnoreCase(status)) {
            tvDeviceStatus.setText("Online");
            tvDeviceStatus.setTextColor(0xFF2E7D32);
            tvStatusBadge.setText("● Online");
            tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
        } else {
            setStatusOffline();
        }
    }

    private void setStatusOffline() {
        tvDeviceStatus.setText("Tidak Terhubung");
        tvDeviceStatus.setTextColor(0xFFF44336);
        tvStatusBadge.setText("● Offline");
        tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_inactive);
    }

    private void sendWifiConfig(String ssid, String password) {
        btnConnect.setEnabled(false);
        layoutLoading.setVisibility(View.VISIBLE);
        tvLoadingText.setText("Mengirim konfigurasi...");

        executor.execute(() -> {
            try {
                URL url = new URL("http://" + ESP_AP_IP + "/setwifi");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setRequestProperty("Content-Type", "text/plain");

                String body = ssid + "," + password;
                OutputStream os = conn.getOutputStream();
                os.write(body.getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    mainHandler.post(() -> {
                        layoutLoading.setVisibility(View.GONE);
                        btnConnect.setEnabled(true);
                        showSuccessDialog();
                    });
                } else {
                    mainHandler.post(() -> {
                        layoutLoading.setVisibility(View.GONE);
                        btnConnect.setEnabled(true);
                        Toast.makeText(this, "Gagal mengirim data", Toast.LENGTH_SHORT).show();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    layoutLoading.setVisibility(View.GONE);
                    btnConnect.setEnabled(true);
                    showErrorDialog();
                });
            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Berhasil!")
                .setMessage("ESP32 akan segera terhubung ke WiFi baru dan restart.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Gagal")
                .setMessage("Pastikan Anda terhubung ke WiFi SIRAMKU_SETUP")
                .setPositiveButton("Coba Lagi", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
