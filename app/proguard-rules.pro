# Hapus Log

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** i(...);
    public static *** v(...);
    public static *** w(...);
    public static *** e(...);
}

# Jangan beri keep untuk seluruh AndroidX
# Biarkan R8 mengoptimalkan

-dontwarn androidx.**

# Firebase

-keep class com.google.firebase.provider.FirebaseInitProvider

-keep class com.google.firebase.messaging.FirebaseMessagingService

-keep class com.google.firebase.components.ComponentRegistrar

# Retrofit

-dontwarn okhttp3.**

-dontwarn retrofit2.**

# Kotlin

-dontwarn kotlin.**