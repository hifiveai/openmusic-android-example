package com.ss.ugc.android.editor.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.bytedance.ies.nlemediajava.VEConfig
import com.ss.ugc.android.editor.base.draft.IDraftHelper
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.IFunctionManager
import com.ss.ugc.android.editor.base.functions.IFunctionHandlerRegister
import com.ss.ugc.android.editor.base.imageloder.IImageLoader
import com.ss.ugc.android.editor.base.logger.ILog
import com.ss.ugc.android.editor.base.network.IJSONConverter
import com.ss.ugc.android.editor.base.network.INetWorker
import com.ss.ugc.android.editor.base.resource.base.IResourceProvider
import com.ss.ugc.android.editor.base.theme.IEditorUIConfig
import com.ss.ugc.android.editor.core.api.canvas.*
import com.ss.ugc.android.editor.core.api.video.EditMedia
import com.ss.ugc.android.editor.core.encrypt.IEncryptProcessor
import java.io.File

open class EditorConfig constructor(builder: Builder) {
    var enableLog: Boolean = builder.enableLog
    var monitorReporter: IMonitorReporter? = builder.monitorReporter
    var videoSelector = builder.mediaSelect
    var resourceProvider = builder.resourceProvider
    var audioSelector = builder.audioSelector
    var context = builder.context
    var draftHelper = builder.draftHelper
    var imageLoader = builder.imageLoader
    val netWorker = builder.netWorker
    val jsonConverter = builder.jsonConverter
    val functionTypeMapper = builder.functionTypeMapper
    val functionBarConfig = builder.functionBarConfig
    val functionExtension = builder.functionExtension
    var compileActionConfig = builder.compilerAction

    var waterMarkPath = builder.waterMarkPath
    var isDefaultSaveInAlbum: Boolean = builder.isDefaultSaveInAlbum
    var isLoopPlay: Boolean = builder.isLoopPlay  //是否循环播放
    var enableLocalSticker: Boolean = builder.enableLocalSticker

    val enableDraftBox = builder.enableDraftBox
    val freezeFrameTime = builder.freezeFrameTime
    val pictureTime = builder.pictureTime
    val effectAmazing = builder.effectAmazing

    // 特效默认作用对象 是否是全局
    val effectApplyGlobel = builder.effectApplyGlobel
    val editorUIConfig = builder.editorUIConfig
    val mediaConverter = builder.mediaConverter
    val fileProviderAuthority = builder.fileProviderAuthority
    val mediaCrop = builder.mediaCrop
    val isFixedRatio = builder.isFixedRatio
    val businessCanvasRatioList = builder.businessCanvasRatioList
    var encryptProcessor = builder.encryptProcessor//加密解密处理器
    val draftDir: File? = builder.draftDir
    var enableTemplateFunction: Boolean = builder.enableTemplateFunction

    init {
        ILog.enableLog = enableLog
    }

    open class Builder {
        var context: Context? = null
        var waterMarkPath: String? = null
        var enableLog = true
        var monitorReporter: IMonitorReporter? = null
        var functionTypeMapper: IFunctionTypeMapper? = null
        var mediaSelect: IMediaSelector? = null
        var resourceProvider: IResourceProvider? = null
        var audioSelector: IAudioSelector? = null
        var draftHelper: IDraftHelper? = null
        var imageLoader: IImageLoader? = null
        var netWorker: INetWorker? = null
        var jsonConverter: IJSONConverter? = null
        var isDefaultSaveInAlbum = true //默认保存在app内部存储空间，不保存在相册
        var isLoopPlay = false //默认不循环播放视频
        var compilerAction: IVideoCompilerConfig? = null
        var resourceConfig: ResourceConfig? = null
        var enableDraftBox = true
        var editorUIConfig: IEditorUIConfig? = null

        var functionBarConfig: IFunctionBarConfig? = null
        var functionExtension: IFunctionExtension? = null

        var freezeFrameTime: Int = 3000 // default 3s
        var pictureTime: Long = 4000 //图片素材默认4s
        var effectAmazing: Boolean = true

        // 特效默认作用对象 是否是全局
        var effectApplyGlobel: Boolean = false
        var enableLocalSticker: Boolean = false
        var mediaConverter: IMediaConverter? = null
        var fileProviderAuthority: String = "com.ss.ugc.android.davincieditor.FileProvider"
        var mediaCrop: IMediaCrop? = null
        var isFixedRatio: CanvasRatio = ORIGINAL
        var businessCanvasRatioList: List<CanvasRatio> =
            listOf(ORIGINAL, RATIO_9_16, RATIO_3_4, RATIO_1_1, RATIO_4_3, RATIO_16_9)
        var encryptProcessor: IEncryptProcessor? = null//加密解密处理器
        var draftDir: File? = null
        var enableTemplateFunction: Boolean = false

        fun setEffectAmazing(effectAmazing: Boolean) = apply {
            this.effectAmazing = effectAmazing
            VEConfig.enableAmazing = false
        }

        fun setEffectApplyGlobel(effectApplyGlobel: Boolean) = apply {
            this.effectApplyGlobel = effectApplyGlobel
        }

        fun setLocalStickerEnable(enableLocalSticker: Boolean) = apply {
            this.enableLocalSticker = enableLocalSticker
        }

        fun setFreezeFrameTime(freezeFrameTime: Int) =
            apply { this.freezeFrameTime = freezeFrameTime }

        fun pictureTime(pictureTime: Long) = apply { this.pictureTime = pictureTime }
        fun compileActionConfig(compilerAction: IVideoCompilerConfig) =
            apply { this.compilerAction = compilerAction }

        fun isDefaultSaveInAlbum(flag: Boolean) = apply { this.isDefaultSaveInAlbum = flag }
        fun enableLog(enable: Boolean) = apply { this.enableLog = enable }
        fun monitorReporter(monitorReporter: IMonitorReporter) = apply { this.monitorReporter = monitorReporter }
        fun mediaSelector(selector: IMediaSelector) = apply { this.mediaSelect = selector }
        fun audioSelector(selector: IAudioSelector) = apply { this.audioSelector = selector }
        fun resourceProvider(resourceProvider: IResourceProvider) =
            apply { this.resourceProvider = resourceProvider }

        fun resourceConfig(resourceConfig: ResourceConfig) = apply { this.resourceConfig = resourceConfig }
        fun context(context: Context) = apply { this.context = context.applicationContext }

        fun draftHelper(draftHelper: IDraftHelper) = apply {
            this.draftHelper = draftHelper
        }

        fun imageLoader(imageLoader: IImageLoader) = apply {
            this.imageLoader = imageLoader
        }

        fun netWorker(netWorker: INetWorker) = apply { this.netWorker = netWorker }

        fun enableTemplateFunction(enableTemplateFunction: Boolean) =
            apply { this.enableTemplateFunction = enableTemplateFunction }

        fun jsonConverter(jsonConverter: IJSONConverter) =
            apply { this.jsonConverter = jsonConverter }

        fun editorUIConfig(editorUIConfig: IEditorUIConfig) = apply {
            this.editorUIConfig = editorUIConfig
        }

        fun functionBarConfig(functionBarConfig: IFunctionBarConfig) = apply { this.functionBarConfig = functionBarConfig }

        fun functionExtension(functionExtension: IFunctionExtension) = apply { this.functionExtension = functionExtension }

        fun enableDraftBox(enableDraftBox: Boolean) = apply { this.enableDraftBox = enableDraftBox }

        fun waterMarkPath(path: String) = apply { this.waterMarkPath = path }

        fun functionTypeMapper(functionTypeMapper: IFunctionTypeMapper) = apply {
            this.functionTypeMapper = functionTypeMapper
        }

        fun mediaConverter(mediaConverter: IMediaConverter) =
            apply { this.mediaConverter = mediaConverter }

        fun fileProviderAuthority(fileProviderAuthority: String) = apply {
            this.fileProviderAuthority = fileProviderAuthority
        }

        fun mediaCrop(mediaCrop: IMediaCrop) = apply {
            this.mediaCrop = mediaCrop
        }

        fun isFixedRatio(canvasRatio: CanvasRatio) = apply {
            this.isFixedRatio = canvasRatio
        }

        fun businessCanvasRatioList(businessCanvasRatioList: List<CanvasRatio>) = apply {
            this.businessCanvasRatioList = businessCanvasRatioList
        }

        /**
         * 配置加密处理器，目前用于模板加密处理
         */
        fun encryptProcess(encryptProcessor: IEncryptProcessor) = apply {
            this.encryptProcessor = encryptProcessor
        }

        fun draftDir(dir: File): Builder {
            this.draftDir = dir
            return this
        }

        fun builder(): EditorConfig {
            return EditorConfig(this)
        }
    }

    interface IMonitorReporter {
        fun report(key: String, paramsMap: MutableMap<String, String>? = mutableMapOf())
    }

    interface IFunctionTypeMapper {
        fun convert(functionType: String): String
    }

    interface IMediaSelector {
        fun obtainAlbumIntent(context: Context, functionType: AlbumFunctionType): Intent
        fun startLocalStickerSelector(activity: Activity)
    }

    interface IAudioSelector {
        fun obtainAudioSelectIntent(context: Context): Intent
    }

    interface IMediaConverter {
        fun obtainMediaFromIntent(data: Intent?, requestCode: Int): MutableList<EditMedia>?
    }

    interface IMediaCrop {
        fun startBusinessMediaCrop(activity: Activity, Image: File)
    }

    /**
     * IMPORTSELECT  标识首次导入视频
     * MAINTRACK 点击轨道上的+ 导入
     * SUBVIDEOTRACK  添加画中画导入
     * CANVAS 自定义画布导入
     * REPLACESLOT 轨道替换
     */
    enum class AlbumFunctionType {
        IMPORTSELECT, MAINTRACK, SUBVIDEOTRACK, CANVAS, REPLACESLOT
    }

    interface IFunctionBarConfig {

        /**
         * 底部栏FunctionItem列表（子节点嵌套）
         */
        fun createFunctionItemList(): ArrayList<FunctionItem>

        /**
         * 选中特定轨道时需要显示特定操作的FunctionItems，如音频、文字、特效等
         */
        fun expendFuncItemOnTrackSelected(selectType: String): FunctionItem?

        /**
         * 创建转场FunctionItem（转场Item不在FunctionItem的树结构中）
         */
        fun createTransactionItem(): FunctionItem?
    }

    interface IVideoCompilerConfig {

        /**
         * 视频合成前的通知
         * [duration] 视频时长，单位是 ms
         * [size] 视频预估的大小 单位是 M
         * return true 的时候会拦截合成，即点击完成不会进行合成
         * return false 会执行合成
         */
        fun onVideoCompileIntercept(
            duration: Long,
            size: Long,
            activity: Activity
        ): Boolean

        fun onVideoCompileDone(path: String, activity: Activity)

        /**
         * return false 不拦截关闭界面事件，自动关闭剪辑页面
         * return  true 拦截关闭个事件，接入方调用 activity.finish 进行关闭
         */
        fun onCloseEdit(activity: Activity): Boolean

        /**
         * 点击关闭按钮前的通知
         * return true 的时候会拦截关闭编辑页，即点击关闭按钮并不会关闭，取而代之的是执行业务方自定义的关闭逻辑
         * return false 点击关闭按钮会关闭编辑页
         */
        fun onCustomCloseMethodIntercept(activity: Activity): Boolean

        /**
         * editor 界面 resume 的回调
         * 暂时放这里
         */
        fun onEditResume(activity: Activity)
    }

    /**
     * Function底部栏扩展接口
     */
    interface IFunctionExtension {

        /**
         * 底部栏FunctionTree扩展
         * 可构建FunctionItem添加到FunctionTree的任意节点
         * @param functionManager: 提供了add/remove/replace/enable/disable的api
         */
        fun onFuncTreeExtension(functionManager: IFunctionManager)

        /**
         *
         * 选中特定轨道时需要显示特定操作的FunctionItems（游离在FunctionTree之外）
         *
         * @param editItem 父节点FunctionItem, 目前支持三种类型：音频、文字、特效
         * @param handlerRegister FunctionHandler注册器
         */
        fun onEditModeExtension(editItem: FunctionItem, handlerRegister: IFunctionHandlerRegister)
    }
}

