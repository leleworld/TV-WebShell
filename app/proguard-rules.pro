# Add project specific ProGuard rules here.
-keep class com.gimytv.app.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-dontwarn android.webkit.**
