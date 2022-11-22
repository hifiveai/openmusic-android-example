package com.ss.ugc.android.editor.bottom.videoeffect

import android.text.TextUtils
import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nleeditor.getMainSegment
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.resource.CategoryInfo
import com.ss.ugc.android.editor.base.resource.ResourceListListener
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.commitDone
import com.ss.ugc.android.editor.core.isVideoEffect
import com.ss.ugc.android.editor.core.utils.Toaster
import java.util.concurrent.TimeUnit


/**
 * time : 2020/12/6
 *
 * description :
 * 滤镜
 */
@Keep
class VideoEffectViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val defaultDuration = TimeUnit.SECONDS.toMicros(3)

    private val nleModel by lazy {
        nleEditorContext.nleModel
    }

    private val nleEditor by lazy {
        nleEditorContext.nleEditor
    }

    private val nleMainTrack by lazy {
        nleEditorContext.nleMainTrack
    }

    private val categoryInfoLiveData: MutableLiveData<List<CategoryInfo>> = MutableLiveData()
    val categoryInfoList: LiveData<List<CategoryInfo>> get() = categoryInfoLiveData

    fun fetchPanelInfo(panel: String) {
        EditorSDK.instance.config.resourceProvider?.fetchPanelInfo(panel, object :
            ResourceListListener<CategoryInfo> {
            override fun onStart() {
            }

            override fun onSuccess(dataList: List<CategoryInfo>) {
                categoryInfoLiveData.value = dataList
            }

            override fun onFailure(exception: Exception?, tips: String?) {
                categoryInfoLiveData.value = ArrayList()
            }
        })
    }

    fun onDone() {
        nleEditor.commitDone()
    }

    companion object {
        const val MIN_DURATION = 1000L
    }

    /**
     * 添加一个视频特效 默认添加到主轨
     *
     * @param path
     * @param name
     */
    fun addVideoEffectForSlot(
        path: String,
        name: String = "",
        id: String = "",
        isApplyAll: Boolean = false
    ) {

        val effectStartTime = nleEditorContext.videoPlayer.curPosition().toLong().toMicro()
        val time =
            (nleEditorContext.videoPlayer.totalDuration() - nleEditorContext.videoPlayer.curPosition()).toLong()
                .toMicro()
        val applyDuration = if (defaultDuration > time) {
            time
        } else {
            defaultDuration
        }
        val videoEffectSlot = NLETrackSlot().apply {
            mainSegment = NLESegmentEffect().apply {
                effectSDKEffect = NLEResourceNode().apply {
                    resourceTag =
                        if (EditorSDK.instance.config.effectAmazing) NLEResTag.AMAZING else NLEResTag.NORMAL
                    resourceFile = path
                    resourceName = name
                    resourceId = id
                }
                applyTargetType = if (isApplyAll) 2 else 0
                effectName = name
            }
            startTime = effectStartTime
            endTime = startTime + applyDuration
            val slotLayer = nleModel.effectLayerMax + 1
            transformZ = slotLayer
            layer = slotLayer

        }
        if (isApplyAll) {
            NLETrack().apply {
                addSlot(videoEffectSlot)
                extraTrackType = NLETrackType.EFFECT
                nleModel.addTrack(this)
                nleEditor.commitDone()
            }

            LiveDataBus.getInstance()
                .with(Constants.KEY_ADD_VIDEO_EFFECT, NLETrackSlot::class.java)
                .value = (videoEffectSlot)

            nleEditorContext.videoPlayer.playRange(
                effectStartTime.toMilli().toInt(),
                (effectStartTime + applyDuration).toMilli().toInt()
            )

        } else {
            // 为空说明第一次添加
            if (nleEditorContext.selectedNleTrackSlot == null) {
                nleMainTrack
                    .apply {
                        this.addVideoEffect(videoEffectSlot)
                        videoEffectSlot.effectRelativeTime =
                            videoEffectSlot.startTime - this.startTime
                        nleEditor.commitDone()
                    }

                LiveDataBus.getInstance()
                    .with(Constants.KEY_ADD_VIDEO_EFFECT, NLETrackSlot::class.java)
                    .value = (videoEffectSlot)

            } else {
                updateEffect(path, name, id)
                // 这里面已经 play range 了所以直接 return
                return
            }

            nleEditorContext.videoPlayer.playRange(
                effectStartTime.toMilli().toInt(),
                (effectStartTime + applyDuration).toMilli().toInt()
            )
        }
    }

    fun updateEffect(
        path: String, name: String = "", id: String,
        newStartTime: Long = -1, newEndTime: Long = -1
    ) {
        nleEditorContext.selectedNleTrack?.let { track ->
            nleEditorContext.selectedNleTrackSlot?.let { slot ->
                NLESegmentEffect.dynamicCast(slot.mainSegment)?.let {

                    it.effectSDKEffect.resourceFile = path
                    it.effectName = name
                    it.effectSDKEffect.resourceName = name
                    it.effectSDKEffect.resourceId = id
                    slot.startTime =
                        if (newStartTime == -1L) {
                            slot.startTime
                        } else {
                            newStartTime
                        }
                    slot.endTime = if (newEndTime == -1L) {
                        slot.endTime
                    } else {
                        newEndTime
                    }

                    nleEditor.commitDone()
                    nleEditorContext.videoPlayer.playRange(
                        slot.startTime.toMilli().toInt(),
                        slot.endTime.toMilli().toInt()
                    )
                    it
                }
            }
        }
    }

    fun deleteEffect() {
        nleEditorContext.selectedNleTrack?.let { track ->
            nleEditorContext.selectedNleTrackSlot?.let { slot ->
                if (track.trackType == NLETrackType.VIDEO) {
                    track.removeVideoEffect(slot)
                } else {
                    track.removeSlot(slot)

                }
                nleEditor.commitDone()
            }
        }
        // 重置选中的track和slot
        nleEditorContext.updateSelectedTrackSlot(null, null)
    }


    /**
     * 复制特效  新增一个slot
     */
    fun copyVideoEffect(): NLETrackSlot? {

        return nleEditorContext.selectedNleTrack?.let { track ->
            nleEditorContext.selectedNleTrackSlot?.let { slot ->

                val total = TimeUnit.MILLISECONDS.toMicros(
                    nleEditorContext.videoPlayer.totalDuration().toLong()
                )

                val applyDuration = Math.min(slot.endTime - slot.startTime, total - slot.endTime)
                val oriEffectSegment = NLESegmentEffect.dynamicCast(slot.mainSegment)

                if (total - slot.endTime < MIN_DURATION) {
                    Toaster.show(nleEditorContext.getString(R.string.ck_tips_not_enough_spacing))
                    return null
                }
                val newSlot = NLETrackSlot().apply {
                    mainSegment = NLESegmentEffect.dynamicCast(
                        slot.getMainSegment<NLESegmentEffect>()?.deepClone(true)
                    )

                    startTime = slot.endTime
                    endTime = startTime + applyDuration
                    layer = getNiceLayer(slot.layer, startTime, endTime)
                }
                if (track.trackType == NLETrackType.EFFECT) {
                    track.apply {
                        addSlot(newSlot)
                    }
                } else {
                    // 默认加载了主轨上
                    track.addVideoEffect(newSlot)
                }

                nleEditor.commitDone(true)
                // 通知选中特效slot
                LiveDataBus.getInstance()
                    .with(Constants.KEY_ADD_VIDEO_EFFECT, NLETrackSlot::class.java)
                    .postValue(newSlot)
                newSlot
            }
        }
    }

    /**
     * 所有目前所有的layer和layer层的VideoEffect
     *
     * @return
     */
    private fun getAllEffectsMap(): HashMap<Int, ArrayList<NLETimeSpaceNode>?> {
        val effectMap = HashMap<Int, ArrayList<NLETimeSpaceNode>?>()
        for (track in nleModel.tracks) {
            if (track.extraTrackType == NLETrackType.EFFECT) {
                for (slot in track.sortedSlots) {
                    val layer = slot.layer
                    if (effectMap[layer] == null) {
                        val list = ArrayList<NLETimeSpaceNode>()
                        list.add(slot)

                        effectMap[layer] = list
                    } else {
                        effectMap[layer]!!.add(slot)
                    }
                }
            } else if (track.extraTrackType == NLETrackType.VIDEO) {
                for (effect in track.videoEffects) {
                    val layer = effect.layer
                    if (effectMap[layer] == null) {
                        val list = ArrayList<NLETimeSpaceNode>()
                        list.add(effect)

                        effectMap[layer] = list
                    } else {
                        effectMap[layer]!!.add(effect)
                    }
                }
            }
        }
        return effectMap
    }

    /**
     * 判断effets数组下的 startTime和endTime时间内 有无特效
     * 有 返回true 否则 返回false
     *
     * @param effets
     * @param startTime
     * @param endTime
     * @return
     */
    private fun isInVideoEffect(
        effets: ArrayList<NLETimeSpaceNode>?,
        startTime: Long, endTime: Long
    ): Boolean {

        effets?.apply {
            for (effect in this) {
                if (startTime in effect.startTime until effect.endTime
                    || endTime in effect.startTime until effect.endTime
                    || (startTime <= effect.startTime && endTime >= effect.endTime)
                ) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 默认放到被复制特效的后面 不过可能已经有了
     * 目前方案：
     *   1. 先去layer层 看下时间是否OK
     *   1.1 若Ok 复制的layer直接这个layer
     *   1.2 若不行，需要+1 看下新的layer是否ok 重复步骤1
     *
     * @param layer
     * @param startTime
     * @param endTime
     * @return
     */
    private fun getNiceLayer(layer: Int, startTime: Long, endTime: Long): Int {
        val layerMap = getAllEffectsMap()
        var niceLayer = layer
        while (isInVideoEffect(layerMap[niceLayer], startTime, endTime)) {
            niceLayer++
        }
        return niceLayer

    }

    fun applyTrack(applyItem: EffectApplyItem?) {
        val oriTrack = nleEditorContext.selectedNleTrack
        val oriEffect = nleEditorContext.selectedNleTrackSlot

        oriTrack?.apply {
            oriEffect?.apply {
                if (oriTrack.extraTrackType == NLETrackType.EFFECT) {
                    oriTrack.sortedSlots.forEach {
                        if (it.name == oriEffect.name) {
                            oriTrack.removeSlot(it)
                        }
                    }
                } else {
                    oriTrack.videoEffects.forEach {
                        if (it.name == oriEffect.name) {
                            oriTrack.removeVideoEffect(it)
                        }
                    }
                }

                applyItem?.track?.apply {
                    if (this.extraTrackType == NLETrackType.EFFECT) {
                        this.addSlot(oriEffect)
                    } else {
                        this.addVideoEffect(oriEffect)
                    }
                }

                nleEditor.commitDone()
                // 更新选中的track和slot
                LiveDataBus.getInstance()
                    .with(Constants.KEY_ADD_VIDEO_EFFECT, NLETrackSlot::class.java).value =
                    oriEffect
                nleEditorContext.videoPlayer.playRange(
                    startTime.toMilli().toInt(),
                    measuredEndTime.toMilli().toInt()
                )
            }
        }
    }


    private fun isVideoEffectTrack(track: NLETrack) = track.isVideoEffect()


    fun selectedEffectPath(): String {

        return nleEditorContext.selectedNleTrack?.let { track ->
            nleEditorContext.selectedNleTrackSlot?.let { slot ->

                NLESegmentEffect.dynamicCast(slot.mainSegment)?.let {
                    it.effectSDKEffect.resourceFile
                } ?: ""

            } ?: ""
        } ?: ""

    }

    fun getApplyTrackId(): EffectApplyItem? {

        nleEditorContext.selectedNleTrack?.let { track ->
            nleEditorContext.selectedNleTrackSlot?.let { slot ->
                return EffectApplyItem(track, slot)
            }
        }
        return null
    }

    fun getExtra(slot: NLETrackSlot, key: String, default: Int): Int {
        return slot.getExtra(key)?.let {
            if (TextUtils.isEmpty(it)) {
                default
            } else {
                it.toInt()
            }
        } ?: default
    }

    fun canInsertEffect(): Boolean {//距离末尾一秒以上时才能添加特效
        return nleEditorContext.videoPlayer.curPosition() + 1000 < nleEditorContext.videoPlayer.totalDuration()
    }


}

