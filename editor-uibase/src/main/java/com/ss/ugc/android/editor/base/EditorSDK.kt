package com.ss.ugc.android.editor.base

import android.content.Context
import com.bytedance.ies.nleeditor.NLE
import com.ss.android.vesdk.VELogUtil
import com.ss.android.vesdk.VESDK
import com.ss.ugc.android.editor.base.EditorConfig.*
import com.ss.ugc.android.editor.base.imageloder.IImageLoader
import com.ss.ugc.android.editor.base.network.IJSONConverter
import com.ss.ugc.android.editor.base.network.INetWorker
import com.ss.ugc.android.editor.base.path.PathConstant
import com.ss.ugc.android.editor.base.resource.ResourceHelper
import com.ss.ugc.android.editor.base.resource.base.IResourceProvider
import com.ss.ugc.android.editor.base.theme.IEditorUIConfig
import com.ss.ugc.android.editor.base.utils.DeviceLevelUtil
import com.ss.ugc.android.editor.base.utils.ToastUtils
import com.ss.ugc.android.editor.core.EditorCoreInitializer
import com.ss.ugc.android.editor.core.utils.DLog
import java.io.File

class EditorSDK {

    var context: Context? = null
        private set

    companion object {
        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EditorSDK()
        }

        const val EDITOR_SP = "editor_sp"
        const val EDITOR_COPY_RES_KEY = "editor_copy_res_key"
    }

    lateinit var config: EditorConfig
    var isInitialized = false
        private set
    fun init(config: EditorConfig) {
        NLE.logLevel = NLE.logLevel //初始化NLE
        if (isInitialized) {
            DLog.e("EditorSDK redundant initialization")
            return
        }
        checkConfig(config)
        this.config = config
        this.context = config.context
        ToastUtils.init(context?.applicationContext)
        ResourceHelper.getInstance().init(getApplication())
        DLog.sEnableLog = true
        EditorCoreInitializer.instance.apply {
            appContext = context?.applicationContext
            resourcePath = ResourceHelper.getInstance().resourcePath
            localResourcePath = ResourceHelper.getInstance().localResourcePath
        }
        config.context?.let {
            DeviceLevelUtil.initLevel(it)
        }

        PathConstant.makeAppDirs()
        VESDK.setLogLevel(VELogUtil.LOG_LEVEL_V)
        VESDK.registerLogger({ level, msg -> }, true)
    NLE.loadNLELibrary(false);
        config.draftDir?.let { draftDir ->
            if (!draftDir.exists()) {
                draftDir.mkdirs()
            }
        }
        isInitialized = true
    }

    fun getDraftDir(): File {
        val draftDir = instance.config.draftDir ?: File(context!!.filesDir, "/template-draft")
        if (!draftDir.exists()) {
            draftDir.mkdirs()
        }
        return draftDir
    }

    private fun checkConfig(config: EditorConfig) {
        if (config.context == null) {
            throw  IllegalStateException("context is required.")
        }
        if (config.imageLoader == null) {
            throw  IllegalStateException("image loader is null pls config it ")
        }
//        if (config.netWorker == null) {
//            throw  IllegalStateException("netWorker is required.")
//        }
        if (config.jsonConverter == null) {
            throw  IllegalStateException("jsonConverter is required.")
        }
        if (config.resourceProvider == null) {
            throw  IllegalStateException("resourceProvider is required.")
        }
    }

    fun setResReady(context: Context, ready: Boolean) {
        context.getSharedPreferences(EDITOR_SP, Context.MODE_PRIVATE)
            .edit().putBoolean(EDITOR_COPY_RES_KEY, ready).apply()
    }

    fun getApplication() = config.context

    fun imageLoader(): IImageLoader = config.imageLoader!!

    fun netWorker(): INetWorker = config.netWorker!!

    fun jsonConverter(): IJSONConverter = config.jsonConverter!!

    fun monitorReporter(): IMonitorReporter? = config.monitorReporter

    fun functionTypeMapper(): IFunctionTypeMapper? = config.functionTypeMapper

    fun functionBarConfig(): IFunctionBarConfig? = config.functionBarConfig

    fun resourceProvider(): IResourceProvider? = config.resourceProvider

    fun editorUIConfig(): IEditorUIConfig? = config.editorUIConfig

    fun functionExtension(): IFunctionExtension? = config.functionExtension
}

