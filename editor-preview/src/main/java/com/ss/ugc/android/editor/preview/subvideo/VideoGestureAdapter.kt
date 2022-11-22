package com.ss.ugc.android.editor.preview.subvideo

import android.graphics.Matrix
import android.util.Log
import android.util.SizeF
import android.view.MotionEvent
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.bytedance.ies.nle.editor_jni.NLESegmentVideo
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.bytedance.ies.nlemediajava.utils.millisToMicros
import com.ss.ugc.android.editor.core.Constants.Companion.TRACK_VIDEO
import com.ss.ugc.android.editor.core.event.SelectSlotEvent
import com.ss.ugc.android.editor.core.getVETrackType
import com.ss.ugc.android.editor.core.inMainTrack
import com.ss.ugc.android.editor.core.toMicro
import com.ss.ugc.android.editor.preview.BaseGestureAdapter

class VideoGestureAdapter (
         var subVideoViewModel: SubVideoViewModel,
         var videoCheck: IVideoChecker
): BaseGestureAdapter() {
    init{
        onGestureListenerAdapter = VideoGestureListener(videoCheck)
    }
    lateinit var viewHolder : SubVideoViewHolder
    var videoWidth: Float = 0F
    var videoHeight: Float = 0F

    var canvasWidth: Float = 0F
    var canvasHeight: Float = 0F

    /**
     * 单指移动的距离，不算吸附，因此可能跟视频的实际位置不一致
     */
    var currTransX: Float = 0F
    var currTransY: Float = 0F

    /**
     * 算上了吸附的真实平移距离，跟视频的实际位置一致
     */
    var realTransX: Float = 0F
    var realTransY: Float = 0F

    var currScale: Float = 1F
    var currRotate: Double = 0.0
    var initDegree: Int = 0
    var currDegree: Int = 0
    var triggerRotate: Boolean = false
    var cropScale: Float = 1F

    val ROTATION_TRIGGER = 20
    val MIN_SCALE = 0.1F
    val ADSORB_DEGREE_OFFSET = 10

    // 播放位置监听
    var playPositionObserver : Observer<Long> = Observer {
        it?.also {
            var dragstatue = subVideoViewModel.dragStateEvent.value
            var color: Int
            var booleanRefresh :Boolean = false
            when (dragstatue) {
                DragState.DRAG_SUB_SELECTED_NO_OPERATION -> {
                    slotFromDragState()?.apply {
                        booleanRefresh = isSegmentInTime(this,it)
                    }
                    color = VideoFramePainter.FRAME_GRAY_COLOR
                }
                DragState.DRAG_SUB_VIDEO -> {
                    slotFromDragState()?.apply {
                        booleanRefresh = isSegmentInTime(this,it)
                    }

                    color = VideoFramePainter.FRAME_COLOR
                }
                DragState.DRAG_MAIN_VIDEO -> {
                    booleanRefresh = slotFromDragState()?.inMainTrack(subVideoViewModel.nleEditorContext.nleModel)?:false
                    color = VideoFramePainter.FRAME_COLOR
                }
                else -> return@Observer
            }
            if(booleanRefresh){
                var frameeInfo = getSeletFrameInfo()
                viewHolder?.updateFrame(frameeInfo,color)
            }else{
                viewHolder?.updateFrame(null)
            }
        }
    }

    // 拖拽状态的监听
    var dragStateObserver : Observer<DragState> = Observer {
        it?.apply {
            if (!viewHolder.videoGestureLayout.enableEdit) {
                updateState(DragState.NONE)
            } else {
                updateState(this)
            }
        }
//                onDragStateChange(it);
    }

    // 主视频选中状态监听
    private val mainVideoSelectObserver = Observer<SelectSlotEvent> { it ->
//        if (it.changeWay.isKeyframe()) return@Observer
//        if (it.changeWay == SegmentChangeWay.HISTORY && it.segment?.keyframes?.isNotEmpty() == true) return@Observer
        it?.slot?.apply {
            var frameInfo = getSeletFrameInfo()
//            currRotate = rotation.toDouble()
            realTransX = transformX
            realTransY = transformY
            viewHolder.updateFrame(frameInfo, VideoFramePainter.FRAME_COLOR)

        }
    }


    private val subVideoObserver = Observer<SelectSlotEvent> {
//        if (it.changeWay.isKeyframe()) return@Observer
        it?.apply {
            val playPosition =
                subVideoViewModel.nleEditorContext.videoPlayer.curPosition().toLong().toMicro()
            if (this.slot == null) {
                viewHolder.updateFrame(null)
                return@apply
            }

            if (isSegmentInTime(this.slot!!, playPosition)) {
                val frameInfo = getSeletFrameInfo()
                slot?.apply {
//                    currRotate = rotation.toDouble()
                    realTransX = transformX
                    realTransY = transformY
                }

                viewHolder.updateFrame(frameInfo,VideoFramePainter.FRAME_COLOR)
            }
//            view?.updateSubVideoFrame(subVideoViewModel,playPosition)
        }
    }

    private val keyframeUpdate =
        Transformations.map(subVideoViewModel.nleEditorContext.keyframeUpdateEvent) {
            it.time.millisToMicros()
        }


    /**
     * 检查点击事件位置是否在NLE模型中的视频位置区域内
     */
    private fun checkInside(canvasWidth: Float,
                            canvasHeight: Float,
                            touchX: Float,
                            touchY: Float,
                            slot: NLETrackSlot
    ): Boolean {
        viewHolder?.videoGestureLayout?.apply {
            val centerX = canvasWidth * slot.transformX + measuredWidth * 0.5F
            val centerY = canvasHeight * slot.transformY + measuredHeight * 0.5F

            val videoSize = getVideoSizeEliminateRotate(slot)
            // 计算进行画面裁剪后的视频大小
            val cropSize = getCroppedSize(videoSize, slot)
            val size = getSuitSize(
                    cropSize.width,
                    cropSize.height,
                    canvasWidth,
                    canvasHeight,
                    slot.scale * cropScale
            )

            val relativeX = touchX - centerX
            val relativeY = touchY - centerY
            val reverseAngle = slot.rotation

            val matrix = Matrix()
            // 把坐标逆时针旋转一下，回到0度时度坐标系，这样直接跟视频宽高对比即可
            matrix.setRotate(reverseAngle)
            val transformPoint = floatArrayOf(relativeX, relativeY)
            matrix.mapPoints(transformPoint)

            val rx = transformPoint[0]
            val ry = transformPoint[1]

            val halfWidth = size.width / 2F
            val halfHeight = size.height / 2F
            return (rx in -halfWidth..halfWidth && ry in -halfHeight..halfHeight)
        }
        return false
    }

    fun onVideoTapped(e: MotionEvent): Boolean {
        return canvasSize(subVideoViewModel).takeIf {
            !(it?.width == 0f || it?.height == 0f)
        }?.let {
            val canvasWidth = it.width
            val canvasHeight = it.height
            val canvasSize = viewHolder!!.getSuitSize(
                    canvasWidth,
                    canvasHeight,
                    viewHolder!!.videoGestureLayout?.measuredWidth.toFloat(),
                    viewHolder!!.videoGestureLayout?.measuredHeight.toFloat(),
                    1F
            )
            this.canvasWidth = canvasSize.width
            this.canvasHeight = canvasSize.height

            val playPosition = subVideoViewModel.nleEditorContext.videoPlayer.curPosition().toLong().toMicro()
            var slot: NLETrackSlot? = null
            var nleModel = subVideoViewModel.nleEditorContext.nleModel
            val tracks : Collection<com.bytedance.ies.nle.editor_jni.NLETrack> = nleModel.tracks
                .filter {
                    it.getVETrackType() == TRACK_VIDEO && it.mainTrack.not()
                }.sortedByDescending {
                    it.layer
                }
            outer@ for (nleTrack in tracks) {
                val slots : Collection<NLETrackSlot> = nleTrack.sortedSlots.filter {
                    isSegmentInTime(it, playPosition)
                }
                inner@ for (s in slots) {
                    if (checkInside(this.canvasWidth, this.canvasHeight, e.x, e.y, s)) {
                        slot = s
                        break@outer
                    }
                }
            }

            slot?.let {
                // 如果可以选中副轨视频就选中，不能就退出
                subVideoViewModel.onVideoTapped(it)
                viewHolder?.apply {
                    onVideoDisplayChangeListener?.onSlotSelect(this.videoGestureLayout,it)
                }
                return true
            }

            //副轨道没有，再考虑主轨道
            val mainVideo = nleModel.tracks?.first {
                it.mainTrack
            }?.slots?.filter {
                isSegmentInTime(it, playPosition)
            }?.firstOrNull {
                checkInside(this.canvasWidth, this.canvasHeight, e.x, e.y, it)
            }
            viewHolder?.apply {
                onVideoDisplayChangeListener?.onSlotSelect(videoGestureLayout,mainVideo)
            }
            if (mainVideo == null) {
                // 选中了，但现在点击了空白位置，就不再选中
                if (subVideoViewModel.dragStateEvent.value == DragState.DRAG_MAIN_VIDEO || subVideoViewModel.dragStateEvent.value == DragState.DRAG_SUB_VIDEO) {
                    subVideoViewModel.selectMainTrack(null)
                }
            } else {
                subVideoViewModel.selectMainTrack(mainVideo)
            }
            true
        } ?: false
    }

    fun getCroppedSize(
            videoSize: SizeF,
            slot: NLETrackSlot
    ): SizeF {
        //因为剪同款业务已经对素材有过裁剪，所以这里不再计算crop后的大小
        val extra = slot.getExtra("business")
        if("cutsame".equals(extra)){
            return SizeF(videoSize.width, videoSize.height)
        }
        val crop = NLESegmentVideo.dynamicCast(slot.mainSegment).crop
        var croppedWidthScale = 0F
        if(crop != null){
            croppedWidthScale = crop.xRightUpper - crop.xLeft
        }

        var videoWidth = if (croppedWidthScale == 0F) {
            videoSize.width
        } else {
            videoSize.width * croppedWidthScale
        }
        var croppedHeightScale = 0F
        if(crop != null){
            croppedHeightScale = crop.yLeftLower - crop.yUpper
        }
        var videoHeight = if (croppedHeightScale == 0F) {
            videoSize.height
        } else {
            videoSize.height * croppedHeightScale
        }
        if (videoHeight.isNaN() || videoHeight.isInfinite()) {
            videoHeight = videoSize.height
        } else if (videoHeight.isNaN() || videoHeight.isInfinite()) {
            videoWidth = videoSize.width
        }
        return SizeF(videoWidth, videoHeight)
    }

    fun getVideoSizeEliminateRotate(slot: NLETrackSlot): SizeF {
        val videoWidth: Float
        val videoHeight: Float
        val rotation = -slot.rotation.toInt()//nle为逆时针
        val videoInfo = NLESegmentVideo.dynamicCast(slot.mainSegment).avFile
//        if (rotation == 90 || rotation == 270) {
//            videoWidth = videoInfo.height.toFloat()
//            videoHeight = videoInfo.width.toFloat()
//        } else {
        videoWidth = videoInfo.width.toFloat()
        videoHeight = videoInfo.height.toFloat()
//        }
        return SizeF(videoWidth, videoHeight)
    }
    inline fun isSegmentInTime(slot: NLETrackSlot, playPosition: Long?) =
        playPosition in slot.startTime until slot.endTime


    fun slotFromDragState(): NLETrackSlot? {
        return when (subVideoViewModel.dragStateEvent.value) {
            DragState.DRAG_MAIN_VIDEO,
            DragState.DRAG_MAIN_VIDEO_NO_SELECTED -> subVideoViewModel.mainVideoSelect.value?.slot
            DragState.DRAG_SUB_SELECTED_NO_OPERATION,
            DragState.DRAG_SUB_VIDEO -> subVideoViewModel.subVideoSelect.value?.slot
            else -> null
        }
    }


    fun updateFrameOnGesture() {
        viewHolder?.updateFrame(
                videoWidth,
                videoHeight,
                canvasWidth,
                canvasHeight,
                realTransX,
                realTransY,
                currScale,
                currDegree,
                cropScale
        )
    }


    fun updateState(dragState: DragState) {
//        this.dragState = dragState
//        when (dragState) {
//            DragState.DRAG_SUB_VIDEO -> {
//                this.tapState = VideoGestureListener.TapState.SUB_VIDEO
//            }
//            else -> {
//                this.tapState = VideoGestureListener.TapState.SUB_VIDEO
//            }
//        }
        observeFrameChange(dragState)
    }

    private fun observeFrameChange(state: DragState?) {
        when (state) {
            DragState.DRAG_SUB_VIDEO -> {
                subVideoViewModel.mainVideoSelect.removeObserver(mainVideoSelectObserver)
                subVideoViewModel.subVideoSelect.observe(subVideoViewModel.activity, subVideoObserver)
                subVideoViewModel.playPositionState.observe(subVideoViewModel.activity, playPositionObserver)
            }
            DragState.DRAG_SUB_SELECTED_NO_OPERATION -> {
                subVideoViewModel.mainVideoSelect.removeObserver(mainVideoSelectObserver)
                subVideoViewModel.subVideoSelect.removeObserver(subVideoObserver)
                subVideoViewModel.playPositionState.observe(subVideoViewModel.activity, playPositionObserver)
            }
            DragState.DRAG_MAIN_VIDEO -> {
                subVideoViewModel.mainVideoSelect.observe(subVideoViewModel.activity, mainVideoSelectObserver)
                subVideoViewModel.subVideoSelect.removeObserver(subVideoObserver)
                subVideoViewModel.playPositionState.observe(subVideoViewModel.activity, playPositionObserver)
            }
            else -> {
                viewHolder?.apply {
                    if (videoGestureLayout?.onTouch == false) videoFramePainter?.updateFrameInfo(null)
                    removeObservers()
                }

            }
        }
    }

    fun startObserData(){
        keyframeUpdate.observe(subVideoViewModel.activity, playPositionObserver)
        subVideoViewModel.playPositionState.observe(subVideoViewModel.activity, playPositionObserver)
        subVideoViewModel.dragStateEvent.observe(subVideoViewModel.activity, dragStateObserver)
        subVideoViewModel.mainVideoSelect.observe(subVideoViewModel.activity, mainVideoSelectObserver)
    }


    private fun removeObservers() {
        keyframeUpdate.removeObserver(playPositionObserver)
        subVideoViewModel.mainVideoSelect.removeObserver(mainVideoSelectObserver)
        subVideoViewModel.playPositionState.removeObserver(playPositionObserver)
        subVideoViewModel.subVideoSelect.removeObserver(subVideoObserver)
        subVideoViewModel.dragStateEvent.removeObserver(dragStateObserver)
    }

    fun getSuitSize(
            originWidth: Float,
            originHeight: Float,
            canvasWidth: Float,
            canvasHeight: Float,
            scale: Float
    ): SizeF {
        val canvasRatio = canvasWidth / canvasHeight
        val videoRatio = originWidth / originHeight
        return if (canvasRatio > videoRatio) {
            val height = canvasHeight * scale
            val width = height * videoRatio
            reportErrorSuitSize(
                1,
                originWidth,
                originHeight,
                canvasWidth,
                canvasHeight,
                width,
                height
            )
            // 先加个简单的保护，收集数据看看
            if (width.isFinite().not() || height.isFinite().not())
                SizeF(720f, 1280f)
            else
                SizeF(width, height)
        } else {
            val width = canvasWidth * scale
            val height = width / videoRatio
            reportErrorSuitSize(
                    2,
                    originWidth,
                    originHeight,
                    canvasWidth,
                    canvasHeight,
                    width,
                    height
            )
            // 先加个简单的保护，收集数据看看
            if (width.isFinite().not() || height.isFinite().not())
                SizeF(720f, 1280f)
            else
                SizeF(width, height)
        }
    }

    private fun reportErrorSuitSize(
            step: Int,
            originWidth: Float,
            originHeight: Float,
            canvasWidth: Float,
            canvasHeight: Float,
            resultWidth: Float,
            resultHeight: Float
    ) {
        if (resultWidth.isFinite().not() || resultWidth.isFinite().not()) {
            Log.e("ErrorSuitSize", "error suitSize, step: $step, ow: $originWidth, oh: $originHeight, cw: $canvasWidth, ch: $canvasHeight, rw: $resultWidth, rh: $resultHeight")
        }

    }

    fun updateSeletSlotProperty(){
        var segment :NLETrackSlot? = slotFromDragState()
        segment?.let {
            canvasSize(subVideoViewModel).takeIf {
                (it.width == 0F || it.height == 0F).not()
            }?.let {

                val suitSize = getSuitSize(
                        it.width,
                        it.height,
                        viewHolder!!.videoGestureLayout?.measuredWidth?.toFloat(),
                        viewHolder!!.videoGestureLayout?.measuredHeight?.toFloat(),
                        1F
                )
                this.canvasWidth = suitSize.width
                this.canvasHeight = suitSize.height
                this.currScale = segment.scale
                this.currTransX = segment.transformX
                this.currTransY = segment.transformY
                Log.e("test-l", "currTransX = " + currTransX + "---currTransY = " + currTransY)
                this.initDegree = -segment.rotation.toInt()//nle为逆时针
                this.currDegree = initDegree
                this.triggerRotate = false
                val videoSize = getVideoSizeEliminateRotate(segment)
                // 计算进行画面裁剪后的视频大小
                val croppedSize = getCroppedSize(videoSize, segment)
                this.videoWidth = croppedSize.width
                this.videoHeight = croppedSize.height
            }
        }
    }

    fun getSeletFrameInfo() :VideoFramePainter.FrameInfo?{
        var slot = slotFromDragState()
        var cSizeF = canvasSize(subVideoViewModel)
        var frameInfo : VideoFramePainter.FrameInfo? = null
        slot?.let { slot ->
            cSizeF.takeIf {
                (it.width == 0F || it.height == 0F).not()
            }?.let {
                val suitSize = getSuitSize(
                        it.width,
                        it.height,
                        viewHolder!!.videoGestureLayout?.measuredWidth.toFloat(),
                        viewHolder!!.videoGestureLayout?.measuredHeight.toFloat(),
                        1f
                )
                canvasWidth = suitSize.width
                canvasHeight = suitSize.height
                val videoSize = getVideoSizeEliminateRotate(slot)
                // 计算进行画面裁剪后的视频大小
                val size = getCroppedSize(videoSize, slot)
                var finalSuitSize = getSuitSize(size.width,size.height,canvasWidth,canvasHeight,slot.scale*1f)
                val centerX = canvasWidth * slot.transformX + viewHolder!!.videoGestureLayout?.measuredWidth * 0.5F
                val centerY = canvasHeight * slot.transformY + viewHolder!!.videoGestureLayout?.measuredHeight * 0.5F
                frameInfo = VideoFramePainter.FrameInfo(finalSuitSize.width,finalSuitSize.height,centerX,centerY,-slot.rotation.toInt())

            }
        }
        return frameInfo;
    }

    fun judgeRotationAdsorptionState() : VideoFramePainter.RotationAdsorptionState{
        val rotationState = viewHolder?.rotationAdsorptionHelper?.check(currDegree, 0)
        return rotationState?:VideoFramePainter.RotationAdsorptionState.NONE
    }
    fun judgeTransAdsorptionState() : VideoFramePainter.TransAdsorptionState{
        val adsorptionState =
                viewHolder?.transAdsorptionHelper?.check(canvasWidth, canvasHeight, currTransX, currTransY)
        return adsorptionState?:VideoFramePainter.TransAdsorptionState.NONE
    }
    fun canvasSize(subVideoViewModel: SubVideoViewModel): SizeF {
        val curResolution = subVideoViewModel.curResolution
        return SizeF(curResolution.width.toFloat(), curResolution.height.toFloat())
    }

    override fun attach(videoGestureLayout: VideoGestureLayout) {
        viewHolder = SubVideoViewHolder(videoGestureLayout)
        startObserData()
    }

    override fun detach() {

        viewHolder.updateFrame(null)
        viewHolder.setOnVideoDisplayChangeListener(null)
        removeObservers()
    }

    override fun onOrientationChange(orientation: Int?) {
    }

}