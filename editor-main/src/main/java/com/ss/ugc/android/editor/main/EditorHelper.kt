package com.ss.ugc.android.editor.main

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nlemediajava.utils.MediaUtil
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.EditorSDK.Companion.EDITOR_COPY_RES_KEY
import com.ss.ugc.android.editor.base.EditorSDK.Companion.EDITOR_SP
import com.ss.ugc.android.editor.base.music.data.MusicItem
import com.ss.ugc.android.editor.base.permission.RequestPermissionBuilder
import com.ss.ugc.android.editor.base.utils.FileUtil
import com.ss.ugc.android.editor.base.utils.PermissionUtil
import com.ss.ugc.android.editor.base.view.export.WaitingDialog
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.api.video.EditMedia
import com.ss.ugc.android.editor.core.avgSpeed
import com.ss.ugc.android.editor.core.getMaxLayer
import com.ss.ugc.android.editor.core.setVETrackType
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.main.EditorActivityDelegate.Companion.DRAFT_RESTORE
import com.ss.ugc.android.editor.picker.mediapicker.PickerConfig
import java.io.File

@SuppressLint("StaticFieldLeak")
object EditorHelper {
    private var isCopying: Boolean = false
    const val EXTRA_KEY_FROM_TYPE = "extra_key_from_type" // 0:默认 1:从拍摄进来 2:草稿 4:模板
    const val EXTRA_FROM_DEFAULT = 0
    const val EXTRA_FROM_RECORD = 2
    const val EXTRA_FROM_MULTI_SELECT = 3
    const val EXTRA_FROM_TEMPLATE = 4
    const val PERMISSION_REQUEST_CODE = 1001

    const val EXTRA_KEY_DRAFT_UUID = "extra_draft_uuid"
    const val EXTRA_KEY_VIDEO_PATH = "extra_video_path"
    const val EXTRA_KEY_VIDEO_PATHS = "extra_video_paths"
    const val EXTRA_KEY_MEDIA_TYPE = "extra_media_type" //代表从拍摄进来的资源类型 图片1 视频3

    private val REQUIRED_PERMISSIONS = arrayOf(
        permission.READ_EXTERNAL_STORAGE,
        permission.INTERNET,
        permission.WRITE_EXTERNAL_STORAGE
    )

    const val EXTRA_KEY_AUDIO_SELECT = "extra_key_audio_select"
    const val RESULT_CODE_AUDIO_SELECT = 31


    /**
     * 直接打开编辑sdk 自带的相册选择图片或者视频进入编辑
     */
    fun startEditor(context: Context) {
        val intent = Intent()
//        intent.putExtra("bussiness_id",bussinessId)
        startEditor(context, intent)
    }


    /**
     * 业务方需要透传一些自定义数据时可以调用这个接口，把要透传的数据设置到 intent 中
     */
    fun startEditor(context: Context, intent: Intent) {
        intent.setClass(context, EditorActivity::class.java)
        when {
            PermissionUtil.hasPermission(
                context,
                REQUIRED_PERMISSIONS
            ) -> {
                startActivityAfterResReady(context, intent)
            }
            context is FragmentActivity -> {
                RequestPermissionBuilder(context, REQUIRED_PERMISSIONS.toList())
                    .callback { allGranted, _, deniedList ->
                        if (allGranted) {
                            startActivityAfterResReady(context, intent)
                        } else {
                            Toaster.show("No Permission of $deniedList")
                        }
                    }
                    .request()
            }
            else -> {
                Toaster.toast("为保证功能正常使用，请确保打开所需权限")
            }
        }
    }

    private fun startActivityAfterResReady(
        context: Context,
        intent: Intent
    ) {

        // 判断是不是有copy 资源
        if (isCopying) {
            Toaster.show("正在复制资源到 SD 卡，请稍后，复制完成后会自动跳转到编辑页面")
            return
        }
        val resReady = context.getSharedPreferences(EditorSDK.EDITOR_SP, MODE_PRIVATE)
            .getBoolean(EDITOR_COPY_RES_KEY, false)
        val isShowDialog = false
        if (!resReady) {
            val dialog = WaitingDialog(context)
            // 设置ProgressDialog 标题
            dialog.setTitle("资源复制中...")
            // 设置ProgressDialog 提示信息
            // 设置ProgressDialog 是否可以按退回按键取消
            dialog.setCancelable(false)
            dialog.setOnCancelListener { }
            var startTime = System.currentTimeMillis()
            EditorResCopyTask(context, object : EditorResCopyTask.IUnzipViewCallback {
                override fun onStartTask() {
//                    Toaster.show("正在复制资源到 SD 卡，请稍后，复制完成后会自动跳转到编辑页面")
                    isCopying = true
                    if (isShowDialog) {
                        dialog.show()
                        dialog.setProgress(" 正在复制资源到 SD 卡，请稍后")
                    }

                    startTime = System.currentTimeMillis()
                }

                // 这里要注意一下，在子线程回调目前
                override fun onEndTask(result: Boolean) {
                    isCopying = false
                    if (isShowDialog) {
                        dialog.dismiss()
                    }
                    val endTime = System.currentTimeMillis()
                    DLog.d("copy cost ", "cost = ${endTime - startTime}")

                    if (result) {
                        context.getSharedPreferences(EDITOR_SP, MODE_PRIVATE)
                            .edit().putBoolean(EDITOR_COPY_RES_KEY, true).apply()
                        context.startActivity(intent)
                    }
                }
            }).execute(EditorResCopyTask.DIR, EditorResCopyTask.LOCAL_DIR)
        } else {
            context.startActivity(intent)
        }
    }

    /**
     * 使用接入自己的相册选择好素材后跳转
     * [Media] 选择好后的素材
     */
    fun startEditorWithMedia(context: Context, media: ArrayList<EditMedia>) {
        val intent = Intent(context, EditorActivity::class.java)
        intent.putParcelableArrayListExtra(PickerConfig.EXTRA_RESULT, media)
        intent.putExtra(EXTRA_KEY_FROM_TYPE, EXTRA_FROM_MULTI_SELECT)
        startActivityAfterResReady(context, intent)
    }

    /**
     * 使用接入自己的相册选择好素材后跳转
     * [Media] 选择好后的素材
     */
    fun startEditorWithMedia(context: Context, intent: Intent, media: ArrayList<EditMedia>) {
        intent.putParcelableArrayListExtra(PickerConfig.EXTRA_RESULT, media)
        intent.putExtra(EXTRA_KEY_FROM_TYPE, EXTRA_FROM_MULTI_SELECT)
        startActivityAfterResReady(context, intent)
    }

    /**
     * [filePath] 录制后的文件path
     * [isVideo] 是否是是视频，图自的时候传false
     */
    fun startEditorAfterRecord(context: Context, filePath: String, isVideo: Boolean) {
        startEditorAfterRecord(context, Intent(), filePath, isVideo)
    }

    fun startEditorAfterRecord(
        context: Context,
        intent: Intent,
        filePath: String,
        isVideo: Boolean
    ) {
        intent.setClass(context, EditorActivity::class.java)
        intent.putExtra(EXTRA_KEY_FROM_TYPE, EXTRA_FROM_RECORD)
        val list = arrayListOf<EditMedia>()
        list.add(EditMedia(filePath, isVideo))
        startEditorWithMedia(context, intent, list)
    }

    fun onEditorMediaSelected(activity: Activity, selects: ArrayList<EditMedia>) {
        val intent = Intent()
        intent.putParcelableArrayListExtra(PickerConfig.EXTRA_RESULT, selects)
        activity.setResult(PickerConfig.RESULT_CODE, intent)
        activity.finish()
    }

    fun onMusicSelected(activity: Activity, musicItem: MusicItem) {
        val intent = Intent()
        intent.putExtra(EXTRA_KEY_AUDIO_SELECT, musicItem)
        activity.setResult(RESULT_CODE_AUDIO_SELECT, intent)
        activity.finish()
    }

    fun onEditorWithDraftID(activity: Activity, draftID: String) {
        val context = activity as Context
        val intent = Intent(activity, EditorActivity::class.java)
        intent.putExtra(EXTRA_KEY_DRAFT_UUID, draftID)
        intent.putExtra(EXTRA_KEY_FROM_TYPE, DRAFT_RESTORE)
        when {
            PermissionUtil.hasPermission(
                context,
                REQUIRED_PERMISSIONS
            ) -> {
                startActivityAfterResReady(context, intent)
            }
            context is FragmentActivity -> {
                RequestPermissionBuilder(context, REQUIRED_PERMISSIONS.toList())
                    .callback { allGranted, _, deniedList ->
                        if (allGranted) {
                            startActivityAfterResReady(context, intent)
                        } else {
                            Toaster.show("No Permission of $deniedList")
                        }
                    }
                    .request()
            }
            else -> {
                Toaster.toast("为保证功能正常使用，请确保打开所需权限")
            }
        }
    }

    fun getSelectMediaDuration(selectList: MutableList<EditMedia>): String {
        val stringBuilder = StringBuilder()
        selectList.forEachIndexed { index, item ->
            if (item.isVideo) {
                stringBuilder.append(MediaUtil.getRealVideoMetaDataInfo(item.path).duration / 1000)
                stringBuilder.append(",")
            } else {
                stringBuilder.append("0")
                stringBuilder.append(",")
            }
        }
        if (stringBuilder.isNotEmpty()) {
            return stringBuilder.deleteCharAt(stringBuilder.length - 1).toString()
        }
        return "0"
    }
}


