package com.example.smartagrosiramku;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationService extends Service {

    private static final String TAG = "NotificationService";
    private static final String FOREGROUND_CHANNEL_ID = "siramku_monitoring_channel";
    private static final String ALERT_CHANNEL_ID = "siramku_alert_channel";
    private static final int FOREGROUND_NOTIF_ID = 101;
    private static final int ALERT_NOTIF_ID_BASE = 200;

    private DatabaseReference sensorRef;
    private ValueEventListener tdsListener;
    private ValueEventListener waterLevelListener;

    private float currentTds = 0f;
    private int currentAir = 0;

    private SharedPreferences sharedPreferences;
    private static final long NOTIF_COOLDOWN = 30 * 60 * 1000; // 30 menit cooldown

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("SiramkuNotifPrefs", MODE_PRIVATE);
        createNotificationChannels();
        startForegroundServiceCompat();
        setupFirebaseListeners();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager == null) return;

            // Channel untuk Foreground Service (Biasa, tidak mengganggu)
            NotificationChannel foregroundChannel = new NotificationChannel(
                    FOREGROUND_CHANNEL_ID,
                    "Monitoring Sensor",
                    NotificationManager.IMPORTANCE_LOW
            );
            foregroundChannel.setDescription("Menampilkan status pemantauan sensor real-time.");
            manager.createNotificationChannel(foregroundChannel);

            // Channel untuk Alert Darurat (Pop-up/Heads-up, dengan suara & getaran)
            NotificationChannel alertChannel = new NotificationChannel(
                    ALERT_CHANNEL_ID,
                    "Pemberitahuan Sensor Kritis",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alertChannel.setDescription("Memberikan peringatan jika sensor melewati batas aman.");
            alertChannel.enableLights(true);
            alertChannel.enableVibration(true);
            
            // Set sound
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            alertChannel.setSound(alarmSound, audioAttributes);
            
            manager.createNotificationChannel(alertChannel);
        }
    }

    private void startForegroundServiceCompat() {
        Intent notificationIntent = new Intent(this, Dashboard.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
                .setContentTitle("Siramku Monitoring Aktif")
                .setContentText("Memantau sensor TDS dan Level Air secara real-time.")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        // Memulai service di latar depan sesuai regulasi Android 14+
        startForeground(FOREGROUND_NOTIF_ID, builder.build());
    }

    private void setupFirebaseListeners() {
        sensorRef = FirebaseDatabase.getInstance().getReference("Sensors");

        tdsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Float value = snapshot.getValue(Float.class);
                        if (value != null) {
                            currentTds = value;
                            checkThresholdsAndNotify();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Gagal mengolah nilai TDS: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "TDS listener dibatalkan: " + error.getMessage());
            }
        };

        waterLevelListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Integer value = snapshot.getValue(Integer.class);
                        if (value != null) {
                            currentAir = value;
                            checkThresholdsAndNotify();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Gagal mengolah level air: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Water level listener dibatalkan: " + error.getMessage());
            }
        };

        sensorRef.child("tds_ppm").addValueEventListener(tdsListener);
        sensorRef.child("water_level_pct").addValueEventListener(waterLevelListener);
    }

    private void checkThresholdsAndNotify() {
        long currentTime = System.currentTimeMillis();

        long lastWaterLevelNotifTime = sharedPreferences.getLong("lastWaterLevelNotifTime", 0);
        long lastHighTdsNotifTime = sharedPreferences.getLong("lastHighTdsNotifTime", 0);
        long lastLowTdsNotifTime = sharedPreferences.getLong("lastLowTdsNotifTime", 0);

        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("Sensors/notifications");

        // 1. Level air rendah ( < 20%)
        if (currentAir > 0 && currentAir < 20 && (currentTime - lastWaterLevelNotifTime > NOTIF_COOLDOWN)) {
            sharedPreferences.edit().putLong("lastWaterLevelNotifTime", currentTime).apply();
            
            String title = "Level Air Rendah";
            String message = "Level air di bawah 20%, segera isi ulang tangki untuk mencegah kerusakan sistem.";
            String key = notifRef.push().getKey();
            if (key != null) {
                NotifikasiLog log = new NotifikasiLog(title, message, "error", currentTime, false);
                notifRef.child(key).setValue(log);
            }
            showPopupNotification(title, message, 1);
        }

        // 2. Pemberian nutrisi berlebihan (TDS > 1000)
        if (currentTds > 1000 && (currentTime - lastHighTdsNotifTime > NOTIF_COOLDOWN)) {
            sharedPreferences.edit().putLong("lastHighTdsNotifTime", currentTime).apply();
            
            String title = "Nutrisi Berlebih";
            String message = "Nilai TDS melebihi batas aman (> 1000 ppm). Tambahkan air bersih untuk menormalkan.";
            String key = notifRef.push().getKey();
            if (key != null) {
                NotifikasiLog log = new NotifikasiLog(title, message, "error", currentTime, false);
                notifRef.child(key).setValue(log);
            }
            showPopupNotification(title, message, 2);
        }

        // 3. Kekurangan nutrisi (TDS < 400)
        if (currentTds > 0 && currentTds < 400 && (currentTime - lastLowTdsNotifTime > NOTIF_COOLDOWN)) {
            sharedPreferences.edit().putLong("lastLowTdsNotifTime", currentTime).apply();
            
            String title = "Kekurangan Nutrisi";
            String message = "Nilai TDS di bawah batas ideal (< 400 ppm). Pompa nutrisi perlu diaktifkan.";
            String key = notifRef.push().getKey();
            if (key != null) {
                NotifikasiLog log = new NotifikasiLog(title, message, "warning", currentTime, false);
                notifRef.child(key).setValue(log);
            }
            showPopupNotification(title, message, 3);
        }
    }

    private void showPopupNotification(String title, String message, int idOffset) {
        Intent intent = new Intent(this, Notifikasi.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, idOffset, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(ALERT_NOTIF_ID_BASE + idOffset, builder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorRef != null) {
            if (tdsListener != null) {
                sensorRef.child("tds_ppm").removeEventListener(tdsListener);
            }
            if (waterLevelListener != null) {
                sensorRef.child("water_level_pct").removeEventListener(waterLevelListener);
            }
        }
    }
}
