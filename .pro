# AppHealth - ProGuard Rules
# This file contains configuration for ProGuard to ensure proper functionality of the application.

-keepattributes Signature
-keepattributes *Annotation*

-keep class com.argane.healthlog.data.** { *; }
-keep class com.example.data.** { *; }

-dontwarn org.jetbrains.kotlin.**