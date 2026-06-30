# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK's default ProGuard configuration.

############################################
# REMOVE LOGS IN RELEASE
############################################
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** i(...);
    public static *** v(...);
    public static *** w(...);
    public static *** e(...);
}

############################################
# FIREBASE
############################################

-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

############################################
# GOOGLE PLAY SERVICES
############################################

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

############################################
# RETROFIT
############################################

-dontwarn okhttp3.**
-dontwarn retrofit2.**

############################################
# GSON
############################################

-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

############################################
# KOTLIN
############################################

-dontwarn kotlin.**

############################################
# ANDROIDX
############################################

-dontwarn androidx.**