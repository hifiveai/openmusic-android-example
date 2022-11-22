package com.ss.ugc.android.editor.core

import android.view.SurfaceView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nlemediajava.keyframe.bean.KeyframeInfo
import com.ss.ugc.android.editor.core.api.adjust.IAdjustEditor
import com.ss.ugc.android.editor.core.api.animation.IAnimationEditor
import com.ss.ugc.android.editor.core.api.audio.IAudioEditor
import com.ss.ugc.android.editor.core.api.canvas.ICanvasEditor
import com.ss.ugc.android.editor.core.api.common.ICommonEditor
import com.ss.ugc.android.editor.core.api.filter.IFilterEditor
import com.ss.ugc.android.editor.core.api.keyframe.IKeyframeEditor
import com.ss.ugc.android.editor.core.api.keyframe.KeyframeUpdate
import com.ss.ugc.android.editor.core.api.sticker.IStickerEditor
import com.ss.ugc.android.editor.core.api.transition.ITransitionEditor
import com.ss.ugc.android.editor.core.api.video.IVideoEditor
import com.ss.ugc.android.editor.core.event.*
import com.ss.ugc.android.editor.core.listener.OnUndoRedoListener
import com.ss.ugc.android.editor.core.manager.IVideoPlayer

interface IEditorContext {
    /**
     * 初始化
     */
    fun init(workSpace: String?, surfaceView: SurfaceView)

    /**
     * 剪辑操作事件
     */
    //主轨slot move事件
    val mainTrackSlotMoveEvent: MutableLiveData<MainTrackSlotMoveEvent>

    //主轨slot clip(伸缩)事件
    val mainTrackSlotClipEvent: MutableLiveData<MainTrackSlotClipEvent>

    //普通轨道slot move事件
    val trackSlotMoveEvent: MutableLiveData<TrackSlotMoveEvent>

    //普通轨道slot clip事件
    val trackSlotClipEvent: MutableLiveData<TrackSlotClipEvent>

    //普通轨道slot选中事件
    val selectedTrackSlotEvent: MutableLiveData<SelectedTrackSlotEvent>

    //转场点击事件
    val transitionTrackSlotClickEvent: MutableLiveData<TransitionTrackSlotClickEvent>

    //修改分辨率事件
    val changeResolutionEvent: MutableLiveData<Int>

    //修改帧率事件
    val changeFpsEvent: MutableLiveData<Int>

    //修改画布比例事件
    val changeRatioEvent: MutableLiveData<Float>

    //选中贴纸事件
    val selectStickerEvent: MutableLiveData<SelectStickerEvent>

    //slot选中事件
    val selectSlotEvent: MutableLiveData<SelectSlotEvent>

    //slot改变事件
    val slotChangeChangeEvent: MutableLiveData<SelectSlotEvent>
    val slotSelectChangeEvent: MutableLiveData<SelectSlotEvent>

    //面板操作事件
    val panelEvent: MutableLiveData<PanelEvent>

    //视频播放位置事件
    val videoPositionEvent: MutableLiveData<Long>

    //声音改变事件
    val volumeChangedEvent: MutableLiveData<VolumeEvent>

    //动画改变事件
    val animationChangedEvent: MutableLiveData<NLEVideoAnimation?>

    //移动贴纸slot事件
    val moveStickerSlotEvent: MutableLiveData<UpdateClipRangeEvent>

    //伸缩贴纸slot事件
    val clipStickerSlotEvent: MutableLiveData<AdjustClipRangeEvent>

    //关闭原声/开启原声点击事件
    val oriAudioMuteEvent: MutableLiveData<Boolean>

    val keyframeUpdateEvent: LiveData<KeyframeUpdate>

    var keyframeTimeRange: (() -> Float)?

    //轨道替换事件
    val slotReplaceEvent: MutableLiveData<SelectSlotEvent>

    val updateCoverEvent: MutableLiveData<Boolean>

    //关闭底部面板事件
    val closePanelEvent: MutableLiveData<ClosePanelEvent>
    val updateTrackEvent: MutableLiveData<Boolean>

    val nleEditor: NLEEditor
    var nleModel: NLEModel
    var nleMainTrack: NLETrack
    var selectedNleTrackSlot: NLETrackSlot?
    var selectedNleTrack: NLETrack?
    var selectedNleCoverTrack: NLETrack?
    var selectedNleCoverTrackSlot: NLETrackSlot?
    var currentAudioSlot: NLETrackSlot?
    var preTransitionNleSlot: NLETrackSlot?
    var nextTransitionNleSlot: NLETrackSlot?
    var isLoopPlay:Boolean
    var hasSettingTemplate:Boolean // 记录是否设置过模版的槽位


    /**
     * 视频播放器
     */
    val videoPlayer: IVideoPlayer

    /**
     * 视频编辑器
     */
    val videoEditor: IVideoEditor

    /**
     * 画布编辑器
     */
    val canvasEditor: ICanvasEditor

    /**
     * 贴纸编辑器
     */
    val stickerEditor: IStickerEditor

    /**
     * 滤镜编辑器
     */
    val filterEditor: IFilterEditor

    /**
     * 动画编辑器
     */
    val animationEditor: IAnimationEditor

    /**
     * 转场编辑器
     */
    val transitionEditor: ITransitionEditor

    /**
     * 音频编辑器
     */
    val audioEditor: IAudioEditor

    /**
     * 调节编辑器
     */
    val adjustEditor: IAdjustEditor

    /**
     * 通用编辑器
     */
    val commonEditor: ICommonEditor

    /**
     * 关键帧编辑器
     */
    val keyframeEditor: IKeyframeEditor

    fun moveTrack(listener: (Int, Int) -> Unit)

    fun stopTrack()

    fun saveDraft(): String

    fun restoreDraft(data: String): NLEError

    fun undo(): Boolean

    fun redo(): Boolean

    fun canUndo(): Boolean

    fun canRedo(): Boolean

    fun addUndoRedoListener(listener: OnUndoRedoListener)

    fun removeUndoRedoListener(listener: OnUndoRedoListener)

    fun commit()

    fun done(actionMsg: String? = null)

    fun transientDone();

    fun updateSelectedTrackSlot(nleTrack: NLETrack?, nleTrackSlot: NLETrackSlot?)

    fun getKeyframeInfo(filterIndex: Int): KeyframeInfo?

    fun getString(resId: Int): String
}
