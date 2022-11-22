package com.ss.ugc.android.editor.base.path

import android.Manifest
import android.content.Context
import android.os.Environment
import androidx.annotation.MainThread
import androidx.annotation.RequiresPermission
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.utils.FileUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 *  author : wenlongkai
 *  date : 2019/3/28 下午5:22
 *  description :
 */
object PathConstant {
    val APP_DIR = EditorSDK.instance.config.context!!.filesDir.absolutePath ?: ""

    private val APP_SDCARD_DIR = EditorSDK.instance.config.context!!.getExternalFilesDir(null) ?: ""

    val NEW_DRAFT_DIR = "$APP_DIR/newdrafts"

    val DOWNLOAD_AUDIO_SAVE_PATH = "$APP_DIR/downloadAudio/"
    val DOWNLOAD_SOUND_SAVE_PATH = "$APP_DIR/downloadSound/"
    val DOWNLOAD_MUSIC_SAVE_PATH = "$APP_DIR/downloadMusic/"
    val DOWNLOAD_MATERIAL_SAVE_PATH = "$APP_DIR/downloadMaterial/"
    val DOWNLOAD_TEMPLATE_SAVE_PATH = "$APP_DIR/downloadTemplateVideo/"
    val DOWNLOAD_TUTORIAL_SAVE_PATH = "$APP_DIR/downloadGuideVideo/"
    val DOWNLOAD_GUIDE_PATH = "$APP_DIR/downloadGuide/"

    // 模板临时文件目录
    val TEMPLATE_TMP = "$APP_DIR/templatetmp/"

    // 云草稿上传前预处理临时文件目录
    val UPLOAD_TMP = "$APP_DIR/uploadtmp/"

    private val TEXT_SAVE_PATH = "$APP_DIR/text/"

    val EFFECT_DIR = "$APP_DIR/effect/"

    val CANVAS_DIR = "$APP_DIR/canvas/"

    val LOCAL_CANVAS_DIR = "$APP_DIR/local_canvas/"

    val CACHE_DIR = "$APP_DIR/cache/"

    val AUDIO_DIR = "$APP_DIR/audio/"

    val ENHANCE_DIR = "$APP_DIR/enhance/"

    val REVERSE_DIR = "$APP_DIR/reverse/"

    val CARTOON_DIR = "$APP_DIR/cartoon/"

    const val VE_WORK_SPACE = "ve_workspace"

    val DISK_CACHE_DIR = "$APP_SDCARD_DIR/disk_cache/"

    val EXPORT_DIR = "$APP_SDCARD_DIR/export"

    val EPILOGUE_DIR = "$APP_DIR/epilogue/"
    val CUT_SAME_WORKSPACE_DIR = "$APP_DIR/cut_same_workspace"

    val REPLICATE_TEMP = "$APP_DIR/replicate_tmp/"

    val CLOUD_DRAFT_DOWNLOAD_DIR = "$APP_DIR/clouddraft/"

    // 片尾版本号
    private const val EPILOGUE_VERSION = 2
    private const val EPILOGUE_NAME = "epilogue"
    private const val EPILOGUE_SUFFIX = ".mp4"

    // 片尾在asset目录下的名称
    const val ASSETS_EPILOGUE_NAME = "$EPILOGUE_NAME$EPILOGUE_VERSION$EPILOGUE_SUFFIX"

    private const val ANIM_NAME = "anim_in"
    const val NEW_ANIM_NAME = "epilogue_anim"
    const val ANIM_NAME_SUFFIX = ".zip"

    val EPILOGUE_VIDEO_FILE = "$EPILOGUE_DIR$EPILOGUE_NAME$EPILOGUE_VERSION$EPILOGUE_SUFFIX"
    val EPILOGUE_TEXT_ANIM = "$EPILOGUE_DIR$NEW_ANIM_NAME"
    val OLD_EPILOGUE_TEXT_ANIM = "$EPILOGUE_DIR$ANIM_NAME"

    val EPILOGUE_ANIM_PATH = EPILOGUE_DIR + NEW_ANIM_NAME + ANIM_NAME_SUFFIX
    val OLD_EPILOGUE_ANIM_PATH = EPILOGUE_DIR + ANIM_NAME + ANIM_NAME_SUFFIX

    // 抽帧图片存放路径
    val EXTRACT_FRAME_DIR = "$APP_DIR/frame/"

    // 草稿附带性能信息文件后缀
    const val DRAFT_PERFORMANCE_FILE_SUFFIX = "_performance.json"

    const val COVER_NAME = "cover.png"

    val MEDIA_TAG_CACHE_DIR = "$APP_DIR/media_tag_cache/"

    @MainThread
    fun makeAppDirs() {
        FileUtil.makeDir(APP_DIR)
        FileUtil.makeDir(DOWNLOAD_AUDIO_SAVE_PATH)
        FileUtil.makeDir(TEMPLATE_TMP)
        FileUtil.makeDir(EFFECT_DIR)
        FileUtil.makeDir(TEXT_SAVE_PATH)
        FileUtil.makeDir(CACHE_DIR)
        FileUtil.makeDir(DISK_CACHE_DIR)
        FileUtil.makeDir(AUDIO_DIR)
        FileUtil.makeDir(ENHANCE_DIR)
        FileUtil.makeDir(REVERSE_DIR)
        FileUtil.makeDir(DOWNLOAD_SOUND_SAVE_PATH)
        FileUtil.makeDir(DOWNLOAD_MUSIC_SAVE_PATH)
        FileUtil.makeDir(CARTOON_DIR)
        try {
            FileUtil.makeDir(EFFECT_DIR)
        } catch (ignore: Exception) {
        }
        FileUtil.makeDir(CANVAS_DIR)
        FileUtil.makeDir(LOCAL_CANVAS_DIR)
        FileUtil.makeDir(MEDIA_TAG_CACHE_DIR)
        FileUtil.makeDir(CLOUD_DRAFT_DOWNLOAD_DIR)
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getSDPath(): String {
        var sdDir: File? = null
        // 判断sd卡是否存在
        val sdCardExist = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory() // 获取根目录
        }
        return sdDir?.toString() ?: ""
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getSaveName(templateId: String = "0") =
        getMediaDir() + File.separator + getSaveFileName(templateId)

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getMediaDir(): String {
        val sdcardPath = getSDPath()
        val mediaDir = getSpecialCameraPath(EditorSDK.instance.config.context!!).ifBlank { "$sdcardPath/相机" }
        val xjDir = File(mediaDir)
        return if (xjDir.exists()) {
            xjDir.absolutePath
        } else {
            val cameraDir = "$sdcardPath/${Environment.DIRECTORY_DCIM}/Camera"
            if (!File(cameraDir).exists()) {
                File(cameraDir).mkdirs()
            }
            cameraDir
        }
    }

    fun getSaveFileName(templateId: String = "0") = "lv_${templateId}_${SimpleDateFormat(
        "yyyyMMddHHmmss",
        Locale.getDefault()
    ).format(Date())}.mp4"

    private val OEM_SYSTEM_PKGS = arrayOf(
        "com.htc", "com.meizu.mstore",
        "com.sonyericsson.android.camera", "com.yulong.android.settings.backup",
        "com.bbk.account", "com.gionee.account"
    )

    @Suppress("ComplexMethod")
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private fun getSpecialCameraPath(context: Context): String {
        val sdcardPath = getSDPath()
        var path = ""
        var pkgIndex = 0
        var file: File
        for (i in 1 until OEM_SYSTEM_PKGS.size) {
            try {
                if (context.packageManager.getPackageInfo(OEM_SYSTEM_PKGS[i], 0) != null) {
                    pkgIndex = i
                    break
                }
            } catch (e: Throwable) {
            }
        }

        when (pkgIndex) {
            0 -> {
                file = File(sdcardPath, "/DCIM/100MEDIA")
                if (file.exists()) {
                    path = file.absolutePath
                }
            }
            1 -> {
                file = File(sdcardPath, "/Camera")
                if (file.exists()) {
                    path = file.absolutePath
                } else {
                    file = File(sdcardPath, "/DCIM")
                    if (file.exists()) {
                        path = file.absolutePath
                    }
                }
            }
            2 -> {
                file = File(sdcardPath, "/DCIM/100ANDRO")
                if (file.exists()) {
                    path = file.absolutePath
                }
            }
            3 -> {
                file = File(sdcardPath, "/Camera")
                if (file.exists()) {
                    path = file.absolutePath
                }
            }
            4 -> {
                file = File(sdcardPath, "/相机")
                if (file.exists()) {
                    path = file.absolutePath
                } else {
                    file = File(sdcardPath, "/相机/照片")
                    if (file.exists()) {
                        path = file.absolutePath
                    }
                }
            }
            5 -> {
                file = File(sdcardPath, "/照相机/Camera")
                if (file.exists()) {
                    path = file.absolutePath
                }
            }
            else -> {
                path = ""
            }
        }
        return path
    }


    fun getProjectDir(projectId: String): File = File(NEW_DRAFT_DIR, projectId)

    fun getOldProjectDir(projectId: String): File = File("$APP_DIR/drafts", projectId)

    fun getCoverFile(projectId: String): File = File(getProjectDir(projectId), COVER_NAME)


}
