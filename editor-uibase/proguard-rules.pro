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
-keep class com.ss.ugc.android.editor.base.resource.ResourceBean{
*;
}

-keep class com.ss.ugc.android.editor.base.resource.ResourceBean$Resource{
*;
}

-keep class com.ss.ugc.android.editor.base.resource.ResourceItem{
*;
}

-keep class com.bytedance.speech.speechengine.SpeechEngineImpl{
*;
}

#不混淆cv包下类 否则sdk初始化会有混淆找不到类crash
-keep class com.bytedance.labcv.** { *; }
-keep class com.bef.effectsdk.** { *; }

-keep class com.bytedance.speech.speechengine.SpeechEngine {*;}

-keep public class * extends com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel{*;}


