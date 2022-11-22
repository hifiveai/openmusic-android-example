
#保留行号
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature

# ------------------------ NLE ------------
-keep class com.bytedance.ies.nle.editor_jni.NLEEditorJniJNI {
    *** SwigDirector_NLEEditorListener_onResult(...);
    *** SwigDirector_NLEChangeListener_onChange(...);
    *** SwigDirector_NLELoggerListener_onLog(...);
    *** SwigDirector_NLEBranchListener_onChanged(...);
    *** SwigDirector_NLEResourceSynchronizer_fetch(...);
    *** SwigDirector_NLEResourceSynchronizer_push(...);
    *;
}

-keep class com.bytedance.ies.nle.editor_jni.NLEEditorListener { *; }
-keep class com.bytedance.ies.nle.editor_jni.NLEModel { *; }
-keep enum com.bytedance.ies.nle.editor_jni.NLEError { *; }

-keep class com.bytedance.ies.nle.editor_jni.NLEChangeListener { *; }

-keep class com.bytedance.ies.nle.editor_jni.NLELoggerListener { *; }
-keep enum com.bytedance.ies.nle.editor_jni.LogLevel { *; }

-keep class com.bytedance.ies.nle.editor_jni.NLEResourceSynchronizer { *; }
-keep class com.bytedance.ies.nle.editor_jni.NLEResourceFetchCallback { *; }
-keep class com.bytedance.ies.nle.editor_jni.NLEResourcePushCallback { *; }

# ------------------------ NLE ------------



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

-keep class com.bytedance.speech.speechengine.SpeechEngine {*;}