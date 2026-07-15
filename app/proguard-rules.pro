# =====================================================================
# NextQRScanner — R8 / ProGuard rules (release build, full mode)
# =====================================================================
# The goal: aggressive shrinking + obfuscation while keeping the reflective
# surfaces (Room, kotlinx.serialization, Hilt, ML Kit, billing) intact.

# --- General ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Do not strip our own crash-relevant metadata but obfuscate names.
-repackageclasses 'com.nextqr.scanner.o'
-allowaccessmodification

# --- Kotlin ---
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }

# --- Coroutines ---
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# --- kotlinx.serialization ---
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.nextqr.scanner.**$$serializer { *; }
-keepclasseswithmembers class com.nextqr.scanner.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Retrofit / OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# --- Room ---
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# --- SQLCipher ---
-keep class net.zetetic.database.** { *; }
-dontwarn net.zetetic.database.**

# --- Hilt / Dagger ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# --- ML Kit ---
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# --- Play Billing ---
-keep class com.android.billingclient.api.** { *; }

# --- Play Integrity ---
-keep class com.google.android.play.core.integrity.** { *; }

# --- ZXing ---
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# --- Our domain models are serialized/persisted via reflection surfaces ---
-keep class com.nextqr.scanner.domain.model.** { *; }
-keep class com.nextqr.scanner.data.remote.dto.** { *; }
-keep class com.nextqr.scanner.data.local.entity.** { *; }

# Strip all Log.* calls in release for privacy-safe logging.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
