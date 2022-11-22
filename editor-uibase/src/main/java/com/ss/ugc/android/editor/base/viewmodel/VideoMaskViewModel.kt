package com.ss.ugc.android.editor.base.viewmodel

import android.text.TextUtils
import android.util.SizeF
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nlemediajava.utils.millisToMicros
import com.caverock.bytedancesvg.SVG
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.resource.ResourceHelper
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.core.api.canvas.ORIGINAL
import com.ss.ugc.android.editor.core.api.video.MaskParam
import com.ss.ugc.android.editor.core.commitDone
import com.ss.ugc.android.editor.core.event.PanelEvent
import com.ss.ugc.android.editor.core.event.PanelEvent.Panel.VIDEO_MASK
import com.ss.ugc.android.editor.core.event.PanelEvent.State.CLOSE
import com.ss.ugc.android.editor.core.event.PanelEvent.State.OPEN
import com.ss.ugc.android.editor.core.event.SelectSlotEvent
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

@Keep
class VideoMaskViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    val nleModel = nleEditorContext.nleModel

    companion object {
        private const val INDEX_VIDEO_MASK_START = 12000
        const val GEOMETRIC_SHAPE = "geometric_shape"
    }

    val playPositionState = nleEditorContext.videoPositionEvent

    val maskSegmentState = nleEditorContext.slotChangeChangeEvent

    val keyframeUpdateState: LiveData<Long> =
        Transformations.map(nleEditorContext.keyframeUpdateEvent) {
            it.time.millisToMicros()
        }

    var isChange:AtomicBoolean = AtomicBoolean(false)

    //记录蒙版切换为五角星或心型前的数据
    var lastMaskType = ""
    var lastMaskWidth: Float = 0F
    var lastMaskHeight: Float = 0F
    var lastBaseMaskWidth: Float = 0F
    var lastBaseMaskHeight: Float = 0F

    fun show() {
        nleEditorContext.panelEvent.postValue(
            PanelEvent(
                VIDEO_MASK,
                OPEN
            )
        )
    }

    fun hide() {
        nleEditorContext.panelEvent.postValue(
            PanelEvent(
                VIDEO_MASK,
                CLOSE
            )
        )
    }

    fun getSelectedSlot(): NLETrackSlot? {
        return nleEditorContext.selectedNleTrackSlot
    }

    /**
     * 当前播放进度
     */
    fun curPos():Int {
        return nleEditorContext.videoPlayer.curPosition()
    }

    fun pausePlay() {
        return nleEditorContext.videoPlayer.pause()
    }

    private fun getVideoSizeEliminateRotate(
        rotation: Float,
        height: Long,
        width: Long
    ): SizeF {
        val videoWidth: Float
        val videoHeight: Float
//        if (rotation == 90F || rotation == 270F) {
//            videoWidth = height.toFloat()
//            videoHeight = width.toFloat()
//        } else {
        videoWidth = width.toFloat()
        videoHeight = height.toFloat()
//        }
        return SizeF(videoWidth, videoHeight)
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

    private fun getCroppedSize(videoSize: SizeF, nleTrackSlot: NLETrackSlot): SizeF {

        val croppedWidthScale = getCropWidthScale(nleTrackSlot)
        val videoWidth = if (croppedWidthScale == 0F) {
            1F
        } else {
            videoSize.width * croppedWidthScale
        }
        val croppedHeightScale = getCropHeightScale(nleTrackSlot)
        val videoHeight = if (croppedHeightScale == 0F) {
            1F
        } else {
            videoSize.height * croppedHeightScale
        }
        if (videoWidth <= 0 || videoHeight <= 0) {
            return SizeF(1.0F, 1.0F)
        }
        return SizeF(videoWidth, videoHeight)
    }

    private fun getNewMaskSize(slot: NLETrackSlot, force: Boolean = false): SizeF {
        val maskInfo = slot.masks?.firstOrNull()
        if (maskInfo != null && !force) {
            return SizeF(maskInfo.segment.height, maskInfo.segment.height)
        }
        nleModel.tracks
        val maskSizeF: SizeF? = NLESegmentVideo.dynamicCast(slot.mainSegment).let {
            val width = it.resource.width
            val height = it.resource.height

            val rotation = -slot.rotation

            getVideoSizeEliminateRotate(rotation = rotation, height = height, width = width)


        }.let {
            getCroppedSize(it, slot)
        }.let {
            val w: Float
            val h: Float

            // 默认素材短边的1/2
            if (it.width < it.height) {
                w = 0.5F
                h = w * it.width / it.height
            } else {
                h = 0.5F
                w = h * it.height / it.width
            }
            SizeF(w, h)
        }

        return maskSizeF ?: SizeF(1F, 1F)


    }

    private fun hookVeBug() {
        // fix bug 目前VE引擎到最后一帧 蒙版会失效 需要手动
        nleEditorContext?.videoPlayer?.apply {
            if (this.curPosition() == this.totalDuration()) {
                this.seekToPosition(this.curPosition() - 50)
            }
        }
    }

    fun recheckMaskSize(slot: NLETrackSlot) {
        //替换视频时，重新计算svg类型的mask宽高，避免蒙版变形
        slot.masks?.firstOrNull()?.segment?.let { oriSegmentMask ->
            val size = getNewMaskSize(slot, true)
            oriSegmentMask.apply {
                this.width = size.width
                this.height = size.height
//                        this.centerX = 0f
//                        this.centerY = 0f
                if (oriSegmentMask.maskType == GEOMETRIC_SHAPE) {//仅svg类型
                    checkResize(slot, this)
                }
            }
            maskSegmentState.value = SelectSlotEvent(slot)
        }
    }

    fun updateMask(res: ResourceItem, isDone:Boolean = true) {
        var slot = getSelectedSlot()

        var segmentMask: NLESegmentMask? = null

        slot?.let { it ->
            val oriSegmentMask = it.masks?.firstOrNull()?.segment

            it.masks?.forEach { mask ->
                it.clearMask()
            }
//            if (!TextUtils.isEmpty(res.mask)) {
            val size = getNewMaskSize(slot)
            segmentMask = oriSegmentMask?.apply {
                if (TextUtils.isEmpty(res.path).not()) {
                    this.maskType = res.mask
                }
//                    this.width = size.width
//                    this.height = size.height
                effectSDKMask = NLEResourceNode().apply {
                    this.resourceType = NLEResType.MASK
                    this.resourceId = res.resourceId
                    this.resourceFile = res.path ?: "" }
            } ?: NLESegmentMask().apply {
                this.width = size.width
                this.height = size.height
                this.centerX = 0f
                this.centerY = 0f
                if (res.path.isNotEmpty()) {
                    this.maskType = res.mask
                }
                this.invert = oriSegmentMask?.invert ?: false

                effectSDKMask = NLEResourceNode().apply {
                    this.resourceType = NLEResType.MASK
                    this.resourceFile = res.path ?: ""
                    this.resourceId = res.resourceId
                }
            }

            //上一次的类型不是心型或五角星 记录切换前的宽高
            if (lastMaskType != GEOMETRIC_SHAPE && lastMaskType.isNotEmpty()) {
                lastMaskWidth = segmentMask!!.width
                lastMaskHeight = segmentMask!!.height
            }

            //上一次的类型是心型或五角星 当前类型不是心型或五角星
            //还原之前的宽高(心型或五角星因为特殊强制了1:1比例,切回其他类型时需要还原)
            if (lastMaskType == GEOMETRIC_SHAPE && res.mask != GEOMETRIC_SHAPE
                && res.path.isNotEmpty()) {
                NLESegmentVideo.dynamicCast(it.mainSegment)?.apply {
                    //心型或五角星切回后 蒙版要等比例放大或缩小
                    val ratio: Float
                    if (lastMaskWidth > lastMaskHeight) {
                        ratio = segmentMask!!.height.div(lastBaseMaskHeight)
                    } else {
                        ratio = segmentMask!!.width.div(lastBaseMaskWidth)
                    }
                    segmentMask?.width = lastMaskWidth.times(ratio)
                    segmentMask?.height = lastMaskHeight.times(ratio)
                }
            }

            if (res.mask == GEOMETRIC_SHAPE) {
                checkResize(it, segmentMask)
            }

            //上一次的类型不是心型或五角星 记录切换后的心型或五角星基础宽高
            if (lastMaskType != GEOMETRIC_SHAPE && lastMaskType.isNotEmpty()) {
                lastBaseMaskWidth = segmentMask!!.width
                lastBaseMaskHeight = segmentMask!!.height
            }

            lastMaskType = res.mask ?: ""

            NLEMask().apply {
                this.segment = segmentMask
                it.addMask(this)
                // 每段视频只有一个蒙版, 固定值12000
                this.transformZ = INDEX_VIDEO_MASK_START
            }

            nleEditorContext.nleEditor.commitDone(isDone)
            maskSegmentState.value = SelectSlotEvent(it)
        }
    }

    private fun checkResize(
        it: NLETrackSlot,
        segmentMask: NLESegmentMask?
    ) {
        NLESegmentVideo.dynamicCast(it.mainSegment)?.apply {
            val videoSize = getVideoSizeEliminateRotate(
                -it.rotation,
                this.avFile.height,
                this.avFile.width

            )
            val videoSizeF: SizeF = getCroppedSize(videoSize,it)

            // 对于svg类型的蒙板，传给effect的高度需要是1：1的填充
            val maskWidth = segmentMask!!.width * videoSizeF.width
            val maskHeight = segmentMask!!.height * videoSizeF.height

            val newSize = limitMaxSize(getAspectRatio(segmentMask.effectSDKMask.resourceFile), SizeF(maskWidth, maskHeight))
            val maxValue = max(newSize.width, newSize.height)
            val newWidth = maxValue / videoSizeF.width
            val newHeight = maxValue / videoSizeF.height
            segmentMask?.width = newWidth
            segmentMask?.height = newHeight
        }

    }

    private fun getAspectRatio(filePath: String): Float {
        var ratio = 1F
        runCatching {
            val svgFilePath = "$filePath/material.svg"
            val svgFile = File(svgFilePath)
            if (svgFile.exists().not()) {
                return@runCatching
            }
            val fis = FileInputStream(svgFile)
            val bis = BufferedInputStream(fis)
            val svg = SVG.getFromInputStream(bis)
            ratio = svg?.documentAspectRatio ?: 1F
            fis.close()
            bis.close()
        }.onFailure {
        }
        return ratio
    }

    private fun limitMaxSize(aspectRatio: Float, target: SizeF): SizeF {
        return if (target.width / target.height > aspectRatio) {
            val width = target.height * aspectRatio
            SizeF(width, target.height)
        } else {
            val height = target.width / aspectRatio
            SizeF(target.width, height)
        }
    }


    fun onGestureEnd() {
        if (isChange.get()) {
            if(nleEditorContext.keyframeEditor.hasKeyframe()){
                nleEditorContext.done(nleEditorContext.getString(R.string.ck_keyframe))
            }else {
                nleEditorContext.done()
            }
        }
        isChange.compareAndSet(false, true)
    }

    fun updateCenter(maskCenterX: Float, maskCenterY: Float) {
        updateMaskPararm(maskCenterX= maskCenterX, maskCenterY =  maskCenterY,isDone = false)
        isChange.compareAndSet(false, true)
    }

    fun updateFeather(maskFeather: Float, isDone:Boolean = false) {
        updateMaskPararm(maskFeather = maskFeather, isDone =  isDone)
        isChange.compareAndSet(false, true)
    }

    fun updateSize(maskWidth: Float, maskHeight: Float) {
        updateMaskPararm(maskWidth = maskWidth, maskHeight= maskHeight, isDone =  false)
        isChange.compareAndSet(false, true)
    }

    fun updateRotation(maskRotate: Float) {
        updateMaskPararm(maskRotate = maskRotate, isDone =  false)
        isChange.compareAndSet(false, true)
    }

    fun updateCorner(maskRoundCorner: Float) {
        updateMaskPararm(maskRoundCorner = maskRoundCorner, isDone = false)
        isChange.compareAndSet(false, true)
    }

    fun updateInvert(invert: Boolean) {
        updateMaskPararm(invert = invert)
        isChange.compareAndSet(false, true)
    }

    fun updateMaskPararm(maskWidth: Float? = null, maskHeight: Float? = null,
                         maskCenterX: Float? = null, maskCenterY: Float? = null,
                         maskRotate: Float? = null, maskRoundCorner: Float? = null,
                         invert: Boolean? = null, maskFeather: Float? = null,
                         isDone: Boolean? = true) {
        hookVeBug()
        nleEditorContext.videoEditor.mask(
            MaskParam(maskWidth = maskWidth, maskHeight = maskHeight,
                maskCenterX = maskCenterX, maskCenterY = maskCenterY, maskRotate = maskRotate,
                invert = invert, maskRoundCorner = maskRoundCorner, maskFeather = maskFeather,
                isDone = isDone)
        )

    }

    /**
     * 获取当前蒙版在蒙版列表的index
     *
     * @return
     */
    fun getCurrentMaskIndex(): Int {
        var maskIndex = 0
        nleEditorContext.selectedNleTrackSlot?.masks?.firstOrNull()?.apply {
            ResourceHelper.getInstance().videoMaskList.forEachIndexed { index, resourceItem ->
                if ( resourceItem.path == this.segment.resource.resourceFile) {
                    return index
                }
            }
        }
        return maskIndex
    }

    fun getFetchResIndex( resList: List<ResourceItem>) :Int {
        var maskIndex = 0
        nleEditorContext.selectedNleTrackSlot?.masks?.firstOrNull()?.apply {
            resList?.forEachIndexed { index, resourceItem ->
                if (resourceItem.path == this.segment.resource.resourceFile) {
                    return index
                }
            }
        }
        return maskIndex
    }

    fun getCurrentMask(): String {
        return nleEditorContext.selectedNleTrackSlot?.masks?.firstOrNull()?.segment?.resource?.resourceFile
            ?: ""
    }

    /**
     * 获取羽化的值
     *
     * @return
     */
    fun getCurrentMaskFeather(): Float {
        var maskFeather = 0F
        nleEditorContext.selectedNleTrackSlot?.masks?.firstOrNull()?.apply {
            maskFeather = this.segment.feather
        }
        return maskFeather
    }

    fun getCurrentMaskInvert(): Boolean {
        var isInvert = false
        nleEditorContext.selectedNleTrackSlot?.masks?.firstOrNull()?.apply {
            isInvert =  this.segment.invert
        }
        return isInvert

    }

    fun getCanvasRatio() = nleEditorContext.canvasEditor.getRatio(ORIGINAL)
}
