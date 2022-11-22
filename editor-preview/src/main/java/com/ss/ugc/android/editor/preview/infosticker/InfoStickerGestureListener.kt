package com.ss.ugc.android.editor.preview.infosticker

import android.annotation.SuppressLint
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.util.SizeF
import android.view.MotionEvent
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.data.TextPanelTab
import com.ss.ugc.android.editor.base.event.InfoStickerOperationEvent
import com.ss.ugc.android.editor.base.event.InfoStickerOperationType
import com.ss.ugc.android.editor.base.event.TextOperationEvent
import com.ss.ugc.android.editor.base.event.TextOperationType
import com.ss.ugc.android.editor.base.utils.CommonUtils
import com.ss.ugc.android.editor.core.SegmentType
import com.ss.ugc.android.editor.core.event.ClosePanelEvent
import com.ss.ugc.android.editor.core.getSegmentType
import com.ss.ugc.android.editor.core.isTextSticker
import com.ss.ugc.android.editor.preview.gesture.MoveGestureDetector
import com.ss.ugc.android.editor.preview.gesture.OnGestureListenerAdapter
import com.ss.ugc.android.editor.preview.gesture.RotateGestureDetector
import com.ss.ugc.android.editor.preview.gesture.ScaleGestureDetector
import com.ss.ugc.android.editor.preview.subvideo.VideoEditorGestureLayout
import kotlin.math.*

class InfoStickerGestureListener() : OnGestureListenerAdapter() {

    override fun onDoubleClick(videoEditorGestureLayout: VideoEditorGestureLayout, e: MotionEvent?): Boolean {
        e ?: return super.onDoubleClick(videoEditorGestureLayout, e)
        if (CommonUtils.isFastClick(1000)) {
            return true
        }
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            if (infoStickerEditorView.textPanelTab == TextPanelTab.SEARCH) return false
            val stickers = stickerAdapters?.getStickers() ?: return false
            val sticker = findTouchItem(infoStickerEditorView, stickers, e.x, e.y)
            stickerAdapters?.stickerUIViewModel?.nleEditorContext?.closePanelEvent?.value =
                ClosePanelEvent()//切换时关闭底部功能面板
            when (sticker?.getSegmentType()) {
                SegmentType.TEXT_TEMPLATE ->
                    stickerAdapters?.also {
                        stickerAdapters?.setSelected(sticker.id.toString())
                        showEditPanelIfTouchText(infoStickerEditorView, it, sticker, e)
                        it.stickerUIViewModel.nleEditorContext.closePanelEvent.value =
                            ClosePanelEvent(false)//切换时关闭底部功能面板
                    }
                else -> {
                    sticker?.also {
                        stickerAdapters?.setSelected(it.id.toString())
                        stickerAdapters?.showEditPanel(sticker)
                        stickerAdapters?.stickerUIViewModel?.nleEditorContext?.closePanelEvent?.value =
                            ClosePanelEvent(false)//切换时关闭底部功能面板
                    }
                }
            }
        }
        return true
    }

    private fun showEditPanelIfTouchText(
        infoStickerGestureView: InfoStickerGestureView,
        stickerAdapters: InfoStickerGestureAdapter,
        sticker: NLETrackSlot,
        e: MotionEvent
    ) {
        val param = stickerAdapters.getTextTemplateParam(sticker.id.toString())
        var textIndex = -1
        val textsBounds = param?.textsBounds() ?: emptyList()
        if (param != null && textsBounds.size == param.text_list?.size) {
            kotlin.run {
                textsBounds.forEachIndexed { index, rectF ->
                    val box = getTextBoxRect(infoStickerGestureView, sticker, rectF)
                    if (detectInItemContent(sticker, box, e.x, e.y)) {
                        textIndex = index
                        return@run
                    }
                }
            }
        }
        if (param != null && textIndex != -1) {
            stickerAdapters.showTextTemplateEditPanel(sticker.id.toString(), textIndex)
        }
    }

    private fun getTextBoxRect(infoStickerGestureView: InfoStickerGestureView, sticker: NLETrackSlot, rect: RectF): RectF {
        val w = infoStickerGestureView.measuredWidth
        val h = infoStickerGestureView.measuredHeight
        val bounds = rect.run { RectF(left * w, top * h, right * w, bottom * h) }
        val rotation = sticker?.rotation ?: 0F
        rotateRect(bounds, bounds.centerX(), bounds.centerY(), rotation)
        return bounds
    }


    private fun findTouchItem(videoEditorGestureLayout: VideoEditorGestureLayout, stickers: List<NLETrackSlot>, x: Float, y: Float): NLETrackSlot? {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            val playPosition = stickerAdapters!!.getPlayPosition() ?: 0L
            stickers.sortedByDescending { it.layer }.forEach {
                val bounds = getItemRect(infoStickerEditorView,it, stickerAdapters!!.getStickerBoundingBox(it)?.box) ?: return@forEach
                if (playPosition in it.startTime..it.endTime &&
                        detectInItemContent(it, bounds, x, y)
                ) {
                    return it
                }
            }
        }

        return null
    }

    private fun detectInItemContent(
            sticker: NLETrackSlot,
            bounds: RectF,
            x: Float,
            y: Float
    ): Boolean {
        val cx = bounds.centerX()
        val cy = bounds.centerY()

        // 将用户点击的位置顺时针绕矩形中心旋转贴纸的角度 判断用户是否点中贴纸
        val rotation = sticker.rotation
        val cosAngle = cos(Math.toRadians(rotation.toDouble()))
        val sinAngle = sin(Math.toRadians(rotation.toDouble()))
        val transX = ((x - cx) * cosAngle - (y - cy) * sinAngle + cx).toFloat()
        val transY = ((x - cx) * sinAngle + (y - cy) * cosAngle + cy).toFloat()

        return bounds.contains(transX, transY)
    }

    private fun getItemRect(infoStickerGestureView: InfoStickerGestureView, sticker: NLETrackSlot, sizeBox: SizeF): RectF? {
        infoStickerGestureView.apply {
            val width = sizeBox.width * measuredWidth
            val height = sizeBox.height * measuredHeight
            val position = stickerAdapters!!.transformPosition(sticker)
            val left = position.x * measuredWidth - width / 2
            val top = position.y * measuredHeight - height / 2
            val bounds = RectF(left, top, left + width, top + height)

            val rotation = sticker.rotation
            rotateRect(bounds, bounds.centerX(), bounds.centerY(), rotation)
            return bounds
        }

    }

    private fun rotateRect(
            rect: RectF,
            center_x: Float,
            center_y: Float,
            rotateAngle: Float
    ) {
        val x = rect.centerX()
        val y = rect.centerY()
        val sinA = sin(Math.toRadians(rotateAngle.toDouble())).toFloat()
        val cosA = cos(Math.toRadians(rotateAngle.toDouble())).toFloat()
        val newX = center_x + (x - center_x) * cosA - (y - center_y) * sinA
        val newY = center_y + (y - center_y) * cosA + (x - center_x) * sinA

        val dx = newX - x
        val dy = newY - y

        rect.offset(dx, dy)
    }

    override fun onMoveBegin(videoEditorGestureLayout: VideoEditorGestureLayout,
                             detector: MoveGestureDetector?,
                             downX: Float,
                             downY: Float
    ): Boolean {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            val handle = stickerAdapters!!.infoSticker != null && stickerAdapters!!.isSelectedInTime
            if (handle) {
                stickerAdapters?.isOnMoving = true
            }
            return handle
        }

    }

    override fun onMove(videoEditorGestureLayout: VideoEditorGestureLayout, detector: MoveGestureDetector): Boolean {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            val sticker = stickerAdapters!!.infoSticker
            if (sticker == null || !stickerAdapters!!.isSelectedInTime) return super.onMove(this,detector)
            val dx = detector.focusDelta.x
            val dy = detector.focusDelta.y
            if (dx == 0f && dy == 0f) {
                return false
            }
            val width = stickerAdapters!!.boundingBox.box.width * measuredWidth
            val height = stickerAdapters!!.boundingBox.box.height * measuredHeight
            val left = stickerAdapters!!.x * measuredWidth - width / 2
            val top = stickerAdapters!!.y * measuredHeight - height / 2
            val rect = RectF(left, top, left + width, top + height)
            rotateRect(rect, rect.centerX(), rect.centerY(), stickerAdapters!!.rotation.toFloat())

            stickerAdapters!!.deltaDx += dx
            stickerAdapters!!.deltaDy += dy

            if (abs((measuredWidth / 2 - (rect.centerX() + stickerAdapters!!.deltaDx))) < 30) {
                stickerAdapters!!.x = 0.5F
                stickerAdapters!!.adsorbVertical = true
            } else {
                stickerAdapters!!.adsorbVertical = false
                stickerAdapters!!.x += stickerAdapters!!.deltaDx / measuredWidth
                stickerAdapters!!.deltaDx = 0F
            }

            if (abs((measuredHeight / 2 - (rect.centerY() + stickerAdapters!!.deltaDy))) < 30) {
                stickerAdapters!!.y = 0.5F
                stickerAdapters!!.adsorbHorizontal = true
            } else {
                stickerAdapters!!.adsorbHorizontal = false
                stickerAdapters!!.y += stickerAdapters!!.deltaDy / measuredHeight
                stickerAdapters!!.deltaDy = 0F
            }

            when {
                stickerAdapters!!.adsorbHorizontal && stickerAdapters!!.adsorbVertical -> setAdsorbState(InfoStickerGestureView.AdsorbState.ALL)
                stickerAdapters!!.adsorbVertical -> setAdsorbState(InfoStickerGestureView.AdsorbState.VERTICAL)
                stickerAdapters!!.adsorbHorizontal -> setAdsorbState(InfoStickerGestureView.AdsorbState.HORIZONTAL)
                else -> setAdsorbState(InfoStickerGestureView.AdsorbState.NONE)
            }

            stickerAdapters!!.x = max(
                    MIN_OFFSET, min(
                    stickerAdapters!!.x,
                    MAX_OFFSET
            )
            )
            stickerAdapters!!.y = max(
                    MIN_OFFSET, min(
                    stickerAdapters!!.y,
                    MAX_OFFSET
            )
            )

            stickerAdapters!!?.gestureViewModel?.changePosition(stickerAdapters!!.x, stickerAdapters!!.y)
            onInfoStickerDisPlayChangeListener?.onMoving(this,stickerAdapters!!.x,stickerAdapters!!.y)
            stickerAdapters!!.moveXDiff = (stickerAdapters!!.x * view.measuredWidth - width / 2F - left) / view.measuredWidth
            stickerAdapters!!.moveYDiff = (stickerAdapters!!.y * view.measuredHeight - height / 2F - top) / view.measuredHeight
            stickerAdapters!!.boundingBox.transform(stickerAdapters!!.moveXDiff, stickerAdapters!!.moveYDiff)
            setFramePosition(sticker.id.toString(), stickerAdapters!!.x, stickerAdapters!!.y)
            setTextItemRect(stickerAdapters!!.boundingBox.textBoxes)
        }

        return true
    }

    override fun onMoveEnd(videoEditorGestureLayout: VideoEditorGestureLayout, detector: MoveGestureDetector?) {
        super.onMoveEnd(videoEditorGestureLayout,detector)
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            stickerAdapters?.isOnMoving = false
            stickerAdapters!!.gestureViewModel?.onGestureEnd()
            setAdsorbState(InfoStickerGestureView.AdsorbState.NONE)
        }
    }

    override fun onUp(
        videoEditorGestureLayout: VideoEditorGestureLayout,
        event: MotionEvent
    ): Boolean {
        (videoEditorGestureLayout as InfoStickerGestureView).stickerAdapters?.isOnMoving = false
        return super.onUp(videoEditorGestureLayout, event)
    }

    override fun onScaleBegin(videoEditorGestureLayout: VideoEditorGestureLayout, scaleFactor: ScaleGestureDetector?): Boolean {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            return stickerAdapters!!?.isSelectedInTime && stickerAdapters!!.infoSticker != null
        }

    }

    override fun onScale(videoEditorGestureLayout: VideoEditorGestureLayout, scaleFactor: ScaleGestureDetector?): Boolean {
        val scaleDiff = scaleFactor?.scaleFactor ?: 1F
        return onScale(videoEditorGestureLayout,scaleDiff)
    }

    @SuppressLint("LongLogTag")
    fun onScale(videoEditorGestureLayout: VideoEditorGestureLayout, scaleDiff: Float): Boolean {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            stickerAdapters!!.apply {
                val sticker = infoSticker
                if (sticker == null || !isSelectedInTime) return false
                if (scaleDiff.isInfinite() || scaleDiff.isNaN()) {
                    val text = "scale is invalid:$scaleDiff"
                    Log.e(TAG, text)
                    return false
                }

                val nowScale = scale * scaleDiff
                if (sticker.isTextSticker()) {
                    // 最小缩放值检测
                    if (nowScale < MIN_TEXT_SCALE || nowScale > MAX_TEXT_SCALE) {
                        return true
                    }
                } else {
                    // 最小缩放值检测
                    if (nowScale < MIN_STICKER_SCALE || nowScale > MAX_STICKER_SCALE) {
                        return true
                    }
                }
                scale = nowScale
                gestureViewModel?.scale(scaleDiff)
                onInfoStickerDisPlayChangeListener?.onScaling(infoStickerEditorView,scaleDiff)
                boundingBox.scale(PointF(x, y), scaleDiff)
                infoStickerEditorView.setFrameSize(sticker, boundingBox.box)
                infoStickerEditorView.setFramePosition(sticker.id.toString(), x, y)
                infoStickerEditorView.setTextItemRect(boundingBox.textBoxes)
            }

        }

        return true
    }

    fun refreshLayout(infoStickerGestureView: InfoStickerGestureView) {
        infoStickerGestureView.apply {
            stickerAdapters!!.apply {
                val sticker = infoSticker
                if (sticker == null || !isSelectedInTime) return
                boundingBox = tryGetBoundingBox(sticker)
                val width = boundingBox.box.width * infoStickerGestureView.measuredWidth
                val height = boundingBox.box.height * infoStickerGestureView.measuredHeight
                val left = x * infoStickerGestureView.measuredWidth - width / 2
                val top = y * infoStickerGestureView.measuredHeight - height / 2
                val rect = RectF(left, top, left + width, top + height)
                rotateRect(rect, rect.centerX(), rect.centerY(), rotation.toFloat())

                val dx = 0F
                val dy = 0F

                deltaDx += dx
                deltaDy += dy

                x = max(
                        MIN_OFFSET, min(
                        x,
                        MAX_OFFSET
                )
                )
                y = max(
                        MIN_OFFSET, min(
                        y,
                        MAX_OFFSET
                )
                )

                gestureViewModel?.changePosition(x,y)

                infoStickerGestureView.setFrameSize(sticker, boundingBox.box)
                infoStickerGestureView.setFramePosition(sticker.id.toString(), x, y)
                infoStickerGestureView.setTextItemRect(boundingBox.textBoxes)
            }
        }

    }

    override fun onRotationBegin(videoEditorGestureLayout: VideoEditorGestureLayout, detector: RotateGestureDetector?): Boolean {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            val sticker = stickerAdapters!!.infoSticker
            return if (sticker != null && stickerAdapters!!.isSelectedInTime) {
                if (stickerAdapters!!.initRotation % 90 == 0) {
                    stickerAdapters!!.adsorbedDegree = stickerAdapters!!.initRotation
                }
                true
            } else false
        }

    }

    override fun onRotation(videoEditorGestureLayout: VideoEditorGestureLayout, radian: Float): Boolean {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            stickerAdapters?.apply {
                val sticker = infoSticker
                if (sticker == null || !isSelectedInTime) return super.onRotation(videoEditorGestureLayout,radian)
                var rotate = Math.toDegrees(radian.toDouble())
                while (rotate > 180) rotate = 360 - rotate
                while (rotate < -180) rotate += 360
                currDegree -= rotate.toFloat()
                val tmpRotation = initRotation + currDegree.toInt()
                if (rotation == tmpRotation) return true

                val adsorbOffset = tmpRotation % 90
                val toAdsorbRotation: Int = when {
                    adsorbOffset == 0 -> tmpRotation
                    abs(adsorbOffset) < 10 -> {
                        tmpRotation - adsorbOffset
                    }
                    abs(adsorbOffset) > 80/*90 - 10*/ -> {
                        tmpRotation + ((if (adsorbOffset < 0) -90 else 90) - adsorbOffset)
                    }
                    else -> -1
                }

                if (adsorbing) {
                    // If moving out of adsorb area or moving to another adsorb side,
                    // then reset the adsorb state and rotate to the exact rotation.
                    if (toAdsorbRotation == -1 ||
                            (adsorbSide == 0 && toAdsorbRotation < tmpRotation) ||
                            (adsorbSide == 1 && toAdsorbRotation > tmpRotation)
                    ) {
                        rotation = tmpRotation
                        adsorbing = false
                    } else {
                        // If adsorb state not changed, then return immediately
                        return true
                    }
                } else { // Not performing adsorbing
                    when {
                        toAdsorbRotation == -1 -> { // Move out of adsorb area
                            rotation = tmpRotation
                            adsorbedDegree = -1
                        }
                        toAdsorbRotation != adsorbedDegree -> { // Move out of the adsorbed area
                            rotation = toAdsorbRotation
                            adsorbedDegree = toAdsorbRotation
                            adsorbSide = if (toAdsorbRotation > tmpRotation) 0 else 1
                            adsorbing = true
                        }
                        else -> { // Still in adsorbed area
                            rotation = tmpRotation
                        }
                    }
                }
                val targetRotation = rotation % 360F
                gestureViewModel?.rotate(targetRotation)
                onInfoStickerDisPlayChangeListener?.onRotating(infoStickerEditorView,targetRotation)
                infoStickerEditorView.setFrameRotate(sticker.id.toString(), rotation.toFloat())
            }
        }

        return true
    }

    override fun onRotationEnd(videoEditorGestureLayout: VideoEditorGestureLayout, angle: Float): Boolean {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            stickerAdapters?.apply {
                gestureViewModel?.onScaleRotateEnd()

            }
        }
        return super.onRotationEnd(videoEditorGestureLayout,angle)
    }

    override fun onSingleTapConfirmed(videoEditorGestureLayout: VideoEditorGestureLayout, e: MotionEvent?): Boolean {
        e ?: return super.onSingleTapConfirmed(videoEditorGestureLayout,e)
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            stickerAdapters?.apply {
                val stickers = getStickers() ?: return false
                val sticker = findTouchItem(videoEditorGestureLayout,stickers, e.x, e.y)
                if (sticker == null) {
                   setSelected(null, true)
                } else {
//                    val vmAdapter = viewModelAdapter ?: return true
//            if (sticker.type == DraftTypeUtils.SegmentType.TYPE_TEXT_TEMPLATE) {
//                showEditPanelIfTouchText(vmAdapter, sticker, e)
//            }
                    setSelected(sticker.id.toString(), true)
                }
                stickerUIViewModel.nleEditorContext.closePanelEvent.value = ClosePanelEvent()//切换时关闭底部功能面板
            }
        }

        return true
    }

//    private fun showEditPanelIfTouchText(
//            vmAdapterI: IStickerGestureViewModelAdapter,
//            sticker: InfoSticker,
//            e: MotionEvent
//    ) {
//        val param = vmAdapter.getTextTemplateParam(sticker.id)
//        var textIndex = -1
//        val textsBounds = param?.textsBounds() ?: emptyList()
//        if (param != null && textsBounds.size == param.texts.size) {
//            kotlin.run {
//                textsBounds.forEachIndexed { index, rectF ->
//                    val box = getTextBoxRect(sticker, rectF)
//                    if (detectInItemContent(sticker, box, e.x, e.y)) {
//                        textIndex = index
//                        return@run
//                    }
//                }
//            }
//        }
//        if (param != null && textIndex != -1) {
//            vmAdapter.showTextTemplateEditPanel(sticker.id, textIndex)
//        }
//    }

//    private fun getTextBoxRect(sticker: InfoSticker, rect: RectF): RectF {
//        val w = view.measuredWidth
//        val h = view.measuredHeight
//        val bounds = rect.run { RectF(left * w, top * h, right * w, bottom * h) }
//        val rotation = sticker.clipInfo?.rotation?.toFloat() ?: 0F
//        rotateRect(bounds, bounds.centerX(), bounds.centerY(), -rotation)
//        return bounds
//    }

    fun onEditSticker(videoEditorGestureLayout: VideoEditorGestureLayout) {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            stickerAdapters?.apply {
                showEditPanel(infoSticker)
            }
        }
    }

    fun onDeleteSticker(videoEditorGestureLayout: VideoEditorGestureLayout) {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            stickerAdapters?.apply {
                gestureViewModel.remove()
                stickerUIViewModel.textOperation.value = TextOperationEvent(TextOperationType.DELETE)
                stickerUIViewModel.infoStickerOperation.value = InfoStickerOperationEvent(InfoStickerOperationType.DELETE)
            }
        }
    }

    fun onCopySticker(videoEditorGestureLayout: VideoEditorGestureLayout) {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            stickerAdapters?.apply {
                gestureViewModel.copy()
            }
        }
    }

//    @SuppressLint("LongLogTag")
    fun onScaleRotateSticker(videoEditorGestureLayout: VideoEditorGestureLayout, scale: Float, rotate: Float) {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            stickerAdapters?.apply {
                val sticker = infoSticker ?: return

                val width = boundingBox.box.width * scale
                val height = boundingBox.box.height * scale
                if (width.isFinite() && height.isFinite()) {
                    boundingBox.scale(PointF(x, y), scale)
                    this.scale *= scale
//            if (sticker.hasKeyframes) {
//                this.rotation = (this.rotation + rotate.toInt())
//            } else {

                    rotation = (rotation + rotate.toInt()) % 360
//            }
                    gestureViewModel?.scaleRotate(scale, -rotation.toFloat())//nle为逆时针
//            viewModelAdapter?.stickerUIViewModel?.textOperation?.value =
//                TextOperationEvent(TextOperationType.SCALE_ROTATE)
                    infoStickerEditorView.setFrameSize(sticker, boundingBox.box)
                    val segmentId = sticker.id.toString()
                    infoStickerEditorView.setFrameRotate(segmentId, rotation.toFloat())
                    infoStickerEditorView.setFramePosition(segmentId, x, y)
                    infoStickerEditorView.setTextItemRect(boundingBox.textBoxes)
                } else {
//                    Log.e(TAG, "scale error")
                }
            }
        }

    }

    fun onStickerRotateEnd(videoEditorGestureLayout: VideoEditorGestureLayout) {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            stickerAdapters?.gestureViewModel?.onScaleRotateEnd()
        }
    }

    fun onFlipSticker(videoEditorGestureLayout: VideoEditorGestureLayout) {
        var infoStickerEditorView = videoEditorGestureLayout as InfoStickerGestureView
        infoStickerEditorView.apply {
            stickerAdapters?.gestureViewModel?.flip()
        }
    }

    companion object {
        private const val TAG = "InfoStickerGestureListener"

        private const val MAX_OFFSET = 0.98F
        private const val MIN_OFFSET = 0.02F

        private const val MIN_TEXT_SCALE: Float = 0F
        private const val MAX_TEXT_SCALE: Float = 200.0F

        private const val MIN_STICKER_SCALE: Float = 0F
        private const val MAX_STICKER_SCALE: Float = 200.0F
    }

}
