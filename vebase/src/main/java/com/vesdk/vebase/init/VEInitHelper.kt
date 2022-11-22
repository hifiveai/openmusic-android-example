package com.vesdk.vebase.init

import android.app.Application
import android.widget.Toast
import com.bef.effectsdk.FileResourceFinder
import com.vesdk.vebase.log.LogKit
import com.ss.android.vesdk.VEAuth
import com.ss.android.vesdk.VEConfigCenter
import com.ss.android.vesdk.VEConfigKeys
import com.ss.android.vesdk.VESDK
import com.vesdk.vebase.Constant
import com.vesdk.vebase.R
import com.vesdk.vebase.old.util.FileUtilKt
import com.vesdk.vebase.resource.ResourceHelper
import java.io.File

/**
 *  author  lijunguan
 *  date  2022/6/8 513 下午
 *  description
 */
object VEInitHelper {

    private const val LICENSE_PATH = "resource/LicenseBag.bundle"
    private const val LICENSE_NAME =
        "Hifve_test_20221031_20221231_com.hfopen.videosdk_4.2.6.5.licbag"

    var isInitialized = false
        private set

    private var authRet: Int = -1


    fun checkAuth(): Boolean {
        if (!isInitialized) {
            LogKit.e(Constant.TAG, "VeSDK is uninitialised", null)
            return false
        }
        return authRet == 0
    }

    fun init(app: Application) {
        if (isInitialized) {
            LogKit.w(Constant.TAG, "VeSDK redundant initialization")
            return
        }
        //1. 初始化VE SDK
        val workspaceDir = getWorkSpaceDir(app)
        LogKit.d(Constant.TAG, "init vesdk,work space dir$workspaceDir")
        VESDK.init(app, workspaceDir.absolutePath)
        VESDK.setAssetManagerEnable(true)
        //保证720P以上是硬解码
        VESDK.enableHDH264HWDecoder(true, 700)
        VESDK.enableTTByteVC1Decoder(true)
        //设置算法（人脸、手势、物体识别...）模型目录
        VESDK.setEffectResourceFinder(FileResourceFinder(ResourceHelper.getInstance().modelPath))

        //2. 鉴权license相关  关键, 必须正确初始化鉴权文件，才能使用VE 相关功能
        // Demo 这里是把鉴权文件从 asset 目录下，copy 到本地，方便读取。
        val licenseTarget = File(app.getExternalFilesDir("license"), LICENSE_NAME)
        val copy =
            FileUtilKt.copyAssetFile(app, "$LICENSE_PATH/$LICENSE_NAME", licenseTarget.absolutePath)
        if (!copy) {
            Toast.makeText(
                app,
                app.getString(R.string.vesdk_copy_license_sucess),
                Toast.LENGTH_SHORT
            ).show()
            LogKit.e(Constant.TAG, app.getString(R.string.vesdk_copy_license_sucess), null)
        }
        //进行鉴权，实际接入根据可以自己鉴权文件实际存放位置处理，保证可以访问到即可
        authRet = VEAuth.init(licenseTarget.absolutePath)
        if (authRet != 0) {
            LogKit.e(Constant.TAG, getAuthReadableTips(), null)
        } else {
            LogKit.i(Constant.TAG, "init ve license completed ")
        }
        //开启sticker新引擎

        //开启sticker新引擎
        VESDK.setEnableStickerAmazing(true)

        // 解决导入16：9的视频或图片出现黑边的问题
        VEConfigCenter.getInstance()
            .updateValue(VEConfigKeys.KEY_ENABLE_RENDER_ENCODE_RESOLUTION_ALIGN4, true)
        isInitialized = true
    }

    fun getAuthReadableTips(): String {
        if (checkAuth()) return ""
        var tips = "鉴权不通过，请联系技术支持"
        when (authRet) {
            //LICBAG_API_FAIL
            -600 -> tips = "鉴权失败"
            -601 -> tips = "文件解析失败,请检查文件是否存在或损坏"
            -602 -> tips = "鉴权类型不匹配"
            -603 -> tips = "无效的版本，请检查是否用了旧版本的授权文件"
            -604 -> tips = "无效的数据块，请检查授权文件是否正确，是否损坏"
            -605 -> tips = "非法授权文件，请检查授权文件是否正确"
            -606 -> tips = "授权文件过期，请检查授权文件是否正确，是否需要更新"
            -607 -> tips = "请求功能不匹配，请检查是否购买对应功能"
            -608 -> tips = "授权包类型不匹配，请检查授权文件是否正确，是否损坏"
            -609 -> tips = "包名不匹配，请检查申请授权文件和应用的包名是否一致"
        }
        return tips
    }


    fun getWorkSpaceDir(app: Application): File {
        val workspace = app.getExternalFilesDir(null) ?: app.filesDir
        if (workspace.exists()) {
            workspace.mkdir()
        }
        return workspace
    }


}