/*
 * ================================================================
 * SmartAgro Siramku - ESP32 Firmware
 * Platform : PlatformIO + Arduino Framework
 * Deskripsi: Membaca Sensor TDS & Water Level, mengendalikan
 *            2 pompa via Relay, dan sinkronisasi dua arah
 *            dengan Firebase Realtime Database.
 *
 *            FITUR BARU: Konfigurasi WiFi via Aplikasi Android
 *            ESP32 menjalankan Access Point (SIRAMKU_SETUP)
 *            bersamaan dengan mode Station. Aplikasi Android
 *            terhubung ke AP, mengirim SSID+Password baru via
 *            HTTP POST, dan ESP32 menyimpan ke NVS lalu restart.
 * ================================================================
 *
 * === KONFIGURASI PIN ===
 * Sensor TDS        (A)  --> GPIO 34
 * Sensor Water Level(S)  --> GPIO 35
 * Relay Pompa Air   (IN1)--> GPIO 13
 * Relay Pompa Nutrisi(IN2)-> GPIO 14
 * ================================================================
 */

#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>
#include <Firebase_ESP_Client.h>
// Wajib disertakan untuk helper addData dan error print
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// ================================================================
// KONFIGURASI - ISI SESUAI DATA ANDA
// ================================================================
#define DEFAULT_SSID     "Putrakembar"
#define DEFAULT_PASSWORD "Molymocy01"

// Access Point untuk konfigurasi WiFi dari Aplikasi Android
#define AP_SSID     "SIRAMKU_SETUP"
#define AP_PASSWORD "12345678"

// Ambil dari Firebase Console > Project Settings > General
// Contoh: "https://nama-proyek-default-rtdb.asia-southeast1.firebasedatabase.app"
#define DATABASE_URL  "https://siramku-10ead-default-rtdb.asia-southeast1.firebasedatabase.app"

// Ambil dari Firebase Console > Project Settings > Service Accounts > Database Secrets
// ATAU lebih aman: gunakan API Key dari Project Settings > General
#define API_KEY       "AIzaSyB-FTmbBeDp3It7aSm1uAN4WvF6SNfBJf0"

// ================================================================
// DEFINISI PIN
// ================================================================
#define PIN_TDS          34   // Sinyal Analog dari Sensor TDS
#define PIN_WATER_LEVEL  35   // Sinyal Analog dari Sensor Water Level
#define PIN_RELAY_POMPA_AIR      13   // IN1 Relay -> Pompa Air
#define PIN_RELAY_POMPA_NUTRISI  14   // IN2 Relay -> Pompa Nutrisi
#define I2C_SDA          22   // SDA LCD -> kabel terpasang ke GPIO 22 (D22)
#define I2C_SCL          21   // SCL LCD -> kabel terpasang ke GPIO 21 (D21)

// ================================================================
// KONFIGURASI THRESHOLD (AMBANG BATAS OTOMATIS)
// ================================================================
#define TDS_MIN         300   // PPM minimum (di bawah ini, pompa nutrisi ON otomatis)
#define WATER_LEVEL_MIN  20   // Persentase minimum (di bawah ini, buzzer/alert ON)

// ================================================================
// VARIABEL GLOBAL
// ================================================================
FirebaseData   fbdo;        // Untuk semua operasi Firebase (read & write)
FirebaseAuth   auth;
FirebaseConfig config;

// LCD - alamat akan di-deteksi otomatis saat setup()
// Kedua alamat yang umum: 0x27 atau 0x3F
LiquidCrystal_I2C lcd(0x27, 16, 2); // default, bisa berubah saat runtime

// Preferences (NVS) untuk menyimpan WiFi credentials
Preferences prefs;

// WebServer untuk menerima konfigurasi WiFi dari aplikasi Android
WebServer server(80);

// WiFi credentials yang aktif (dimuat dari NVS saat boot)
String activeSSID;
String activePassword;

unsigned long lastSendTime  = 0;
const long    SEND_INTERVAL = 5000; // Kirim data setiap 5 detik

bool pompaAirStatus     = false;
bool pompaNutrisiStatus = false;

// Indeks slot histori (0=Sen, 1=Sel, ..., 6=Min)
// Setiap hari indeks akan bergeser saat ESP32 baru menyala.
// Untuk testing: nilai dimulai dari 0 dan bertambah otomatis.
int  indeksHari = 0;
unsigned long lastHistoriTime = 0;
const long    HISTORI_INTERVAL = 60000; // Update histori setiap 1 menit

// Flag apakah WiFi Station berhasil terhubung
bool wifiConnected = false;

// ================================================================
// FUNGSI PEMBANTU: Konversi ADC ESP32 ke Persentase Level Air
// ================================================================
int bacaWaterLevel() {
    int raw = analogRead(PIN_WATER_LEVEL); // Nilai 0 - 4095
    // Sensor analog water level: semakin dalam air, nilai ADC semakin tinggi
    // Kalibrasi: 0 = kering, 4095 = tercelup penuh (100%)
    int persentase = map(raw, 0, 4095, 0, 100);
    return constrain(persentase, 0, 100);
}

// ================================================================
// FUNGSI PEMBANTU: Konversi ADC ESP32 ke nilai TDS (PPM)
// ================================================================
float bacaTDS() {
    int raw = analogRead(PIN_TDS); // Nilai 0 - 4095 dari ESP32 12-bit ADC
    // Rumus konversi tegangan ke PPM (perkiraan untuk sensor analog murah)
    float voltase = raw * (3.3 / 4095.0);
    // Koefisien kalibrasi standar dari datasheet TDS Meter v1.0
    float tds = (133.42 * voltase * voltase * voltase 
               - 255.86 * voltase * voltase 
               + 857.39 * voltase) * 0.5;
    return tds > 0 ? tds : 0;
}

// ================================================================
// FUNGSI: Atur status Relay
// ================================================================
void setRelay(int pin, bool nyala) {
    // Relay umumnya bersifat ACTIVE LOW:
    // LOW  = Relay aktif (pompa NYALA)
    // HIGH = Relay tidak aktif (pompa MATI)
    digitalWrite(pin, nyala ? LOW : HIGH);
}

// ================================================================
// FUNGSI: Dengarkan perintah dari Firebase (Read)
// ================================================================
void listenKontrolDariFirebase() {
    // Baca status Pompa Air dari Firebase
    if (Firebase.RTDB.getBool(&fbdo, "Controls/pompa_air/status")) {
        bool statusBaru = fbdo.boolData();
        if (statusBaru != pompaAirStatus) {
            pompaAirStatus = statusBaru;
            setRelay(PIN_RELAY_POMPA_AIR, pompaAirStatus);
            Serial.printf("[Firebase] Pompa Air -> %s\n", pompaAirStatus ? "NYALA" : "MATI");
        }
    } else {
        Serial.printf("[ERROR] Gagal baca pompa_air: %s\n", fbdo.errorReason().c_str());
    }
}

// ================================================================
// FUNGSI: Kirim data sensor ke Firebase (Write)
// ================================================================
void kirimDataSensor(float tds, int waterLevel) {
    // Kirim TDS ke Firebase
    if (Firebase.RTDB.setFloat(&fbdo, "Sensors/tds_ppm", tds)) {
        Serial.printf("[Firebase] TDS terkirim: %.1f PPM\n", tds);
    } else {
        Serial.printf("[ERROR] Gagal kirim TDS: %s\n", fbdo.errorReason().c_str());
    }

    // Kirim Water Level ke Firebase
    if (Firebase.RTDB.setInt(&fbdo, "Sensors/water_level_pct", waterLevel)) {
        Serial.printf("[Firebase] Water Level terkirim: %d%%\n", waterLevel);
    } else {
        Serial.printf("[ERROR] Gagal kirim Water Level: %s\n", fbdo.errorReason().c_str());
    }

    // ---- Simpan ke histori mingguan (untuk grafik Dashboard Android) ----
    // Node: Sensors/history/mingguan/0 s/d /6 (Senin-Minggu)
    String pathMingguan = "Sensors/history/mingguan/" + String(indeksHari);
    Firebase.RTDB.setFloat(&fbdo, pathMingguan.c_str(), waterLevel);
    Serial.printf("[Firebase] Histori[%d] terkirim: %d%%\n", indeksHari, waterLevel);
}

// ================================================================
// FUNGSI: Setup HTTP Server untuk Konfigurasi WiFi
// ================================================================
void setupWifiConfigServer() {
    // ---- Endpoint: GET /status ----
    // Mengembalikan info status ESP32 dalam format plain text
    // Format: DEVICE_NAME|STATUS|SSID|IP_ADDRESS
    server.on("/status", HTTP_GET, []() {
        server.sendHeader("Access-Control-Allow-Origin", "*");
        
        String status = (WiFi.status() == WL_CONNECTED) ? "Online" : "Offline";
        String ssid   = (WiFi.status() == WL_CONNECTED) ? WiFi.SSID() : activeSSID;
        String ip     = (WiFi.status() == WL_CONNECTED) ? WiFi.localIP().toString() : "Tidak tersedia";
        
        // Format: DEVICE_NAME|STATUS|SSID|IP_ADDRESS
        String response = "ESP32 SIRAMKU|" + status + "|" + ssid + "|" + ip;
        server.send(200, "text/plain", response);
        
        Serial.println("[Web] GET /status -> " + response);
    });

    // ---- Endpoint: POST /setwifi ----
    // Menerima SSID dan password WiFi baru dari aplikasi Android
    // Format body: SSID,PASSWORD (dipisah koma)
    server.on("/setwifi", HTTP_POST, []() {
        server.sendHeader("Access-Control-Allow-Origin", "*");
        
        if (server.hasArg("plain")) {
            String body = server.arg("plain");
            int commaIdx = body.indexOf(',');
            
            if (commaIdx > 0) {
                String newSSID = body.substring(0, commaIdx);
                String newPass = body.substring(commaIdx + 1);
                
                // Validasi: SSID tidak boleh kosong
                newSSID.trim();
                newPass.trim();
                
                if (newSSID.length() == 0) {
                    server.send(400, "text/plain", "ERROR: SSID tidak boleh kosong");
                    return;
                }
                
                // Simpan ke NVS (Preferences)
                prefs.putString("wifi_ssid", newSSID);
                prefs.putString("wifi_pass", newPass);
                
                Serial.printf("[Web] WiFi credentials disimpan: SSID='%s'\n", newSSID.c_str());
                
                server.send(200, "text/plain", "OK: WiFi berhasil dikonfigurasi. ESP32 akan restart.");
                
                // Beri waktu respons terkirim sebelum restart
                delay(1500);
                ESP.restart();
                return;
            }
        }
        server.send(400, "text/plain", "ERROR: Format tidak valid. Gunakan: SSID,PASSWORD");
    });

    // ---- Endpoint: OPTIONS (CORS preflight) ----
    server.on("/setwifi", HTTP_OPTIONS, []() {
        server.sendHeader("Access-Control-Allow-Origin", "*");
        server.sendHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        server.sendHeader("Access-Control-Allow-Headers", "Content-Type");
        server.send(200);
    });

    server.begin();
    Serial.println("[Web] HTTP Server dimulai pada port 80");
}

// ================================================================
// FUNGSI: Koneksi WiFi Station
// ================================================================
bool connectWiFiStation() {
    Serial.print("[WiFi] Menghubungkan ke ");
    Serial.println(activeSSID);

    WiFi.begin(activeSSID.c_str(), activePassword.c_str());

    // Timeout: tunggu hingga 20 detik (40 x 500ms)
    int wifiTries = 0;
    while (WiFi.status() != WL_CONNECTED && wifiTries < 40) {
        delay(500);
        Serial.print(".");
        wifiTries++;
    }

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\n[WiFi] Terhubung! IP: " + WiFi.localIP().toString());
        return true;
    } else {
        Serial.println("\n[WiFi] GAGAL TERHUBUNG! Silakan konfigurasi via aplikasi.");
        return false;
    }
}

// ================================================================
// SETUP
// ================================================================
void setup() {
    Serial.begin(115200);
    delay(1000); // Beri waktu Serial Monitor untuk siap
    Serial.println("\n=== SmartAgro Siramku - Booting... ===");

    // ================================================================
    // INISIALISASI I2C & LCD
    // Wire.begin() HARUS dipanggil SATU KALI di sini dengan pin kustom.
    // Jangan panggil lcd.init() karena akan memanggil Wire.begin() ulang!
    // ================================================================
    Wire.begin(I2C_SDA, I2C_SCL);
    delay(300); // Tunggu I2C bus stabil

    // Scan I2C: cari alamat LCD yang aktif
    Serial.printf("[I2C] Scanning bus... SDA=GPIO%d SCL=GPIO%d\n", I2C_SDA, I2C_SCL);
    uint8_t alamatLCD = 0;
    for (uint8_t addr = 1; addr < 127; addr++) {
        Wire.beginTransmission(addr);
        if (Wire.endTransmission() == 0) {
            Serial.printf("[I2C] Device ditemukan di alamat: 0x%02X\n", addr);
            if (alamatLCD == 0) alamatLCD = addr; // Ambil alamat pertama
        }
    }

    if (alamatLCD == 0) {
        Serial.println("[LCD] GAGAL: Tidak ada device I2C ditemukan!");
        Serial.println("      Cek kabel: VCC=5V, GND, SDA, SCL");
    } else {
        Serial.printf("[LCD] Menggunakan alamat: 0x%02X\n", alamatLCD);
        
        // Gunakan alamat yang ditemukan scanner
        LiquidCrystal_I2C lcdTemp(alamatLCD, 16, 2);
        lcdTemp.begin(16, 2); // JANGAN lcd.init() -- akan reset Wire!
        lcdTemp.backlight();
        delay(200);
        lcdTemp.clear();
        lcdTemp.setCursor(0, 0);
        lcdTemp.print("SmartAgro Siram");
        lcdTemp.setCursor(0, 1);
        lcdTemp.print("Booting...");
        Serial.println("[LCD] Teks sudah dikirim ke layar.");

        // Perbarui juga objek lcd global agar loop() bisa pakai
        lcd = LiquidCrystal_I2C(alamatLCD, 16, 2);
        lcd.begin(16, 2);
        lcd.backlight();
    }

    // Inisialisasi pin
    pinMode(PIN_TDS,          INPUT);
    pinMode(PIN_WATER_LEVEL,  INPUT);
    pinMode(PIN_RELAY_POMPA_AIR,     OUTPUT);
    pinMode(PIN_RELAY_POMPA_NUTRISI, OUTPUT);

    // Pastikan pompa dalam kondisi MATI saat pertama menyala
    Serial.println("Mematikan Relay Pompa...");
    setRelay(PIN_RELAY_POMPA_AIR,     false);
    setRelay(PIN_RELAY_POMPA_NUTRISI, false);

    // ================================================================
    // INISIALISASI PREFERENCES (NVS) - Muat WiFi credentials tersimpan
    // ================================================================
    prefs.begin("wifi", false);
    activeSSID     = prefs.getString("wifi_ssid", DEFAULT_SSID);
    activePassword = prefs.getString("wifi_pass", DEFAULT_PASSWORD);
    Serial.printf("[NVS] SSID tersimpan: '%s'\n", activeSSID.c_str());

    // ================================================================
    // MODE AP+STA (Access Point + Station bersamaan)
    // AP selalu aktif agar aplikasi Android bisa konfigurasi WiFi
    // Station mencoba konek ke WiFi yang tersimpan
    // ================================================================
    WiFi.mode(WIFI_AP_STA);

    // Mulai Access Point
    WiFi.softAP(AP_SSID, AP_PASSWORD);
    delay(500);
    Serial.println("[AP] Access Point aktif!");
    Serial.printf("[AP] SSID: %s | Password: %s\n", AP_SSID, AP_PASSWORD);
    Serial.println("[AP] IP: " + WiFi.softAPIP().toString());

    // Tampilkan di LCD
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("AP: SIRAMKU");
    lcd.setCursor(0, 1);
    lcd.print(WiFi.softAPIP().toString());

    // Mulai HTTP Server (selalu aktif, baik STA konek maupun tidak)
    setupWifiConfigServer();

    // Coba koneksi WiFi Station
    wifiConnected = connectWiFiStation();
    
    if (wifiConnected) {
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("WiFi OK!");
        lcd.setCursor(0, 1);
        lcd.print(WiFi.localIP().toString());
    } else {
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("WiFi GAGAL!");
        lcd.setCursor(0, 1);
        lcd.print("Config via App");
        // Jangan return! AP tetap aktif untuk konfigurasi
    }

    // ---- Inisialisasi Firebase (hanya jika WiFi terhubung) ----
    if (wifiConnected) {
        config.api_key       = API_KEY;
        config.database_url  = DATABASE_URL;
        config.token_status_callback = tokenStatusCallback;

        // Beri waktu jaringan stabil sebelum signUp
        delay(1000);

        // Daftar sebagai pengguna anonim (aktifkan di Firebase Console > Authentication > Anonymous)
        if (Firebase.signUp(&config, &auth, "", "")) {
            Serial.println("[Firebase] Login anonim sukses!");
        } else {
            Serial.printf("[Firebase] Gagal login: %s\n", config.signer.signupError.message.c_str());
            Serial.println("[Firebase] Pastikan Anonymous Auth diaktifkan di Firebase Console.");
        }
        
        Firebase.begin(&config, &auth);
        Firebase.reconnectWiFi(true);

        Serial.println("[Firebase] Menghubungkan ke database...");
    }
}

// ================================================================
// LOOP UTAMA
// ================================================================
void loop() {
    // Selalu handle HTTP requests (AP selalu aktif)
    server.handleClient();

    // Auto-reconnect WiFi jika putus
    if (WiFi.status() != WL_CONNECTED) {
        if (wifiConnected) {
            // Sebelumnya terhubung, coba reconnect
            Serial.println("[WiFi] Koneksi terputus! Mencoba reconnect...");
            wifiConnected = false;
        }
        
        // Coba reconnect setiap 10 detik (tidak blocking)
        static unsigned long lastReconnect = 0;
        if (millis() - lastReconnect > 10000) {
            lastReconnect = millis();
            WiFi.disconnect();
            WiFi.begin(activeSSID.c_str(), activePassword.c_str());
            int tries = 0;
            while (WiFi.status() != WL_CONNECTED && tries < 10) {
                delay(500);
                Serial.print(".");
                tries++;
            }
            if (WiFi.status() == WL_CONNECTED) {
                Serial.println("\n[WiFi] Reconnect berhasil!");
                wifiConnected = true;
                
                // Re-init Firebase setelah reconnect
                if (!Firebase.ready()) {
                    config.api_key       = API_KEY;
                    config.database_url  = DATABASE_URL;
                    config.token_status_callback = tokenStatusCallback;
                    Firebase.signUp(&config, &auth, "", "");
                    Firebase.begin(&config, &auth);
                    Firebase.reconnectWiFi(true);
                }
            } else {
                Serial.println("\n[WiFi] Reconnect gagal, coba lagi nanti...");
            }
        }
        return; // Jangan lanjut ke Firebase jika belum terhubung
    }

    if (!Firebase.ready()) {
        Serial.println("[Firebase] Menunggu koneksi...");
        delay(1000);
        return;
    }

    // 1. Selalu dengarkan perintah dari Aplikasi Android (via Firebase)
    listenKontrolDariFirebase();

    // 2. Kirim data sensor setiap SEND_INTERVAL milidetik (default: 5 detik)
    if (millis() - lastSendTime > SEND_INTERVAL) {
        lastSendTime = millis();

        float tds        = bacaTDS();
        int   waterLevel = bacaWaterLevel();

        // Tampilkan di Serial Monitor untuk debugging
        Serial.printf("\n--- Pembacaan Sensor ---\n");
        Serial.printf("TDS        : %.1f PPM\n", tds);
        Serial.printf("Water Level: %d%%\n",     waterLevel);

        // Tampilkan di LCD
        lcd.clear();
        lcd.setCursor(0, 0);
        lcd.print("TDS: ");
        lcd.print(tds, 1);
        lcd.print(" PPM");

        lcd.setCursor(0, 1);
        lcd.print("Water: ");
        lcd.print(waterLevel);
        lcd.print("%");

        // Kirim ke Firebase
        kirimDataSensor(tds, waterLevel);

        // 3. Logika Otomatis: Jika air di bawah batas minimum, tampilkan peringatan
        if (waterLevel < WATER_LEVEL_MIN) {
            Serial.println("[PERINGATAN] Level Air RENDAH! Harap isi ulang reservoir.");
            Firebase.RTDB.setBool(&fbdo, "Sensors/alert_air_rendah", true);
        } else {
            Firebase.RTDB.setBool(&fbdo, "Sensors/alert_air_rendah", false);
        }
    }

    // 4. Geser indeks histori setiap HISTORI_INTERVAL (setiap menit untuk testing)
    if (millis() - lastHistoriTime > HISTORI_INTERVAL) {
        lastHistoriTime = millis();
        indeksHari = (indeksHari + 1) % 7; // Berputar: 0,1,2,3,4,5,6,0,1,...
    }

    delay(200); // Jeda singkat agar prosesor tidak terlalu panas
}
