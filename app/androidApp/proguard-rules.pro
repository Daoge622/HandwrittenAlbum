# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

-keep class com.dot.gallery.feature.editor.adjustments.** { *; }
-keep class com.drew.** { *; }
-keep class java.io.** { *; }
-keep class com.adobe.** { *; }
-keep class ai.onnxruntime.** { *; }

-dontwarn com.google.auto.value.AutoValue$Builder
-dontwarn com.google.auto.value.AutoValue
-dontwarn org.tensorflow.lite.gpu.GpuDelegateFactory$Options$GpuBackend
-dontwarn org.tensorflow.lite.gpu.GpuDelegateFactory$Options

# Keep custom Glide decoders and model loaders (HEIF/JXL/Encrypted)
-keep class com.dot.gallery.core.decoder.glide.** { *; }
-keep class com.radzivon.bartoshyk.avif.** { *; }
-keep class com.awxkee.jxlcoder.** { *; }
-dontwarn com.radzivon.bartoshyk.avif.**
-dontwarn com.awxkee.jxlcoder.**

# Cloud provider models (Retrofit DTOs, Room entities, serialization)
-keep class com.dot.gallery.cloud.** { *; }
-keep class com.dot.gallery.cloud.data.entity.** { *; }
-keep class com.dot.gallery.cloud.core.** { *; }
-keep class com.dot.gallery.cloud.image.** { *; }

-dontwarn javax.el.BeanELResolver
-dontwarn javax.el.ELContext
-dontwarn javax.el.ELResolver
-dontwarn javax.el.ExpressionFactory
-dontwarn javax.el.FunctionMapper
-dontwarn javax.el.ValueExpression
-dontwarn javax.el.VariableMapper
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.Oid
# 手写相册: @Parcelize is a compile-time annotation from the parcelize plugin; it is not on the
# runtime classpath of every module, so R8 reports it missing. Safe to ignore.
-dontwarn kotlinx.parcelize.**

# 手写相册: Room (2.x, used by WorkManager) instantiates generated *_Impl databases by reflection.
# Without these rules R8 strips WorkDatabase_Impl and the app crashes in Application.onCreate
# ("Failed to create an instance of class androidx.work.impl.WorkDatabase").
-keep class androidx.work.impl.WorkDatabase_Impl { <init>(); }
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class * extends androidx.room3.RoomDatabase { <init>(); }
-keepnames class androidx.work.impl.WorkDatabase
