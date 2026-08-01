# Finora release (R8/ProGuard) rules.
# isMinifyEnabled / isShrinkResources are enabled for the release build type in app/build.gradle.kts.

# ---------------------------------------------------------------------------
# General: keep attributes needed for correct reflection/generics behavior
# and for readable stack traces (paired with Crashlytics' native symbol upload).
# ---------------------------------------------------------------------------
-keepattributes Signature, InnerClasses, EnclosingMethod, Exceptions
-keepattributes *Annotation*
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# Room
# ---------------------------------------------------------------------------
# Room's generated DAO/database implementations are produced at compile time by KSP (not via
# runtime reflection), so R8 renaming class members is normally safe - the generated code and
# the entities are renamed consistently together. Entities are still kept defensively to protect
# against any future reflection-based use (e.g. if a debugging/export tool is added later).
-keep class com.example.data.*Entity { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# ---------------------------------------------------------------------------
# Enums persisted by name (Room TypeConverters use .name / .valueOf(), see AppConverters.kt).
# These must NOT be renamed, or every already-persisted row becomes unreadable (falls back to the
# converter's default value) the next time the app updates.
# ---------------------------------------------------------------------------
-keepclassmembers enum com.example.data.TransactionType { *; }
-keepclassmembers enum com.example.data.TransactionCategory { *; }
-keepclassmembers enum com.example.data.PaymentMethod { *; }
-keepclassmembers enum com.example.data.AccountType { *; }
-keep enum com.example.data.TransactionType
-keep enum com.example.data.TransactionCategory
-keep enum com.example.data.PaymentMethod
-keep enum com.example.data.AccountType

# Standard rule so R8 doesn't strip enum valueOf()/values() support classes in general.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ---------------------------------------------------------------------------
# Retrofit / OkHttp
# (Present as a dependency for future network features; not currently wired to a live endpoint,
# but kept correct so it works the moment an API service interface is added.)
# ---------------------------------------------------------------------------
-keep interface retrofit2.** { *; }
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# ---------------------------------------------------------------------------
# Moshi
# (moshi-kotlin-codegen generates adapters at compile time for any @JsonClass(generateAdapter =
# true) type, so runtime reflection is minimal - these rules cover that generated code path.)
# ---------------------------------------------------------------------------
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep class **JsonAdapter { *; }
-keepnames @com.squareup.moshi.JsonClass class *
-dontwarn com.squareup.moshi.**

# ---------------------------------------------------------------------------
# Kotlin reflection / coroutines
# ---------------------------------------------------------------------------
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.coroutines.Continuation
-dontwarn kotlinx.coroutines.**
-dontwarn kotlin.reflect.**

# ---------------------------------------------------------------------------
# Firebase / Crashlytics
# (Crashlytics ships its own consumer-proguard-rules.pro bundled in the AAR; these are extra
# safety for correct deobfuscation mapping upload.)
# ---------------------------------------------------------------------------
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.**

# ---------------------------------------------------------------------------
# Dependency injection frameworks: none are used in this project (no Hilt/Dagger/Koin) -
# intentionally no rules here. Add framework-specific keep rules if one is introduced later.
# ---------------------------------------------------------------------------

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
