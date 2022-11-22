package com.bytedance.ckdemo.editor

import android.app.Application
import com.ss.android.vesdk.vedemo.editor.JSONConverterImpl
import com.ss.ugc.android.davincieditor.image.GlideImageLoader
import com.ss.ugc.android.editor.base.EditorConfig
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.main.EditorActivity
import com.ss.ugc.android.editor.resource.BuildInResourceProvider

/**
 *  author : lijunguan
 *  date : 2022/6/7 3:15 下午
 *  description :
 */
object EditorSDKInitHelper {


    fun init(app: Application) {
        init(getDefaultBuilder(app).builder())
    }

    fun init(config: EditorConfig) {
        EditorSDK.instance.init(config)
    }

    fun getDefaultBuilder(app: Application) = EditorConfig.Builder()
        .context(app)
        .enableLog(true)
        .imageLoader(GlideImageLoader())
        .jsonConverter(JSONConverterImpl())
        .functionBarConfig(StandardFunctionBarConfig())
        //图片素材 持续时间
        .pictureTime(3000)
        .setLocalStickerEnable(true)
        .resourceProvider(BuildInResourceProvider())
        .fileProviderAuthority(
            String.format(
                EditorActivity.URI_PREVIEW,
                app.applicationInfo.packageName
            )
        )
}