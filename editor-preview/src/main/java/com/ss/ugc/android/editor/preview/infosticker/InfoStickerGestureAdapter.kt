package com.ss.ugc.android.editor.preview.infosticker

import android.graphics.PointF
import android.graphics.RectF
import android.util.SizeF
import androidx.lifecycle.Observer
import com.bytedance.ies.nle.editor_jni.NLESegmentTextTemplate
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.bytedance.ies.nlemediajava.TextTemplate
import com.ss.ugc.android.editor.base.data.TextInfo
import com.ss.ugc.android.editor.base.data.TextPanelTab
import com.ss.ugc.android.editor.base.event.*
import com.ss.ugc.android.editor.base.listener.GestureObserver
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.viewmodel.PreviewStickerViewModel
import com.ss.ugc.android.editor.base.viewmodel.StickerGestureViewModel
import com.ss.ugc.android.editor.base.viewmodel.adapter.IStickerGestureViewModelAdapter
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.core.SegmentType
import com.ss.ugc.android.editor.core.event.EmptyEvent
import com.ss.ugc.android.editor.core.event.ShowTextPanelEvent
import com.ss.ugc.android.editor.core.getSegmentType
import com.ss.ugc.android.editor.core.previewIconPath

/**
 * time : 2020/12/29
 *
 * description :
 * 1. 订阅贴纸文字相关数据,更新主页面预览区的UI
 * 2. 操作预览区UI，更新贴纸文字
 *
 */
class InfoStickerGestureAdapter(
        private val previewStickerViewModel: PreviewStickerViewModel
) : IStickerGestureViewModelAdapter {

    override val gestureViewModel: StickerGestureViewModel = previewStickerViewModel.gestureViewModel
    override val stickerUIViewModel: StickerUIViewModel = previewStickerViewModel.stickerUIViewModel

    private val owner = previewStickerViewModel.activity

    var  infoStickerGestureView: InfoStickerGestureView? = null
    var infoSticker: NLETrackSlot? = null
    var isSelectedInTime = false
    var isOnMoving: Boolean = false

    var x = 0F
    var y = 0F
    var deltaDx = 0F
    var deltaDy = 0F
    var adsorbHorizontal = false
    var adsorbVertical = false

    var scale = 1F
    var boundingBox: ItemBox = ItemBox(SizeF(0F, 0F))

    /**
     * -1 is not in adsorb state
     * 0 is adsorb larger angle
     * 1 is adsorb smaller angle
     */
    var adsorbSide = -1
    var adsorbing = false
    var adsorbedDegree = -1
    var currDegree = 0F
    var initRotation = 0
    var rotation = 0

//    private var infoSticker: NLETrackSlot? = null

    /**
     * 拖动贴纸时的拖动量 [0~1]
     *
     */
    var moveXDiff: Float = 0F
    var moveYDiff: Float = 0F

    //用这个记录playPositionObserver传过来的值，当关键帧更新时需要用到，否则会出现时间不一致
    private var currentTime = 0L

    fun bindView(infoStickerGestureView: InfoStickerGestureView){
        this.infoStickerGestureView = infoStickerGestureView
        onStop()
        onStart()
    }
    private val segmentObserver: Observer<SegmentState> by lazy {
        Observer<SegmentState> {
            it?.apply {
                observer.onSelectedChange(segment.infoSticker)
                observer.checkFlipButtonVisibility(segment.textInfo)
                if (newAdd) {
                    stickerUIViewModel.animSelectedFrame.value = EmptyEvent()
                }
            }
        }
    }


    private val textBoundUpdateObserver: Observer<EmptyEvent> by lazy {
        Observer<EmptyEvent> {
            it?.apply {
//                val sticker = gestureViewModel.segmentState.value?.segment?.infoSticker
//                observer.onSelectedChange(sticker)
            }
        }
    }

    private val playPositionObserver: Observer<Long> by lazy {
        Observer<Long> {
            it?.apply {
                // Whatever there is any segment selected, remove all place holders once the
                // play position changed.
                currentTime = it
                observer.removeAllPlaceholders()
                observer.updateFrame(it)
            }
        }
    }

    private val frameObserver: Observer<Long> by lazy {
        Observer<Long> {
            it?.apply {
                //更新选中的占位图
                observer.updateFrame(it, true)
            }
        }
    }

    private val cancelStickerPlaceholderObserver: Observer<CancelStickerPlaceholderEvent> by lazy {
        Observer<CancelStickerPlaceholderEvent> {
            it?.apply {
                observer.removePlaceholder(segmentId)
            }
        }
    }
    private val animFrameObserver: Observer<EmptyEvent> by lazy {
        Observer<EmptyEvent> {
            observer.animateStickerIn()
        }
    }
    private val textPanelTabObserver: Observer<TextPanelTabEvent> by lazy {
        Observer<TextPanelTabEvent> {
            observer.onTextPanelTabChange(it?.tab, gestureViewModel.segmentState.value?.segment?.textInfo)
        }
    }


    override fun onStart() {
        gestureViewModel.keyframeUpdate.observe(owner,frameObserver)
        gestureViewModel.segmentState.observe(owner, segmentObserver)
        gestureViewModel.playPosition.observe(owner, playPositionObserver)
        gestureViewModel.selectedViewFrame.observe(owner, frameObserver)
        gestureViewModel.textBoundUpdate.observe(owner, textBoundUpdateObserver)
        stickerUIViewModel.animSelectedFrame.observe(owner, animFrameObserver)
        stickerUIViewModel.cancelStickerPlaceholderEvent.observe(owner, cancelStickerPlaceholderObserver)
        stickerUIViewModel.textPanelTab.observe(owner, textPanelTabObserver)
    }

    override fun onStop() {
        gestureViewModel.keyframeUpdate.removeObserver(frameObserver)
        gestureViewModel.segmentState.removeObserver(segmentObserver)
        gestureViewModel.playPosition.removeObserver(playPositionObserver)
        gestureViewModel.selectedViewFrame.removeObserver(frameObserver)
        gestureViewModel.textBoundUpdate.removeObserver(textBoundUpdateObserver)
        stickerUIViewModel.animSelectedFrame.removeObserver(animFrameObserver)
        stickerUIViewModel.cancelStickerPlaceholderEvent.removeObserver(cancelStickerPlaceholderObserver)
        stickerUIViewModel.textPanelTab.removeObserver(textPanelTabObserver)

    }

    override fun getStickers(): List<NLETrackSlot> {
        return gestureViewModel.getStickerSegments().mapNotNull {
            it.infoSticker
        }
    }

    override fun getBoundingBox(id: String): SizeF? {
        return gestureViewModel.getBoundingBox(id)
    }

    override fun getTextTemplateParam(id: String): TextTemplate? {
        return gestureViewModel.getTextTemplateInfo(id)
    }

    override fun getPlayPosition(): Long {
        return gestureViewModel.playPosition.value ?: 0L
    }

    override fun showTextPanel() {
        stickerUIViewModel.showTextPanelEvent.value =
            ShowTextPanelEvent(gestureViewModel.getCoverMode())
    }

    override fun showEditPanel(slot: NLETrackSlot?) {
        slot ?: return
        when (slot.getSegmentType()) {
            SegmentType.TEXT_STICKER ->
                stickerUIViewModel.showTextPanelEvent.value =
                    ShowTextPanelEvent(gestureViewModel.getCoverMode())
            else ->
                stickerUIViewModel.showStickerAnimPanelEvent.value = ShowStickerAnimPanelEvent()
        }
    }

    override fun showTextTemplateEditPanel(id: String, textIndex: Int) {
        stickerUIViewModel.textTemplatePanelTab.value = TextTemplatePanelTabEvent(index = textIndex, edit = true)
    }

    override fun setSelected(id: String?, byClick: Boolean) {
        val preSegment = gestureViewModel.segmentState.value?.segment
        infoStickerGestureView?.apply {
            onInfoStickerDisPlayChangeListener?.onSlotSelect(this,preSegment)
        }
        if (byClick && preSegment != null) {
            val isBlank = preSegment.textInfo?.text?.isBlank() == true
            val isTextSticker = preSegment.type
            if (isTextSticker && isBlank) {
                deleteTextStickOnChangeFocus(preSegment.id)
            }
        }
        stickerUIViewModel.selectStickerEvent.value = gestureViewModel.getSelectInfoSticker(id)
    }

    private fun deleteTextStickOnChangeFocus(id: String) {
        gestureViewModel.deleteTextStickOnChangeFocus(id)
        stickerUIViewModel.textOperation.value = TextOperationEvent(TextOperationType.DELETE)
    }

    override fun canDeselect(): Boolean {
        //文字贴纸面板都不显示
        val isTextPanelShowing = gestureViewModel.textPanelVisibility.value == true
        val isStickerPanelShowing = gestureViewModel.stickerPanelVisibility.value == true
        return !isTextPanelShowing && !isStickerPanelShowing
    }

    /**
     * Just for interface isolation
     */
    val observer: GestureObserver by lazy {
        object : GestureObserver {
            override fun onSelectedChange(sticker: NLETrackSlot?) {
                if (isOnMoving) {
                    return//正在移动时不刷新，修复：添加文本轨道，添加关键帧1移动文本，光标往后移再移动文本自动添加关键帧2，光标移至关键帧2偏左位置，再次移动文本，不跟手
                }
                onInfoSkickerSelectedChange(infoStickerGestureView,sticker)
            }

            override fun updateFrame(time: Long, force: Boolean) {
                if (isOnMoving) {
                    return//正在移动时不刷新，修复：添加文本轨道，添加关键帧1移动文本，光标往后移再移动文本自动添加关键帧2，光标移至关键帧2偏左位置，再次移动文本，不跟手
                }
                val sticker = infoSticker ?: return
                val inTime = isPositionInSticker(currentTime, sticker)
                //这个scale的计算方式比较奇怪，可以参考InfoStickerGestureListener.scale方法
                val boxScale = sticker.scale / scale
                if (force) {
                    val position = transformPosition(sticker)
                    x = position.x
                    y = position.y
                    scale = sticker.scale
                    rotation = -sticker.rotation.toInt()//NLE为逆时针
                    initRotation = rotation
                    boundingBox = tryGetBoundingBox(sticker)
                }else{
                    if (isSelectedInTime == inTime) return
                }

                isSelectedInTime = inTime
                if (inTime) {
                    infoStickerGestureView?.showFrame()
                    val segmentId = sticker.id.toString()
                    if (boundingBox.textBoxes.isNotEmpty()) {
                        boundingBox.scale(PointF(x, y), boxScale)
                        infoStickerGestureView?.setTextItemRect(boundingBox.textBoxes)
                    }
                    infoStickerGestureView?.setFrameSize(sticker, boundingBox.box)
                    infoStickerGestureView?.setFramePosition(segmentId, x, y)
                    infoStickerGestureView?.setFrameRotate(segmentId, rotation.toFloat())
                } else {
                    infoStickerGestureView?.dismissFrame()
                }
            }

            override fun animateStickerIn() {
                val sticker = infoSticker ?: return
                if (isSelectedInTime) {
                    val position = transformPosition(sticker)
                    val segmentId = sticker.id.toString()
                    infoStickerGestureView?.animateIn(
                            segmentId,
                            position,
                            sticker.previewIconPath()
                    )
                    infoStickerGestureView?.setFrameRotate(segmentId, -sticker.rotation)//nle为逆时针
                }
            }

            override fun onTextPanelTabChange(tab: TextPanelTab?, textInfo: TextInfo?) {
                infoStickerGestureView?.setTextPanelTab(tab, textInfo)
            }

            override fun onTextTemplatePanelVisibilityChange(
                    switchTemplate: Boolean,
                    updatingText: Boolean
            ) {
                infoStickerGestureView?.setTextTemplateAction(switchTemplate, updatingText)
            }

            override fun removePlaceholder(id: String) {
                infoStickerGestureView?.removePlaceholder(id)
            }

            override fun removeAllPlaceholders() {
                infoStickerGestureView?.removeAllPlaceholders()
            }

            override fun checkFlipButtonVisibility(textInfo: TextInfo?) {
                infoStickerGestureView?.checkFlipButtonVisibility(textInfo)
            }
        }
    }

    private fun onInfoSkickerSelectedChange(infoStickerGestureView: InfoStickerGestureView?, sticker: NLETrackSlot?) {
        this.infoSticker = sticker
        if (sticker == null) {
            x = 0.5F
            y = 0.5F
            scale = 1F
            boundingBox = ItemBox(SizeF(0F, 0F))
            rotation = 0

            infoStickerGestureView?.dismissFrame()
        } else {
            val position = transformPosition(sticker)
            x = position.x
            y = position.y
            scale = sticker.scale
            rotation = -sticker.rotation.toInt()//nle为逆时针
            initRotation = rotation
            boundingBox = tryGetBoundingBox(sticker)
            val playPosition = getPlayPosition() ?: 0L
            isSelectedInTime = isPositionInSticker(playPosition, sticker)

            if (isSelectedInTime) {
                infoStickerGestureView?.showFrame()
                infoStickerGestureView?.setFrameSize(sticker, boundingBox.box)
                infoStickerGestureView?.setFramePosition(sticker.id.toString(), x, y)
                infoStickerGestureView?.setFrameRotate(sticker.id.toString(), rotation.toFloat())
                infoStickerGestureView?.setTextItemRect(boundingBox.textBoxes)
                infoStickerGestureView?.showCopyButton(ThemeStore.previewUIConfig.stickerEditViewConfig.editIconConfig.enable)
            } else {
                infoStickerGestureView?.dismissFrame()
            }
        }
    }

    /**
     * 在判断当前时间点是否在sticker时间范围时，让start time -1000（1毫秒）扩大范围，
     * 否则会出现在添加新sticker时因为误差导致sticker框不显示的问题
     */
    private fun isPositionInSticker(position: Long, sticker: NLETrackSlot) =
        position in (sticker.startTime - 1000)..sticker.endTime

    /**
     * transform [-1, 1] to [0, 1]
     */
    fun transformPosition(slot: NLETrackSlot): PointF {
        val x = (slot.transformX + 1) / 2F
        val y = -(slot.transformY - 1) / 2F
        return PointF(x, y)
    }

    fun tryGetBoundingBox(sticker: NLETrackSlot): ItemBox {
        return getStickerBoundingBox(sticker)
    }

    fun getStickerBoundingBox(sticker: NLETrackSlot): ItemBox {
        return if (isTextTemplate(sticker)) {
            val templateParam = getTextTemplateParam(sticker.id.toString())?: TextTemplate(
                emptyList())
            ItemBox(
                templateParam.boundingBox() ?: SizeF(0F, 0F),
                templateParam.textsBounds()
            )
        } else {
            val box = getBoundingBox(sticker.id.toString()) ?: SizeF(0F, 0F)
            ItemBox(box, emptyList())
        }
    }

    private fun isTextTemplate(sticker: NLETrackSlot): Boolean{
        return NLESegmentTextTemplate.dynamicCast(sticker.mainSegment) != null
    }


    class ItemBox(var box: SizeF, val textBoxes: List<RectF> = emptyList()) {
        fun scale(center: PointF, scale: Float) {
            box = SizeF(box.width * scale, box.height * scale)
            // 通过计算原矩形中心缩放后的位置再算出矩形缩放后的位置
            textBoxes.forEach {
                // 缩放后的矩形中心再 X,Y 方向上的变量
                val yDiff = (it.centerY() - center.y) * (scale - 1)
                val xDiff = (it.centerX() - center.x) * (scale - 1)
                // 缩放后的矩形中心
                val cy = it.centerY() + yDiff
                val cx = it.centerX() + xDiff
                // 缩放后矩形宽高的变量
                val wDiff = it.width() * (scale - 1)
                val hDiff = it.height() * (scale - 1)
                // 缩放后的矩形 Left,Top
                val l = cx - (it.width() + wDiff) / 2
                val t = cy - (it.height() + hDiff) / 2
                it.set(l, t, cx + (it.width() + wDiff) / 2, cy + (it.height() + hDiff) / 2)
            }
        }

        fun transform(xDiff: Float, yDiff: Float) {
            textBoxes.forEach {
                it.offset(xDiff, yDiff)
            }
        }
    }


}