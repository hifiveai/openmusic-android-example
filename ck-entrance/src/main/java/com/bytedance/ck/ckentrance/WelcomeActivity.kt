package com.bytedance.ck.ckentrance

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.vesdk.vebase.log.LogKit
import com.dmcbig.mediapicker.PickerActivity
import com.dmcbig.mediapicker.PickerConfig
import com.dmcbig.mediapicker.entity.Media
import com.ss.ugc.android.editor.base.EditorConfig.IVideoCompilerConfig
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.fragment.FragmentHelper
import com.ss.ugc.android.editor.base.permission.RequestPermissionBuilder
import com.ss.ugc.android.editor.base.utils.PermissionUtil
import com.ss.ugc.android.editor.base.view.export.WaitingDialog
import com.ss.ugc.android.editor.main.EditorActivity
import com.ss.ugc.android.editor.main.EditorHelper
import com.ss.ugc.android.editor.main.draft.DraftFragment
import com.vesdk.vebase.Constant
import com.vesdk.vebase.ToastUtils
import com.vesdk.vebase.init.VEInitHelper
import com.vesdk.vebase.util.ReEncodeUtil
import com.vesdk.vebase.util.ReEncodeUtil.ReEncodeInterface
import com.vesdk.verecorder.record.demo.PreviewActivity
import java.io.File

class WelcomeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var tvVersion: TextView
    private lateinit var btnRecorder: View
    private lateinit var bgEdit: View
    private lateinit var mProgressBar: ProgressBar
    private var waitingDialog: WaitingDialog? = null

    private var draftFragment: DraftFragment? = null
    private val fragmentHelper = FragmentHelper(R.id.fragment_container)

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        initView()
    }

    private fun initView() {
        mProgressBar = findViewById(R.id.smooth_progress)
        mProgressBar.visibility = View.GONE
        tvVersion = findViewById(R.id.tv_version)
        btnRecorder = findViewById(R.id.bg_record)
        bgEdit = findViewById(R.id.bg_edit)
        tvVersion.text = BdSDkInitHelper.getVESDKVersionInfo()
        btnRecorder.setOnClickListener(this)
        bgEdit.setOnClickListener(this)
        findViewById<View>(R.id.bg_hepai).setOnClickListener(this)
        findViewById<View>(R.id.bg_drafts).setOnClickListener(this)
        waitingDialog = WaitingDialog(this)

        if (!VEInitHelper.checkAuth()) {
            showAuthDialog()
        }

        // demo 视频编辑回调，给到业务提供自己处理逻辑
        EditorSDK.instance.config.compileActionConfig = object : IVideoCompilerConfig {
            override fun onVideoCompileIntercept(
                duration: Long,
                size: Long,
                activity: Activity
            ): Boolean {
                LogKit.d(TAG, "video compile before \$size")
                return false
            }

            override fun onVideoCompileDone(path: String, activity: Activity) {
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                intent.data = Uri.fromFile(File(path))
                sendBroadcast(intent)
                //调用系统播放器播放视频
                previewExportVideo(path)
                // 点击完成后保存到本地，同时存入草稿箱，提示toast文案“已保存到本地和草稿箱”
                LogKit.d(TAG, "video compile done , just finish \$path")
                if (activity is EditorActivity) {
                    activity.editorActivityDelegate.saveTemplateDraft()
                    ToastUtils.show(getString(R.string.ck_has_saved_local_and_draft))
                }
                activity.finish()
            }

            override fun onCloseEdit(activity: Activity): Boolean {
                LogKit.d(TAG, "now exit edit page")
                return false
            }

            override fun onCustomCloseMethodIntercept(activity: Activity): Boolean {
                LogKit.d(TAG, "custom close method")
                // 业务方的代码逻辑
                return false
            }

            override fun onEditResume(activity: Activity) {}
        }

    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.bg_record -> { //视频拍摄
                checkPermissions {
                    checkResourceReady {
                        PreviewActivity.startPreviewActivity(this, null)
                    }
                }
            }
            R.id.bg_hepai -> { //合拍
                selectVideo(CHOSE_VIDEO_REQUEST_CODE, 1)
            }
            R.id.bg_edit -> { //视频创作
                if (VEInitHelper.checkAuth()) {
                    checkPermissions {
                        checkResourceReady {
                            EditorHelper.startEditor(this)

                        }
                    }
                } else {
                    showAuthDialog()
                }
            }
            R.id.bg_drafts -> { //草稿
                if (!VEInitHelper.checkAuth()) {
                    showAuthDialog()
                    return
                }
                draftFragment = DraftFragment()
                draftFragment?.let { fragment ->
                    fragmentHelper.bind(this).startFragment(fragment)
                }
            }
        }

    }

    private fun checkPermissions(action: () -> Unit) {
        if (PermissionUtil.hasPermission(this, permissions)) {
            action()
        } else {
            RequestPermissionBuilder(this, permissions.toList())
                .callback { allGranted, _, deniedList ->
                    if (allGranted) {
                        action()
                    } else {
                        Toast.makeText(
                            this,
                            "No Permission of $deniedList",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .request()
        }
    }

    private fun checkResourceReady(action: () -> Unit) {
        if (BdSDkInitHelper.isResReady(this)) {
            action()
        } else {
            waitingDialog?.show()
            waitingDialog?.setProgress(getString(R.string.ck_tips_resource_loading))
            BdSDkInitHelper.copyResourceToLocal(this) { ret, t ->
                if (ret == 0) {
                    action()
                } else {
                    Toast.makeText(this, "resource copy failed", Toast.LENGTH_SHORT)
                        .show()
                }
                waitingDialog?.dismiss()
            }
        }
    }

    private fun selectVideo(requestCode: Int, maxCount: Int) {
        val intent = Intent(this, PickerActivity::class.java)
        intent.putExtra(
            PickerConfig.SELECT_MODE,
            PickerConfig.PICKER_VIDEO
        ) //default image and video (Optional)
        val maxSize = 188743680L //long long long long类型
        intent.putExtra(PickerConfig.MAX_SELECT_SIZE, maxSize) //default 180MB (Optional)
        intent.putExtra(PickerConfig.MAX_SELECT_COUNT, maxCount) //default 40 (Optional)
        startActivityForResult(intent, requestCode)
    }

    private fun showAuthDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.title_auth_failed))
            .setMessage(VEInitHelper.getAuthReadableTips())
            .setCancelable(false)
            .setPositiveButton(R.string.ck_confirm) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOSE_VIDEO_REQUEST_CODE) {
            if (data == null) {
                return
            }
            val select: List<Media>? = data.getParcelableArrayListExtra(
                PickerConfig.EXTRA_RESULT
            )
            if (select != null && select.isNotEmpty()) {
                waitingDialog?.run {
                    setCancelable(false)
                    show()
                    setProgress(getString(R.string.ck_tips_resource_loading))
                }


                // 选取的合拍视频
                val duetVideoPath = select[0].path
                val canImport = com.ss.android.vesdk.VEUtils.isCanImport(duetVideoPath)
                LogKit.d(TAG, "canImport....$canImport")
                if (canImport != 0) {
                    waitingDialog?.dismiss()
                    ToastUtils.show(getString(R.string.ck_tips_the_material_not_supported))
                    return
                }
                // 对合拍视频进行抽离出视频和音频 并且把音频文件从aac转成pcm，提升性能
                ReEncodeUtil.reEncodeVideo(this, duetVideoPath, object : ReEncodeInterface {
                    override fun complete(ret: Int, videoPath: String, audioPath: String) {
                        LogKit.d(TAG, "合拍视频 处理完成....video:$videoPath  audio:$audioPath")
                        waitingDialog!!.dismiss()
                        val bundle = Bundle()
                        bundle.putBoolean(Constant.isDuet, true)
                        bundle.putString(Constant.duetVideoPath, videoPath)
                        bundle.putString(Constant.duetAudioPath, audioPath)
                        checkPermissions {
                            checkResourceReady {
                                PreviewActivity.startPreviewActivity(this@WelcomeActivity, bundle)
                            }
                        }
                    }

                    override fun error(ret: Int) {
                        LogKit.d(TAG, "合拍视频 处理失败..... ret:$ret")
                        waitingDialog!!.dismiss()
                        ToastUtils.show("处理失败:$ret")
                    }
                })
            }
        }
    }

    fun previewExportVideo(exportFilePath: String?) {
        val file = File(exportFilePath)
        val uri: Uri?
        uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val uriPreview = String.format(EditorActivity.URI_PREVIEW, applicationInfo.packageName)
            FileProvider.getUriForFile(
                applicationContext,
                uriPreview,
                file
            )
        } else {
            Uri.fromFile(file)
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, "video/*")
        startActivity(intent)
    }

    companion object {
        const val TAG = "MainActivity"
        private const val CHOSE_VIDEO_REQUEST_CODE = 0

    }
}