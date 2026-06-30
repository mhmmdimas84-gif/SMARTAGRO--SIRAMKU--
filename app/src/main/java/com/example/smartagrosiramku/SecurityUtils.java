package com.example.smartagrosiramku;

import android.app.Activity;
import android.os.Build;
import android.view.WindowManager;

import java.io.File;

/**
 * SecurityUtils — Utilitas keamanan untuk SmartAgroSiramku.
 *
 * Menyediakan:
 * - Screenshot protection (FLAG_SECURE) via applySecureWindow()
 * - Root detection heuristik
 * - Emulator detection heuristik
 */
public class SecurityUtils {

    // =========================================================
    // Screenshot / Screen Recording Protection
    // =========================================================

    /**
     * Terapkan FLAG_SECURE pada window Activity.
     *
     * Mencegah screenshot, screen recording, dan tampilan di recent apps.
     * Wajib dipanggil di onCreate() SEBELUM setContentView().
     *
     * @param activity Activity yang akan dilindungi
     */
    public static void applySecureWindow(Activity activity) {
        if (activity != null && !activity.isFinishing()) {
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
            );
        }
    }

    // =========================================================
    // Root Detection
    // =========================================================

    /**
     * Mendeteksi apakah device kemungkinan di-root.
     *
     * @return true jika indikasi root ditemukan
     */
    public static boolean isRooted() {
        return checkSuBinary() || checkBuildTags() || checkRootPaths();
    }

    private static boolean checkSuBinary() {
        String[] suPaths = {
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su-backup",
            "/data/local/su",
            "/data/local/bin/su",
            "/data/local/xbin/su"
        };
        for (String path : suPaths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkBuildTags() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootPaths() {
        String[] rootPaths = {
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/data/local/bin/busybox",
            "/system/xbin/busybox"
        };
        for (String path : rootPaths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    // =========================================================
    // Emulator Detection
    // =========================================================

    /**
     * Mendeteksi apakah app berjalan di emulator Android.
     *
     * @return true jika kemungkinan berjalan di emulator
     */
    public static boolean isEmulator() {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.FINGERPRINT.contains("emulator")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT)
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"));
    }
}
