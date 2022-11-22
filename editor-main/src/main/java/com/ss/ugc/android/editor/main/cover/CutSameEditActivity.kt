package com.ss.ugc.android.editor.main.cover

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.main.R
import com.ss.ugc.android.editor.main.cover.imageCover.CutSameData
import com.ss.ugc.android.editor.main.cover.imageCover.MediaConstant
import com.ss.ugc.android.editor.main.cover.imageCover.MediaConstant.EDIT_TYPE_CANVAS
import com.ss.ugc.android.editor.main.cover.imageCover.MediaConstant.MEDIA_TYPE_IMAGE
import com.ss.ugc.android.editor.main.cover.imageCover.VideoController
import com.ss.ugc.android.editor.main.cover.utils.ScreenListener
import com.ss.ugc.android.editor.preview.gesture.OnGestureListenerAdapter
import com.ss.ugc.android.editor.preview.subvideo.VideoEditorGestureLayout
import kotlinx.android.synthetic.main.activity_cut_same_edit.*
import java.util.HashMap

/**
 * @since 2021-07-09
 */
class CutSameEditActivity : AppCompatActivity() {

    private var editData: CutSameData? = null
    private var inView = ""
    private var controller: VideoController? = null
    private var recordState = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cut_same_edit)
//        NotchUtil.addPaddingTopWhenNotch(layoutRoot)

        intent.apply {
            editData = getParcelableExtra(EDIT_VIDEO_INPUTDATA)

            inView = getStringExtra(UI_TYPE) ?: ""
        }
        if (editData == null) {
            finish()
            return
        }

        report()
        if (editData!!.editType == EDIT_TYPE_CANVAS && editData!!.mediaType == MEDIA_TYPE_IMAGE) {
            select_next_step.visibility = View.GONE
        }

        val previewLp = previewView.layoutParams

        val outSize = Point()
        windowManager.defaultDisplay.getSize(outSize)
        previewLp.height = outSize.y - if (editData!!.mediaType == MEDIA_TYPE_IMAGE) {
            SizeUtil.dp2px(30f)
        } else {
            SizeUtil.dp2px(180f)
        }
        previewView.layoutParams = previewLp
        val rootLp = editPreviewRoot.layoutParams
        rootLp.height = outSize.y - if (editData!!.mediaType == MEDIA_TYPE_IMAGE) {
            SizeUtil.dp2px(30f)
        } else {
            SizeUtil.dp2px(180f)
        }
        editPreviewRoot.layoutParams = rootLp
        editData?.let { data ->
            // 会被改动，先赋值保护
            val videoStart = data.start
//            editVideoDuration.text = String.format("%.1fs", (data.duration.toFloat() / 1000))
            controller =
                VideoController(
                    this@CutSameEditActivity,
                    previewView,
                    data
                )
            cutSameTip.text = getString(R.string.ck_drag_select_show_region)
            controller?.let { controller ->
                if (data.editType == MediaConstant.EDIT_TYPE_VIDEO) {
                    // 跟视频 视频封面图片裁剪会走这
                    editPreviewRoot.post {
                        val maskBox = lockView.selectBoxScaleFactor(data.width, data.height)
                        controller.calculateInnerScaleFactor(maskBox.second, maskBox.third)
                        // 跟随视频模式需要手势操作
                        editPreviewRoot.setOnGestureListener(controller.onGestureListener)
                        editPreviewRoot.enableEdit = true
                    }
                } else {
                    // 跟画布
                    lockView.visibility = View.GONE
                    cutSameTip.visibility = View.GONE
                    if (data.mediaType == MediaConstant.MEDIA_TYPE_VIDEO) {
                        editPreviewRoot.setOnGestureListener(object : OnGestureListenerAdapter() {
                            override fun onUp(videoEditorGestureLayout: VideoEditorGestureLayout, event: MotionEvent): Boolean {
                                controller.switchVideoState()
                                return true
                            }
                        })
                    }
                }

                controller.seekTo(videoStart) {
                    controller.continueStart()
                }
                controller.onPause = {
                    if (data.mediaType == MediaConstant.MEDIA_TYPE_VIDEO) {
                        previewStart.visibility = View.VISIBLE
                    }
                }
                controller.onStart = {
                    previewStart.visibility = View.GONE
                }
                previewStart.setOnClickListener {
                    controller.continueStart()
                }
            }
        }

        select_next_step.setOnClickListener {
            setResult()
            finish()
        }
        cancelEdit.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setResult() {
        val callbackKey = intent.getStringExtra(REQUEST_CALLBACK_KEY)
        if (!callbackKey.isNullOrEmpty()) {
            callbackMap.remove(callbackKey)?.onEditEnd(editData)
        } else {
            val intent = Intent()
            intent.putExtra(UI_TYPE, inView)
            intent.putExtra(EDIT_VIDEO_OUTPUT, editData)
            setResult(Activity.RESULT_OK, intent)
        }
    }

    override fun onBackPressed() {
        val callbackKey = intent.getStringExtra(REQUEST_CALLBACK_KEY)
        if (!callbackKey.isNullOrEmpty()) {
            callbackMap.remove(callbackKey)?.onEditEnd(null)
        } else {
            val intent = Intent()
            intent.putExtra(UI_TYPE, inView)
            intent.putExtra(EDIT_VIDEO_OUTPUT, editData)
            setResult(Activity.RESULT_CANCELED, intent)
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        recordState = controller?.isPlaying ?: false
        controller?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (recordState && ScreenListener.getScreenState(this)) {
            controller?.continueStart()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller?.release()
        controller = null
    }

    private fun report() {
        val param = HashMap<String, String>()
        param["enter_from"] = if (inView == "") {
            "after_import"
        } else {
            "import"
        }
        param["material_type"] = if (editData!!.mediaType == MediaConstant.MEDIA_TYPE_VIDEO) {
            "video"
        } else {
            "photo"
        }
        param["template_type"] = if (editData!!.editType == EDIT_TYPE_CANVAS) {
            "follow_canvas"
        } else {
            "follow_video"
        }
//        ReportManager.onEvent("template_edit_cut_show", param)
    }

    companion object {

        const val EDIT_VIDEO_OUTPUT = "edit_video_outputdata"

        const val EDIT_VIDEO_INPUTDATA = "edit_video_inputdata"
        const val UI_TYPE = "ui_type"
//        const val REQUEST_CALLBACK_KEY = "request_callback_key"

        private const val TAG = "CutSameEditActivity"
        private const val REQUEST_CALLBACK_KEY = "request_callback_key"
        const val REQUEST_CODE = 10010

        private val callbackMap: MutableMap<String, Callback> = mutableMapOf()

        fun startEdit(
            activity: Activity,
            data: CutSameData,
            uiType: String,
            callbackKey: String,
            callback: Callback
        ) {
            callbackMap[callbackKey] = callback
//            SmartRouter.buildRoute(activity, "//cut_same_edit")
//                .withParam(EDIT_VIDEO_INPUTDATA, data)
//                .withParam(UI_TYPE, uiType)
//                .withParam(REQUEST_CALLBACK_KEY, callbackKey)
//                .open()

            val intent = Intent(activity, CutSameEditActivity::class.java)
            intent.putExtra(EDIT_VIDEO_INPUTDATA, data)
            intent.putExtra(UI_TYPE, uiType)
            intent.putExtra(REQUEST_CALLBACK_KEY, callbackKey)
            activity.startActivity(intent)

        }
    }

    interface Callback {
        fun onEditEnd(data: CutSameData?)
    }
}
