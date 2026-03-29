# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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

# 1. Giữ lại các Class Models của bạn (Thay đổi package nếu cần)
-keep class com.example.vetbook.data.models.** { *; }
-keep class com.example.vetbook.domain.models.** { *; }

# 2. Giữ lại các quy tắc chung cho Firebase & Serialization
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# 3. Giữ lại các constructor không tham số (No-argument constructors)
# Đây là phần quan trọng nhất để sửa lỗi "Class o7.b does not define a no-argument constructor"
-keepclassmembers class com.example.vetbook.** {
    public <init>(...);
}

# 4. Giữ lại các thuộc tính cho Retrofit/Moshi/Gson (nếu bạn dùng)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.squareup.moshi.Json <fields>;
}

# Bảo vệ PayOS
-keep class vn.payos.** { *; }

# Giữ lại các thuộc tính hệ thống quan trọng
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# For Retrofit hoặc Gson/Moshi cho PayOs only
-keepattributes SourceFile, LineNumberTable

# Giữ lại các class chứa @SerializedName để Gson hoạt động
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Bảo vệ các Model DTO cụ thể của PayOS
-keep class com.example.vetbook.data.network.Payos** { *; }