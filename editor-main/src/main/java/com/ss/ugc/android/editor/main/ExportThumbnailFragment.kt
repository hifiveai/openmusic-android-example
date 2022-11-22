package com.ss.ugc.android.editor.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Environment

import androidx.core.content.FileProvider
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bytedance.ies.nlemedia.IGetImageListener
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.EditorSDK.Companion.instance
import com.ss.ugc.android.editor.base.utils.runOnUiThread
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.api.video.IExportStateListener
import com.ss.ugc.android.editor.core.manager.IVideoPlayer
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.DLog.d
import com.ss.ugc.android.editor.core.utils.DLog.e
import com.ss.ugc.android.editor.core.utils.Toaster.show
import kotlinx.android.synthetic.main.fragment_thumbnail_export.*
import java.io.File
import java.nio.ByteBuffer

class
ExportThumbnailFragment : Fragment() {

    // Fragment size for bitmap centering
    private lateinit var sizeRect: Rect
    private var appWidth: Int = 0

    // View
    private lateinit var thisView: View
    private lateinit var nleEditorContext: NLEEditorContext
    private lateinit var videoPlayer: IVideoPlayer
    private var editorSDK = EditorSDK.instance

    // Sharing
    private val shareResId: Int = R.drawable.ic_color_share
    private val shareText: String = "分享"

    // Done
    private val doneResId: Int = R.drawable.ic_checked_done
    private val doneText: String = "完成"

    // Tags
    private val TAG = "ExportThumbnailFragment"

    // pausing compile
    private var compilePaused: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DLog.d(TAG, "ThumbFragment is created.")
    }

    override fun onDestroy() {
        super.onDestroy()
        DLog.d(TAG, "ThumbFragment is destroyed.")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        d(TAG, "ExportThumbnailFragment created.")
        super.onCreateView(inflater, container, savedInstanceState)
        thisView = inflater.inflate(R.layout.fragment_thumbnail_export, container, false)

        nleEditorContext = (activity as EditorActivity).nleEditorContext
        videoPlayer = nleEditorContext.videoPlayer
        thisView.visibility = View.GONE

        return thisView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // sizing
        sizeRect = Rect()
        thisView.getWindowVisibleDisplayFrame(sizeRect)
        appWidth = sizeRect.width()

        export_success_constraint_layout.visibility = View.GONE

        getFirstFrameForThumbnail()
    }

    /**
     * 获取首帧缩略图
     */
    private fun getFirstFrameForThumbnail() {
        DLog.d(TAG, "Getting first frame")
        videoPlayer.getImages(
            intArrayOf(0), appWidth * 2 / 3, -1, object : IGetImageListener {
                override fun onGetImageData(
                    bytes: ByteArray?,
                    pts: Int,
                    width: Int,
                    height: Int,
                    score: Float
                ): Int {
                    if (width > 0 && height > 0) {
                        return try {
                            val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val buffer = ByteBuffer.wrap(bytes)
                            bm.copyPixelsFromBuffer(buffer)
                            runOnUiThread {
                                export_template_view.updateBitmap(bm, width, height, appWidth)
                                thisView.visibility = VISIBLE
                                compileVideo()
                                DLog.d(TAG, "FIRST FRAME ACQUIRED")
                                videoPlayer.cancelGetVideoFrames()
                            }
                            0
                        } catch (e: Exception) {
                            e(TAG, "An exception occurred.", e)
                            e.printStackTrace()
                            -1
                        }
                    } else {
                        return -2
                    }
                }
            })
    }

    /**
     * 导出视频
     */
    private fun compileVideo() {
        EditorExportUtils.reportExportClickEvent()
        nleEditorContext.videoEditor.exportVideo(
            null,
            false,
            this.requireContext(),
            instance.config.waterMarkPath,
            object : IExportStateListener {
                override fun onExportError(error: Int, ext: Int, f: Float, msg: String?) {
                    EditorExportUtils.reportExportVideoFinishedEvent(false, error, msg!!)
                    d(TAG, "onCompileError:$error")
                    show("Error:$error", Toast.LENGTH_SHORT)
                    videoPlayer.prepare()
                }

                override fun onExportDone() {
//                    if (compilePaused) {
//                        return
//                    } else {
//                        ensureFrameFilled()
//                        eraseFrame()
//                        prepareSharing()
//                        EditorExportUtils.reportExportVideoFinishedEvent(true, 0, "")
//                        d("onCompileDone")
//                        // show("成功保存到系统相册", Toast.LENGTH_SHORT)
//                    }
                }

                override fun onExportProgress(progress: Float) {
                    // d(TAG, "onCompileProgress:$progress")
                    setProgress(progress)
                }
            }, EditorSDK.instance.config.isFixedRatio
        )
    }

    fun setProgress(progress: Float) {
//        export_template_view.onProgressChanged(progress)
    }

    private fun getExportFilePath(): String? {
        return Environment.getExternalStorageDirectory().path + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Camera" + File.separator
    }

    fun ensureFrameFilled() {
        export_template_view.fillFrame()
    }

    fun eraseFrame() {
     //   export_template_view.eraseFrameWithAnimation(800L)
    }

    fun prepareSharing() {
        notice_text.setText(R.string.export_saved)
        notice_text.textSize = 25F
        notice_subtitle.text = ""

        setUpAppShare(shareText, shareResId, doneText, doneResId)

        export_success_constraint_layout.visibility = VISIBLE
    }

    private fun setUpAppShare(
        shareText: String,
        shareResId: Int,
        doneText: String,
        doneResId: Int
    ) {
        share_icon.setImageResource(shareResId)
        share_icon.scaleType = ImageView.ScaleType.FIT_CENTER
        share_button.text = shareText

        done_icon.setImageResource(doneResId)
        done_icon.scaleType = ImageView.ScaleType.FIT_CENTER
        done_button.text = doneText

        share_button_layout.setOnClickListener {
            val appId = this.requireContext().applicationInfo.packageName
            val path: Uri = FileProvider.getUriForFile(
                this.requireContext(), "${appId}.FileProvider",
                File(this.getExportFilePath())
            )
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, path)
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Sharing this video")
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.type = "video/*"
            startActivity(Intent.createChooser(shareIntent, "sharing..."))
        }

        done_button_layout.setOnClickListener {
            var exportFilePath: String? = this.getExportFilePath()
            videoPlayer.prepare() //合成后需要重新prepare一下 此时位于0的位置
            if (editorSDK.config.compileActionConfig != null) {
                val config = editorSDK.config.compileActionConfig
                config?.onVideoCompileDone(exportFilePath!!, activity!!)
            } else {
                activity!!.finish()
//                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//                intent.data = Uri.fromFile(File(exportFilePath))
//                (activity as EditorActivity).sendBroadcast(intent)
//                videoPlayer.seekToPosition(0, SeekMode.EDITOR_SEEK_FLAG_LastSeek, true)
//                //调用系统播放器播放视频
//                (activity as EditorActivity).previewExportVideo(exportFilePath)
            }
        }
    }

    fun pauseCompile() {
        compilePaused = true
        videoPlayer.pause()
    }
}