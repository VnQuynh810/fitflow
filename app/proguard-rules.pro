# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class io.github.vnquynh810.fitflow.**$$serializer { *; }
-keepclassmembers class io.github.vnquynh810.fitflow.** { *** Companion; }
-keepclasseswithmembers class io.github.vnquynh810.fitflow.** { kotlinx.serialization.KSerializer serializer(...); }

# OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
