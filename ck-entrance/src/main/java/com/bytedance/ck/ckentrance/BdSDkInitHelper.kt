package com.bytedance.ck.ckentrance

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import com.bytedance.ckdemo.editor.EditorSDKInitHelper
import com.vesdk.vebase.log.DebugLogger
import com.vesdk.vebase.log.LogKit
import com.ss.android.vesdk.VELogProtocol
import com.ss.android.vesdk.VELogUtil
import com.ss.android.vesdk.VESDK
import com.ss.android.vesdk.VEVersionUtil
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.ILogger
import com.vesdk.vebase.DemoApplication
import com.vesdk.vebase.init.VEInitHelper
import com.vesdk.vebase.resource.ResourceHelper
import com.vesdk.vebase.util.ExecutorsPool
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.system.measureTimeMillis

/**
 *  author : lijunguan
 *  date : 2022/6/6 8:21 下午
 *  description :
 */
object BdSDkInitHelper {

    const val TAG_VESDK = "VESDK---"
    const val TAG_EDITOR = "EDITOR---"
    const val TAG = "BdSDKInit---"

    /**
     * VESDK  未初始化， check 是否调用[VEInitHelper.init]
     */
    const val CODE_VE_SDK_UNINITIALIZED = -1

    /**
     * VESDK 证书校验失败， check 是否正确copy 证书， 也可以通过：[VEInitHelper.getAuthReadableTips] 获取详细信息
     */
    const val CODE_VE_AUTH_FAILED = -2

    /**
     * 未发现对应资源：check 资源包是否copy 到本地
     */
    const val CODE_RESOURCE_NOT_FOUND = -3

    /**
     * SharedPreferences 用来保存资源初始化的状态，防止多次初始化资源
     * Note: 实际接入，可以灵活选择自己项目的KeyValue 库
     */
    private var sharedPref: SharedPreferences? = null

    /**
     * 初始化所有Demo 所需组件， 快捷式接入
     */
    fun initAll(app: Application) {
        DemoApplication.initContext(app)
        val costTime = measureTimeMillis {
            VEInitHelper.init(app)
            initLog()
            EditorSDKInitHelper.init(app)
        }
        LogKit.i(TAG, " All SDKs initialised, cost time :$costTime")
        // Demo相关assets资源 存放在 vebase、editor-tobres  module下
        copyResourceToLocal(app, null)
    }

    fun isResReady(context: Context): Boolean {
        if (sharedPref == null) {
            sharedPref = context.getSharedPreferences(EditorSDK.EDITOR_SP, Context.MODE_PRIVATE)
        }
        return sharedPref!!.getBoolean(EditorSDK.EDITOR_COPY_RES_KEY, false)
    }


    /**
     * copy 美颜，特效，编辑贴纸，文字等所有资源 到本地目录
     * invoke threadsafe
     *
     * Note: Demo 提供简单实现，异步IO 线程一次copy 所有资源
     * 建议接入方根据自己购买的资源数量、大小 ，和实际业务使用场景综合考虑，如果资源量非常大
     * 1. 根据使用场景，频率优先级 分批copy 资源， 使用前check 对应资源是否存在，是否需要等待copy完成等。
     * 2. 自己网络服务器下发资源，替换本地资源
     *
     * @param callback 资源copy 完成的回调， ret:0 表示成功， -1 表示失败，  主线程回调
     */
    fun copyResourceToLocal(context: Context, callback: ((ret: Int, t: Throwable?) -> Unit)?) {
        if (isResReady(context)) {
            LogKit.w(TAG, "resources are ready, no need to copy them again")
            callback?.let {
                ExecutorsPool.instance.MAIN_EXECUTOR.execute { it.invoke(0, null) }
            }
            return
        }
        val appContext = context.applicationContext
        ExecutorsPool.instance.IO_EXECUTOR.execute {
            // check 资源是否已经copy 完成，
            // Note: Demo IO_EXECUTOR 是单线程实现，接入方自己实现，check 是否有多次copy资源的风险
            // Note: 这里资源检测只是检测 SP 的标记位，不检测实际文件是否存在， 如果接入方资源方案不安全目录(不建议)，建议自己check
            if (isResReady(appContext)) {
                callback?.let {
                    ExecutorsPool.instance.MAIN_EXECUTOR.execute { it.invoke(0, null) }
                }
                return@execute
            }
            try {
                fun initResource(context: Context, assetChild: String) {
                    val rootPath = ResourceHelper.getInstance().resourceRootPath
                    // 清空目标目录下的文件，防止有旧数据
                    File(rootPath, assetChild).deleteRecursively()

                    val costTime = measureTimeMillis {
                        copyAssets(context.assets, assetChild, rootPath)
                    }
                    LogKit.i(
                        TAG,
                        "copy resource: costTime:$costTime assetChild:$assetChild , rootPath:$rootPath"
                    )
                }
                /**
                 * ResourceHelper.RESOURCE，ResourceHelper.LOCAL_RESOURCE，是Demo项目，资源包的Asset存放目录
                 * Note: 接入方如果有调整，要根据自己实际存放目录情况调整代码
                 */
                initResource(appContext, ResourceHelper.RESOURCE)
                initResource(appContext, ResourceHelper.LOCAL_RESOURCE)
                sharedPref?.edit()?.putBoolean(EditorSDK.EDITOR_COPY_RES_KEY, true)?.apply()
                callback?.let {
                    ExecutorsPool.instance.MAIN_EXECUTOR.execute { it.invoke(0, null) }
                }
            } catch (e: Exception) {
                LogKit.e(TAG, "copy resource", e)
                sharedPref?.edit()?.putBoolean(EditorSDK.EDITOR_COPY_RES_KEY, false)?.apply()
                callback?.let {
                    ExecutorsPool.instance.MAIN_EXECUTOR.execute { it.invoke(-1, e) }
                }
            }
        }
    }

    private fun initLog() {
        //给LogKit 注入一个默认实现，输出日志到控制台
        LogKit.inject(DebugLogger())
        // 这里统一使用LogKit 代理， VESDK, NLE， Editor 三个模块的日志输出。
        //设置log输出等级
        VESDK.setLogLevel(VELogUtil.LOG_LEVEL_D)
        VESDK.setEffectLogLevel(VELogUtil.BEF_LOG_LEVEL_DEBUG)
        VESDK.registerLogger { i, s ->
            when (i) {
                VELogProtocol.VELOG_VERBOSE -> LogKit.v(TAG_VESDK, s)
                VELogProtocol.VELOG_DEBUG -> LogKit.d(TAG_VESDK, s)
                VELogProtocol.VELOG_INFO -> LogKit.i(TAG_VESDK, s)
                VELogProtocol.VELOG_WARN -> LogKit.w(TAG_VESDK, s)
                VELogProtocol.VELOG_ERROR -> LogKit.e(TAG_VESDK, s, null)
            }
        }
        DLog.setLogger(object : ILogger {
            override fun i(msg: String) {
                LogKit.i(TAG_EDITOR, msg)
            }

            override fun d(msg: String) {
                LogKit.i(TAG_EDITOR, msg)
            }

            override fun w(msg: String) {
                LogKit.i(TAG_EDITOR, msg)
            }

            override fun e(msg: String?, t: Throwable?) {
                LogKit.e(TAG_EDITOR, msg ?: "", t)
            }
        })
    }


    /**
     * 递归拷贝Asset目录中的文件到rootDir中
     * Recursively copy the files in the Asset directory to rootDir
     * @param assets
     * @param path  assets 下的path  eg: resource/duet.bundle/duet.json
     * @param dstRootDir
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun copyAssets(assets: AssetManager, path: String, dstRootDir: String) {
        if (isAssetsDir(assets, path)) {
            val dir = File(dstRootDir + File.separator + path)
            check(!(!dir.exists() && !dir.mkdirs())) { "mkdir failed" }
            for (s in assets.list(path)!!) {
                copyAssets(assets, "$path/$s", dstRootDir)
            }
        } else {
            val input = assets.open(path)
            val dest = File(dstRootDir, path)
            copyToFileOrThrow(input, dest)
        }

    }

    private fun isAssetsDir(assets: AssetManager, path: String): Boolean {
        try {
            val files = assets.list(path)
            return files != null && files.isNotEmpty()
        } catch (e: IOException) {
            LogKit.e(TAG_EDITOR, "isAssetsDir:", e)
        }
        return false
    }

    @Throws(IOException::class)
    private fun copyToFileOrThrow(inputStream: InputStream, destFile: File) {
        if (destFile.exists()) {
            return
        }
        val file = destFile.parentFile
        if (file != null && !file.exists()) {
            file.mkdirs()
        }
        inputStream.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    fun getVESDKVersionInfo(): String =
        "vesdk:" + VEVersionUtil.getVESDKVersion() + "     effect:" + VESDK.getEffectSDKVer()

    /**
     * 自检码，完善一致常见错误，遇到错误，对应自检。
     */
    fun getSelfCheckCode(context: Context) {
        when {
            !VEInitHelper.isInitialized -> CODE_VE_SDK_UNINITIALIZED
            // VESDK 鉴权不通过， 详细原因可以通过 VEInitHelper.getAuthReadableTips()获取更详细信息
            VEInitHelper.checkAuth() -> CODE_VE_AUTH_FAILED
            !(File(
                ResourceHelper.getInstance().resourceRootPath,
                ResourceHelper.RESOURCE
            ).exists())
            -> CODE_RESOURCE_NOT_FOUND
            !(File(
                ResourceHelper.getInstance().resourceRootPath,
                ResourceHelper.LOCAL_RESOURCE
            ).exists()) -> CODE_RESOURCE_NOT_FOUND
        }
    }


}