# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in e:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools-proguard.html

# Add any project specific keep rules here:

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.sensordiary.data.** { *; }
-keep class com.example.sensordiary.model.** { *; }

# Compose
-keepclassmembers class androidx.compose.ui.platform.ComposeView {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Preserve line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
