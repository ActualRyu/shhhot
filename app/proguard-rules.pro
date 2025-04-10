# Keep the application classes
-keep class com.ryucodes.shhhot.** { *; }

# Rules for ML Kit
-keep class com.google.mlkit.vision.text.** { *; }
-keep class com.google.android.gms.vision.** { *; }

# Rules for Compose
-keepclassmembers class androidx.compose.** { *; }

# Keep source file names for better stack traces
-keepattributes SourceFile,LineNumberTable

# Remove debug logs in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...); 
    public static int d(...); 
    public static int i(...); 
}