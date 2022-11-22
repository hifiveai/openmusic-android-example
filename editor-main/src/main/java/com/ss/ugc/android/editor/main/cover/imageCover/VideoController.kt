package com.ss.ugc.android.editor.main.cover.imageCover

import android.graphics.Point
import android.graphics.RectF
import android.os.Environment
import android.view.MotionEvent
import android.view.SurfaceView
import androidx.fragment.app.FragmentActivity
import com.bytedance.ies.nle.editor_jni.NLEEditorListener
import com.bytedance.ies.nlemediajava.width
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.data.VideoMetaDataInfo
import com.ss.ugc.android.editor.base.utils.MediaUtil
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.base.utils.runOnUiThread
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.api.video.EditMedia
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.preview.gesture.MoveGestureDetector
import com.ss.ugc.android.editor.preview.gesture.OnGestureListenerAdapter
import com.ss.ugc.android.editor.preview.gesture.ScaleGestureDetector
import com.ss.ugc.android.editor.preview.subvideo.VideoEditorGestureLayout
import kotlin.math.max
import kotlin.math.min

/**
 * @since 2021-07-09
 */
class VideoController(
    val activity: FragmentActivity,
    val previewView: SurfaceView,
    val data: CutSameData
) {

    companion object {
        const val TAG = "VideoController"

        /**
         *  抹除超过五位小数后的误差
         */
        fun removeDeviation(float: Float): Float {
            // kotlin的扩展函数会抛出异常
            return Math.round((float * 100000)) / 100000.0F
        }
    }

    private var canvasHeight: Int
    private var canvasWidth: Int
//    private val videoEditor = SimpleVideoPlayer(data.path, previewView)

    // 初始化视频之后，会根据视频数量返回相同数量的 segmentId, 用于接下来的视频操作

    var isPlaying: Boolean = false

    // 显示 scale 用于显示时缩放，需要与操作后的 data.scale 相乘，得到最终 scale
    private var innerScaleFactor = 1F

//    val videoInfo: VideoMetaDataInfo
//        get() = videoEditor.videoInfo
    val videoInfo: VideoMetaDataInfo
        get() = MediaUtil.getRealVideoMetaDataInfo(data.path)

    var boxRectF: RectF = RectF()
    var videoRectF = RectF()
    private var centerX = 0F
    private var centerY = 0F
    var width = 0F
    var height = 0F

    var onProgressListener: ((Float) -> Unit)? = null
    var onPause: () -> Unit = {}
    var onStart: () -> Unit = {}
    lateinit var editorContext: NLEEditorContext
    //作为全局变量避免匿名类被GC
    private val nleEditorListener = object : NLEEditorListener() {
        override fun onChanged() {
            runOnUiThread {
                editorContext.videoPlayer.player?.dataSource = editorContext.nleModel
            }
        }
    }
    init {
        val outSize = Point()
        activity.windowManager.defaultDisplay.getSize(outSize)
        canvasWidth = outSize.x
        canvasHeight = outSize.y - if (data.mediaType == MediaConstant.MEDIA_TYPE_IMAGE) {
            SizeUtil.dp2px(30f)
        } else {
            SizeUtil.dp2px(180f)
        }

        initEditor(data.path)

        initVeCanvas(canvasWidth, canvasHeight)


    }



    /**
     * 初始化nel
     */
    private fun initEditor(path: String) {
        editorContext = EditViewModelFactory.Companion.viewModelProvider(activity)
            .get(NLEEditorContext::class.java)
//        val mSurfaceView: SurfaceView = findViewById(R.id.mPreview)
        val rootPath = Environment.getExternalStorageDirectory().path
        editorContext.init(rootPath, previewView)

        val select: MutableList<EditMedia> = arrayListOf()
        select.add(EditMedia(path, true))
        editorContext.videoEditor.importMedia(select, 4000, EditorSDK.instance.config.isFixedRatio)
        activity.lifecycle.addObserver(editorContext)
        editorContext.videoPlayer.player?.dataSource = editorContext.nleModel
        editorContext.videoPlayer.seek(0)
        editorContext.nleEditor.addConsumer(nleEditorListener)
    }

    private fun initVeCanvas(canvasWidth: Int, canvasHeight: Int) {
        editorContext.nleModel?.apply {
            width = canvasWidth
            canvasRatio = canvasWidth.toFloat() / canvasHeight
        }
        editorContext.commit()

    }


    fun calculateInnerScaleFactor(boxWidth: Float, boxHeight: Float) {
        // 计算视频本来相对 canvas 的缩放大小
        var width = videoInfo.width
        var height = videoInfo.height
        if (videoInfo.rotation == 90 || videoInfo.rotation == 270) {
            width = videoInfo.height
            height = videoInfo.width
        }
        val canvasScaleFactor = getSaleFactorMin(
            width.toFloat(),
            height.toFloat(),
            canvasWidth.toFloat(),
            canvasHeight.toFloat()
        )
        val videoWidth = width * canvasScaleFactor
        val videoHeight = height * canvasScaleFactor
        // 根据视频在画布上真实显示的大小计算出视频在显示的时候需要缩放的比例 （一边铺满白框）
        val videoFrameScaleFactor = getSaleFactorMax(
            videoWidth,
            videoHeight,
            boxWidth,
            boxHeight
        )
        innerScaleFactor = videoFrameScaleFactor
        calculateBoxCoordinates(boxWidth, boxHeight)
        calculateVideoCoordinates(
            videoWidth * videoFrameScaleFactor,
            videoHeight * videoFrameScaleFactor
        )

        restoreBoxPositionInfo()
        moveVideo()
    }

    /**
     * The box position can be calculated by `data.scaleFactor`, `data.transitionX` and
     * `data.transitionY`.
     *
     * Restore these value by `data.veTranslateXXX`
     */
    private fun restoreBoxPositionInfo() {
        val scale =
            if (boxRectF.width() / boxRectF.height() >= videoRectF.width() / videoRectF.height()) {
                1F / (data.veTranslateRDX - data.veTranslateLUX)
            } else {
                1F / (data.veTranslateRDY - data.veTranslateLUY)
            }
        data.scaleFactor = with(scale) { if (this != 0F && this.isFinite()) this else 1F }

        val width = boxRectF.width() / (data.veTranslateRDX - data.veTranslateLUX)
        centerX = width * (0.5F - data.veTranslateLUX) + boxRectF.left
        data.translateX = with((centerX - videoRectF.centerX()) / canvasWidth) {
            if (this.isFinite()) this else 0F
        }
        val height = boxRectF.height() / (data.veTranslateRDY - data.veTranslateLUY)
        centerY = height * (0.5F - data.veTranslateLUY) + boxRectF.top
        data.translateY = with((centerY - videoRectF.centerY()) / canvasHeight) {
            if (this.isFinite()) this else 0F
        }
    }

    /**
     * 用于计算标记框相对于TextureView的左上角的初始位置
     */
    private fun calculateBoxCoordinates(boxWidth: Float, boxHeight: Float) {
        val faultBoxWidth = boxWidth - 1
        val faultBoxHeight = boxHeight - 1
        val left = (canvasWidth - faultBoxWidth) / 2
        val top = (canvasHeight - faultBoxHeight) / 2
        boxRectF.set(
            left,
            top,
            left + faultBoxWidth,
            top + faultBoxHeight
        )
//        BLog.d(TAG, " init box rect is $boxRectF")
        DLog.d( " init box rect is $boxRectF")
    }

    /**
     * 用于计算视频相对于TextureView的左上角的初始位置
     */
    private fun calculateVideoCoordinates(videoWidth: Float, videoHeight: Float) {
        val left = (canvasWidth - videoWidth) / 2
        val top = (canvasHeight - videoHeight) / 2
        videoRectF.set(
            left,
            top,
            left + videoWidth,
            top + videoHeight
        )
    }

    private fun calculateVETrans() {
        centerX = videoRectF.centerX() + data.translateX * canvasWidth
        centerY = videoRectF.centerY() + data.translateY * canvasHeight
        width = videoRectF.width() * data.scaleFactor
        height = videoRectF.height() * data.scaleFactor
        data.veTranslateLUX = removeDeviation((boxRectF.left - (centerX - width / 2)) / width)
        data.veTranslateLUY = removeDeviation((boxRectF.top - (centerY - height / 2)) / height)
        data.veTranslateRDX = removeDeviation(1 + (boxRectF.right - (centerX + width / 2)) / width)
        data.veTranslateRDY =
            removeDeviation(1 + (boxRectF.bottom - (centerY + height / 2)) / height)
//        BLog.d(
//            TAG,
//            "left is " + (centerX - width / 2) + " right is " +
//                    (centerX + width / 2) + " boxRectF.left " + boxRectF.left +
//                    " boxRectF.right " + boxRectF.right +
//                    "center x is $centerX center y is $centerY width is $width height is $height"
//        )
        DLog.d( "left is " + (centerX - width / 2) + " right is " +
                (centerX + width / 2) + " boxRectF.left " + boxRectF.left +
                " boxRectF.right " + boxRectF.right +
                "center x is $centerX center y is $centerY width is $width height is $height")
    }

    fun videoLength(): Long {
        return videoInfo.duration.toLong()
    }

//    private fun onProgress(position: Int) {
//        val currentPosition = position - data.start
//        val duration = data.duration
//        onProgressListener?.invoke(currentPosition / duration.toFloat())
//        if (currentPosition >= duration) {
//            videoEditor.seekDone(data.start.toInt(), true)
//        }
//    }
//
//    fun setStartSeek(start: Long, isLastSeek: Boolean) {
//        videoEditor.seeking(start.toInt())
//        if (isLastSeek) {
//            videoEditor.seekDone(start.toInt(), true)
//            data.start = start
//        }
//    }

    fun seekTo(start: Long, callback: (Int) -> Unit) {
//        videoEditor.seekDone(start.toInt(), true) { position ->
//            data.start = start
//            callback.invoke(position)
//        }
        editorContext.videoPlayer.seek(0)
    }

    fun pause() {
//        videoEditor.pause()
        editorContext.videoPlayer.pause()
    }

    fun pauseWithCallback() {
//        videoEditor.pause()
        editorContext.videoPlayer.pause()
        onPause()
    }

    fun switchVideoState() {
        if (isPlaying) {
            pauseWithCallback()
        } else {
            continueStart()
        }
    }

    fun continueStart() {
//        videoEditor.play()
        editorContext.videoPlayer.play()
        onStart()
    }

    fun release() {
//        videoEditor.destroy()
        editorContext.videoPlayer.destroy()
    }

    val onGestureListener = object : OnGestureListenerAdapter() {
        // 记录播放状态
        var playerState = isPlaying

        // 是否对视频进行了改变
        var changeVideo = false

        // 标记是否移动
        var changeVideoLocation = false

        override fun onScaleBegin(videoEditorGestureLayout: VideoEditorGestureLayout, scaleFactor: ScaleGestureDetector?): Boolean {
//            BLog.d(TAG, "onScaleBegin")
            DLog.d("onScaleBegin")
            return super.onScaleBegin(videoEditorGestureLayout, scaleFactor)
        }

        override fun onScale(videoEditorGestureLayout: VideoEditorGestureLayout,scaleFactor: ScaleGestureDetector?): Boolean {

            scaleFactor?.apply {
                val tempCenterX = centerX
                val tempCenterY = centerY
                val tempWidth = width * this.scaleFactor
                val tempHeight = height * this.scaleFactor
                val tempScale = data.scaleFactor * this.scaleFactor
                if ((tempCenterX - tempWidth / 2 <= boxRectF.left &&
                            tempCenterX + tempWidth / 2 >= boxRectF.right) &&
                    (tempCenterY - tempHeight / 2 <= boxRectF.top &&
                            tempCenterY + tempHeight / 2 >= boxRectF.bottom) || this.scaleFactor > 1.0F
                ) {
                    // data.scaleFactor 是操作 scale
                    data.scaleFactor *= this.scaleFactor
                    data.scaleFactor = max(0.3f, data.scaleFactor)
                    moveVideo()
                    changeVideo = true
                } else if (tempScale >= 1.0F) {
                    // 支持视频在边缘时仍可缩小，此时需要修改translate，使得视频贴靠边框
                    if (tempCenterX - tempWidth / 2 > boxRectF.left) {
                        data.translateX += (tempWidth - width) / canvasWidth * 0.5F
                    } else if (tempCenterX + tempWidth / 2 < boxRectF.right) {
                        data.translateX -= (tempWidth - width) / canvasWidth * 0.5F
                    }
                    if (tempCenterY - tempHeight / 2 > boxRectF.top) {
                        data.translateY += (tempHeight - height) / canvasHeight * 0.5F
                    } else if (tempCenterY + tempHeight / 2 < boxRectF.bottom) {
                        data.translateY -= (tempHeight - height) / canvasHeight * 0.5F
                    }
                    data.scaleFactor = tempScale
                    moveVideo()
                    changeVideo = true
                }
            }
            return true
        }

        override fun onScaleEnd(videoEditorGestureLayout: VideoEditorGestureLayout, scaleFactor: Float): Boolean {
//            BLog.d(TAG, "onScaleEnd")
            DLog.d("onScaleEnd")
            return super.onScaleEnd(videoEditorGestureLayout,scaleFactor)
        }

        override fun onMoveBegin(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            detector: MoveGestureDetector?,
            downX: Float,
            downY: Float
        ): Boolean {
//            BLog.d(TAG, "onMoveBegin")
            DLog.d("onMoveBegin")
            return super.onMoveBegin(videoEditorGestureLayout,detector, downX, downY)
        }

        override fun onMove(videoEditorGestureLayout: VideoEditorGestureLayout, detector: MoveGestureDetector): Boolean {
            var isTranslate = false
            val tempCenterX = centerX + detector.focusDelta.x
            val tempCenterY = centerY + detector.focusDelta.y
            val tempWidth = width
            val tempHeight = height
            if (tempCenterX - tempWidth / 2 <= boxRectF.left && tempCenterX + tempWidth / 2 >= boxRectF.right) {
                data.translateX += detector.focusDelta.x / canvasWidth
                isTranslate = true
            }
            if (tempCenterY - tempHeight / 2 <= boxRectF.top && tempCenterY + tempHeight / 2 >= boxRectF.bottom) {
                data.translateY += detector.focusDelta.y / canvasHeight
                isTranslate = true
            }
//            BLog.d(
//                TAG,
//                "on move left is " + (tempCenterX - tempWidth / 2) + " right is " +
//                        (tempCenterX + tempWidth / 2) + " boxRectF.left " + boxRectF.left +
//                        " boxRectF.right " + boxRectF.right + " tempCenterX " + tempCenterX +
//                        " center x " + centerX + " is translate " + isTranslate +
//                        " detector.focusDelta.x " + detector.focusDelta.x
//            )
            DLog.d("on move left is " + (tempCenterX - tempWidth / 2) + " right is " +
                    (tempCenterX + tempWidth / 2) + " boxRectF.left " + boxRectF.left +
                    " boxRectF.right " + boxRectF.right + " tempCenterX " + tempCenterX +
                    " center x " + centerX + " is translate " + isTranslate +
                    " detector.focusDelta.x " + detector.focusDelta.x)
            if (isTranslate) {
                moveVideo()
                changeVideo = true
                changeVideoLocation = true
            }
            return true
        }

        override fun onMoveEnd(videoEditorGestureLayout: VideoEditorGestureLayout, detector: MoveGestureDetector?) {
//            BLog.d(TAG, "onMoveEnd")
            DLog.d("onMoveEnd")
            if (changeVideoLocation) {
                detector?.apply {
//                    ReportManager.onEvent("template_video_edit_adjust")
                }
            }
        }

        override fun onDown(videoEditorGestureLayout: VideoEditorGestureLayout,event: MotionEvent): Boolean {
//            BLog.d(TAG, "onDown")
            DLog.d("onDown")
            playerState = isPlaying
            changeVideo = false
            // 标记是否移动
            changeVideoLocation = false
            pause()
            return super.onDown(videoEditorGestureLayout,event)
        }

        override fun onUp(videoEditorGestureLayout: VideoEditorGestureLayout, event: MotionEvent): Boolean {
//            BLog.d(TAG, "onUp")
            DLog.d("onUp")
            if (changeVideo && playerState) {
                continueStart()
            } else if (changeVideo.not() && playerState) {
                pauseWithCallback()
            } else if (changeVideo.not() && playerState.not()) {
                continueStart()
            }
            return super.onUp(videoEditorGestureLayout,event)
        }
    }

    fun moveVideo() {
//        videoEditor.updateVideoTransform(
//            innerScaleFactor * data.scaleFactor,
//            data.translateX,
//            data.translateY
//        )
        updateVideoFrame(
            innerScaleFactor * data.scaleFactor,
            data.translateX,
            data.translateY)

        calculateVETrans()
    }

    private fun updateVideoFrame(
        scale: Float,
        transX: Float,
        transY: Float,
        rotation: Float = 0F ) {
        editorContext.nleMainTrack.getSlotByIndex(0)?.apply {
            transformX = transX
            transformY = transY
            this.scale = scale
            this.rotation = rotation
            editorContext.commit()
        }
    }


    private fun getSaleFactorMin(srcX: Float, srcY: Float, distX: Float, distY: Float): Float {
        val sx = distX / srcX
        val sy = distY / srcY
        return min(sx, sy)
    }

    private fun getSaleFactorMax(srcX: Float, srcY: Float, distX: Float, distY: Float): Float {
        val sx = distX / srcX
        val sy = distY / srcY
        return max(sx, sy)
    }
}
