package com.example.smartagrosiramku;

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

/**
 * WifiConfigActivity - Halaman konfigurasi WiFi untuk ESP32 SIRAMKU.
 *
 * Cara kerja:
 * 1. Pengguna menghubungkan ponsel ke AP "SIRAMKU_SETUP" (password: 12345678)
 * 2. Halaman ini mengirim HTTP GET ke 192.168.4.1/status untuk cek status ESP32
 * 3. Pengguna memasukkan SSID + password WiFi baru
 * 4. Aplikasi mengirim HTTP POST ke 192.168.4.1/setwifi dengan body "SSID,PASSWORD"
 * 5. ESP32 menyimpan credentials ke NVS, restart, dan terhubung ke WiFi baru
 */
public class WifiConfigActivity extends AppCompatActivity {

    // IP default Access Point ESP32
    private static final String ESP_AP_IP = "192.168.4.1";
    private static final int CONNECT_TIMEOUT = 5000;  // 5 detik
    private static final int READ_TIMEOUT    = 5000;  // 5 detik

    // Views - Info perangkat
    private TextView tvStatusBadge, tvDeviceName, tvDeviceStatus;
    private TextView tvCurrentSSID, tvIPAddress;

    // Views - Form input
    private TextInputEditText etSSID, etPassword;
    private Button btnConnect;
    private LinearLayout layoutLoading;
    private TextView tvLoadingText;

    // Views - Status result
    private CardView cardStatusResult;
    private View viewStatusDot;
    private TextView tvStatusResult;

    // Threading
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_config);

        initializeViews();
        setupListeners();

        // Otomatis cek status ESP32 saat halaman dibuka
        checkESP32Status();
    }

    private void initializeViews() {
        // Tombol kembali (menggunakan icon wifi yang dirotasi sebagai arrow)
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setImageResource(android.R.drawable.ic_menu_revert);
        btnBack.setRotation(0);

        // Info perangkat
        tvStatusBadge  = findViewById(R.id.tvStatusBadge);
        tvDeviceName   = findViewById(R.id.tvDeviceName);
        tvDeviceStatus = findViewById(R.id.tvDeviceStatus);
        tvCurrentSSID  = findViewById(R.id.tvCurrentSSID);
        tvIPAddress    = findViewById(R.id.tvIPAddress);

        // Form input
        etSSID      = findViewById(R.id.etSSID);
        etPassword  = findViewById(R.id.etPassword);
        btnConnect  = findViewById(R.id.btnConnect);

        // Loading
        layoutLoading = findViewById(R.id.layoutLoading);
        tvLoadingText = findViewById(R.id.tvLoadingText);

        // Status result
        cardStatusResult = findViewById(R.id.cardStatusResult);
        viewStatusDot    = findViewById(R.id.viewStatusDot);
        tvStatusResult   = findViewById(R.id.tvStatusResult);
    }

    private void setupListeners() {
        // Tombol kembali
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Tombol Hubungkan
        btnConnect.setOnClickListener(v -> {
            String ssid = etSSID.getText() != null ? etSSID.getText().toString().trim() : "";
            String pass = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (ssid.isEmpty()) {
                etSSID.setError("SSID tidak boleh kosong");
                etSSID.requestFocus();
                return;
            }

            sendWifiConfig(ssid, pass);
        });
    }

    /**
     * Cek status ESP32 via HTTP GET ke /status
     * Format respons: DEVICE_NAME|STATUS|SSID|IP_ADDRESS
     */
    private void checkESP32Status() {
        tvStatusBadge.setText("Mengecek...");
        tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_inactive);
        tvDeviceStatus.setText("Mengecek...");
        tvDeviceStatus.setTextColor(0xFF757575);

        executor.execute(() -> {
            try {
                URL url = new URL("http://" + ESP_AP_IP + "/status");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String response = reader.readLine();
                    reader.close();
                    conn.disconnect();

                    // Parse: DEVICE_NAME|STATUS|SSID|IP_ADDRESS
                    String[] parts = response.split("\\|");
                    if (parts.length >= 4) {
                        String deviceName = parts[0];
                        String status     = parts[1];
                        String ssid       = parts[2];
                        String ipAddr     = parts[3];

                        mainHandler.post(() -> {
                            tvDeviceName.setText(deviceName);

                            if ("Online".equals(status)) {
                                tvDeviceStatus.setText("Online");
                                tvDeviceStatus.setTextColor(0xFF2E7D32);
                                tvStatusBadge.setText("● Online");
                                tvStatusBadge.setTextColor(0xFF2E7D32);
                                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_green);
                            } else {
                                tvDeviceStatus.setText("Offline");
                                tvDeviceStatus.setTextColor(0xFFF44336);
                                tvStatusBadge.setText("● Offline");
                                tvStatusBadge.setTextColor(0xFFF44336);
                                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_inactive);
                            }

                            tvCurrentSSID.setText(ssid);
                            tvIPAddress.setText(ipAddr);
                        });
                    }
                } else {
                    conn.disconnect();
                    mainHandler.post(this::setStatusOffline);
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(this::setStatusOffline);
            }
        });
    }

    private void setStatusOffline() {
        tvDeviceName.setText("ESP32 SIRAMKU");
        tvDeviceStatus.setText("Tidak Terhubung");
        tvDeviceStatus.setTextColor(0xFFF44336);
        tvStatusBadge.setText("● Offline");
        tvStatusBadge.setTextColor(0xFFF44336);
        tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_inactive);
        tvCurrentSSID.setText("-");
        tvIPAddress.setText("-");
    }

    /**
     * Kirim konfigurasi WiFi baru ke ESP32 via HTTP POST ke /setwifi
     * Body: SSID,PASSWORD (plain text dipisah koma)
     */
    private void sendWifiConfig(String ssid, String password) {
        // Tampilkan loading
        btnConnect.setEnabled(false);
        layoutLoading.setVisibility(View.VISIBLE);
        tvLoadingText.setText("Mengirim konfigurasi ke ESP32...");
        cardStatusResult.setVisibility(View.GONE);

        executor.execute(() -> {
            try {
                URL url = new URL("http://" + ESP_AP_IP + "/setwifi");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setRequestProperty("Content-Type", "text/plain");

                // Kirim body: SSID,PASSWORD
                String body = ssid + "," + password;
                OutputStream os = conn.getOutputStream();
                os.write(body.getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                String responseText = response.toString();

                mainHandler.post(() -> {
                    layoutLoading.setVisibility(View.GONE);
                    btnConnect.setEnabled(true);

                    if (responseCode == 200 && responseText.startsWith("OK")) {
                        // Berhasil!
                        showSuccessResult();
                        showSuccessDialog();
                    } else {
                        // Server merespons tapi ada error
                        showErrorResult("ESP32: " + responseText);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    layoutLoading.setVisibility(View.GONE);
                    btnConnect.setEnabled(true);
                    showErrorResult("Koneksi gagal, periksa SSID dan password.");
                    showErrorDialog();
                });
            }
        });
    }

    private void showSuccessResult() {
        cardStatusResult.setVisibility(View.VISIBLE);
        viewStatusDot.setBackgroundResource(R.drawable.bg_circle_green);
        tvStatusResult.setText("ESP32 berhasil dikonfigurasi! Perangkat akan restart dan terhubung ke WiFi baru.");
        tvStatusResult.setTextColor(0xFF2E7D32);

        // Refresh status setelah 5 detik (waktu ESP32 restart)
        mainHandler.postDelayed(this::checkESP32Status, 5000);
    }

    private void showErrorResult(String message) {
        cardStatusResult.setVisibility(View.VISIBLE);
        viewStatusDot.setBackgroundResource(R.drawable.bg_circle_red);
        tvStatusResult.setText(message);
        tvStatusResult.setTextColor(0xFFF44336);
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Berhasil! ✅")
                .setMessage("ESP32 berhasil terhubung ke jaringan WiFi.\n\nPerangkat akan restart secara otomatis dan terhubung ke jaringan baru.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Gagal ❌")
                .setMessage("Koneksi gagal, periksa SSID dan password.\n\nPastikan:\n• Ponsel terhubung ke WiFi SIRAMKU_SETUP\n• ESP32 dalam kondisi menyala\n• SSID dan password WiFi benar")
                .setPositiveButton("Coba Lagi", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
