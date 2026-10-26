# ProGuard / R8 Rules for Lockeyy

# Keep data models
-keepclassmembers class com.ssecure.applock.model.** { *; }
-keep class com.ssecure.applock.model.** { *; }

# Keep Services, Receivers and Activities
-keep class com.ssecure.applock.service.** { *; }
-keep class com.ssecure.applock.receiver.** { *; }
-keep class com.ssecure.applock.ui.** { *; }

# AndroidX Biometric
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Jetpack Compose
-keepclassmembers class androidx.compose.ui.platform.** { *; }
-dontwarn androidx.compose.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Material 3 & AndroidX
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
