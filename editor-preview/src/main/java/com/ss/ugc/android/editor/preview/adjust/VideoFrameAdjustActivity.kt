package com.ss.ugc.android.editor.preview.adjust


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bytedance.ies.nle.editor_jni.NLEEditorListener
import com.bytedance.ies.nle.editor_jni.NLEResType
import com.bytedance.ies.nlemediajava.width
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CLIP_INDEX
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_FRAME_ROTATE_ANGLE
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_FRAME_SCALE
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_FRAME_TRANSLATE_X
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_FRAME_TRANSLATE_Y
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_LEFT_BOTTOM
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_LEFT_TOP
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_RATIO
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_RIGHT_BOTTOM
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_CROP_RIGHT_TOP
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_MEDIA_TYPE
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_SEGMENT_ID
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_SOURCE_TIME_RANGE_END
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_SOURCE_TIME_RANGE_START
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_VIDEO_HEIGHT
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_VIDEO_PATH
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_VIDEO_SOURCE_DURATION
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.ARG_VIDEO_WIDTH
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.MAX_SCALE
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.MIN_SCALE
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.RESULT_CROP_ROTATE_ANGLE
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.RESULT_CROP_SCALE
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.RESULT_CROP_TRANSLATE_X
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.RESULT_CROP_TRANSLATE_Y
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.RESULT_DATA_LEFT_BOTTOM
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.RESULT_DATA_LEFT_TOP
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.RESULT_DATA_RIGHT_BOTTOM
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.RESULT_DATA_RIGHT_TOP
import com.ss.ugc.android.editor.base.constants.CropConstants.Companion.RESULT_SEGMENT_ID
import com.ss.ugc.android.editor.base.extensions.show
import com.ss.ugc.android.editor.base.utils.FileUtil.stringForTime
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.base.utils.StatusBarUtil.setStatusBarColor
import com.ss.ugc.android.editor.base.utils.runOnUiThread
import com.ss.ugc.android.editor.core.Constants.Companion.KEY_MAIN
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.api.canvas.ORIGINAL
import com.ss.ugc.android.editor.core.api.video.EditMedia
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import com.ss.ugc.android.editor.preview.R
import com.ss.ugc.android.editor.preview.adjust.utils.GeometryUtils
import com.ss.ugc.android.editor.preview.adjust.utils.PadUtil
import com.ss.ugc.android.editor.preview.adjust.utils.toRectF
import com.ss.ugc.android.editor.preview.adjust.view.CropAdjustRect
import com.ss.ugc.android.editor.preview.adjust.view.RulerProgressBar
import com.ss.ugc.android.editor.preview.gesture.MoveGestureDetector
import com.ss.ugc.android.editor.preview.gesture.OnGestureListenerAdapter
import com.ss.ugc.android.editor.preview.gesture.RotateGestureDetector
import com.ss.ugc.android.editor.preview.gesture.ScaleGestureDetector
import com.ss.ugc.android.editor.preview.subvideo.VideoEditorGestureLayout
import com.ss.ugc.android.editor.preview.subvideo.VideoGestureLayout
import kotlinx.android.synthetic.main.activity_video_frame_adjust.*
import java.io.File
import kotlin.math.abs
import kotlin.math.min

class VideoFrameAdjustActivity : AppCompatActivity() {
    private val  TAG = "VideoFrameAdjustActivity"
    private lateinit var segmentId: String
    private var canvasWidth = 0
    private var canvasHeight = 0
    private var videoSourceDuration = 0

    private var clipIndex = 0
    private var videoWidth = 0
    private var videoHeight = 0

    private var videoOriginalSize: Size = Size(0, 0)
    lateinit var editorContext: NLEEditorContext

    // 视频帧适配画布的原始大小
    private var videoFrameOriginalRect = Rect()
    private val videoNextFrameMatrix = Matrix()

    private var originScale = 1F
    private var curScale = 1F
    private var curRotateAngle = 0F
    private var curPlayPosition = 0
    private var mediaType = 0
    private var currTransX: Float = 0F
    private var currTransY: Float = 0F
    private var cropLeftTop: PointF = PointF(0F, 0F)
    private var cropRightTop: PointF = PointF(1F, 0F)
    private var cropLeftBottom: PointF = PointF(0F, 1F)
    private var cropRightBottom: PointF = PointF(1F, 1F)

    private var onScaling = false
    private var onRotating = false
    private var onCropScaleDoingAnim = false
    private var rotateOriginScale = 1.0F
    private var skipCropScale = false

    private var animScale = 1F
    private var originCenterPoint = PointF()
    private var originRectF = Rect()
    private var scaleStartCenterPoint = PointF()
    private var scaleEndCenterPoint = PointF()
    private var lastScale = 1F
    private var lastRotateAngle = 0F
    private var lastDeltaScale = 1F
    private var totalDeltax = 0F
    private var totalDeltay = 0F
    private var lastWhiteRect = Rect()

    //因交互修改目前无用的几个数据
    private var cropRatio = ""
    private var sourceTimeRangeStart = 0L
    private var sourceTimeRangeEnd = 0L

    private val layoutId: Int
        get() = R.layout.activity_video_frame_adjust

    lateinit var gestureLayout: VideoGestureLayout
    private lateinit var mIvPlay: ImageView
    lateinit var mTvPlayTime: TextView

    private val mHandler = Handler(Looper.getMainLooper())

    private var runnable: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            val duration = sourceTimeRangeEnd - sourceTimeRangeStart
            mTvPlayTime.text = stringForTime(
                getCurrentPosition() - sourceTimeRangeStart.toInt() / 1000
            ) + "/" + stringForTime(duration.toInt() / 1000)
            mHandler.postDelayed(this, 500)
        }
    }


    private val onGestureListener = object : OnGestureListenerAdapter() {
        override fun onDoubleClick(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            e: MotionEvent?
        ): Boolean {
            return true
        }

        override fun onScaleBegin(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            scaleFactor: ScaleGestureDetector?
        ): Boolean {
            if (onCropScaleDoingAnim) {
                return super.onScaleBegin(videoEditorGestureLayout, scaleFactor)
            }
            onScaling = true
            return super.onScaleBegin(videoEditorGestureLayout, scaleFactor)
        }

        override fun onScale(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            scaleFactor: ScaleGestureDetector?
        ): Boolean {
            if (scaleFactor == null || onCropScaleDoingAnim) {
                return super.onScale(videoEditorGestureLayout, scaleFactor)
            }
            if (onScaling) {
                val scale = with(curScale * scaleFactor.scaleFactor) {
                    when {
                        this < MIN_SCALE -> MIN_SCALE
                        this > MAX_SCALE -> MAX_SCALE
                        else -> this
                    }
                }
                transformVideoFrame(curRotateAngle, scale, 0F, 0F)
                return true
            }
            return super.onScale(videoEditorGestureLayout, scaleFactor)
        }

        override fun onRotation(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            radian: Float
        ): Boolean {
            var rotate = - radian * 1000 / 10f
//            while (rotate > 180) rotate = 360 - rotate
//            while (rotate < -180) rotate += 360
            rotate += rotateProgressBar.getProgress().toFloat()
            Log.d(TAG, "on rotation with degree: $rotate  radian:  $radian ")
            val deltaScale = getMinScale(rotate.toFloat()) + 0.005F
            transformVideoFrame(rotate.toFloat(), deltaScale, 0F, 0F)

            return super.onRotation(videoEditorGestureLayout, radian)
        }

        override fun onRotationEnd(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            angle: Float
        ): Boolean {
            Log.d(TAG, "onRotationEnd with degree $angle")

            return super.onRotationEnd(videoEditorGestureLayout, angle)
        }

        override fun onScaleEnd(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            scaleFactor: Float
        ): Boolean {

            if (onCropScaleDoingAnim) {
                return super.onScaleEnd(videoEditorGestureLayout, scaleFactor)
            }

            onScaling = false
            return true
        }

        override fun onRotationBegin(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            detector: RotateGestureDetector?
        ): Boolean {
            Log.d(TAG, "onRotationBegin with degree")

            if (onCropScaleDoingAnim) {
                return super.onRotationBegin(videoEditorGestureLayout, detector)
            }
//            onRotating = true
            return true
        }

        override fun onDown(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            event: MotionEvent
        ): Boolean {
            editorContext.videoPlayer.pause()
            return true
        }

        override fun onUp(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            event: MotionEvent
        ): Boolean {
            updateReset()
            return true
        }

        override fun onMove(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            detector: MoveGestureDetector
        ): Boolean {
            if (onCropScaleDoingAnim) {
                return super.onMove(videoEditorGestureLayout, detector)
            }
            if (onScaling || onRotating) return super.onMove(videoEditorGestureLayout, detector)

            val deltaPx = detector.focusDelta.x
            val deltaPy = detector.focusDelta.y

            transformVideoFrame(curRotateAngle, curScale, deltaPx, deltaPy)
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        setStatusBarColor(this, Color.BLACK)
        ModuleCommon.init(this.application, "DavinciEdittor")
        // TODO: 2021/4/12  这里通过外部依赖工具类设置界面顶部
        // NotchUtil.addPaddingTopWhenNotch(adjust_ly_root)
        receivedDataBundle()
        initView()
        initListener()
        updateReset()
    }

    /**
     * 接收Intentbundle视频图像数据用于初始化
     */
    private fun receivedDataBundle() {
        val videoPath = intent.getStringExtra(ARG_VIDEO_PATH) ?: ""
        if (File(videoPath).exists().not()) {
            finish()
            return
        } else {
            videoWidth = intent.getIntExtra(ARG_VIDEO_WIDTH, 0)
            videoHeight = intent.getIntExtra(ARG_VIDEO_HEIGHT, 0)

            if (videoWidth == 0 || videoHeight == 0) {
                finish()
                return
            }

            cropRatio = intent.getStringExtra(ARG_CROP_RATIO) ?: "free"
            cropLeftTop = intent.getParcelableExtra(ARG_CROP_LEFT_TOP) ?: cropLeftTop
            cropRightTop = intent.getParcelableExtra(ARG_CROP_RIGHT_TOP) ?: cropRightTop
            cropLeftBottom = intent.getParcelableExtra(ARG_CROP_LEFT_BOTTOM) ?: cropLeftBottom
            cropRightBottom = intent.getParcelableExtra(ARG_CROP_RIGHT_BOTTOM) ?: cropRightBottom
            segmentId = intent.getStringExtra(ARG_SEGMENT_ID) ?: ""
            curScale = intent.getFloatExtra(ARG_CROP_FRAME_SCALE, 1F)
            curRotateAngle = intent.getFloatExtra(ARG_CROP_FRAME_ROTATE_ANGLE, 0F)
            currTransX = intent.getFloatExtra(ARG_CROP_FRAME_TRANSLATE_X, 0F)
            currTransY = intent.getFloatExtra(ARG_CROP_FRAME_TRANSLATE_Y, 0F)
            videoSourceDuration = intent.getIntExtra(ARG_VIDEO_SOURCE_DURATION, 0)
            sourceTimeRangeStart = intent.getLongExtra(ARG_SOURCE_TIME_RANGE_START, 0L)
            sourceTimeRangeEnd = intent.getLongExtra(ARG_SOURCE_TIME_RANGE_END, 0L)
            DLog.d("JFFFF", "sourceTimeRangeStart=$sourceTimeRangeStart sourceTimeRangeEnd=$sourceTimeRangeEnd")
            curPlayPosition = 0 /*intent.getIntExtra(ARG_CURRENT_PLAY_TIME, 0)*/
            clipIndex = intent.getIntExtra(ARG_CLIP_INDEX, 0)
            mediaType = intent.getIntExtra(ARG_MEDIA_TYPE, 0)

            initEditor(videoPath)
        }

    }
    private fun initView() {
        gestureLayout = findViewById(R.id.rlPreview)
        mIvPlay = findViewById(R.id.iv_play)
        mTvPlayTime = findViewById(R.id.tv_playtime)
        gestureLayout.enableEdit = true
        // 用post方法，Runnable 对象中的方法会在 View 的 measure、layout 等事件完成后触发。
        // 能够正确得到宽高，初始化VE & 视频帧初始坐标系videoFrameOriginalRect & 裁剪框的大小等状态
        postGetPreviewSizeToCalFrameInitVe()
        val endTimeInSec = (sourceTimeRangeEnd - sourceTimeRangeStart) / 1000000
        mTvPlayTime.text = String.format("00:00/%02d:%02d", endTimeInSec / 60, endTimeInSec % 60)
        if (mediaType != NLEResType.VIDEO.swigValue()) {
            mTvPlayTime.visibility = View.GONE
            mIvPlay.visibility = View.GONE
        }
        adapterForPad()
    }

    private fun initListener() {
        mIvPlay.setOnClickListener {
            if (it.isActivated) {
                editorContext.videoPlayer.pause()
            } else {
                editorContext.videoPlayer.playRange(sourceTimeRangeStart.toInt() / 1000, sourceTimeRangeEnd.toInt() / 1000)
            }
        }
        initVideoPlayObserver()
        setWhiteRectCropListener()
        initSeekRotateProgressBarListener()
        initConfirmResetListener()
        gestureLayout.setOnGestureListener(onGestureListener)

    }



    @SuppressLint("SetTextI18n")
    private fun initVideoPlayObserver() {
        if (mediaType != NLEResType.VIDEO.swigValue()) {
            return
        }
        LiveDataBus.getInstance().with<Int>(KEY_MAIN, Int::class.java)
            .observe(this, Observer<Int?> { position ->
                when (position) {
                    NLEEditorContext.STATE_PLAY -> { //代表播放
                        mHandler.removeCallbacks(runnable) // 防止多次调play的时候 有重复
                        editorContext.stopTrack()
                        mHandler.post(runnable)
                        mIvPlay.isActivated = true
                    }
                    NLEEditorContext.STATE_PAUSE -> { //代表暂停 统一处理ui状态
                        mHandler.removeCallbacks(runnable)
                        mIvPlay.isActivated = false
                        editorContext.stopTrack()
                    }
                    NLEEditorContext.STATE_BACK -> { // 点击关闭转场面板按钮
                    }
                    NLEEditorContext.STATE_SEEK -> { // seek了
                        mHandler.removeCallbacks(runnable)
                        mIvPlay.isActivated = false
                        editorContext.stopTrack()
                    }
                    NLEEditorContext.DELETE_CLIP -> { // 删除clip
                        editorContext.videoPlayer.pause()
                    }
                    NLEEditorContext.SPLIT_CLIP -> {
                        mHandler.removeCallbacks(runnable)
                        mIvPlay.isActivated = false
                        editorContext.stopTrack()
                    }
                }
            })
    }

    private fun getCurrentPosition(): Int {
        return editorContext.videoPlayer.curPosition()
    }

    private fun getTotalDuration(): Int {
        return editorContext.videoPlayer.totalDuration()
    }
    //作为全局变量避免匿名类被GC
    private val nleEditorListener = object : NLEEditorListener() {
        override fun onChanged() {
            runOnUiThread {
                editorContext.videoPlayer.player?.dataSource = editorContext.nleModel
            }
        }
    }

    /**
     * 初始化nel
     */
    private fun initEditor(path: String) {
        editorContext = viewModelProvider(this).get(NLEEditorContext::class.java)
        val mSurfaceView: SurfaceView = findViewById(R.id.mPreview)
        val rootPath = filesDir.absolutePath
        // 初始化 NLEEditor
        editorContext.nleEditor.toString()
        editorContext.init(rootPath, mSurfaceView)

        val select: MutableList<EditMedia> = arrayListOf()
        select.add(EditMedia(path, true))
        editorContext.videoEditor.importMedia(
            select,
            EditorSDK.instance.config.pictureTime,
            ORIGINAL
        )

        lifecycle.addObserver(editorContext)
        editorContext.videoPlayer.player?.dataSource = editorContext.nleModel
        DLog.d("JFFFF", "initEditor::sourceTimeRangeStart=${sourceTimeRangeStart.toInt()}")
        editorContext.videoPlayer.seek(sourceTimeRangeStart.toInt()/1000)
        editorContext.nleEditor.addConsumer(nleEditorListener)
    }

    private fun postGetPreviewSizeToCalFrameInitVe() {
        mPreview.post {
            canvasWidth = mPreview.width
            canvasHeight = mPreview.height
            videoOriginalSize = getMaxContentSize(
                videoWidth, videoHeight, canvasWidth, canvasHeight
            )
            val frameContentLeft = (canvasWidth - videoOriginalSize.width) / 2
            val frameContentTop = (canvasHeight - videoOriginalSize.height) / 2
            videoFrameOriginalRect.set(
                frameContentLeft, frameContentTop,
                frameContentLeft + videoOriginalSize.width,
                frameContentTop + videoOriginalSize.height
            )
            // 告诉裁剪框图片的展示宽高，让裁剪框适配图片的宽高
            viewCropAdjustRect.resetCurrentFrame(
                videoOriginalSize.width,
                videoOriginalSize.height
            )

            initVeCanvas(canvasWidth, canvasHeight)

            if (viewCropAdjustRect.getCropMode() == CropAdjustRect.CropMode.FREE) {
                // 自由模式有裁剪，恢复裁剪白框比例为原始样式
                val widthF = (cropRightTop.x - cropLeftTop.x)
                val heightF = (cropLeftBottom.y - cropLeftTop.y)
                if (1.0F - widthF > 0.001F ||
                    1.0F - heightF > 0.001F
                ) {
                    val ratioInCrop =
                        (widthF * videoOriginalSize.width) / (heightF * videoOriginalSize.height)
                    viewCropAdjustRect.setFreeModeCropRect(ratioInCrop)
                }
            }
            viewCropAdjustRect.show()

            rotateProgressBar.setCurrentIndicator(curRotateAngle.toInt())

            updateVideoFrame()
        }
    }

    private fun setWhiteRectCropListener() {
        viewCropAdjustRect.setCropListener(object : CropAdjustRect.CropListener {

            override fun onActionDown(curRect: Rect) {
                val centerPoint = PointF()
                centerPoint.x =
                    (videoFrameOriginalRect.right - videoFrameOriginalRect.left) / 2.toFloat() + videoFrameOriginalRect.left
                centerPoint.y =
                    (videoFrameOriginalRect.bottom - videoFrameOriginalRect.top) / 2.toFloat() + videoFrameOriginalRect.top
                val matrix = getOptMatrix()
                val centerPointArray = FloatArray(2)
                centerPointArray[0] = centerPoint.x
                centerPointArray[1] = centerPoint.y
                val mapCenterPointArray = FloatArray(2)
                matrix.mapPoints(mapCenterPointArray, centerPointArray)
                originCenterPoint.x = mapCenterPointArray[0]
                originCenterPoint.y = mapCenterPointArray[1]
                originRectF = Rect(viewCropAdjustRect.getWhiteRect())
            }

            override fun skipActionUp(
                originalWidth: Int,
                originalHeight: Int,
                targetWidth: Int,
                targetHeight: Int
            ): Boolean {
                val scaleX: Float = targetWidth.toFloat() / originalWidth
                val scaleY: Float = targetHeight.toFloat() / originalHeight
                val deltaScale = min(scaleX, scaleY)
                return curScale * deltaScale < 1F
            }

            override fun onNoCropChange() {
                setAllActionEnableState(true)
                updateReset()
            }

            override fun onCropScaleAnimStart(
                deltaLeft: Int,
                deltaTop: Int,
                deltaRight: Int,
                deltaBottom: Int,
                originalWidth: Int,
                originalHeight: Int,
                targetWidth: Int,
                targetHeight: Int
            ) {

                val scaleX: Float = targetWidth.toFloat() / originalWidth
                val scaleY: Float = targetHeight.toFloat() / originalHeight
                val deltaScale = min(scaleX, scaleY)

                if (curScale * deltaScale > MAX_SCALE) {
                    skipCropScale = true
                    return
                }

                setAllActionEnableState(false)
                onCropScaleDoingAnim = true
                animScale = curScale

                lastScale = curScale
                curScale *= deltaScale
                lastDeltaScale = deltaScale
                val scaleStartLeft = originRectF.left + deltaLeft
                val scaleStartTop = originRectF.top + deltaTop
                val scaleStartRight = originRectF.right + deltaRight
                val scaleStartBottom = originRectF.bottom + deltaBottom
                scaleStartCenterPoint.x =
                    (scaleStartRight - scaleStartLeft) / 2.toFloat() + scaleStartLeft
                scaleStartCenterPoint.y =
                    (scaleStartBottom - scaleStartTop) / 2.toFloat() + scaleStartTop

                scaleEndCenterPoint.x =
                    (originCenterPoint.x + deltaScale * (scaleStartCenterPoint.x - originCenterPoint.x))
                scaleEndCenterPoint.y =
                    (originCenterPoint.y + deltaScale * (scaleStartCenterPoint.y - originCenterPoint.y))

            }

            override fun onCropScaleAnimProgress(fraction: Float) {
                if (skipCropScale) {
                    return
                }

                val endRect = viewCropAdjustRect.getWhiteRect()
                val pointerCenter = Point()
                pointerCenter.x = (endRect.right - endRect.left) / 2 + endRect.left
                pointerCenter.y = (endRect.bottom - endRect.top) / 2 + endRect.top
                val dx = pointerCenter.x - scaleEndCenterPoint.x
                val dy = pointerCenter.y - scaleEndCenterPoint.y
                val animTranX = currTransX + (dx / canvasWidth) * fraction
                val animTranY = currTransY + (dy / canvasHeight) * fraction
                val aScale = animScale + (curScale - animScale) * fraction
                updateVideoFrame(tranX = animTranX, tranY = animTranY, scale = aScale)
            }

            override fun onCropScaleAnimEnd() {
                if (skipCropScale) {
                    skipCropScale = false
                    return
                }

                setAllActionEnableState(true)
                lastWhiteRect = viewCropAdjustRect.getWhiteRect()
                val pointerCenter = PointF()
                pointerCenter.x =
                    (lastWhiteRect.right - lastWhiteRect.left) / 2.toFloat() + lastWhiteRect.left
                pointerCenter.y =
                    (lastWhiteRect.bottom - lastWhiteRect.top) / 2.toFloat() + lastWhiteRect.top
                val dx = pointerCenter.x - scaleEndCenterPoint.x
                val dy = pointerCenter.y - scaleEndCenterPoint.y

                adjustOrInvokeTranslate(dx, dy)
                onCropScaleDoingAnim = false
                updateReset()
            }

            override fun isUnableToCropToThisPoint(
                leftTop: Point,
                rightTop: Point,
                leftBottom: Point,
                rightBottom: Point
            ): Boolean {
                val videoFramePointList = getVideoFramePointList(
                    curRotateAngle,
                    curScale,
                    currTransX * canvasWidth,
                    currTransY * canvasHeight
                )
                val dstList = listOf(
                    leftTop,
                    rightTop,
                    rightBottom,
                    leftBottom
                )
                val result = !GeometryUtils.isPolyContainsPoly(videoFramePointList, dstList)
                if (result) {
                    val sb = StringBuilder()
                    for (point in videoFramePointList) {
                        sb.append(point.toString())
                        sb.append(",")
                    }
                }
                return result
            }
        })
    }

    private fun updateReset() {
        val enable = isResetEnable()
        tvReset.alpha = if (enable) 1f else 0.4f
    }

    private val offset = 0.0001f

    private fun isResetEnable(): Boolean {
        if (abs(curScale - originScale) >= offset) {
            return true
        }
        if (abs(currTransX) >= offset) {
            return true
        }
        if (abs(currTransY) >= offset) {
            return true
        }
        if (abs(curRotateAngle) >= offset) {
            return true
        }
        return false
    }

    private fun initConfirmResetListener() {
        tvReset.setOnClickListener {
            viewCropAdjustRect.resetCurrentFrame(
                videoOriginalSize.width,
                videoOriginalSize.height
            )
            originScale = 1F
            viewCropAdjustRect.setCropMode(CropAdjustRect.CropMode.FREE)
            resetFrameState()
            lastWhiteRect = viewCropAdjustRect.getWhiteRect()
            updateReset()
        }
        pbbVideoFrameCrop.setOnClickListener {
            val cropRectFArray = calCropRectResultToSetData()
            val data = Intent()

            // 缩放值增加一个最小限制保护
            if (curScale < MIN_SCALE) {
                curScale = MIN_SCALE
            }

            data.putExtra(RESULT_SEGMENT_ID, segmentId)
            data.putExtra(RESULT_CROP_SCALE, curScale)
            data.putExtra(RESULT_CROP_ROTATE_ANGLE, curRotateAngle)
            data.putExtra(RESULT_CROP_TRANSLATE_X, currTransX)
            data.putExtra(RESULT_CROP_TRANSLATE_Y, currTransY)
            data.putExtra(RESULT_DATA_LEFT_TOP, PointF(cropRectFArray[0], cropRectFArray[1]))
            data.putExtra(RESULT_DATA_RIGHT_TOP, PointF(cropRectFArray[2], cropRectFArray[3]))
            data.putExtra(RESULT_DATA_LEFT_BOTTOM, PointF(cropRectFArray[4], cropRectFArray[5]))
            data.putExtra(RESULT_DATA_RIGHT_BOTTOM, PointF(cropRectFArray[6], cropRectFArray[7]))
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }


    /**
     * 计算VE需要的裁剪点结果，按左上，右上，左下，右下排列。
     */
    private fun calCropRectResultToSetData(): FloatArray {
        val cropRectArray = FloatArray(8)
        val whiteRect = viewCropAdjustRect.getWhiteRect()
        val matrix = Matrix()
        matrix.setRotate(
            -curRotateAngle,
            videoFrameOriginalRect.width() / 2F,
            videoFrameOriginalRect.height() / 2F
        )
        matrix.postScale(
            curScale,
            curScale,
            videoFrameOriginalRect.width() / 2F,
            videoFrameOriginalRect.height() / 2F
        )
        matrix.postTranslate(
            currTransX * canvasWidth,
            currTransY * canvasHeight
        )

        val dstFLoaArray = FloatArray(8)
        val srcFloatArray = FloatArray(8)
        srcFloatArray[0] = (whiteRect.left - videoFrameOriginalRect.left).toFloat()
        srcFloatArray[1] = (whiteRect.top - videoFrameOriginalRect.top).toFloat()
        srcFloatArray[2] = (whiteRect.right - videoFrameOriginalRect.left).toFloat()
        srcFloatArray[3] = (whiteRect.top - videoFrameOriginalRect.top).toFloat()
        srcFloatArray[4] = (whiteRect.left - videoFrameOriginalRect.left).toFloat()
        srcFloatArray[5] = (whiteRect.bottom - videoFrameOriginalRect.top).toFloat()
        srcFloatArray[6] = (whiteRect.right - videoFrameOriginalRect.left).toFloat()
        srcFloatArray[7] = (whiteRect.bottom - videoFrameOriginalRect.top).toFloat()

        val invertMatrix = Matrix()
        matrix.invert(invertMatrix)
        invertMatrix.mapPoints(dstFLoaArray, srcFloatArray)

        cropRectArray[0] = dstFLoaArray[0] / videoOriginalSize.width
        cropRectArray[1] = dstFLoaArray[1] / videoOriginalSize.height
        cropRectArray[2] = dstFLoaArray[2] / videoOriginalSize.width
        cropRectArray[3] = dstFLoaArray[3] / videoOriginalSize.height
        cropRectArray[4] = dstFLoaArray[4] / videoOriginalSize.width
        cropRectArray[5] = dstFLoaArray[5] / videoOriginalSize.height
        cropRectArray[6] = dstFLoaArray[6] / videoOriginalSize.width
        cropRectArray[7] = dstFLoaArray[7] / videoOriginalSize.height

        for (i in 0.until(cropRectArray.size)) {
            if (cropRectArray[i] < 0F) {
                cropRectArray[i] = 0F
            } else if (cropRectArray[i] > 1F) {
                cropRectArray[i] = 1F
            }
        }
        return cropRectArray
    }

    private fun resetFrameState() {
        curScale = originScale

        currTransX = 0F
        currTransY = 0F
        curRotateAngle = 0F
        lastDeltaScale = 1F
        rotateProgressBar.setCurrentIndicator(curRotateAngle.toInt())

        updateVideoFrame()
    }

    private fun getVideoFramePointList(
        rotateAngle: Float,
        scale: Float,
        transPx: Float,
        transPy: Float
    ): List<Point> {

        val dstFLoaArray = FloatArray(8)
        val srcFloatArray = FloatArray(8)

        val px =
            ((videoFrameOriginalRect.right - videoFrameOriginalRect.left) / 2 + videoFrameOriginalRect.left).toFloat()
        val py =
            ((videoFrameOriginalRect.bottom - videoFrameOriginalRect.top) / 2 + videoFrameOriginalRect.top).toFloat()

        videoNextFrameMatrix.reset()
        videoNextFrameMatrix.postScale(scale, scale, px, py)
        videoNextFrameMatrix.postRotate(rotateAngle, px, py)
        videoNextFrameMatrix.postTranslate(transPx, transPy)

        srcFloatArray[0] = videoFrameOriginalRect.left.toFloat()
        srcFloatArray[1] = videoFrameOriginalRect.top.toFloat()
        srcFloatArray[2] = videoFrameOriginalRect.right.toFloat()
        srcFloatArray[3] = videoFrameOriginalRect.top.toFloat()
        srcFloatArray[4] = videoFrameOriginalRect.right.toFloat()
        srcFloatArray[5] = videoFrameOriginalRect.bottom.toFloat()
        srcFloatArray[6] = videoFrameOriginalRect.left.toFloat()
        srcFloatArray[7] = videoFrameOriginalRect.bottom.toFloat()

        videoNextFrameMatrix.mapPoints(dstFLoaArray, srcFloatArray)

        val leftTop = Point(dstFLoaArray[0].toInt(), dstFLoaArray[1].toInt())
        val rightTop = Point(dstFLoaArray[2].toInt(), dstFLoaArray[3].toInt())
        val rightBottom = Point(dstFLoaArray[4].toInt(), dstFLoaArray[5].toInt())
        val leftBottom = Point(dstFLoaArray[6].toInt(), dstFLoaArray[7].toInt())

        val list = ArrayList<Point>()
        list.add(leftTop)
        list.add(rightTop)
        list.add(rightBottom)
        list.add(leftBottom)
        return list
    }

    private fun getMinScale(angle: Float): Float {
        val tempTransPx = currTransX * canvasWidth
        val tempTransPy = currTransY * canvasHeight
        val px =
            ((videoFrameOriginalRect.right - videoFrameOriginalRect.left) / 2 + videoFrameOriginalRect.left).toFloat()
        val py =
            ((videoFrameOriginalRect.bottom - videoFrameOriginalRect.top) / 2 + videoFrameOriginalRect.top).toFloat()

        val dstPointF = PointF()
        val dstArray = FloatArray(2)
        val srcArray = FloatArray(2)
        srcArray[0] = px
        srcArray[1] = py

        videoNextFrameMatrix.reset()
        videoNextFrameMatrix.postScale(curScale, curScale, px, py)
        videoNextFrameMatrix.postRotate(-curRotateAngle, px, py)
        videoNextFrameMatrix.postTranslate(tempTransPx, tempTransPy)
        videoNextFrameMatrix.mapPoints(dstArray, srcArray)
        dstPointF.x = dstArray[0]
        dstPointF.y = dstArray[1]
        val cropRectF = viewCropAdjustRect.getWhiteRect().toRectF()
        val internalPointer = PointF()
        internalPointer.x = (cropRectF.right + cropRectF.left) / 2
        internalPointer.y = (cropRectF.top + cropRectF.bottom) / 2
        return GeometryUtils.getRotateWithMinScale(
            cropRectF, videoFrameOriginalRect.toRectF(), internalPointer, dstPointF,
            angle
        )
    }

    private fun getMinScaleWithOutPointer(angle: Float): Float {
        val srcPointF = PointF()
        val dstPointF = PointF()

        srcPointF.x = 0F
        srcPointF.y = 0F
        dstPointF.x = 0F
        dstPointF.y = 0F

        return GeometryUtils.getRotateWithMinScale(
            viewCropAdjustRect.getWhiteRect().toRectF(),
            videoFrameOriginalRect.toRectF(),
            srcPointF,
            dstPointF,
            angle
        )
    }

    private fun transformVideoFrame(angle: Float, scale: Float, deltaPx: Float, deltaPy: Float) {
        val isRefreshVideoFrame = isRefreshVideoFrame(angle, scale, deltaPx, deltaPy)
        if (!isRefreshVideoFrame) {
            curRotateAngle = lastRotateAngle
            curScale = lastScale
            rotateProgressBar.setCurrentIndicator(curRotateAngle.toInt())
            return
        }
        totalDeltax += deltaPx
        totalDeltay += deltaPy
        lastRotateAngle = curRotateAngle
        lastScale = curScale
        rotateProgressBar.setCurrentIndicator(curRotateAngle.toInt())


        updateVideoFrame()
    }

    @Suppress("ComplexMethod")
    private fun isRefreshVideoFrame(
        angle: Float,
        scale: Float,
        deltaPx: Float,
        deltaPy: Float
    ): Boolean {
        curRotateAngle = when {
            angle > 45 -> {
                45F
            }
            angle < -45 -> {
                -45F
            }
            else -> {
                angle
            }
        }
        val minScale = originScale * getMinScaleWithOutPointer(curRotateAngle) + 0.001F
        val hasScaleChange: Boolean
        curScale = if (scale < minScale) {
            hasScaleChange = curScale != minScale
            minScale
        } else {
            hasScaleChange = curScale != scale
            scale
        }
        val tempTransPx = currTransX * canvasWidth + deltaPx
        val tempTransPy = currTransY * canvasHeight + deltaPy
        val videoFramePointList =
            getVideoFramePointList(curRotateAngle, curScale, tempTransPx, tempTransPy)
        val cropPointList = GeometryUtils.rect2PointList(viewCropAdjustRect.getWhiteRect())
        val containValues = GeometryUtils.calPolyContainsPoly(videoFramePointList, cropPointList)
        var ret = true
        containValues.forEach {
            ret = ret && it
        }
        var isRefreshVideoFrame: Boolean
        if (ret) {
            currTransX = tempTransPx / canvasWidth
            currTransY = tempTransPy / canvasHeight
            isRefreshVideoFrame = true
        } else {
            isRefreshVideoFrame = false
            if (deltaPx.toInt() != 0 && GeometryUtils.isPolyContainsPoly(
                    getVideoFramePointList(
                        curRotateAngle,
                        curScale,
                        tempTransPx,
                        tempTransPy - deltaPy
                    ), cropPointList
                )
            ) {
                currTransX = tempTransPx / canvasWidth
                isRefreshVideoFrame = true
            } else if (deltaPy.toInt() != 0 && GeometryUtils.isPolyContainsPoly(
                    getVideoFramePointList(
                        curRotateAngle,
                        curScale,
                        tempTransPx - deltaPx,
                        tempTransPy
                    ), cropPointList
                )
            ) {
                currTransY = tempTransPy / canvasHeight
                isRefreshVideoFrame = true
            }else if (hasScaleChange) {
                var noContainsCount = 0
                var noContainsIndex = 0
                var index = 0
                containValues.forEach {
                    if (!it) {
                        noContainsCount++
                        noContainsIndex = index
                    }
                    index++
                }
                if (noContainsCount == 1) {
                    val point = videoFramePointList[noContainsIndex]
                    val lastVideoFramePointList =
                        getVideoFramePointList(curRotateAngle, lastScale, tempTransPx, tempTransPy)
                    val lastPoint = lastVideoFramePointList[noContainsIndex]
                    val dPx = point.x - lastPoint.x
                    val dPy = point.y - lastPoint.y

                    val transX = tempTransPx - dPx
                    val transY = tempTransPy - dPy

                    if (
                        GeometryUtils.isPolyContainsPoly(
                            getVideoFramePointList(
                                curRotateAngle,
                                curScale,
                                transX,
                                transY
                            ), cropPointList
                        )
                    ) {
                        currTransX = transX / canvasWidth
                        currTransY = transY / canvasHeight
                        isRefreshVideoFrame = true
                    }
                }
            }
        }
        return isRefreshVideoFrame
    }

    private fun getMaxContentSize(
        originWidth: Int,
        originHeight: Int,
        canvasWidth: Int,
        canvasHeight: Int
    ): Size {
        val canvasRatio = canvasWidth.toFloat() / canvasHeight
        val videoRatio = originWidth.toFloat() / originHeight
        return if (canvasRatio > videoRatio) {
            val height = canvasHeight
            val width = height * videoRatio
            Size(width.toInt(), height)
        } else {
            val width = canvasWidth
            val height = width / videoRatio
            Size(width, height.toInt())
        }
    }



    private fun initSeekRotateProgressBarListener() {
        rotateProgressBar.setOnProgressListener(object :
            RulerProgressBar.OnProgressChangedListener {
            override fun onActionDown() {
                rotateOriginScale = curScale
            }

            override fun onProgress(progress: Int) {
                Log.d(TAG, "onProgress $progress")
                val angle = progress.toFloat()
                val deltaScale = getMinScale(angle) + 0.005F
                transformVideoFrame(angle, deltaScale, 0F, 0F)
            }

            override fun onActionUp(progress: Int) {
                updateReset()
            }
        })
    }

    private fun initVeCanvas(canvasWidth: Int, canvasHeight: Int) {
        editorContext.nleModel?.apply {
            width = canvasWidth
            canvasRatio = canvasWidth.toFloat() / canvasHeight
        }
        editorContext.commit()

    }

    private fun getOptMatrix(): Matrix {
        val matrix = Matrix()
        val px =
            ((videoFrameOriginalRect.right - videoFrameOriginalRect.left) / 2 + videoFrameOriginalRect.left).toFloat()
        val py =
            ((videoFrameOriginalRect.bottom - videoFrameOriginalRect.top) / 2 + videoFrameOriginalRect.top).toFloat()

        matrix.setRotate(
            curRotateAngle,
            px,
            py
        )
        matrix.postScale(
            curScale,
            curScale,
            px,
            py
        )
        matrix.postTranslate(
            currTransX * canvasWidth,
            currTransY * canvasHeight
        )
        return matrix
    }

    @Suppress("ComplexMethod")
    private fun adjustOrInvokeTranslate(transPx: Float, transPy: Float) {

        val videoFramePointList =
            getVideoFramePointList(
                curRotateAngle,
                curScale,
                currTransX * canvasWidth + transPx,
                currTransY * canvasHeight + transPy
            )
        val cropPointList = GeometryUtils.rect2PointList(viewCropAdjustRect.getWhiteRect())
        val containValues = GeometryUtils.calPolyContainsPoly(videoFramePointList, cropPointList)
        var ret = true
        containValues.forEach {
            ret = ret && it
        }

        var transX = transPx
        var transY = transPy

        if (!ret) {
            // top
            if ((!containValues[0] && !containValues[1])) {
                var distValue = Float.MAX_VALUE
                val distList0 =
                    GeometryUtils.calcYRayCastingDis(cropPointList[0], videoFramePointList)

                val distList1 =
                    GeometryUtils.calcYRayCastingDis(cropPointList[1], videoFramePointList)

                distList0?.forEach {
                    distValue = getMinAbstractValue(it, distValue)
                }

                distList1?.forEach {
                    distValue = getMinAbstractValue(it, distValue)
                }

                if (distValue != Float.MAX_VALUE) {
                    transY -= distValue
                }
            }

            // bottom 越界

            if ((!containValues[2] && !containValues[3])) {
                var distValue = Float.MAX_VALUE
                val distList2 =
                    GeometryUtils.calcYRayCastingDis(cropPointList[2], videoFramePointList)

                val distList3 =
                    GeometryUtils.calcYRayCastingDis(cropPointList[3], videoFramePointList)

                distList2?.forEach {
                    distValue = getMinAbstractValue(it, distValue)
                }

                distList3?.forEach {
                    distValue = getMinAbstractValue(it, distValue)
                }

                if (distValue != Float.MAX_VALUE) {
                    transY -= distValue
                }
            }

            // left
            if ((!containValues[0] && !containValues[3])) {
                var distValue = Float.MAX_VALUE
                val distList0 =
                    GeometryUtils.calcXRayCastingDis(cropPointList[0], videoFramePointList)

                val distList3 =
                    GeometryUtils.calcXRayCastingDis(cropPointList[3], videoFramePointList)

                distList3?.forEach {
                    distValue = getMinAbstractValue(it, distValue)
                }

                distList0?.forEach {
                    distValue = getMinAbstractValue(it, distValue)
                }

                if (distValue != Float.MAX_VALUE) {
                    transX -= distValue
                }
            }

            // right 越界
            if ((!containValues[1] && !containValues[2])) {
                var distValue = Float.MAX_VALUE
                val distList1 =
                    GeometryUtils.calcXRayCastingDis(cropPointList[1], videoFramePointList)

                val distList2 =
                    GeometryUtils.calcXRayCastingDis(cropPointList[2], videoFramePointList)

                distList1?.forEach {
                    distValue = getMinAbstractValue(it, distValue)
                }

                distList2?.forEach {
                    distValue = getMinAbstractValue(it, distValue)
                }

                if (distValue != Float.MAX_VALUE) {
                    transX -= distValue
                }
            }
            val videoFramePointList2 =
                getVideoFramePointList(
                    curRotateAngle,
                    curScale,
                    currTransX * canvasWidth + transX,
                    currTransY * canvasHeight + transY
                )

            GeometryUtils.isPolyContainsPoly(videoFramePointList2, cropPointList)
        }

        currTransX += transX / canvasWidth
        currTransY += transY / canvasHeight
        lastScale = curScale
        updateVideoFrame()

    }

    private fun updateVideoFrame(
        tranX: Float = currTransX,
        tranY: Float = currTransY,
        scale: Float = curScale ) {
        editorContext.nleMainTrack.getSlotByIndex(0)?.apply {
            transformX = tranX
            transformY = tranY
            this.scale = scale
            rotation = curRotateAngle
            editorContext.commit()
        }
    }

    private fun getMinAbstractValue(value1: Float, value2: Float): Float {
        return if (abs(value1) > abs(value2)) value2 else value1
    }

    // 设置重置和确定按钮是否可点击状态
    private fun setAllActionEnableState(enable: Boolean) {
        tvReset.isEnabled = enable
        pbbVideoFrameCrop.isEnabled = enable
    }

    // 适配平板
    private fun adapterForPad() {
        if (PadUtil.isPad) {
            setSliderBarMargin(OrientationManager.getOrientation())
            requestedOrientation =
                if (OrientationManager.isLand()) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }

    // 适配平板 旋转控件margin
    private fun setSliderBarMargin(orientation: Int) {
        rotateProgressBar.apply {
            val lp = layoutParams as ViewGroup.MarginLayoutParams
            val margin = if (PadUtil.isLandscape(orientation)) {
                (SizeUtil.getScreenWidth(context = ModuleCommon.application) * (180 / 1193F)).toInt()
            } else {
                (SizeUtil.getScreenWidth(context = ModuleCommon.application) * (40 / 834F)).toInt()
            }
            lp.marginStart = margin
            lp.marginEnd = margin
            layoutParams = lp
        }
    }
}
