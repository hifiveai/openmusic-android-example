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


-keep class com.ss.ugc.android.editor.core.api.sticker.TextStyleInfo{
*;
}


-keep class com.ss.ugc.android.editor.core.utils.LiveDataBus{
*;
}
-keep class com.ss.ugc.android.editor.core.utils.LiveDataBus$BusMutableLiveData{
*;
}

-keep class android.arch.core.internal.SafeIterableMap{
*;
}

-keep class android.arch.lifecycle.LiveData$ObserverWrapper{
*;
}


#--------------- NLE ----------------------
-keep class com.bytedance.ies.nleedtor.*{
*;
}

-keep class com.bytedance.ies.nle.editor_jni.*{
          *;
}
-keepclasseswithmembernames class com.bytedance.ies.nleedtor.* {
      native <methods>;
      static <methods>;
  }



-keep class com.bytedance.ies.nlemediajava.TextTemplate{
*;
}

-keep class com.bytedance.ies.nlemediajava.TextContent{
*;
}
