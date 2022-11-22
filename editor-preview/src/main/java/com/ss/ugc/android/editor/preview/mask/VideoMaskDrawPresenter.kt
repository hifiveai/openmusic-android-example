package com.ss.ugc.android.editor.preview.mask

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.text.TextUtils
import android.util.SizeF
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import com.bytedance.ies.nle.editor_jni.NLESegmentMask
import com.bytedance.ies.nle.editor_jni.NLESegmentVideo
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.bytedance.ies.nlemediajava.Log
import com.bytedance.ies.nlemediajava.nleSlotId
import com.ss.ugc.android.editor.base.viewmodel.VideoMaskViewModel
import com.ss.ugc.android.editor.core.event.SelectSlotEvent
import com.ss.ugc.android.editor.core.inMainTrack
import com.ss.ugc.android.editor.core.toMicro
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.preview.BaseGestureAdapter
import com.ss.ugc.android.editor.preview.R
import com.ss.ugc.android.editor.preview.gesture.MoveGestureDetector
import com.ss.ugc.android.editor.preview.gesture.OnGestureListenerAdapter
import com.ss.ugc.android.editor.preview.gesture.RotateGestureDetector
import com.ss.ugc.android.editor.preview.gesture.ScaleGestureDetector
import com.ss.ugc.android.editor.preview.mask.presenter.CircleMaskPresenter
import com.ss.ugc.android.editor.preview.mask.presenter.GeometricMaskPresenter
import com.ss.ugc.android.editor.preview.mask.presenter.LineMaskPresenter
import com.ss.ugc.android.editor.preview.mask.presenter.MirrorMaskPresenter
import com.ss.ugc.android.editor.preview.subvideo.VideoEditorGestureLayout
import com.ss.ugc.android.editor.preview.subvideo.VideoGestureLayout
import com.vesdk.veeditor.edit.mask.presenter.NoneMaskPresenter
import com.vesdk.veeditor.edit.mask.presenter.RoundRectMaskPresenter
import kotlin.math.max

/**
 * 视频编辑蒙版的Present处理类
 *
 * 对上监听View事件
 * 对下 调用ViewModel对象
 */
class VideoMaskDrawPresenter(
    val mVideoMaskViewModel: VideoMaskViewModel,
    val view: VideoGestureLayout
) : BaseGestureAdapter() {

    companion object {
        const val TAG = "VideoMaskGestureAdapter"
    }

    private var maskState = MaskVideoState.MASK_MAIN_VIDEO
    private var segmentId: Int = -1
    private var segmentInfo: NLETrackSlot? = null

    private var maskPresenter: AbstractMaskPresenter? = null

    private var videoRotate: Int = 0
    private var videoWidth: Float = 0F
    private var videoHeight: Float = 0F
    private var videoCenterX: Float = 0F // 相对于view自身左上角的位置
    private var videoCenterY: Float = 0F // 相对于view自身左上角的位置
    private var layoutOffsetToScreenTop = 0f // 由于某些机型有做刘海屏处理，view或者父view会有顶部预留

    private var maskWidth: Float = 0F // 为该视频宽度的百分比
    private var maskHeight: Float = 0F // 为该视频高度的百分比
    private var maskCenterX: Float = 0F // 相对视频中心点
    private var maskCenterY: Float = 0F
    private var maskCenterPointX: Float = 0F
    private var maskCenterPointY: Float = 0F
    private var maskRotate: Float = 0F
    private var maskFeather: Float = 0F
    private var maskRoundCorner: Float = 0F
    private var maskInvert = false
    private var maskResPath = ""
    private var tvRotate: TextView? = null

    private val mainVideoObserver = Observer<SelectSlotEvent> {
        it?.slot?.apply {
            DLog.d("$TAG subscribeMaskChange: mask update on main video")
            updateMainVideoMask(it?.slot)
        }


    }

    private val subVideoObserver = Observer<SelectSlotEvent> {
        it?.slot?.apply {
            val playPosition = (mVideoMaskViewModel.curPos() ?: 0).toLong()
            updateSubVideoMask(it.slot, playPosition.toMicro())
        }

    }

    private val playPositionObserver = Observer<Long> {
        when (maskState) {
            MaskVideoState.MASK_MAIN_VIDEO -> {
                updateMainVideoMask(mVideoMaskViewModel.getSelectedSlot())
            }
            MaskVideoState.MASK_SUB_VIDEO -> {
                updateSubVideoMask(mVideoMaskViewModel.getSelectedSlot(), (it ?: 0))
            }
        }
    }

    private val keyframeUpdateObserver = Observer<Long> {
        mVideoMaskViewModel.getSelectedSlot()?.let { slot ->
            slot.masks?.firstOrNull()?.segment?.let { maskInfo ->
                maskWidth = maskInfo.width
                maskHeight = maskInfo.height
                maskCenterX = maskInfo.centerX
                maskCenterY = maskInfo.centerY
                maskRotate = maskInfo.rotation
                maskFeather = maskInfo.feather
                maskRoundCorner = maskInfo.roundCorner
                calcMaskCenterPoint()
            }
            when (maskState) {
                MaskVideoState.MASK_MAIN_VIDEO -> {
                    updateMask()
                }
                MaskVideoState.MASK_SUB_VIDEO -> {
                    if (isSubVideoInTime(slot, it ?: 0)) {
                        updateMask()
                    } else {
                        setSegmentInfo(null)
                    }
                }
            }
        }
    }

    private fun updateMask() {
        DLog.d("$TAG updateMask  hasMask = ${hasMask()}")
        if (hasMask()) {
            if (videoWidth == 0F || videoHeight == 0F) {
                maskPresenter?.updateMaskInfo(null)
                return
            }
            maskPresenter?.updateMaskInfo(
                MaskPresenterInfo(
                    videoWidth,
                    videoHeight,
                    videoCenterX,
                    videoCenterY,
                    videoRotate,
                    maskWidth,
                    maskHeight,
                    maskCenterX,
                    maskCenterY,
                    maskCenterPointX,
                    maskCenterPointY,
                    maskRotate,
                    maskFeather,
                    maskRoundCorner,
                    maskInvert,
                    maskResPath
                )
            )
        } else {
            //第一次进入蒙版选择页时也需要传递数据
            maskPresenter?.updateMaskInfo(
                MaskPresenterInfo(
                    videoWidth,
                    videoHeight,
                    videoCenterX,
                    videoCenterY,
                    videoRotate,
                    maskWidth,
                    maskHeight,
                    maskCenterX,
                    maskCenterY,
                    maskCenterPointX,
                    maskCenterPointY,
                    maskRotate,
                    maskFeather,
                    maskRoundCorner,
                    maskInvert,
                    maskResPath
                )
            )
        }
    }


    private fun setSegmentInfo(slot: NLETrackSlot?) {
        val maskInfo = slot?.masks?.let {
            if (it.size > 0) {
                it.first()?.segment
            } else {
                null
            }
        }

        segmentInfo = slot
        updateMaskData(maskInfo)
        DLog.d( "$TAG setSegmentInfo --> ${slot == null}")
        segmentInfo?.apply {
            segmentId = slot?.nleSlotId!!
            //无模板状态下要绘制选择矩形 需要video的宽高数据
//            if (!hasMask()) {
//                DLog.d("$TAG setSegmentInfo no mask, return")
//                return
//            }

            var canvasRatio = mVideoMaskViewModel.getCanvasRatio()
            var canvasWidth: Float
            var canvasHeight: Float


            getSuitSize(
                canvasRatio,
                view.measuredWidth.toFloat(),
                view.measuredHeight.toFloat(),
                1F
            ).let {
                canvasWidth = it.width
                canvasHeight = it.height
            }
            videoRotate = -slot.rotation.toInt()
            val videoSize = getVideoSizeEliminateRotate(
                videoRotate,
                slot.mainSegment.resource.height.toFloat(),
                slot.mainSegment.resource.width.toFloat()
            )

//            // 计算进行画面裁剪后的视频大小
            val croppedSize = getCroppedSize(videoSize)
            if (croppedSize.width != 0F && croppedSize.height != 0F) {
                getSuitSize(
                    croppedSize.width / croppedSize.height,
                    canvasWidth,
                    canvasHeight,
                    slot.scale
                ).let {
                    videoWidth = it.width
                    videoHeight = it.height
                }
            }

            videoCenterX = canvasWidth * slot.transformX + view.measuredWidth * 0.5F
            videoCenterY = canvasHeight * slot.transformY + view.measuredHeight * 0.5F

            calcMaskCenterPoint()
        }

    }

    private fun getVideoSizeEliminateRotate(
        rotation: Int,
        height: Float,
        width: Float
    ): SizeF {
        val videoWidth: Float
        val videoHeight: Float
//        if (rotation == 90 || rotation == 270) {
//            videoWidth = height
//            videoHeight = width
//        } else {
            videoWidth = width
            videoHeight = height
//        }
        return SizeF(videoWidth, videoHeight)
    }

    private fun getSuitSize(
        ratio:Float,
        canvasWidth: Float,
        canvasHeight: Float,
        scale: Float
    ): SizeF {
        val canvasRatio = canvasWidth / canvasHeight
        return if (canvasRatio > ratio) {
            val height = canvasHeight * scale
            val width = height * ratio
            if (width.isFinite().not() || height.isFinite().not()) SizeF(720f, 1280f) else SizeF(
                width,
                height
            )
        } else {
            val width = canvasWidth * scale
            val height = width / ratio
            if (width.isFinite().not() || height.isFinite().not()) SizeF(720f, 1280f) else SizeF(
                width,
                height
            )
        }
    }
    private fun getCropWidthScale(selectedSlot: NLETrackSlot?): Float {

        if (selectedSlot == null) {
            return 1f
        }  else {
            NLESegmentVideo.dynamicCast(selectedSlot.mainSegment)?.apply {
                this.crop?.apply {
                    return  xRightUpper - xLeft
                }

            }
        }
        return 1f
    }


    private fun getCropHeightScale(selectedSlot: NLETrackSlot?): Float {

        if (selectedSlot == null) {
            return 1f
        }  else {
            NLESegmentVideo.dynamicCast(selectedSlot.mainSegment)?.apply {
                this.crop?.apply {
                    return yLeftLower -yUpper
                }
            }
        }
        return 1f
    }
    private fun getCroppedSize(
        videoSize: SizeF
    ): SizeF {
        // TODO: 2021/4/9
        val croppedWidthScale = getCropWidthScale(segmentInfo)
        val videoWidth = if (croppedWidthScale == 0F) {
            1F
        } else {
            videoSize.width * croppedWidthScale
        }
        val croppedHeightScale = getCropHeightScale(segmentInfo)
        val videoHeight = if (croppedHeightScale == 0F) {
            1F
        } else {
            videoSize.height * croppedHeightScale
        }
        return SizeF(videoWidth, videoHeight)
    }


    private fun hasMask(): Boolean {
        return segmentId > 0 && segmentInfo?.masks?.size ?: 0 > 0
    }

    private fun updateMainVideoMask(slot: NLETrackSlot?) {
        Log.d(TAG, "updateMainVideoMask ----> $maskState")
        if (maskState != MaskVideoState.MASK_MAIN_VIDEO) return
        setSegmentInfo(slot)
        updateMask()
    }

    private fun updateSubVideoMask(slot: NLETrackSlot?, timestamp: Long) {
        Log.d(TAG, "updateSubVideoMask ----> $maskState")

        if (maskState != MaskVideoState.MASK_SUB_VIDEO) return
        if (slot != null && isSubVideoInTime(slot, timestamp)) {
            setSegmentInfo(slot)
        } else {
            setSegmentInfo(null)
        }
        updateMask()
    }


    private fun updateMaskData(maskInfo: NLESegmentMask?) {
        if (null == maskInfo || maskInfo.effectSDKMask == null || TextUtils.isEmpty(maskInfo.effectSDKMask.resourceFile)) {
            maskPresenter = createPresenter(MaterialVideoMask.ResourceType.NONE)
        } else {
            maskWidth = maskInfo.width
            maskHeight = maskInfo.height
            maskCenterX = maskInfo.centerX
            maskCenterY = maskInfo.centerY
            maskRotate = maskInfo.rotation
            maskFeather = maskInfo.feather
            maskRoundCorner = maskInfo.roundCorner
            maskInvert = maskInfo.invert
            maskResPath = maskInfo.effectSDKMask.resourceFile
            maskPresenter =
                createPresenter(MaterialVideoMask.ResourceType.parseName(maskInfo.maskType))
        }
    }

    /**
     * 当前时间是否在副轨的效果范围内
     *
     * @param slot
     * @param playPosition  单位是微秒
     * @return
     */
    private fun isSubVideoInTime(slot: NLETrackSlot, playPosition: Long):Boolean {
        Log.d(TAG, "isSubVideoInTime ${playPosition}   slot.startTime= ${slot.startTime}" +
                " slot.endTime = ${ slot.endTime}")
        return playPosition in slot.startTime until slot.endTime

    }


    override fun attach(videoGestureLayout: VideoGestureLayout) {
        DLog.d("$TAG  attach = $layoutOffsetToScreenTop")

        view.let {

            val screenLocation = IntArray(2) { 0 }
            it.getLocationOnScreen(screenLocation)
            layoutOffsetToScreenTop = screenLocation[1].toFloat()
            DLog.d("$TAG  layoutOffsetToScreenTop = $layoutOffsetToScreenTop")
            mVideoMaskViewModel.playPositionState.observe(
                mVideoMaskViewModel.activity,
                playPositionObserver
            )
            mVideoMaskViewModel.maskSegmentState.observe(
                mVideoMaskViewModel.activity,
                mainVideoObserver
            )
            mVideoMaskViewModel.maskSegmentState.observe(
                mVideoMaskViewModel.activity,
                subVideoObserver
            )
            mVideoMaskViewModel.keyframeUpdateState.observe(
                mVideoMaskViewModel.activity,
                keyframeUpdateObserver
            )
        }
        updateState(isShow = true)
        videoGestureLayout.setOnGestureListener(createListener())
    }
    private fun updateState(isShow:Boolean) {
        val selectedSlot = mVideoMaskViewModel.maskSegmentState.value?.slot?.let {
            DLog.d("$TAG ,>>>>>>> updateState empty =  ${it == null}")

            if (it.inMainTrack(mVideoMaskViewModel.nleModel)) { // 主轨
                maskState = MaskVideoState.MASK_MAIN_VIDEO
                it
            } else {
                maskState = MaskVideoState.MASK_SUB_VIDEO
                val playPosition = mVideoMaskViewModel.playPositionState.value ?: 0
                if (isSubVideoInTime(it, playPosition) && isShow) it else null
            }

        }


        DLog.d("$TAG ,>>>>>>> updateState = empty ${selectedSlot == null}")
        setSegmentInfo(selectedSlot)
        updateMask()
    }

    override fun detach() {
        mVideoMaskViewModel.playPositionState.removeObserver(playPositionObserver)
        mVideoMaskViewModel.maskSegmentState.removeObserver(mainVideoObserver)
        mVideoMaskViewModel.maskSegmentState.removeObserver(subVideoObserver)
        mVideoMaskViewModel.keyframeUpdateState.removeObserver(keyframeUpdateObserver)
        updateState(isShow = false)
    }

    override fun onOrientationChange(orientation: Int?) {
        // pad 屏幕旋转后需要重新计算坐标点
        val segmentInfo = when (maskState) {
            MaskVideoState.MASK_MAIN_VIDEO -> {
                mVideoMaskViewModel.maskSegmentState.value?.slot
            }
            MaskVideoState.MASK_SUB_VIDEO -> {
                mVideoMaskViewModel.maskSegmentState.value?.slot
            }
        }
        setSegmentInfo(segmentInfo)
        updateMask()
    }

    private fun calcMaskCenterPoint() {
        DLog.d("$TAG calcMaskCenterPoint")
        // 中心点相对于视频位置，视频中心点为坐标
        val pointX = maskCenterX * videoWidth / 2f
        val pointY = maskCenterY * videoHeight / 2f
        // 逆转下坐标
        val matrix = Matrix()
        matrix.setRotate(-videoRotate.toFloat())
        val transformPoint = floatArrayOf(pointX, pointY)
        matrix.mapPoints(transformPoint)
        val realPointX = transformPoint[0]
        val realPointY = transformPoint[1]
        // 根据视频的中心点，计算出蒙板中心点坐标
        maskCenterPointX = videoCenterX + realPointX
        maskCenterPointY = videoCenterY - realPointY
    }

    private fun createPresenter(videoMask: MaterialVideoMask.ResourceType): AbstractMaskPresenter? {
        createListener()
        return when (videoMask) {
            MaterialVideoMask.ResourceType.LINE -> LineMaskPresenter(view, videoMaskListener!!)
            MaterialVideoMask.ResourceType.MIRROR -> MirrorMaskPresenter(view, videoMaskListener!!)
            MaterialVideoMask.ResourceType.CIRCLE -> CircleMaskPresenter(view, videoMaskListener!!)
            MaterialVideoMask.ResourceType.RECTANGLE -> RoundRectMaskPresenter(
                view,
                videoMaskListener!!
            )
            MaterialVideoMask.ResourceType.GEOMETRIC_SHAPE -> GeometricMaskPresenter(
                view,
                videoMaskListener!!
            )
            MaterialVideoMask.ResourceType.NONE -> NoneMaskPresenter(view, videoMaskListener!!)
        }
    }

    var videoMaskListener: VideoMaskGestureListener? = null

    open fun createListener(): VideoMaskGestureListener {
        if (videoMaskListener == null) {
            videoMaskListener = VideoMaskGestureListener()
        }
        onGestureListenerAdapter = videoMaskListener
        return videoMaskListener!!
    }

    enum class MaskVideoState {
        MASK_MAIN_VIDEO,
        MASK_SUB_VIDEO
    }


    /**
     * 监听手势View
     */
    inner class VideoMaskGestureListener() : OnGestureListenerAdapter(), IMaskGestureCallback {
        private var moving = false
        private var rotating = false
        private var scaling = false
        private var onTouching = false

        override fun onMoveBegin(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            detector: MoveGestureDetector?,
            downX: Float,
            downY: Float
        ): Boolean {
            if (hasMask()) {
                DLog.d("$TAG onMoveBegin")
                // 由于downY是相对于屏幕左上角的值，所以要减去view距离屏幕左上角的位置
                maskPresenter?.onMoveBegin(downX, max(0F, downY - layoutOffsetToScreenTop))
            }
            return super.onMoveBegin(videoEditorGestureLayout, detector, downX, downY)
        }

        override fun onMove(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            detector: MoveGestureDetector
        ): Boolean {
            if (hasMask()) {
                maskPresenter?.onMove(detector.focusDelta.x, detector.focusDelta.y)
            }
            return super.onMove(videoEditorGestureLayout, detector)
        }

        override fun onMoveEnd(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            detector: MoveGestureDetector?
        ) {
            if (moving) {
                moving = false
                mVideoMaskViewModel.onGestureEnd()
            }
            super.onMoveEnd(videoEditorGestureLayout, detector)
        }

        override fun onScaleBegin(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            scaleFactor: ScaleGestureDetector?
        ): Boolean {
            if (hasMask()) {
                DLog.d("$TAG onScaleBegin")
                maskPresenter?.onScaleBegin()
                scaling = true
            }
            return super.onScaleBegin(videoEditorGestureLayout, scaleFactor)
        }

        override fun onScale(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            scaleFactor: ScaleGestureDetector?
        ): Boolean {
            if (scaling && hasMask() && scaleFactor != null) {
                maskPresenter?.onScale(scaleFactor.scaleFactor)
                return true
            }
            return super.onScale(videoEditorGestureLayout, scaleFactor)
        }

        override fun onScaleEnd(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            scaleFactor: Float
        ): Boolean {
            if (moving) {
                moving = false
                mVideoMaskViewModel.onGestureEnd()
            }
            scaling = false
            return super.onScaleEnd(videoEditorGestureLayout, scaleFactor)
        }

        override fun onRotationBegin(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            detector: RotateGestureDetector?
        ): Boolean {
            if (hasMask()) {
                DLog.d("$TAG onRotationBegin")
                maskPresenter?.onRotateBegin()

                if (tvRotate == null) {
                    tvRotate = view.findViewById(R.id.tvRotate)
                }
                tvRotate?.text = ""
                tvRotate?.visibility = View.VISIBLE
            }
            return super.onRotationBegin(videoEditorGestureLayout, detector)
        }

        override fun onRotation(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            radian: Float
        ): Boolean {
            if (hasMask()) {
                var rotate = Math.toDegrees(radian.toDouble())
                while (rotate > 180) rotate = 360 - rotate
                while (rotate < -180) rotate += 360
                maskPresenter?.onRotation(rotate.toFloat() % 360F)
            }
            return super.onRotation(videoEditorGestureLayout, radian)
        }

        override fun onRotationEnd(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            angle: Float
        ): Boolean {
            if (rotating) {
                rotating = false
                mVideoMaskViewModel.onGestureEnd()
            }
            tvRotate?.text = ""
            tvRotate?.visibility = View.GONE
            return super.onRotationEnd(videoEditorGestureLayout, angle)
        }

        override fun onDown(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            event: MotionEvent
        ): Boolean {
            onTouching = true
            mVideoMaskViewModel.pausePlay()
            return super.onDown(videoEditorGestureLayout, event)
        }


        override fun onUp(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            event: MotionEvent
        ): Boolean {
            if (hasMask()) {
                maskPresenter?.onUp()
            }
            onTouching = false
            return super.onUp(videoEditorGestureLayout, event)
        }

        override fun onMaskMove(centerPointX: Float, centerPointY: Float, maskCenter: PointF) {
            maskCenterPointX = centerPointX
            maskCenterPointY = centerPointY
            if (maskCenterX == maskCenter.x && maskCenterY == maskCenter.y) {
                return
            }
            maskCenterX = maskCenter.x
            maskCenterY = maskCenter.y
            updateMask()
            mVideoMaskViewModel.updateCenter(maskCenterX, maskCenterY)
            moving = true
        }

//        override fun onMaskFeather(feather: Float) {
//            if (maskFeather == feather) {
//                return
//            }
//            maskFeather = feather
//            updateMask()
//            mVideoMaskViewModel.updateFeather(maskFeather)
//            moving = true
//        }

        override fun onSizeChange(width: Float, height: Float) {
            if (maskWidth == width && maskHeight == height) {
                return
            }
            maskWidth = width
            maskHeight = height
            updateMask()
            mVideoMaskViewModel.updateSize(maskWidth, maskHeight)
            moving = true
        }

        override fun onRotationChange(degrees: Float) {
            maskRotate = degrees % 360F
            updateMask()
            mVideoMaskViewModel.updateRotation(maskRotate)
            rotating = true
            tvRotate?.text = String.format("%.2f°", if (maskRotate > 0) maskRotate else 360 + maskRotate)
        }

        override fun onRoundCornerChange(corner: Float) {
            if (corner == maskRoundCorner) {
                return
            }
            maskRoundCorner = corner
            updateMask()
            mVideoMaskViewModel.updateCorner(maskRoundCorner)
            moving = true
        }

        override fun dispatchDraw(
            videoEditorGestureLayout: VideoEditorGestureLayout,
            canvas: Canvas?
        ) {
            canvas?.let {
                maskPresenter?.dispatchDraw(it)
            }
        }


    }
}