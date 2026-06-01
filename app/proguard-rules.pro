# Marathon Tracker ProGuard rules

# Keep Hilt-generated code
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel *;
}

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit + Gson
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Gson data classes
-keep class com.marathon.tracker.data.remote.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Glance widget
-keep class androidx.glance.** { *; }
