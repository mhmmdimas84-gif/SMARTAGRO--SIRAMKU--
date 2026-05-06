/*
 * ================================================================
 * SmartAgro Siramku - ESP32 Firmware
 * Platform : PlatformIO + Arduino Framework
 * Deskripsi: Membaca Sensor TDS & Water Level, mengendalikan
 *            2 pompa via Relay, dan sinkronisasi dua arah
 *            dengan Firebase Realtime Database.
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
#include <Firebase_ESP_Client.h>
// Wajib disertakan untuk helper addData dan error print
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// ================================================================
// KONFIGURASI - ISI SESUAI DATA ANDA
// ================================================================
#define WIFI_SSID     "POCO X6 Pro 5G"
#define WIFI_PASSWORD "00000000"

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

// ================================================================
// KONFIGURASI THRESHOLD (AMBANG BATAS OTOMATIS)
// ================================================================
#define TDS_MIN         300   // PPM minimum (di bawah ini, pompa nutrisi ON otomatis)
#define WATER_LEVEL_MIN  20   // Persentase minimum (di bawah ini, buzzer/alert ON)

// ================================================================
// VARIABEL GLOBAL
// ================================================================
FirebaseData   fbdo;
FirebaseAuth   auth;
FirebaseConfig config;

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
    }

    // Baca status Pompa Nutrisi dari Firebase
    if (Firebase.RTDB.getBool(&fbdo, "Controls/pompa_nutrisi/status")) {
        bool statusBaru = fbdo.boolData();
        if (statusBaru != pompaNutrisiStatus) {
            pompaNutrisiStatus = statusBaru;
            setRelay(PIN_RELAY_POMPA_NUTRISI, pompaNutrisiStatus);
            Serial.printf("[Firebase] Pompa Nutrisi -> %s\n", pompaNutrisiStatus ? "NYALA" : "MATI");
        }
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
// SETUP
// ================================================================
void setup() {
    Serial.begin(115200);
    Serial.println("\n=== SmartAgro Siramku - Booting... ===");

    // Inisialisasi pin
    pinMode(PIN_TDS,          INPUT);
    pinMode(PIN_WATER_LEVEL,  INPUT);
    pinMode(PIN_RELAY_POMPA_AIR,     OUTPUT);
    pinMode(PIN_RELAY_POMPA_NUTRISI, OUTPUT);

    // Pastikan pompa dalam kondisi MATI saat pertama menyala
    setRelay(PIN_RELAY_POMPA_AIR,     false);
    setRelay(PIN_RELAY_POMPA_NUTRISI, false);

    // ---- Koneksi WiFi ----
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("[WiFi] Menghubungkan ke " WIFI_SSID);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println("\n[WiFi] Terhubung! IP: " + WiFi.localIP().toString());

    // ---- Inisialisasi Firebase ----
    config.api_key       = API_KEY;
    config.database_url  = DATABASE_URL;

    // Login Anonim (Wajib diaktifkan di Firebase Console > Authentication)
    auth.user.email    = "";
    auth.user.password = "";
    if (Firebase.signUp(&config, &auth, "", "")) {
        Serial.println("[Firebase] Login sukses!");
    } else {
        Serial.printf("[Firebase] Gagal login: %s\n", config.signer.signupError.message.c_str());
    }

    config.token_status_callback = tokenStatusCallback;
    
    Firebase.begin(&config, &auth);
    Firebase.reconnectWiFi(true);

    Serial.println("[Firebase] Menghubungkan ke database...");
}

// ================================================================
// LOOP UTAMA
// ================================================================
void loop() {
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
