package com.ss.ugc.android.editor.core

import android.content.res.Resources
import android.text.TextUtils
import android.util.Log
import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nlemediajava.DefaultNLEIdVEIndexMapper
import com.bytedance.ies.nlemediajava.nleId
import com.bytedance.ies.nlemediajava.nleSlotId
import com.bytedance.ies.nlemediajava.utils.MediaUtil
import com.bytedance.ies.nlemediajava.utils.VideoMetaDataInfo
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.set
import kotlin.math.max

const val RELATIVE_EFFECT_TIME = "relative_effect_time"
const val TEMPLATE_CANVAS_RATIO = "TEMPLATE_CANVAS_RATIO"
fun NLETrackSlot?.isEffectSlot(): Boolean {
    this?.apply {
        return NLESegmentEffect.dynamicCast(this.mainSegment) != null
    }
    return false
}

fun NLEModel.getTrackByVideoEffect(slot: NLETrackSlot): NLETrack? {
    this.tracks.forEach {
        if (it.trackType == NLETrackType.VIDEO) {
            it.videoEffects.forEach { effect ->
                if (effect.name == slot.name) {
                    return it
                }

            }
        } else if (it.trackType == NLETrackType.EFFECT) {
            it.slots.forEach { effect ->
                if (effect.name == slot.name) {
                    return it
                }

            }
        }
    }
    return null
}


fun NLESegmentVideo.avgSpeed(): Float = if (curveAveSpeed != 1.0) curveAveSpeed.toFloat() else absSpeed.toFloat()

/**
 * 特效和slot的相对
 */
var NLETimeSpaceNode.effectRelativeTime: Long
    get() {
        if (hasExtra(RELATIVE_EFFECT_TIME)) {
            return getExtra(RELATIVE_EFFECT_TIME).toLong()
        } else {
            return 0
        }
    }
    set(value) {
        setExtra(RELATIVE_EFFECT_TIME, value.toString())
    }

/**
 * 画中画用slot上保存的trackId  主轨和音轨用veTrackIndex
 *  - 获取主轨和音轨的trackIndex时  slot可以为null
 */
fun NLETrack.index(slot: NLETrackSlot? = null): Int {
    return if (mainTrack) {
        0
    } else {
        return let {
            if (this.extraTrackType == NLETrackType.VIDEO) slot?.pipTrackIndex()
//        else veTrackIndex
            else 0
        }
            ?: throw IllegalArgumentException("sub track index is not allowed empty!")
    }
}

fun NLETrack.setIndex(index: Int) {
//    veTrackIndex = index
//    setExtra(Constants.TRACK_INDEX, index.toString())
}

fun NLETrack.getVETrackType(): String {
    return getExtra(Constants.TRACK_TYPE)
}

fun NLETrack.setVETrackType(trackType: String) {
    setExtra(Constants.TRACK_TYPE, trackType)
}

/**
 * 记录保存的track的数量
 */
fun NLEModel.setTrackLayer(count: Int) {
    setExtra(Constants.TRACK_LAYER, count.toString())
}

fun NLEModel.trackMaxLayer(): Int {
    return if (TextUtils.isEmpty(getExtra(Constants.TRACK_LAYER))) {
        0
    } else {
        getExtra(Constants.TRACK_LAYER).toInt()
    }
}

fun NLEModel.hasClips(): Boolean {
    return tracks.size > 0
}

/**
 * 获取副轨的数量
 */
fun NLEModel.getSubTrackSize(): Int {
    return tracks.filter {
        !it.mainTrack && it.getVETrackType() == Constants.TRACK_VIDEO
    }.size
}

fun NLEModel.getAudioTrackSize(): Int {
    return tracks.filter {
        !it.mainTrack && it.getVETrackType() == Constants.TRACK_AUDIO
    }.size
}


fun NLEEditorContext.setExtra(extraKey: String, extraVal: String) {
    nleModel.setExtra(extraKey, extraVal)
}

fun NLEEditorContext.getExtra(extraKey: String): String? {
    return nleModel.getExtra(extraKey)
}

fun NLEEditorContext.hasExtra(extraKey: String): Boolean {
    return nleModel.hasExtra(extraKey)
}

fun NLEEditorContext.setSlotExtra(extraKey: String, extraVal: String) {
    selectedNleTrackSlot?.setExtra(extraKey, extraVal)
}

fun NLEEditorContext.getSlotExtra(extraKey: String): String? {
    return selectedNleTrackSlot?.getExtra(extraKey)
}

fun NLEEditorContext.isRewind(): Boolean {
    return NLESegmentAudio.dynamicCast(selectedNleTrackSlot?.mainSegment)?.rewind == true
}

fun NLEEditorContext.hasSlotExtra(extraKey: String): Boolean {
    return selectedNleTrackSlot?.hasExtra(extraKey) ?: false
}

fun NLEModel.getAddTrackLayer(trackType: String): Int {
    var trackLayer = getMaxLayer(trackType) + 1
    getShouldAddTrack(trackType)?.apply {
        removeTrack(this) // 如果是空的track 先remove掉 再新添同layer的track
        trackLayer = this.layer
    }
    return trackLayer
}

fun NLEModel.getAddTrackAudioLayer(startTime: Long): Int {
    val layerMap = mutableMapOf<Int, MutableList<NLETrackSlot>>()
    tracks.filter {
        !it.mainTrack && it.getVETrackType() == Constants.TRACK_AUDIO
    }.forEach { track ->
        if (layerMap[track.layer] == null) {
            layerMap[track.layer] = mutableListOf()
        }
        layerMap[track.layer]?.addAll(track.sortedSlots)
    }

    var targetLayer = 0

    kotlin.run outside@{
        //排个顺序
        layerMap.toSortedMap(Comparator { o1, o2 -> (o1 - o2) }).forEach track@{
            var find = true
            it.value.forEach slot@{ slot ->
                if (slot.startTime >= startTime || slot.endTime > startTime) {
                    // 这个 layer 不行
                    find = false
                }
            }
            if (find) {
                return@outside
            }
            targetLayer++
        }
    }

    return max(0, targetLayer)
}

fun NLEModel.getAddFilterTrack(seqIn: Long, seqOut: Long, filterType: String): NLETrack? {

    for (index in 0..100) {
        val tracks = tracks.filter {
            !it.mainTrack && it.getVETrackType() == filterType && it.layer == index
        }
        if (tracks.isEmpty()) { //说明没有此layer的track 直接新建并return
            return NLETrack().apply {
                layer = index
            }
        }
        val slots = mutableListOf<NLETrackSlot>()
        tracks.forEach { track ->
            track.slots.filter { slot ->
                (slot.startTime <= seqIn && seqIn <= slot.endTime) ||
                        slot.startTime <= seqOut && seqOut <= slot.endTime ||
                        seqIn <= slot.startTime && seqOut >= slot.endTime
            }.forEach {
                slots.add(it)
            }
        }
        if (slots.isEmpty()) { //说明track可以放下要添加的slot
            return NLETrack().apply {
                layer = index
            }
        }
    }
    return null
//    return max(0, targetLayer)
}

fun NLEModel.getRecordSegmentCount(): Int {
    val list = mutableListOf<NLETrackSlot>()
    var number = 0
    val language = Locale.getDefault().language
    val namePrefix = if (!TextUtils.equals(language, "zh")) {
        "Recording"
    } else {
        "录音"
    }
    tracks.filter {
        !it.mainTrack && it.getVETrackType() == Constants.TRACK_AUDIO
    }.forEach { track ->
        track.sortedSlots.forEach {
            if (it.mainSegment.resource.resourceType == NLEResType.RECORD) {
                list.add(it)
                number = max(
                    number,
                    it.mainSegment.resource.resourceName.removePrefix(namePrefix).trim().toInt()
                )
            }
        }
    }
    return number
}

fun NLEEditorContext.checkHasEmptySticker(): Boolean {
    if (isCoverMode) {
        selectedNleCoverTrack?.also { track ->
            selectedNleCoverTrackSlot?.also { slot ->
                if (track.isTrackSticker()) {
                    NLESegmentTextSticker.dynamicCast(slot.mainSegment)?.apply {
                        return content.isEmptyTextSticker()
                    }
                }
            }
        }
        return false
    } else {
        selectedNleTrack?.also { track ->
            selectedNleTrackSlot?.also { slot ->
                if (track.isTrackSticker()) {
                    NLESegmentTextSticker.dynamicCast(slot.mainSegment)?.apply {
                        return content.isEmptyTextSticker()
                    }
                }
            }
        }
        return false
    }
}

fun NLEEditorContext.selectedFlowerPath(): String {
    return if (isCoverMode) {
        selectedNleCoverTrack?.let { track ->
            selectedNleCoverTrackSlot?.let { slot ->
                val currentSticker = getCurrentSticker()
                currentSticker?.style?.flower?.resourceFile ?: ""
            } ?: ""
        } ?: ""
    } else {
        selectedNleTrack?.let { track ->
            selectedNleTrackSlot?.let { slot ->
                val currentSticker = getCurrentSticker()
                currentSticker?.style?.flower?.resourceFile ?: ""
            } ?: ""
        } ?: ""
    }
}

fun NLEEditorContext.selectedFontPath(): String {
    return if (isCoverMode) {
        selectedNleCoverTrack?.let { track ->
            selectedNleCoverTrackSlot?.let { slot ->
                val currentSticker = getCurrentSticker()
                currentSticker?.style?.font?.resourceFile ?: ""
            } ?: ""
        } ?: ""
    } else {
        selectedNleTrack?.let { track ->
            selectedNleTrackSlot?.let { slot ->
                val currentSticker = getCurrentSticker()
                currentSticker?.style?.font?.resourceFile ?: ""
            } ?: ""
        } ?: ""
    }
}

fun NLEEditorContext.getCurrentSticker(): NLESegmentTextSticker? {
    return if (isCoverMode) {
        selectedNleCoverTrack?.let { track ->
            selectedNleCoverTrackSlot?.let { slot ->
                if (track.isTrackSticker()) {
                    NLESegmentTextSticker.dynamicCast(slot.mainSegment)
                } else {
                    null
                }
            }
        }
    } else {
        selectedNleTrack?.let { track ->
            selectedNleTrackSlot?.let { slot ->
                if (track.isTrackSticker()) {
                    NLESegmentTextSticker.dynamicCast(slot.mainSegment)
                } else {
                    null
                }
            }
        }
    }
}

fun NLEEditorContext.selectedTextTemplatePath(): String {
    return selectedNleTrack?.let {
        selectedNleTrackSlot?.let {
            getCurrentTextTemplate()?.effectSDKFile?.resourceFile ?: ""
        } ?: ""
    } ?: ""
}

fun NLEEditorContext.getCurrentTextTemplate(): NLESegmentTextTemplate? {
    return selectedNleTrack?.let { track ->
        selectedNleTrackSlot?.let { slot ->
            if (track.isTrackSticker()) {
                NLESegmentTextTemplate.dynamicCast(slot.mainSegment)
            } else {
                null
            }
        }
    }
}

fun NLESegment.getVolume(): Float {
    return NLESegmentVideo.dynamicCast(this)?.volume
        ?: NLESegmentAudio.dynamicCast(this)?.volume ?: 1.0f
}

fun NLESegment.setVolume(volume: Float) {
    NLESegmentVideo.dynamicCast(this)?.volume = volume
    NLESegmentAudio.dynamicCast(this)?.volume = volume
}


private val density: Float
    get() = Resources.getSystem().displayMetrics.density

private val scaleDensity: Float
    get() = Resources.getSystem().displayMetrics.scaledDensity


/**
 * 设置轨道和slot上的滤镜 保存是哪一个
 */
fun NLEModel.setFilterPosition(filterType: String, position: Int) {
    setExtra(filterType, position.toString())
}

fun NLETrackSlot.setFilterPosition(filterType: String, position: Int) {
    setExtra(filterType, position.toString())
}

fun NLEModel.getFilterPosition(filterType: String, default: Int): Int {
    return getExtra(filterType)?.let {
        if (TextUtils.isEmpty(it)) {
            default
        } else {
            it.toInt()
        }
    } ?: default
}

// 获取目前已添加了多少个全局调节的slot
fun NLEModel.getAdjustSegmentCount(): Int {
    val list = mutableListOf<NLETrackSlot>()
    var number = 0
    tracks.filter {
        !it.mainTrack && it.getVETrackType() == Constants.TRACK_FILTER_ADJUST
    }.forEach { track ->
        track.sortedSlots.forEach {
            number++
        }
    }
    return number
}

fun NLETrackSlot.getFilterPosition(filterType: String, default: Int): Int {
    return getExtra(filterType)?.let {
        if (TextUtils.isEmpty(it)) {
            default
        } else {
            it.toInt()
        }
    } ?: default
}


fun NLEModel.setFilterIntensity(adjustType: String, intensity: Float) {
    setExtra(adjustType, intensity.toString())
}

fun NLETrackSlot.setFilterIntensity(adjustType: String, intensity: Float) {
    setExtra(adjustType, intensity.toString())
}

fun NLEModel.getFilterIntensity(filterType: String, default: Float): Float {
    tracks.filter {
        it.extraTrackType == NLETrackType.FILTER
    }?.forEach {
        it.slots.forEach { slot ->
            NLESegmentFilter.dynamicCast(slot.mainSegment)?.apply {
                if (TextUtils.equals(filterName, filterType)) {
                    return this.intensity
                }
            }
        }
    }
    return 0F
}

fun NLETrackSlot.getFilterIntensity(filterType: String, default: Float): Float {
    return getExtra(filterType)?.let {
        if (TextUtils.isEmpty(it)) {
            default
        } else {
            it.toFloat()
        }
    } ?: default
}


fun NLETrack.getVolumeIntensity(filterType: String, default: Float): Float {
    return getExtra(filterType)?.let {
        if (TextUtils.isEmpty(it)) {
            default
        } else {
            it.toFloat()
        }
    } ?: default
}

fun NLETrack.isTrackSticker() = this.getVETrackType() == Constants.TRACK_STICKER
fun NLETrack.isVideoEffect() = this.getVETrackType() == Constants.TRACK_VIDEO_EFFECT

fun String.isEmptyTextSticker() = this == emptySticker

fun String.nullToEmptySticker(): String = if (this.isEmpty()) {
    emptySticker
} else {
    this
}

fun String.emptyStickerToEmpty(): String = if (this == emptySticker) {
    ""
} else {
    this
}

val emptySticker by lazy { EditorCoreInitializer.instance.appContext!!.resources.getString(R.string.ck_enter_text) }


const val previewIconPath = "previewIconPath"
fun NLETrackSlot.previewIconPath(): String {
    return this.getExtra(previewIconPath)
}

fun NLETrackSlot.setPreviewIconPath(iconPath: String) {
    this.setExtra(previewIconPath, iconPath)
}

fun NLETrackSlot.isTextSticker(): Boolean {
    return NLESegmentTextSticker.dynamicCast(this.mainSegment) != null
}

fun NLETrackSlot.isInfoSticker(): Boolean {
    return NLESegmentInfoSticker.dynamicCast(this.mainSegment) != null
}

fun NLETrackSlot.getSegmentType(): SegmentType {
    if (NLESegmentTextTemplate.dynamicCast(this.mainSegment) != null) {
        return SegmentType.TEXT_TEMPLATE
    } else if (NLESegmentTextSticker.dynamicCast(this.mainSegment) != null) {
        return SegmentType.TEXT_STICKER
    }
    return SegmentType.TEXT_ANIM
}

enum class SegmentType {
    TEXT_STICKER,
    TEXT_ANIM,
    TEXT_TEMPLATE;
}

fun NLETrackSlot.isImageSticker(): Boolean {
    return NLESegmentImageSticker.dynamicCast(this.mainSegment) != null
}

fun NLETrackSlot.isVideoSlot(): Boolean {
    return NLESegmentVideo.dynamicCast(this.mainSegment) != null
}

fun Long.toMilli(): Long {
    return TimeUnit.MICROSECONDS.toMillis(this)
}

fun Long.toMicro(): Long {
    return TimeUnit.MILLISECONDS.toMicros(this)
}

fun Long.toSecond(): Int {
    return (this / 1000_000).toInt()
}

/**
 * 获取当前track类型中的 layer的最大值
 *  trackType = TRACK_VIDEO / TRACK_AUDIO / TRACK_STICKER
 */
fun NLEModel.getMaxLayer(trackType: String): Int {
    var maxLayer = -1

    return tracks.filter {
        !it.mainTrack && it.getVETrackType() == trackType
    }.forEach {
        maxLayer = if (it.layer > maxLayer) it.layer else maxLayer
    }.let {
        maxLayer
    }
}

/**
 * 新增track时  获取添加的track的layer应该设置为多少
 * getTracks不保证顺序
 */
fun NLEModel.getShouldAddLayer(trackType: String): Int {
    var layer = -1
    return tracks.filter {
        !it.mainTrack && it.getVETrackType() == trackType
    }.sortedBy { it.layer }.forEach {
        if (it.slots.size == 0) {

            return it.layer
        }
        layer = if (it.layer > layer) it.layer else layer
    }.let {
        layer + 1
    }
}

fun NLEModel.getShouldAddTrack(trackType: String): NLETrack? {
    return tracks.filter {
        !it.mainTrack && it.getVETrackType() == trackType
    }.sortedBy {
        it.layer
    }.forEach {
        if (it.slots.size == 0) {
            return it
        }
    }.let {
        null
    }
}

fun NLETrackSlot.getStickerIndex(indexMapper: DefaultNLEIdVEIndexMapper?): Int {
    var stickerIndex = -1
    indexMapper?.getStickerIndex(nleSlotId)?.apply {
        stickerIndex = this
    }
    if (stickerIndex < 0) {
        Log.e("getStickerIndex", "error index")
        throw IllegalArgumentException("error index:${stickerIndex}")
    }
    return stickerIndex
}

fun NLETrackSlot.setStickerIndex(stickerIndex: Int, indexMapper: DefaultNLEIdVEIndexMapper?) {
    indexMapper?.setStickerIndex(nleSlotId, stickerIndex)
}


/**
 * 画中画中的slot里 保存的ve对应的trackIndex
 */
fun NLETrackSlot.pipTrackIndex() = this.getExtra(Constants.PIP_TRACK_INDEX).toInt()


fun NLETrackSlot.inMainTrack(nleModel: NLEModel) = nleModel.getTrackBySlot(this)?.mainTrack ?: false

fun NLETrackSlot.track(nleModel: NLEModel?): NLETrack? = nleModel?.getTrackBySlot(this)

/**
 * 获取全局滤镜的NLESegmentFilter
 *
 * @param type
 * @return
 */
fun NLEModel.getFilterByName(type: String): NLESegmentFilter? {
    this.tracks.forEach { it ->
        it.sortedSlots.forEach { it2 ->
            NLESegmentFilter.dynamicCast(it2.mainSegment)?.apply {
                if (this.filterName == type) {
                    return this
                }
            }
        }
    }
    return null

}

/**
 * 添加所有的全局滤镜
 *
 * @return
 */
fun NLEModel.getFilters(): List<NLESegmentFilter> {
    val filters = ArrayList<NLESegmentFilter>()
    this.tracks.forEach { it ->
        it.sortedSlots.forEach { it2 ->
            NLESegmentFilter.dynamicCast(it2.mainSegment)?.apply {
                filters.add(this)
            }
        }
    }
    return filters
}

/**
 * 获取NLEModel里面的主轨
 *
 * @return
 */
fun NLEModel.getMainTrack(): NLETrack? {
    for (track in this.getTracks()) {
        if (track.mainTrack) {
            return track
        }
    }
    return null
}

fun NLEEditorContext.isPipTrack(): Boolean {
    return selectedNleTrack?.isVideoTrack() == true && selectedNleTrack?.mainTrack == false
}

fun NLETrack.isVideoTrack(): Boolean {
    return trackType == NLETrackType.VIDEO || extraTrackType == NLETrackType.VIDEO
}

fun fileInfo(path: String): VideoMetaDataInfo {
    return MediaUtil.getRealVideoMetaDataInfo(path)
}

fun NLEModel.getSlotBySlotId(slotId: Int, trackId: Int? = null): NLETrackSlot? {
    val track = if (trackId != null) {
        tracks.firstOrNull { it.nleId == trackId }
    } else {
        tracks.firstOrNull {
            val targetSlot = it.slots.firstOrNull { slot ->
                slot.nleSlotId == slotId
            }
            targetSlot != null
        }
    }

    return track?.slots?.firstOrNull {
        it.nleSlotId == slotId
    }
}

fun NLENode.getExtraByDefault(extraKey: String, default: String): String {
    val extra = getExtra(extraKey)
    return if (extra.isNullOrEmpty()) default else extra
}

fun NLEModel.setTemplateRatio(ratio: Float) {
    this.setExtra(TEMPLATE_CANVAS_RATIO, ratio.toString())
}

fun NLEModel.getTemplateRatio(): Float? {
    val extra = this.getExtra(TEMPLATE_CANVAS_RATIO)
    return try {
        extra.toFloat()
    } catch (e: Exception) {
        null
    }
}

fun NLESegment.getClipDuration(): Long {
    var duration = this.duration
    NLESegmentAudio.dynamicCast(this)?.let { segment ->
        duration = segment.timeClipEnd - segment.timeClipStart
    }
    return duration
}