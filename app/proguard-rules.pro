# =============================================================================
# 1. GIỮ LẠI CÁC MODELS CỦA PROJECT (VET, PET, CLINIC, REVIEWS)
# =============================================================================
-keep class com.example.vetbook.data.models.** { *; }
-keep class com.example.vetbook.domain.models.** { *; }
-keep class com.example.vetbook.presentation.models.** { *; }

# Giữ lại các constructor không tham số cho Firebase & Serialization
-keepclassmembers class com.example.vetbook.** {
    public <init>(...);
}

# =============================================================================
# 2. BẢO VỆ NETWORK & CLOUDINARY (Sửa lỗi "String arg == null")
# =============================================================================
# Bảo vệ Cloudinary Service và Response Model
-keep class com.example.vetbook.data.network.** { *; }
-keep class com.cloudinary.** { *; }
-dontwarn com.cloudinary.**

# Bảo vệ các Annotation để Gson/Retrofit đọc được tên biến (public_id, secure_url)
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses, SourceFile, LineNumberTable

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# =============================================================================
# 3. BẢO VỆ THƯ VIỆN BÊN THỨ BA (FIREBASE, PAYOS, RETROFIT)
# =============================================================================
# Firebase & Google Play Services
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# PayOS SDK
-keep class vn.payos.** { *; }

# Retrofit & OkHttp
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Gson
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.