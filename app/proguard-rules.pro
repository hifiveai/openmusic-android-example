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

#不混淆cv包下所有的类 否则sdk初始化会有混淆找不到类crash
#-keep class com.bytedance.labcv.effectsdk.** { *; }
#-keep class com.bytedance.labcv.licenselibrary.** { *; }
-keep class com.bytedance.labcv.** { *; }
-keep class com.bef.effectsdk.** { *; }
#keep住vesdk的某些类 防止vedemo加载so时的启动crash
-keep class com.ss.** { *; }


-keep public class com.alibaba.android.arouter.routes.**{*;}
-keep class * implements com.alibaba.android.arouter.facade.template.ISyringe{*;}

-keep class com.vesdk.vebase.app.*{
*;
}


#-------------------------------

# ----------- ViewModel -- start -------
-keep class * extends android.arch.lifecycle.Lifecycle {
    <init>();
}
-keep class * extends android.arch.lifecycle.Lifecycle {
    <init>(android.app.Application);
}
#-keep class com.ss.ugc.android.editor.base.viewmodel.*{
#*;
#}
# ----------- ViewModel --end ---------
# EditorMainViewModel
-keep class com.vesdk.veeditor.edit.main.*{
*;
}
#  com.vesdk.veeditor.edit.draft.DraftViewModel
-keep class com.vesdk.veeditor.edit.draft.*{
*;
}
# com.vesdk.verecorder.record.preview.viewmodel.PreviewModel
-keep class com.vesdk.verecorder.record.preview.viewmodel.*{
*;
}
# com.vesdk.veeditor.edit.sticker.preview.PreviewStickerViewModel
-keepclasseswithmembers class * extends com.vesdk.veeditor.edit.base.BaseEditorViewModel{
        <fields>;
        <methods>;
}

#-------------------------------------

-keep  class * extends com.bytedance.android.winnow.WinnowHolder{
*;
}



-keep class com.vesdk.vebase.resource.*{
*;
}
-keep class com.vesdk.vebase.resource.ResourceBean{
*;
}

-keep class com.vesdk.vebase.resource.ResourceBean$Resource{
*;
}

-keep class com.vesdk.vebase.resource.ResourceItem{
*;
}

-keep class com.vesdk.verecorder.record.demo.view.CountDownDialog{
*;
}
-keep class com.vesdk.verecorder.record.demo.view.CountDownDialog$Callback{
*;
}


#==================

-keep class com.jkb.fragment.rigger.rigger.** {*;}
-keep interface com.jkb.fragment.rigger.rigger.** {*;}
-keep class com.jkb.fragment.swiper.**{*;}

-keep public class * extends android.app.Activity
-keep public class * extends  androidx.fragment.app.Fragment
-keepclassmembers class * extends android.app.Activity {
   public int getContainerViewId();
   public boolean onRiggerBackPressed();
   public void onFragmentResult(int,int,android.os.Bundle);
   public void onLazyLoadViewCreated(android.os.Bundle);
   public int[] getPuppetAnimations();
   public String getFragmentTag();
   public boolean onInterruptBackPressed();
}
-keepclassmembers class * extends  androidx.fragment.app.Fragment {
   public int getContainerViewId();
   public boolean onRiggerBackPressed();
   public void onFragmentResult(int,int,android.os.Bundle);
   public void onLazyLoadViewCreated(android.os.Bundle);
   public int[] getPuppetAnimations();
   public String getFragmentTag();
   public boolean onInterruptBackPressed();
}

# ------- LiveData没有被hook住，需要keep下--------------
-keep class com.vesdk.vebase.LiveDataBus{
*;
}
-keep class com.vesdk.vebase.LiveDataBus$BusMutableLiveData{
*;
}
-keep class androidx.arch.core.internal.SafeIterableMap{
*;
}

-keep class androidx.lifecycle.LiveData$ObserverWrapper{
*;
}

-keep class com.bytedance.ies.nleedtor.*{
*;
}
-keep class com.ss.ugc.android.editor.bottom.event.* {
*;
}
-keep class com.bytedance.ies.nle.editor_jni.*{
          *;
}

-keep class com.bytedance.ies.nlemediajava.keyframe.bean.*{
  *;
}


-keepclasseswithmembernames class com.bytedance.ies.nleedtor.* {
      native <methods>;
      static <methods>;
  }


