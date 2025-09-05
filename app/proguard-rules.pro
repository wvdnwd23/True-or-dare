# Hilt/DI
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }
-keep class com.wes.truthdare.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Vosk (ASR)
-keep class org.vosk.** { *; }

# Security Crypto / Datastore (safe)
-keep class androidx.security.** { *; }
-keep class androidx.datastore.** { *; }