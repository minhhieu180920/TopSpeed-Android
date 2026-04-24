# Add project specific ProGuard rules here.
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep JNI classes
-keepclasseswithmembers class com.topspeed.audio.game.** {
    native <methods>;
}

# Keep Oboe classes
-keep class com.google.oboe.** { *; }
