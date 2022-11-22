package com.ss.ugc.android.editor.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.bytedance.ies.nle.editor_jni.*
import com.bytedance.ies.nle.editor_jni.NLEError.SUCCESS
import com.bytedance.ies.nleeditor.getMainSegment
import com.bytedance.ies.nlemedia.SeekMode
import com.bytedance.ies.nlemedia.SeekMode.EDITOR_SEEK_FLAG_LastSeek
import com.bytedance.ies.nlemediajava.nleSlotId
import com.bytedance.ies.nlemediajava.reverseVideoPath
import com.ss.ugc.android.editor.base.EditorConfig
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.EditorSDK.Companion.instance
import com.ss.ugc.android.editor.base.constants.ActivityForResultCode
import com.ss.ugc.android.editor.base.constants.ActivityForResultCode.Companion.CHOSE_VIDEO_REQUEST_CODE
import com.ss.ugc.android.editor.base.constants.ActivityForResultCode.Companion.REPLACE_VIDEO_REQUEST_CODE
import com.ss.ugc.android.editor.base.constants.CropConstants
import com.ss.ugc.android.editor.base.draft.TemplateDraftItem
import com.ss.ugc.android.editor.base.fragment.FragmentHelper
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.imageloder.ImageLoader
import com.ss.ugc.android.editor.base.imageloder.ImageOption
import com.ss.ugc.android.editor.base.monitior.ReportConstants
import com.ss.ugc.android.editor.base.monitior.ReportUtils
import com.ss.ugc.android.editor.base.music.data.MusicItem
import com.ss.ugc.android.editor.base.music.data.MusicType.EFFECT
import com.ss.ugc.android.editor.base.music.data.SelectedMusicInfo
import com.ss.ugc.android.editor.base.task.FileUtils
import com.ss.ugc.android.editor.base.theme.ExitFullScreenIConPosition
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.*
import com.ss.ugc.android.editor.base.utils.AndroidVersion.isAtLeastR
import com.ss.ugc.android.editor.base.viewmodel.PreviewStickerViewModel
import com.ss.ugc.android.editor.base.viewmodel.VideoMaskViewModel
import com.ss.ugc.android.editor.bottom.IBottomPanel
import com.ss.ugc.android.editor.bottom.IEventListener
import com.ss.ugc.android.editor.bottom.panel.audio.record.AudioRecordViewModel
import com.ss.ugc.android.editor.core.*
import com.ss.ugc.android.editor.core.NLE2VEEditorHolder.doneListener
import com.ss.ugc.android.editor.core.api.audio.AudioParam
import com.ss.ugc.android.editor.core.api.sticker.ImageStickerParam
import com.ss.ugc.android.editor.core.api.video.CropParam
import com.ss.ugc.android.editor.core.api.video.EditMedia
import com.ss.ugc.android.editor.core.api.video.ImageCoverInfo
import com.ss.ugc.android.editor.core.api.video.Resolution
import com.ss.ugc.android.editor.core.event.*
import com.ss.ugc.android.editor.core.event.PanelEvent.Panel
import com.ss.ugc.android.editor.core.event.PanelEvent.Panel.*
import com.ss.ugc.android.editor.core.event.PanelEvent.Panel.VIDEO_MASK
import com.ss.ugc.android.editor.core.event.PanelEvent.State.*
import com.ss.ugc.android.editor.core.listener.Operation
import com.ss.ugc.android.editor.core.listener.Operation.UNDO
import com.ss.ugc.android.editor.core.listener.SimpleUndoRedoListener
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import com.ss.ugc.android.editor.main.EditorHelper.EXTRA_FROM_MULTI_SELECT
import com.ss.ugc.android.editor.main.EditorHelper.EXTRA_FROM_TEMPLATE
import com.ss.ugc.android.editor.main.EditorHelper.EXTRA_KEY_DRAFT_UUID
import com.ss.ugc.android.editor.main.EditorHelper.EXTRA_KEY_FROM_TYPE
import com.ss.ugc.android.editor.main.EditorHelper.EXTRA_KEY_MEDIA_TYPE
import com.ss.ugc.android.editor.main.EditorHelper.EXTRA_KEY_VIDEO_PATH
import com.ss.ugc.android.editor.main.cover.CutSameEditActivity
import com.ss.ugc.android.editor.main.cover.VideoCoverFragment
import com.ss.ugc.android.editor.main.cover.imageCover.CutSameData
import com.ss.ugc.android.editor.main.cover.imageCover.MediaConstant
import com.ss.ugc.android.editor.main.cover.utils.Crop
import com.ss.ugc.android.editor.main.template.TemplateDraftViewModel
import com.ss.ugc.android.editor.picker.mediapicker.PickType
import com.ss.ugc.android.editor.picker.mediapicker.PickerActivity
import com.ss.ugc.android.editor.picker.mediapicker.PickerConfig
import com.ss.ugc.android.editor.preview.EditTypeEnum
import com.ss.ugc.android.editor.preview.EditTypeEnum.*
import com.ss.ugc.android.editor.preview.EditTypeEnum.STICKER
import com.ss.ugc.android.editor.preview.IPreviewPanel
import com.ss.ugc.android.editor.track.*
import com.ss.ugc.android.editor.track.TrackState.*
import com.ss.ugc.android.editor.track.TrackState.TEXT
import com.ss.ugc.android.editor.track.utils.VideoFrameUtil
import com.ss.ugc.android.editor.track.widget.MultiTrackLayout
import com.ss.ugc.android.editor.track.widget.TrackConfig
import kotlinx.android.synthetic.main.include_editor_top_bar.*
import kotlinx.coroutines.*
import java.io.File
import java.lang.Thread.sleep
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MICROSECONDS
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

class EditorActivityDelegate(
    private val activity: FragmentActivity,
    val previewPanel: IPreviewPanel?,
    val trackPanel: TrackPanel?,
    val bottomPanel: IBottomPanel?,
    @IdRes val containerId: Int? = null
) : IEditorActivityDelegate, View.OnClickListener, CoroutineScope {

    companion object {
        const val TAG = "EditorPageDelegate"
        const val CHOSE_VIDEO = 0
        const val STICKER_TEMP_DIR = "create_sticker_upload"
        const val COVER_SNAPSHOT = "cover_snapshot"
        const val IMAGE_UNSPECIFIED = "image/*"
        const val MAX_SELECTION_COUNT: Int = 1000

        const val DRAFT_FILE_PATH = "data"

        @Deprecated("do not using will to be deleted next version")
        const val SELECT_VIDEO_AUDIO = 1
        const val DRAFT_RESTORE = 2
        const val FILE_DRAFT = 100

        const val LEFT = 0

        const val COVER_RATIO_WIDE = 0
        const val COVER_RATIO_HIGH = 1
    }

    override var viewStateListener: IEditorViewStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ///Monitor::EDIT_PAGE_SHOW
        ReportUtils.doReport(ReportConstants.VIDEO_EDIT_PAGE_SHOW_EVENT)
        nleEditorContext = viewModelProvider(activity).get(NLEEditorContext::class.java)
        nleEditorContext!!.nleEditor.addConsumer(nleEditorListener)  //往nleEditor中加入 监听器
        previewStickerViewModel = viewModelProvider(activity)
            .get(
                PreviewStickerViewModel::class.java
            )
        fragmentHelper.bind(activity)


        type = activity.intent.getIntExtra(EXTRA_KEY_FROM_TYPE, 0)
        uuid = activity.intent.getStringExtra(EXTRA_KEY_DRAFT_UUID)
        mediaType = activity.intent.getIntExtra(EXTRA_KEY_MEDIA_TYPE, 0)
        videoPath = activity.intent.getStringExtra(EXTRA_KEY_VIDEO_PATH) ?: ""
        videoPaths = activity.intent.getStringArrayListExtra(EditorHelper.EXTRA_KEY_VIDEO_PATHS)
            ?: arrayListOf()
        initFunctionExtension()
        val costMill = measureTimeMillis {
            initImport()
        }
        DLog.d(TAG, "EditorActivityDelegate initImport costTime:$costMill")
        registerEvent()
        initVideoFrameLoader()
        ReportUtils.nleModel = nleEditorContext!!.nleModel
    }

    private fun initVideoFrameLoader() {
        nleEditorContext!!.videoFrameLoader = object : IVideoFrameLoader {
            override fun loadVideoFrame(
                selectedSlotPath: String,
                timeStamp: Int,
                isImage: Boolean,
                onCompleted: (videoFrame: Bitmap) -> Unit
            ) {
                VideoFrameUtil.getVideoFrame(selectedSlotPath, timeStamp, isImage) {
                    onCompleted(it!!)
                }
            }
        }
    }

    private fun initFunctionExtension() {
        instance.functionExtension()?.apply {
            bottomPanel?.also {
                this.onFuncTreeExtension(it.getFunctionManager())
            }
        }
    }

    override fun onStart() {
    }

    override fun onResume() {
        nleEditorContext?.apply {
            if (nleEditorContext?.nleModel?.stage != null) {
                videoPlayer.resume()
            }
        }
    }

    override fun onPause() {
        nleEditorContext!!.videoPlayer.posWhenEditorSwitch =
            nleEditorContext!!.videoPlayer.curPosition()
    }

    override fun onStop() {
    }

    override fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
    }

    private val mHandler = Handler(Looper.getMainLooper())

    override var nleEditorContext: NLEEditorContext? = null

    private var nleModel: NLEModel? = null
    private val draftModel: TemplateDraftViewModel by lazy {
        ViewModelProvider(
            activity,
            ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application)
        ).get(TemplateDraftViewModel::class.java)
    }

    override var fragmentHelper = FragmentHelper(containerId)
    private var mHasSetCover = false

    private val recordViewModel: AudioRecordViewModel by lazy {
        viewModelProvider(activity).get(AudioRecordViewModel::class.java)
    }
    private val videoMaskViewModel: VideoMaskViewModel by lazy {
        viewModelProvider(activity).get(VideoMaskViewModel::class.java)
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()

    private var previewStickerViewModel: PreviewStickerViewModel? = null

    private var type: Int = 0 // 0:默认 1:从拍摄进来 2:草稿
    private var templateType: Int = 4 // 4：模板
    private var mediaType: Int = 0 //代表从拍摄进来的资源类型 图片1 视频3
    private var uuid: String? = null
    private var videoPath: String = ""
    private var videoPaths: List<String> = arrayListOf()

    private var hasLoaded = false
    private var videoTrackSelected = false
    private var audioTrackSelected = false
    private var stickerTrackSelected = false

    private var preIsEffect = false // 之前选中特效轨道
    private var preIsAdjust = false // 之前选中调节轨道
    private var preIsFilter = false // 之前选中滤镜轨道
    private var preIsText = false   // 之前选中文字轨道
    private var preIsSticker = false   // 之前选中贴纸轨道
    private var preIsPip = false    //之前选中画中画轨道
    private var localStickerCropOutDir: Uri? = null //本地贴纸功能裁剪本地图片后的输出路径
    private var preIsTextTemplate = false  //之前选中字体模板轨道
    private var preIsAudio = false  //之前选中音乐模板轨道

    private var mIsAllMute = false // 主轨是否静音(关闭原声)
    private var isCanvasEdit = false
    private var preFrameDuration: Int = TrackConfig.DEFAULT_FRAME_DURATION
    private var coverIsTemplate = false // 封面模块是否从模板编辑页面唤起
    private var coverForCompileDoneDraft: String = ""

    private val nleEditorListener: NLEEditorListener = object : NLEEditorListener() {
        override fun onChanged() {
            //Toaster.show("nle model changed!~")
            val model = nleEditorContext!!.nleModel  //获取nle model
            if (model.tracks?.size == 0) {
                return
            }
            //日志耗时约50ms
//            ifCanLog { DLog.d(TAG, "onModelChanged:$model") }
            if (model?.stage != null) {
                nleModel = model
                runOnUiThread {
                    trackPanel?.updateNLEModel(model) //更新model，nle数据更新后，通过这个接口来更新track视图
                    handleVEEditor()
                }
            }
        }
    }

    private val mDoneListener: IDoneListener = object : IDoneListener {
        override fun onDone() {
            runOnUiThread {
                updateUndoRedoActivate()
            }
        }
    }

    private fun updateUndoRedoActivate() {
        viewStateListener?.onUndoViewActivate(nleEditorContext!!.nleEditor.canUndo())
        viewStateListener?.onRedoViewActivate(nleEditorContext!!.nleEditor.canRedo())
    }

    var fullScreenIVPlay: ImageView? = null
    var fullScreenSeekBar: FloatSliderView? = null

    private fun registerEvent() {
        nleEditorContext?.keyframeEditor?.selectKeyframe?.observe(activity) {
            viewStateListener?.onKeyframeIconSelected(it)
        }

        nleEditorContext?.keyframeTimeRange = {
            trackPanel?.getKeyframeTimeRange() ?: 0f
        }

        recordViewModel.recordWavePoints.observe(activity, androidx.lifecycle.Observer {
            it?.let { points ->
                trackPanel?.updateRecordWavePoint(points)
            }
        })

        recordViewModel.recordState.observe(activity) {
            it?.let {
                when {
                    it.closeRecord -> {
                        trackPanel?.switchUIState(AUDIO)
                    }
                    it.isRecording -> {
                        trackPanel?.switchUIState(AUDIORECORD)
                        trackPanel?.startRecordAudio(it.position, it.recordLayer)
                    }
                    else -> {
                        trackPanel?.stopRecordAudio()
                    }
                }
            }
        }


        LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java)
            .observe(activity, androidx.lifecycle.Observer { position ->
                if (position == NLEEditorContext.STATE_PLAY) { //代表播放
                    mHandler.removeCallbacks(runnable) // 防止多次调play的时候 有重复
                    nleEditorContext!!.stopTrack()
                    mHandler.post(runnable)
                    viewStateListener?.onPlayViewActivate(true)
                    nleEditorContext!!.moveTrack { pos: Int, duration: Int? ->
                        trackPanel?.updatePlayState(
                            PlayPositionState(
                                MILLISECONDS.toMicros(pos.toLong()),
                                false,
                                false
                            )
                        )
                    }
                } else if (position == NLEEditorContext.STATE_PAUSE) { //代表暂停 统一处理ui状态
                    DLog.d("editorModel.STATE_PAUSE....")
                    mHandler.removeCallbacks(runnable)
                    viewStateListener?.onPlayViewActivate(false)
                    nleEditorContext!!.stopTrack()
                    viewStateListener?.onPlayTimeChanged(
                        FileUtil.stringForTime(getCurrentPosition()),
                        FileUtil.stringForTime(getTotalDuration())
                    )
                    fullScreenIVPlay?.setImageResource(R.drawable.ic_full_screen_play)
                    if (nleEditorContext!!.videoPlayer.isPlayingInFullScreen) {
                        nleEditorContext!!.videoPlayer.isPlayingInFullScreen = false
                    }
                    DLog.d("暂停了,当前播放位置：" + getCurrentPosition())
                } else if (position == NLEEditorContext.STATE_BACK) { // 点击关闭转场面板按钮
                    DLog.d("取消转场按钮的选中态和所选slot....")
                    trackPanel?.unSelectTransition()
                    if (videoTrackSelected && audioTrackSelected) {
                    } else if (videoTrackSelected && stickerTrackSelected) {
                    } else {
                        trackPanel?.unSelectCurrentSlot()
                    }
                } else if (position == NLEEditorContext.STATE_SEEK) { // seek了
                    DLog.d("editorModel.STATE_SEEK....")
                    mHandler.removeCallbacks(runnable)
                    viewStateListener?.onPlayViewActivate(false)
                    nleEditorContext!!.stopTrack()
                    trackPanel?.updatePlayState(
                        PlayPositionState(
                            MILLISECONDS.toMicros(
                                getCurrentPosition().toLong()
                            ), true, false
                        )
                    )
                    DLog.d("seek了: " + getCurrentPosition())
                    viewStateListener?.onPlayTimeChanged(
                        FileUtil.stringForTime(getCurrentPosition()),
                        FileUtil.stringForTime(getTotalDuration())
                    )
                } else if (position == NLEEditorContext.DELETE_CLIP) { // 删除clip
                    DLog.d("删除了clip片段...." + getCurrentPosition())
                    viewStateListener?.onPlayTimeChanged(
                        FileUtil.stringForTime(getCurrentPosition()),
                        FileUtil.stringForTime(getTotalDuration())
                    )
                    trackPanel?.unSelectTransition()
                    trackPanel?.unSelectCurrentSlot()

                    nleEditorContext!!.videoPlayer.seek(
                        MICROSECONDS.toMillis(
                            trackPanel?.getCurrentSlotInfo()?.playTime ?: 0
                        ).toInt()
                    )
                    trackPanel?.updatePlayState(
                        PlayPositionState(
                            trackPanel?.getCurrentSlotInfo().playTime,
                            true,
                            false
                        )
                    )
                    nleEditorContext!!.videoPlayer.pause()
                } else if (position == NLEEditorContext.SPLIT_CLIP) {
                    trackPanel?.unSelectCurrentSlot()
                    mHandler.removeCallbacks(runnable)
                    viewStateListener?.onPlayViewActivate(false)
                    nleEditorContext!!.stopTrack()
                    DLog.d("SPLIT_CLIP-------" + getCurrentPosition())
                    trackPanel?.updatePlayState(
                        PlayPositionState(
                            MILLISECONDS.toMicros(
                                getCurrentPosition().toLong()
                            ), false, false
                        )
                    )
                } else if (position == NLEEditorContext.FREEZE_FRAME) {
                    mHandler.removeCallbacks(runnable)
                    viewStateListener?.onPlayViewActivate(false)
                    nleEditorContext!!.stopTrack()
                    DLog.d("FREEZE_FRAME-------" + getCurrentPosition())
                } else if (position == NLEEditorContext.SLOT_COPY) {

                } else if (position == NLEEditorContext.SLOT_REPLACE) {
                    nleEditorContext?.selectedNleTrackSlot?.also {
                        val duration = it.mainSegment.getClipDuration() / 1000
                        selectVideo(
                            REPLACE_VIDEO_REQUEST_CODE,
                            1,
                            EditorConfig.AlbumFunctionType.REPLACESLOT,
                            duration,
                            PickType.REPLACE.type
                        )
                    }
                }
            })

        LiveDataBus.getInstance()
            .with(Constants.KEY_ADD_AUDIO, SelectedMusicInfo::class.java) //添加音频
            .observe(activity, androidx.lifecycle.Observer { musicInfo -> addAudio(musicInfo!!) })

        /** 添加完视频特效后 选中新添加的特效slot  */
        LiveDataBus.getInstance().with(Constants.KEY_ADD_VIDEO_EFFECT, NLETrackSlot::class.java)
            .observe(activity, androidx.lifecycle.Observer { slot ->
                slot?.apply {
                    trackPanel?.selectSlot(slot)  //添加新轨道
                }
            })
        /** 添加完调节slot后  选中新添加的slot  */
        LiveDataBus.getInstance().with(Constants.KEY_ADD_FILTER, NLETrackSlot::class.java)
            .observe(activity) { slot ->
                slot?.apply {
                    trackPanel?.selectSlot(slot)  //添加新轨道
                }
            }

        LiveDataBus.getInstance().with(Constants.KEY_STICKER_ENABLE, Boolean::class.java) //贴纸
            .observe(activity) {
                previewPanel?.switchEditType(STICKER) //previewPanel转换成贴纸状态
            }

        LiveDataBus.getInstance().with(Constants.KEY_COMPRESS_SUB_TRACK, Boolean::class.java)
            .observe(activity) {
                it?.let {
                    trackPanel?.compressSubTrack(it)
                }
            }

        nleEditorContext!!.animationChangedEvent.observe(activity) { nleVideoAnimation ->
            trackPanel?.adjustAnimation(
                nleVideoAnimation
            )
        }
        trackPanel?.post { nleEditorContext!!.changeRatioEvent.setValue(nleEditorContext!!.changeRatioEvent.value) }

        //订阅贴纸文字选中事件
        nleEditorContext!!.selectStickerEvent.observe(activity) { selectStickerEvent ->
            if (selectStickerEvent != null) {
                if (selectStickerEvent.isFromCover) {
                    if (selectStickerEvent.slot != null) {
                        previewPanel?.switchEditType(STICKER)
                    }
                    nleEditorContext!!.selectedNleCoverTrackSlot = selectStickerEvent.slot
                    nleEditorContext!!.selectedNleCoverTrack =
                        getCoverStickerTrackBySlot(selectStickerEvent.slot)
                    nleEditorContext!!.slotSelectChangeEvent.value = SelectSlotEvent(
                        selectStickerEvent.slot,
                        true
                    )
                } else {
                    if (!nleEditorContext!!.isCoverMode) {
                        selectStickerEvent.slot?.let {
                            trackPanel?.selectSlot(it)
                        }
                    }
                }
            } else {
                if (nleEditorContext!!.isCoverMode) {
                    nleEditorContext!!.selectedNleCoverTrackSlot = null
                    nleEditorContext!!.selectedNleCoverTrack = null
                    nleEditorContext!!.slotSelectChangeEvent.value = SelectSlotEvent(
                        null,
                        true
                    )
                } else {
                    trackPanel?.unSelectCurrentSlot()
                }
            }
        }
        //关闭底部面板事件
        nleEditorContext!!.closePanelEvent.observe(activity) { event ->
            bottomPanel?.apply {
                if (event?.closeAll == true) {
                    closeOtherPanel("")
                } else {
                    closeOtherPanel(getCurrentPanelFragmentTag())
                }
            }
        }
        //替换视频事件
        nleEditorContext!!.slotReplaceEvent.observe(activity) {
            refreshCover()
            updateFunctionEnable()
        }
        //刷新封面
        nleEditorContext!!.updateCoverEvent.observe(activity) {
            refreshCover()
        }
        //订阅片段选中事件
        nleEditorContext!!.selectSlotEvent.observe(
            activity,
            androidx.lifecycle.Observer { selectSlotEvent ->
                if (selectSlotEvent?.slot != null) {
                    trackPanel?.selectSlot(selectSlotEvent.slot!!)
                } else {
                    trackPanel?.unSelectCurrentSlot()
                }
            })

        //订阅面板事件
        nleEditorContext!!.panelEvent.observe(activity, androidx.lifecycle.Observer { panelEvent ->
            if (panelEvent == null) return@Observer
            when (panelEvent.panel) {
                Panel.STICKER, Panel.TEXT -> {
                    if (panelEvent.state === CLOSE) {
                        trackPanel?.unSelectCurrentSlot()
                        trackPanel?.switchUIState(NORMAL)
                    }
                }
                VIDEO_MASK -> if (panelEvent.state === OPEN) {
                    previewPanel?.switchEditType(EditTypeEnum.VIDEO_MASK)
                } else {
                    previewPanel?.switchEditType(VIDEO)
                }
            }
        })
        //刷新track事件
        nleEditorContext!!.updateTrackEvent.observe(activity) {
            nleModel?.let {
                trackPanel?.updateNLEModel(it)
            }
        }
        handleTrackPanelEvent()
        handleBottomEvent()
    }

    private fun refreshCover() {
        if (!needRefreshDefCover()) {
            return
        }
        nleEditorContext?.nleMainTrack?.sortedSlots?.firstOrNull()?.mainSegment?.resource?.resourceFile?.let { path ->
            //主轨的视频替换后，需要刷新下封面图
            launch(Dispatchers.IO) {
                ImageLoader.loadBitmapSync(activity, path, ImageOption.Builder().width(300).build())
                    ?.let {
                        mHandler.postDelayed({ updateCover(true, it) }, 10)
                    }
            }

        }
    }

    override suspend fun resetCoverToFirstFrame(): Unit = withContext(Dispatchers.IO) {
        nleEditorContext?.nleMainTrack?.sortedSlots?.firstOrNull()?.mainSegment?.resource?.resourceFile?.let { path ->
            val bitmap =
                ImageLoader.loadBitmapSync(activity, path, ImageOption.Builder().width(300).build())
            bitmap?.let { storeCover(bitmap, isInit = false, refreshOnly = false) }
        }
    }

    private fun needRefreshDefCover(): Boolean {
        if (hasSetCover()) {
            return false//设置过封面，不需要刷新
        }

        if (!nleEditorContext?.nleModel?.cover?.snapshot?.resourceFile.isNullOrBlank()) {
            return false//已有封面数据，不需要刷新
        }
        return true
    }

    private var runnable: Runnable = object : Runnable {
        override fun run() {
            viewStateListener?.onPlayTimeChanged(
                FileUtil.stringForTime(getCurrentPosition()),
                FileUtil.stringForTime(getTotalDuration())
            )

            val position = 100 * nleEditorContext!!.videoPlayer.curPosition()
                .toFloat() / nleEditorContext!!.videoPlayer.totalDuration().toFloat()
            setFullScreenSeekBarPosition(fullScreenSeekBar!!, position)
            mHandler.postDelayed(this, 50)
        }
    }

    override fun onUpdateTemplateCover() {
        trackPanel?.trackPanelActionListener?.onUpdateVideoCover()
        this.coverIsTemplate = true
    }

    private fun storeCover(bitmap: Bitmap, isInit: Boolean, refreshOnly: Boolean) {
        launch {
            if (!refreshOnly) {
                if (isInit) {
                    nleEditorContext!!.transientDone()
                    if (type != DRAFT_RESTORE) {//草稿导入不需要trim，需要可以undo对齐IOS
                        updateUndoRedoActivate()//update undo/redo button activate
                    }
                }
//                    把bitmap存成jpeg格式的path，存进snapshot
                val resultFile = getSaveCoverSnapShot()
                withContext(Dispatchers.IO) {
                    BitmapUtil.compressBitmap(bitmap, resultFile)
                }
                val coverRes = NLEResourceNode().apply {
                    resourceFile = resultFile.absolutePath
                    resourceType = NLEResType.IMAGE
                }
                // nleEditorContext!!.nleModel.cover?.snapshot = coverRes
                // nleEditorContext!!.nleTemplateModel.cover?.snapshot = coverRes
    nleEditorContext!!.nleModel.cover?.snapshot = coverRes
                nleEditorContext!!.selectedNleCoverTrackSlot = null
                nleEditorContext!!.selectedNleCoverTrack = null
                nleEditorContext!!.slotSelectChangeEvent.value = SelectSlotEvent(null, true)

                DLog.d(
                    TAG,
                    "onSaveSnapShot nleEditorContext.nleModel.cover.snapshot:${nleEditorContext?.nleModel?.cover?.snapshot?.resourceFile}"
                )

                if (coverIsTemplate) {
                    nleEditorContext!!.saveCoverEvent.value = PanelEvent(TEMPLATE_COVER, SAVE)
                    coverIsTemplate = false
                } else {
                    nleEditorContext!!.saveCoverEvent.value = PanelEvent(COVER, SAVE)
                }
                viewStateListener?.onCoverModeActivate(false)
                TrackConfig.FRAME_DURATION = preFrameDuration
            }
            val coverImage = BitmapUtil.compressBitmap(bitmap, 300F)
            //bitmap 是从glide中转换获取到, 不能随便手动recycle回收 会导致glide内存记录错误
            // 使用重复图片时造成使用已回收Bitmao 导致崩溃问题
            //bitmap.recycle()
            coverImage?.let {
                trackPanel?.setCoverImage(it)
            }
        }
    }

    private fun handleTrackPanelEvent() {
        trackPanel?.trackPanelActionListener = object : TrackPanelActionListener {
            override fun onStartAndDuration(
                slot: NLETrackSlot,
                start: Int,
                duration: Int,
                side: Int
            ) {
                nleEditorContext!!.mainTrackSlotClipEvent.value = (
                        MainTrackSlotClipEvent(slot, start, duration, side)
                        )

                postOnUiThread(50) {
                    val seekTime =
                        if (side == LEFT) slot.startTime else slot.endTime
                    trackPanel?.updatePlayState(
                        PlayPositionState(seekTime, false, false),
                        false
                    )
                    nleEditorContext?.videoPlayer?.seek(seekTime.toInt() / 1000)
                    viewStateListener?.onPlayTimeChanged(
                        FileUtil.stringForTime(seekTime.toInt() / 1000),
                        FileUtil.stringForTime(getTotalDuration())
                    )
                }
            }

            override fun onUpdateVideoCover() {
                (activity as EditorActivity).enable_template_setting_tv.visibility = View.GONE
                if (nleModel != null && containerId != null) {
                    preFrameDuration = TrackConfig.FRAME_DURATION
                    if (hasCoverSticker()) {
                        previewPanel?.switchEditType(STICKER)
                    }
                    fragmentHelper.startFragment(
                        VideoCoverFragment.newInstance(
                            nleModel!!.cover.coverMaterial.type.name, containerId, type
                        )
                    )
                    viewStateListener?.onCoverModeActivate(true)
                    nleModel?.cover?.enable = true
                    nleEditorContext?.commit()
                }
            }

            override fun onAudioMuteClick(isAllMute: Boolean, needUpdate: Boolean) {
                DLog.d("关闭原声被点击----isAllMute: $isAllMute  needUpdate: $needUpdate")
                mIsAllMute = isAllMute
                if (needUpdate) {
                    nleEditorContext?.oriAudioMuteEvent?.value = isAllMute
                }
            }

            override fun onSaveSnapShot(bitmap: Bitmap, isInit: Boolean, refreshOnly: Boolean) {
                storeCover(bitmap, isInit, refreshOnly)
            }

            override fun onKeyframeSelected(keyframe: NLETrackSlot?) {
                nleEditorContext?.keyframeEditor?.selectKeyframe?.value = keyframe
            }

            override fun getSelectedKeyframe(): NLETrackSlot? {
                return nleEditorContext?.keyframeEditor?.selectKeyframe?.value
            }

            /**
             * 是否需要忽略播放时自动点击下一个slot
             */
            override fun ignoreSelectSlotOnPlay(): Boolean {
                return nleEditorContext?.videoPlayer?.isInPlayRange() ?: false
            }

            override fun onMainTrackMoveSlot(
                nleTrackSlot: NLETrackSlot,
                fromIndex: Int,
                toIndex: Int
            ) {
                DLog.d(
                    TAG,
                    "onMainTrackMoveSlot  fromIndex:$fromIndex toIndex:$toIndex\n$nleTrackSlot"
                )
                nleEditorContext!!.mainTrackSlotMoveEvent.postValue(
                    MainTrackSlotMoveEvent(nleTrackSlot, fromIndex, toIndex)
                )
                nleEditorContext?.updateCoverEvent?.postValue(true)//刷新封面
            }

            override fun onMove(
                fromTrackIndex: Int,
                toTrackIndex: Int,
                slot: NLETrackSlot,
                newStart: Long,
                curPosition: Long
            ) {
                DLog.d(
                    TAG,
                    "--onMove fromTrackIndex：$fromTrackIndex toTrackIndex：$toTrackIndex newStart：$newStart curPosition：$curPosition\n$slot"
                )
                nleEditorContext!!.trackSlotMoveEvent.value =
                    TrackSlotMoveEvent(slot, fromTrackIndex, toTrackIndex, newStart, curPosition)

                if (NLESegmentTextSticker.dynamicCast(slot.mainSegment) != null
                    || NLESegmentInfoSticker.dynamicCast(slot.mainSegment) != null || slot.isEffectSlot()
                ) {
                    DLog.d("onMove 选中贴纸或Text轨道")
                    trackPanel?.selectSlot(slot)
                }
            }

            override fun onClip(slot: NLETrackSlot, startDiff: Long, duration: Long) {
                DLog.d(TAG, "--onClip $slot \nstartDiff: $startDiff\nduration: $duration")
                nleEditorContext!!.trackSlotClipEvent.postValue(
                    TrackSlotClipEvent(slot, startDiff, duration)
                )
            }

            override fun onScale(scaleRatio: Float) {
                DLog.d(TAG, "onScale $scaleRatio")
            }

            override fun onScaleEnd() {
                DLog.d(TAG, "onScaleEnd ")
            }

            override fun onScaleBegin() {
                DLog.d(TAG, "onScaleBegin ")
                nleEditorContext!!.videoPlayer.pause()
            }

            override fun onAddResourceClick() {
                selectVideo(
                    ActivityForResultCode.ADD_VIDEO_REQUEST_CODE,
                    MAX_SELECTION_COUNT,
                    EditorConfig.AlbumFunctionType.MAINTRACK,
                    pickType = PickType.ADD.type
                )
            }

            override fun onSegmentSelect(nleTrack: NLETrack?, nleTrackSlot: NLETrackSlot?) {

                var track = nleTrack
                if (nleTrackSlot.isEffectSlot()) {
                    track = nleModel?.getTrackByVideoEffect(nleTrackSlot!!)
                }

                val isPipTrack = !(nleTrack?.mainTrack
                    ?: false) && nleTrack?.extraTrackType == NLETrackType.VIDEO

                if ((nleTrackSlot == nleEditorContext!!.selectedNleTrackSlot || isPipTrack) && isCanvasEdit) {
                    nleEditorContext!!.selectedTrackSlotEvent.value =
                        SelectedTrackSlotEvent(null, null)
                    videoTrackSelected = false
                    trackPanel?.switchUIState(NORMAL)
                    previewPanel?.switchEditType(DEFAULT)
                    closeCanvasMode()
                    nleEditorContext!!.closeCanvasPanelEvent.postValue(true)
                }

                //fix：在动画fragment未pause情况下，切换了选中的slot，导致原slot一直处于动画预览状态
                nleEditorContext!!.selectedNleTrackSlot?.let {
                    if (NLESegmentSticker.dynamicCast(it.mainSegment) != null ||
                        NLESegmentTextTemplate.dynamicCast(it.mainSegment) != null
                    ) {
                        // 修复编辑文字模板时无法预览效果的bug
                        if (NLESegmentTextTemplate.dynamicCast(it.mainSegment) == null
                                || nleTrackSlot?.id != it.id) {
                            nleEditorContext!!.videoPlayer.player?.stopStickerAnimationPreview(it)
                        }
                    }
                }

                DLog.d(TAG, "onSegmentSelect $nleTrackSlot  track: $track")
                nleEditorContext!!.selectedTrackSlotEvent.value =
                    SelectedTrackSlotEvent(nleTrack, nleTrackSlot)

                if (nleTrackSlot != null) { // 打开剪辑面板

                    if (nleEditorContext?.keyframeEditor?.isSupportKeyframe(nleTrackSlot) == true) {
                        viewStateListener?.setKeyframeVisible(true)
                    } else {
                        viewStateListener?.setKeyframeVisible(false)
                    }

                    if (!isCanvasEdit) {
                        nleEditorContext!!.videoPlayer.pause()
                    }
                    if (nleTrack != null && nleTrack.extraTrackType == NLETrackType.VIDEO && !nleTrackSlot.isEffectSlot()) {
                        videoTrackSelected = true
                        DLog.d("onSegmentSelect 选中视频轨")
                        preIsPip = !nleTrack.mainTrack
                        if (isCanvasEdit) {
                            previewPanel?.switchEditType(VIDEO, Color.TRANSPARENT)
                        } else {
                            previewPanel?.switchEditType(VIDEO)
                        }

                        if (!isCanvasEdit) {
                            if (nleTrack.mainTrack) {
                                bottomPanel?.getFunctionManager()
                                    ?.disableFuncItem(FunctionType.TYPE_CUT_BLENDMODE)
                            } else {
                                bottomPanel?.getFunctionManager()
                                    ?.enableFuncItem(FunctionType.TYPE_CUT_BLENDMODE)
                            }
                            updateFunctionEnable()
                            bottomPanel?.getFunctionNavigator()
                                ?.expandCutFuncItem(nleTrack.mainTrack)
                        }
                    } else if (nleTrack != null && nleTrack.extraTrackType == NLETrackType.AUDIO) {
                        audioTrackSelected = true
                        DLog.d("onSegmentSelect 选中音频轨")
                        bottomPanel?.getFunctionNavigator()?.expandAudioFuncItem()
                        if (nleTrackSlot.mainSegment?.resource?.resourceType == NLEResType.RECORD) {
                            bottomPanel?.getFunctionManager()
                                ?.enableFuncItem(FunctionType.TYPE_RECORDING_CHANGE_VOICE)
                        } else {
                            bottomPanel?.getFunctionManager()
                                ?.disableFuncItem(FunctionType.TYPE_RECORDING_CHANGE_VOICE)
                        }
                        preIsAudio = true
//                        return
                    } else if (nleTrack != null && nleTrack.extraTrackType == NLETrackType.STICKER) {
                        stickerTrackSelected = true
                        previewPanel?.switchEditType(STICKER)
                        if (NLESegmentTextSticker.dynamicCast(nleTrackSlot.mainSegment) != null) {
                            DLog.d("onSegmentSelect 选中Text轨道")
                            preIsText = true
                            bottomPanel?.getFunctionNavigator()?.expandTextSelectedPanel()
                        } else if (NLESegmentInfoSticker.dynamicCast(nleTrackSlot.mainSegment) != null ||
                            NLESegmentImageSticker.dynamicCast(nleTrackSlot.mainSegment) != null
                        ) {
                            DLog.d("onSegmentSelect 选中贴纸轨道")
                            preIsSticker = true
                            bottomPanel?.getFunctionNavigator()?.expandStickerFuncItem()
                        } else if (NLESegmentTextTemplate.dynamicCast(nleTrackSlot.mainSegment) != null) {
                            DLog.d("onSegmentSelect 选中文字模板轨道")
                            preIsTextTemplate = true
                            bottomPanel?.getFunctionNavigator()?.expandTextTemplateSelectedPanel()
                        }
                    } else if (nleTrackSlot.isEffectSlot()) {
//                        audioTrackSelected = true;
                        preIsEffect = true
                        DLog.d("onSegmentSelect 选中特效轨")
                        bottomPanel?.getFunctionNavigator()?.expandEffectSelectedPanel()
                    } else if (nleTrack != null && nleTrack.extraTrackType == NLETrackType.FILTER) {
                        val nleFilter = nleTrackSlot.filters[0]
                        val isAdjust =
                            nleFilter.segment.effectSDKFilter.resourceType == NLEResType.ADJUST
                        if (isAdjust) {
                            preIsAdjust = true
                            DLog.d("onSegmentSelect 选中调节轨")
                            bottomPanel?.getFunctionNavigator()?.expandAdjustSelectedPanel()
                        } else {
                            preIsFilter = true
                            DLog.d("onSegmentSelect 选中滤镜轨")
                            bottomPanel?.getFunctionNavigator()?.expandFilterSelectedPanel()
                        }
                    }
                } else { // 关闭剪辑面板
                    videoTrackSelected = false
                    audioTrackSelected = false
                    stickerTrackSelected = false
                    viewStateListener?.setKeyframeVisible(false)
                    bottomPanel?.getFunctionNavigator()?.checkPanelClose()//关闭当前剪辑面板

                    DLog.d("取消选中轨道....")
                    if (preIsEffect) {
                        preIsEffect = false
                        bottomPanel?.getFunctionNavigator()
                            ?.expandFuncItemByType(FunctionType.TYPE_FUNCTION_VIDEO_EFFECT)
                        previewPanel?.switchEditType(DEFAULT)
                        return
                    }
                    if (preIsAdjust) {
                        preIsAdjust = false
                        bottomPanel?.getFunctionNavigator()
                            ?.expandFuncItemByType(FunctionType.TYPE_FUNCTION_ADJUST)
                        previewPanel?.switchEditType(DEFAULT)
                        return
                    }
                    if (preIsFilter) {
                        preIsFilter = false
                        bottomPanel?.getFunctionNavigator()
                            ?.expandFuncItemByType(FunctionType.TYPE_FUNCTION_FILTER_STATE)
                        previewPanel?.switchEditType(DEFAULT)
                        return
                    }
                    if (preIsText) {
                        preIsText = false
                        bottomPanel?.getFunctionNavigator()
                            ?.expandFuncItemByType(FunctionType.TYPE_FUNCTION_TEXT)
                        previewPanel?.switchEditType(STICKER)
                        return
                    }
                    if (preIsPip) {
                        preIsPip = false
                        bottomPanel?.getFunctionNavigator()
                            ?.expandFuncItemByType(FunctionType.TYPE_FUNCTION_PIP)
                        previewPanel?.switchEditType(DEFAULT)
                        return
                    }
                    if (preIsSticker) {
                        preIsSticker = false
                        bottomPanel?.getFunctionNavigator()
                            ?.expandFuncItemByType(FunctionType.TYPE_FUNCTION_STICKER)
                        previewPanel?.switchEditType(STICKER)
                        return
                    }

                    if (preIsTextTemplate) {
                        preIsTextTemplate = false
                        bottomPanel?.getFunctionNavigator()
                            ?.expandFuncItemByType(FunctionType.TYPE_FUNCTION_TEXT)
                        previewPanel?.switchEditType(STICKER)
                        return
                    }
                    if (isCanvasEdit) {
                        trackPanel?.switchUIState(NORMAL)
                        previewPanel?.switchEditType(DEFAULT)
                        closeCanvasMode()
                    }
                    if (preIsAudio) {
                        preIsAudio = false
                        bottomPanel?.getFunctionNavigator()
                            ?.expandFuncItemByType(FunctionType.TYPE_FUNCTION_AUDIO)
                        previewPanel?.switchEditType(DEFAULT)
                        return
                    }
                    previewPanel?.switchEditType(DEFAULT)
                    bottomPanel?.getFunctionNavigator()?.backToRoot()
                    return
                }
                nleTrackSlot?.let {
                    DLog.d("start:" + MICROSECONDS.toMillis(nleTrackSlot.startTime))
                    DLog.d("end  :" + MICROSECONDS.toMillis(nleTrackSlot.endTime))
                    //片段切换后，预览画面切到当前position
                    val playTime = trackPanel?.getCurrentSlotInfo()?.playTime ?: 0
                    if (nleTrackSlot.startTime > playTime) {
                        nleEditorContext!!.videoPlayer.seekToPosition(
                            MICROSECONDS.toMillis(nleTrackSlot.startTime).toInt() + 1,
                            EDITOR_SEEK_FLAG_LastSeek,
                            true
                        )
                        previewStickerViewModel!!.onVideoPositionChange(nleEditorContext!!.videoPlayer.curPosition() * 1000.toLong())
                    } else if (playTime > nleTrackSlot.measuredEndTime) {
                        nleEditorContext!!.videoPlayer.seekToPosition(
                            MICROSECONDS.toMillis(nleTrackSlot.measuredEndTime).toInt() - 1,
                            EDITOR_SEEK_FLAG_LastSeek,
                            true
                        )
                        previewStickerViewModel!!.onVideoPositionChange(nleEditorContext!!.videoPlayer.curPosition() * 1000.toLong())
                    } else {
                        if (!isCanvasEdit) {
                            nleEditorContext?.videoPlayer?.seekToPosition(
                                MICROSECONDS.toMillis(playTime).toInt(),
                                EDITOR_SEEK_FLAG_LastSeek,
                                false
                            )
                            previewStickerViewModel!!.onVideoPositionChange(nleEditorContext!!.videoPlayer.curPosition() * 1000.toLong())
                        }
                    }
                }
            }

            /**
             * SeekInfo(position=19634, autoPlay=false, seekFlag=0, seekPxSpeed=0.0625, seekDurationSpeed=0.41666666)
             */
            override fun onVideoPositionChanged(seek: SeekInfo) {
                // 用户滑动轨道，位置变化
                if (seek.isFromUser) {
                    if (nleEditorContext!!.videoPlayer.isPlaying) {
                        nleEditorContext!!.stopTrack()
                        nleEditorContext!!.videoPlayer.pause()
                    }
                    nleEditorContext!!.videoPlayer.seekToPosition(
                        MICROSECONDS.toMillis(seek.position).toInt(),
                        SeekMode.values()[seek.seekFlag],
                        false
                    )
                    // 这种情况下是在录音，长度已经超过视频长度了
                    if (getTotalDuration() <= seek.position / 1000) {
                        viewStateListener?.onPlayTimeChanged(
                            FileUtil.stringForTime((seek.position / 1000).toInt()),
                            FileUtil.stringForTime((seek.position / 1000).toInt())
                        )
                    } else {
                        viewStateListener?.onPlayTimeChanged(
                            FileUtil.stringForTime(getCurrentPosition() + 1),
                            FileUtil.stringForTime(getTotalDuration())
                        )
                    }
                } else {
                    // TODO: 1/10/21 不是用户主动触发
                }
                nleEditorContext!!.videoPositionEvent.value = seek.position
                previewStickerViewModel!!.onVideoPositionChange(seek.position)
            }

            override fun onTransitionClick(segment: NLETrackSlot, nextSegment: NLETrackSlot) {
                DLog.d(TAG, "get current slot info  " + trackPanel?.getCurrentSlotInfo())
                DLog.d("onTransitionClick segment: $segment  nextSegment: $nextSegment")
                nleEditorContext!!.transitionTrackSlotClickEvent.value =
                    TransitionTrackSlotClickEvent(segment, nextSegment)
                bottomPanel?.getFunctionNavigator()?.showTransactionPanel()
            }
        }
        nleEditorContext!!.addUndoRedoListener(object : SimpleUndoRedoListener() {
            override fun after(op: Operation, succeed: Boolean) {
                trackPanel?.onUndoRedo(if (op === UNDO) 0 else 1, succeed)
            }
        })
    }

    private fun updateFunctionEnable() {
        if (!isCanvasEdit) {
            //当前Slot是否倒放。若倒放，需要将音量FunctionItem置灰
            val selectedMediaType = nleEditorContext?.selectedNleTrackSlot?.mainSegment?.type?.name
            if (selectedMediaType == "VIDEO") {
                bottomPanel?.getFunctionManager()
                    ?.enableFuncItem(FunctionType.TYPE_CUT_FREEZE, false)
                val currentSlotHasReversed = nleEditorContext?.isRewind() == true
                if (!currentSlotHasReversed) {
                    bottomPanel?.getFunctionManager()
                        ?.enableFuncItem(FunctionType.TYPE_CUT_VOLUME, false)
                    bottomPanel?.getFunctionManager()
                        ?.enableFuncItem(FunctionType.TYPE_CUT_CHANGE_VOICE, false)
                } else if (currentSlotHasReversed) {
                    bottomPanel?.getFunctionManager()
                        ?.disableFuncItem(FunctionType.TYPE_CUT_VOLUME, false)
                    bottomPanel?.getFunctionManager()
                        ?.disableFuncItem(FunctionType.TYPE_CUT_CHANGE_VOICE, false)
                }
            } else {
                bottomPanel?.getFunctionManager()
                    ?.disableFuncItem(FunctionType.TYPE_CUT_FREEZE, false)
                bottomPanel?.getFunctionManager()
                    ?.disableFuncItem(FunctionType.TYPE_CUT_VOLUME, false)
                bottomPanel?.getFunctionManager()
                    ?.disableFuncItem(FunctionType.TYPE_CUT_CHANGE_VOICE, false)
            }
        }
    }

    private fun closeCanvasMode() {
        this@EditorActivityDelegate.isCanvasEdit = false
        trackPanel?.enableAutoSelectNext = false
        trackPanel?.selectedTrackStyle = MultiTrackLayout.TrackStyle.CLIP
        trackPanel?.isSelectAgainToRoot = true
    }

    @SuppressLint("ResourceType")
    private fun handleBottomEvent() {
        bottomPanel?.eventListener = object : IEventListener {
            private var stickerLoaded: Boolean = false

            override fun onHideChildren(functionItem: FunctionItem) {
                if (TextUtils.equals(functionItem.type, FunctionType.TYPE_FUNCTION_STICKER)) {
                    LiveDataBus.getInstance()
                        .with(Constants.KEY_STICKER_ENABLE, Boolean::class.java).postValue(false)
                }
            }

            override fun onShowChildren(functionItem: FunctionItem) {
                if (TextUtils.equals(functionItem.type, FunctionType.TYPE_FUNCTION_STICKER)) {
                    LiveDataBus.getInstance()
                        .with(Constants.KEY_STICKER_ENABLE, Boolean::class.java).postValue(true)
                }
            }

            override fun onBackToRootState() {
                if (!(preIsEffect || preIsText || preIsSticker || preIsTextTemplate || preIsAdjust || preIsFilter)) {
                    trackPanel?.switchUIState(NORMAL)
                    previewPanel?.switchEditType(DEFAULT)
                }
                trackPanel?.unSelectCurrentSlot()
            }

            override fun onFuncItemClicked(functionItem: FunctionItem) {
                when (Objects.requireNonNull(functionItem.type)) {
                    FunctionType.TYPE_FUNCTION_STICKER -> {
                        if (type == DRAFT_RESTORE && stickerLoaded.not()) {
                            previewStickerViewModel?.restoreInfoSticker()
                            stickerLoaded = true
                        }
                        previewPanel?.switchEditType(STICKER)
                        trackPanel?.switchUIState(TrackState.STICKER)
                    }
                    FunctionType.TYPE_FUNCTION_TEXT -> {
                        if (type == DRAFT_RESTORE && stickerLoaded.not()) {
                            previewStickerViewModel?.restoreInfoSticker()
                            stickerLoaded = true
                        }
                        previewPanel?.switchEditType(STICKER)
                        trackPanel?.switchUIState(TEXT)
                    }
                    FunctionType.TYPE_FUNCTION_PIP -> {
                        previewPanel?.switchEditType(VIDEO)
                        trackPanel?.switchUIState(PIP)
                    }
                    FunctionType.TYPE_FUNCTION_AUDIO -> {
                        previewPanel?.switchEditType(DEFAULT)
                        trackPanel?.switchUIState(AUDIO)
                    }
                    FunctionType.TYPE_FUNCTION_VIDEO_EFFECT -> {
                        trackPanel?.switchUIState(VIDEOEFFECT)
                    }
                    FunctionType.TYPE_FUNCTION_ADJUST -> {
                        trackPanel?.switchUIState(ADJUST)
                    }
                    FunctionType.TYPE_FUNCTION_FILTER_STATE -> {
                        trackPanel?.switchUIState(FILTER)
                    }
                    else -> {
                    }
                }
            }

            override fun onEditModeChanged(isEditMode: Boolean) {
                if (isEditMode) {
                    trackPanel?.selectCurrentSlot()
                    previewPanel?.switchEditType(VIDEO)
                    trackPanel?.switchUIState(NORMAL)
                } else {
                    trackPanel?.unSelectCurrentSlot()
                }
            }

            override fun onCanvasModeChanged(isCanvasMode: Boolean) {
                this@EditorActivityDelegate.isCanvasEdit = isCanvasMode
                trackPanel?.enableAutoSelectNext = isCanvasMode
                trackPanel?.isSelectAgainToRoot = !isCanvasMode
                if (isCanvasMode) {
                    trackPanel?.selectedTrackStyle = MultiTrackLayout.TrackStyle.LINE
                    trackPanel?.selectCurrentSlot()
                    previewPanel?.switchEditType(VIDEO, Color.TRANSPARENT)
                    trackPanel?.switchUIState(NORMAL)
                } else {
                    trackPanel?.selectedTrackStyle = MultiTrackLayout.TrackStyle.CLIP
                }
            }

            override fun changeAudioStatus() {
                this@EditorActivityDelegate.changeAudioStatus()
            }

            override fun changeVideoStatus() {
                this@EditorActivityDelegate.changeVideoStatus()
            }
        }
        bottomPanel?.show()
    }

    /**
     * 在从模版库进入时，如果拉取的模版有clip，则把这个clip转成crop
     */
    private fun changeClipToCrop() {
    }

    private fun initImport() { // 判断是否从拍摄进来
        DLog.d(TAG, "jump type $type")
        if (type == CHOSE_VIDEO) {
            nleEditorContext!!.nleEditor.commitDone() // 保存草稿的时候需要操作队列不为空
            selectVideo(
                CHOSE_VIDEO_REQUEST_CODE,
                MAX_SELECTION_COUNT,
                EditorConfig.AlbumFunctionType.IMPORTSELECT,
                pickType = PickType.SELECT.type
            )
        } else if (type == SELECT_VIDEO_AUDIO) { //从拍摄进来
            val select: MutableList<EditMedia> = ArrayList()
            videoPaths.forEach {
                select.add(EditMedia(it, mediaType == 3))
            }

            initEditor(select)
            viewStateListener?.onPlayTimeChanged(
                FileUtil.stringForTime(getCurrentPosition()),
                FileUtil.stringForTime(getTotalDuration())
            )
            mHandler.postDelayed({ initCover() }, 100)
        } else if (type == EditorHelper.EXTRA_FROM_MULTI_SELECT) {
            nleEditorContext!!.nleEditor.commitDone() // 保存草稿的时候需要操作队列不为空
            val select: MutableList<EditMedia>? = activity.intent.getParcelableArrayListExtra(PickerConfig.EXTRA_RESULT)
            select?.let {
                DLog.d(TAG, "jump with media $it")
                initEditor(it)
                viewStateListener?.onPlayTimeChanged(
                    FileUtil.stringForTime(getCurrentPosition()),
                    FileUtil.stringForTime(getTotalDuration())
                )
            } ?: activity.finish()
        } else if (type == DRAFT_RESTORE) { // 草稿还原
            if (!nleEditorContext!!.nleEditor.canUndo() && !nleEditorContext!!.nleEditor.canRedo()) {
                nleEditorContext!!.nleEditor.commitDone() // 保存草稿的时候需要操作队列不为空，草稿进编辑页的话，有可能草稿的操作队列不为空，就不需要额外commitDone
            }
            val draft = draftModel.getDraft(uuid!!)
    restoreCKDraft()
            val draftPath = activity.intent.getStringExtra(DRAFT_FILE_PATH)
            if (!draftPath.isNullOrBlank()) {
                val draftsFile = File(draftPath)
                if (draftsFile.exists()) {
                    initDraftEditor()
                    if (!draftPath.isNullOrBlank()) {
                        val readText = draftsFile?.readText()
                        restoreDraftContent(readText, true)
                        draftsFile.delete()
                    }
                } // end for if (drafts
            }
        }
    }

    private fun initNLEContextAfterDraftRestore() {
        nleEditorContext?.nleModel = nleEditorContext!!.nleEditor.model
        nleModel = nleEditorContext?.nleModel
        handleVEEditor()
        nleEditorListener.onChanged()
        DLog.d(TAG, "seek invoke ")
        nleEditorContext!!.videoPlayer.seek(0)
        viewStateListener?.onPlayTimeChanged(
            FileUtil.stringForTime(getCurrentPosition()),
            FileUtil.stringForTime(getTotalDuration())
        )
        mHandler.postDelayed({ initCover() }, 100)
    }

    private fun restoreDraftContent(draft: String, recheckSize: Boolean) {
        val state = nleEditorContext!!.restoreDraft(draft)
        if (recheckSize) {
            //form cutsame draft,slot width/height is template w/h,instead of reality
            checkFileSizeCorrect()
        }

        if (state.swigValue() != SUCCESS.swigValue()) {
            Toaster.show(activity.getString(R.string.ck_log_draft_recover_failed) + state.swigValue())
        } else {
            nleEditorContext!!.videoPlayer.seek(0)
            viewStateListener?.onPlayTimeChanged(
                FileUtil.stringForTime(getCurrentPosition()),
                FileUtil.stringForTime(getTotalDuration())
            )
            mHandler.postDelayed({ initCover() }, 100)
            initNLEContextAfterDraftRestore()
        }
    }

    private fun checkFileSizeCorrect() {
        nleEditorContext?.nleModel?.let { nleModel ->
            nleModel.tracks.forEach { track ->
                track.slots.forEach { slot ->
                    NLESegmentVideo.dynamicCast(slot.mainSegment)?.let { segment ->
                        if (segment.avFile != null) {
                            val videoInfo =
                                MediaUtil.getRealVideoMetaDataInfo(segment.avFile.resourceFile)
                            var width = videoInfo.width.toLong()
                            var height = videoInfo.height.toLong()
                            if (videoInfo.rotation == 90 || videoInfo.rotation == 270) {
                                width = videoInfo.height.toLong()
                                height = videoInfo.width.toLong()
                            }
                            segment.avFile.width = width
                            segment.avFile.height = height
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // 裁剪返回结果
            ActivityForResultCode.ADD_CROP_REQUEST_CODE -> {
                if (data == null || resultCode != Activity.RESULT_OK) {
                    return
                }
                processCropResult(data)
            }
            ActivityForResultCode.ADD_AUDIO_REQUEST_CODE -> {
                if (data == null) {
                    return
                }
                // 选择音乐处理
                val musicItem: MusicItem? =
                    data.getParcelableExtra(EditorHelper.EXTRA_KEY_AUDIO_SELECT)
                musicItem?.let {
                    addAudio(SelectedMusicInfo(musicItem.title, musicItem.uri))
                }
            }
            ActivityForResultCode.CHOSE_VIDEO_REQUEST_CODE -> {
                if (data == null) {
                    return
                }
                processChooseVideoResult(data)
            }
            ActivityForResultCode.ADD_VIDEO_REQUEST_CODE -> {
                if (data == null) {
                    return
                }
                processAddVideoResult(data)
            }
            ActivityForResultCode.PIP_VIDEO_REQUEST_CODE -> {
                if (data == null) {
                    return
                }
                processPipVideoResult(data)
            }
            ActivityForResultCode.REPLACE_VIDEO_REQUEST_CODE -> {
                if (data == null) {
                    return
                }
                val select = getIntentData(data)
                if (select != null && select.size > 0) {
                    val curTime = nleEditorContext!!.videoPlayer.curPosition()
                    nleEditorContext!!.videoEditor.slotReplace(select) {
                        videoMaskViewModel.recheckMaskSize(it)
                    }
                    nleEditorContext!!.videoPlayer.seekToPosition(curTime)
                }
            }
            ActivityForResultCode.COVER_IMAGE_REQUEST_CODE -> {
                if (data == null) {
                    return
                }

                val select = getIntentData(data)
                if (select != null && select.size > 0) {
                    startEdit(activity, select[0].path, true)
                }
            }
            ActivityForResultCode.LOCAL_STICKER_REQUEST_CODE -> {
                processLocalStickerResult(data)
            }
            ActivityForResultCode.LOCAL_STICKER_CROP_REQUEST_CODE -> {
                if (data == null || resultCode != Activity.RESULT_OK) {
                    return
                }

                processLocalStickerCropResult(data)
            }
            ActivityForResultCode.CUSTOM_CANVAS_REQUEST_CODE -> {
                if (data == null) {
                    return
                }
                val select = getIntentData(data)
                if (select != null && select.size > 0) {
                    val customCanvas = File(select[0].path)
                    val copyFile =
                        File("${activity.filesDir.absoluteFile}${File.separator}${System.currentTimeMillis()}${customCanvas.name}")
                    FileUtil.copy(customCanvas.absolutePath, copyFile.absolutePath)
                    val compressedCanvasFile = BitmapUtil.compressSize(copyFile)
                    compressedCanvasFile?.let {
                        val isSuccess =
                            nleEditorContext!!.canvasEditor.updateCanvasStyle(it.absolutePath)
                        if (isSuccess) {
                            nleEditorContext!!.done()
                            nleEditorContext!!.customCanvasImage.postValue(it.absolutePath)
                        }
                    }
                }
            }
        }
    }

    private fun processLocalStickerCropResult(data: Intent?) {
        if (instance.config.mediaConverter != null) {
            val mediaList =
                instance.config.mediaConverter?.obtainMediaFromIntent(
                    data,
                    ActivityForResultCode.LOCAL_STICKER_CROP_REQUEST_CODE
                )
            if (mediaList != null && mediaList.size > 0) {
                val stickerFile = File(mediaList[0].path)
                compressAndAddImageSticker(stickerFile)
            }
        } else {
            val stickerFile = if (isAtLeastR()) {
                getCropFile(activity, localStickerCropOutDir)
            } else {
                File(URI(localStickerCropOutDir.toString()))
            }
            compressAndAddImageSticker(stickerFile)
        }
    }

    private fun compressAndAddImageSticker(stickerFile: File?) {
        val compressedStickerFile = BitmapUtil.compressSize(stickerFile)
        compressedStickerFile?.let {
            if (compressedStickerFile.exists()) {
                addLocalImageSticker(compressedStickerFile.absolutePath)
            }
        }
    }

    fun getCropFile(context: Context, uri: Uri?): File? {
        if (uri == null) {
            return null
        }
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, proj, null, null, null)
        if (cursor?.moveToFirst() == true) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val path = cursor.getString(columnIndex)
            cursor.close()
            return File(path)
        }
        return null
    }

    private fun processLocalStickerResult(data: Intent?) {
        val select = if (instance.config.mediaConverter != null) {
            instance.config.mediaConverter?.obtainMediaFromIntent(
                data,
                ActivityForResultCode.LOCAL_STICKER_REQUEST_CODE
            )
        } else {
            data?.let {
                getIntentData(it)
            }
        }
        if (select != null && select.size > 0) {
            val localImageSticker = select[0]
            val localImageFile = File(localImageSticker.path)
            val copyFile =
                File("${activity.filesDir.absoluteFile}${File.separator}${System.currentTimeMillis()}${localImageFile.name}")
            FileUtil.copy(localImageFile.absolutePath, copyFile.absolutePath)
            val compressedFile = BitmapUtil.compressSize(copyFile)

            try {
                val tempFile = getSaveImageTempFile()
                val state =
                    FileUtils.copyFileFromUri(activity, Uri.fromFile(compressedFile), tempFile)
                if (state && tempFile.exists()) {
                    val tempUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(
                            activity,
                            EditorSDK.instance.config.fileProviderAuthority,
                            tempFile
                        )
                    } else {
                        Uri.fromFile(tempFile)
                    }
                    cropImage(tempUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun processPipVideoResult(data: Intent) {
        val select = getIntentData(data)
        if (select?.isNotEmpty() == true) {
            // 增加视频轨道
            val nleTrackSlot = nleEditorContext!!.videoEditor.addSubVideo(
                select,
                trackPanel?.getCurrentSlotInfo()?.playTime ?: 0,
                instance.config.pictureTime
            )
            trackPanel?.selectSlot(nleTrackSlot!!)
            reportImportMedia(select, true)
            if (nleEditorContext!!.nleModel.getSubTrackSize() >= 2) {
                Toaster.show(activity.getString(R.string.ck_tips_too_many_pip))
            }
        }
    }

    private fun processAddVideoResult(data: Intent) {
        val select = getIntentData(data)
        if (select?.isNotEmpty() == true) {
            if (!isCanvasEdit) {
                trackPanel?.unSelectCurrentSlot()
            }
            val curPosition = getCurrentPosition()
            nleEditorContext!!.videoEditor.insertMediaClips(
                select,
                getInsertIndex(),
                instance.config.pictureTime.toInt(),
                mIsAllMute = mIsAllMute
            )
            if (isCanvasEdit) {
                trackPanel?.selectCurrentSlot()
                nleEditorContext!!.videoPlayer.seek(curPosition)
            }
            reportImportMedia(select, false)
        }
    }

    private fun processChooseVideoResult(data: Intent) {
        val select = getIntentData(data)
        if (select?.isNotEmpty() == true) {
            initEditor(select)
            viewStateListener?.onPlayTimeChanged(
                FileUtil.stringForTime(getCurrentPosition()),
                FileUtil.stringForTime(getTotalDuration())
            )
            mHandler.postDelayed({ initCover() }, 100)
            reportImportMedia(select, false)
        } else {
            activity.finish()
        }
    }

    private fun processCropResult(data: Intent) {
        val leftTop = data.getParcelableExtra<PointF>(CropConstants.RESULT_DATA_LEFT_TOP)
        val rightTop = data.getParcelableExtra<PointF>(CropConstants.RESULT_DATA_RIGHT_TOP)
        val leftBottom = data.getParcelableExtra<PointF>(CropConstants.RESULT_DATA_LEFT_BOTTOM)
        val rightBottom =
            data.getParcelableExtra<PointF>(CropConstants.RESULT_DATA_RIGHT_BOTTOM)
        val cropFrameScale = data.getFloatExtra(CropConstants.RESULT_CROP_SCALE, 1.0f)
        val cropFrameRotateAngle =
            data.getFloatExtra(CropConstants.RESULT_CROP_ROTATE_ANGLE, 0f)
        val cropFrameTranslateX = data.getFloatExtra(CropConstants.RESULT_CROP_TRANSLATE_X, 0f)
        val cropFrameTranslateY = data.getFloatExtra(CropConstants.RESULT_CROP_TRANSLATE_Y, 0f)

        nleEditorContext?.apply {
            setSlotExtra(CropConstants.EXTRA_SCALE, cropFrameScale.toString())
            setSlotExtra(CropConstants.EXTRA_DEGREE, cropFrameRotateAngle.toString())
            setSlotExtra(CropConstants.EXTRA_TRANS_X, cropFrameTranslateX.toString())
            setSlotExtra(CropConstants.EXTRA_TRANS_Y, cropFrameTranslateY.toString())
            videoEditor.crop(
                CropParam(leftTop, rightTop, leftBottom, rightBottom)
            )
        }
    }

    private fun reportImportMedia(select: MutableList<EditMedia>, isPip: Boolean) {
        val type = if (isPip) "pip" else "main"
        ReportUtils.doReport(
            ReportConstants.VIDEO_IMPORT_COMPLETE_CLICK_EVENT,
            mutableMapOf(
                "video_cnt" to select.size.toString(),
                "video_cnt_duration" to EditorHelper.getSelectMediaDuration(select),
                "type" to type
            )
        )
    }

    private fun addLocalImageSticker(localStickerPath: String): Boolean {
        nleEditorContext!!.stickerEditor.applyImageSticker(
            ImageStickerParam(localStickerPath)
        )?.apply {
            nleEditorContext!!.done()
            nleEditorContext!!.selectStickerEvent.value = SelectStickerEvent(this)
        }
        val curPosition = nleEditorContext!!.videoPlayer.curPosition()
        return nleEditorContext!!.stickerEditor.updateStickerTimeRange(
            curPosition.toLong().toMicro(),
            (curPosition + TimeUnit.SECONDS.toMillis(3)).toMicro()
        )
    }

    private fun cropImage(uri: Uri?) {
        if (uri == null) {
            return
        }

        localStickerCropOutDir = getCropTempFile()
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED)
        intent.putExtra("crop", "true")
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("scale", true)
        intent.putExtra("noFaceDetection", true)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, localStickerCropOutDir)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        activity.startActivityForResult(
            intent,
            ActivityForResultCode.LOCAL_STICKER_CROP_REQUEST_CODE
        )
    }

    private fun getCropTempFile(): Uri? {
        return if (isAtLeastR()) {
            val imgFile = getSaveImageTempFileForAndroidR()
            // 通过 MediaStore API 插入file 为了拿到系统裁剪要保存到的uri（因为App没有权限不能访问公共存储空间，需要通过 MediaStore API来操作）
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, imgFile.absolutePath)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            return activity.contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            Uri.fromFile(getSaveImageTempFile())
        }
    }

    private fun getIntentData(data: Intent): MutableList<EditMedia>? {
        var select: MutableList<EditMedia>? = null
        val parcelableArrayListExtra =
            data.getParcelableArrayListExtra<Parcelable>(PickerConfig.EXTRA_RESULT)
        if (parcelableArrayListExtra == null || parcelableArrayListExtra.size <= 0) {
            return select
        }
        if (parcelableArrayListExtra[0] is EditMedia) {
            select = data.getParcelableArrayListExtra(PickerConfig.EXTRA_RESULT)
        }
        return select
    }

    override fun onBackPressed(): Boolean {
        return bottomPanel?.onBackPressed() ?: false
    }

    override fun onEditorSwitch(
        isPlayWhileEditorSwitch: Boolean,
        isFullScreen: Boolean
    ) {//app来回切换时触发
        nleModel?.apply {
            if (isPlayWhileEditorSwitch) {
                if (isFullScreen) {
                    fullScreenIVPlay?.setImageResource(R.drawable.ic_pause)
                }
                play()
                LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java)
                    .postValue(
                        NLEEditorContext.STATE_PLAY
                    )
            } else {
                LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java)
                    .postValue(
                        NLEEditorContext.STATE_PAUSE
                    )
            }
        }
    }

    /**
     * NLE数据驱动触发VEEditor
     */
    private fun handleVEEditor() {
        if (nleEditorContext != null) {
            nleEditorContext!!.videoPlayer.player!!.dataSource = nleEditorContext!!.nleModel
            if ((type == DRAFT_RESTORE || type == EXTRA_FROM_TEMPLATE || type == FILE_DRAFT) && !hasLoaded) {
                val mainTrack = nleModel!!.getMainTrack()
                nleEditorContext!!.nleMainTrack = mainTrack!!
                updateCanvasParams(nleModel!!)
                onUpdateDraft(nleModel!!)
            }
            hasLoaded = true
        }
    }

    /**
     * 草稿升级逻辑的兼容
     *
     */
    private fun onUpdateDraft(model: NLEModel) {
        model.tracks.filter { it.trackType == NLETrackType.VIDEO }?.forEach { it ->
            it.slots.filter { slot ->
                slot.getMainSegment<NLESegmentVideo>() != null
                        && slot.getMainSegment<NLESegmentVideo>()?.rewind == true
                        && slot.getMainSegment<NLESegmentVideo>()?.reversedAVFile == null
            }?.forEach { slot2 ->
                val segment = slot2.getMainSegment<NLESegmentVideo>()
                segment?.reversedAVFile = NLEResourceAV.dynamicCast(
                    segment?.avFile?.deepClone(true)
                )
                segment?.reversedAVFile?.resourceFile = segment?.reverseVideoPath
            }
        }
    }

    private fun initEditor(select: MutableList<EditMedia>) {
        previewPanel?.getPreViewSurface()?.also { surfaceView ->
            val rootPath = activity.filesDir.absolutePath
            nleEditorContext!!.init(rootPath, surfaceView)
            nleEditorContext!!.videoEditor.importMedia(
                select,
                instance.config.pictureTime,
                EditorSDK.instance.config.isFixedRatio
            )
            nleEditorContext!!.videoPlayer.seek(0)
            if (type == EXTRA_FROM_MULTI_SELECT || type == CHOSE_VIDEO ||
                type == SELECT_VIDEO_AUDIO
            ) {
                // 如果是模板生产工具 默认静音所有视频
                val enableTemplateFunction = EditorSDK.instance.config.enableTemplateFunction
                if (enableTemplateFunction) {
                    nleEditorContext!!.oriAudioMuteEvent.postValue(true)
                }
            }
            doneListener = mDoneListener
            activity.lifecycle.addObserver(nleEditorContext!!)
        }
    }

    /**
     * 来自草稿的初始化
     */
    private fun initDraftEditor() {
        previewPanel?.getPreViewSurface()?.also {
            val rootPath = activity.filesDir.absolutePath
            nleEditorContext!!.init(rootPath, it)
            doneListener = mDoneListener
            activity.lifecycle.addObserver(nleEditorContext!!)
        }
    }

    private fun updateCanvasParams(model: NLEModel) {
        val ration = model.canvasRatio
        nleEditorContext!!.changeRatioEvent.value = ration
    }

    fun changeVideoStatus() {
        videoTrackSelected = false
    }

    fun changeAudioStatus() {
        audioTrackSelected = false
    }

    private fun selectVideo(
        requestCode: Int,
        maxCount: Int,
        type: EditorConfig.AlbumFunctionType,
        minVideoTimeThreshold: Long = -1,  //筛选出播放时间大于minVideoTimeThreshold的素材
        pickType: String
    ) {
        val intent = if (instance.config.videoSelector != null) {
            instance.config.videoSelector!!.obtainAlbumIntent(activity, type)
        } else {
            Intent(activity, PickerActivity::class.java)
        }
        val maxSize = 388743680L //long long long LONG类型
        intent.putExtra(PickerConfig.MAX_SELECT_SIZE, maxSize) //default 180MB (Optional)
        intent.putExtra(PickerConfig.MAX_SELECT_COUNT, maxCount) //default 40 (Optional)
        intent.putExtra(PickerConfig.PICK_TYPE, pickType)
        if (minVideoTimeThreshold != -1.toLong()) {
            intent.putExtra(PickerConfig.MIN_VIDEO_TIME_THRESHOLD, minVideoTimeThreshold)
        }
        activity.startActivityForResult(intent, requestCode)

        ReportUtils.doReport(
            ReportConstants.VIDEO_IMPORT_CLICK_EVENT,
            mutableMapOf(
                "type" to "main"
            )
        )
    }

    private fun chooseMusic() {
        if (instance.config.audioSelector != null) {
            val intent = instance.config.audioSelector!!.obtainAudioSelectIntent(activity)
            activity.startActivityForResult(intent, ActivityForResultCode.ADD_AUDIO_REQUEST_CODE)
            return
        }
//        startFragment(AudioFragment.class);
    }


    override fun generateCoverImageForDraft() {
        val draftCover = getSaveCoverSnapShot()
        saveDraftCover(draftCover)
        coverForCompileDoneDraft = draftCover.absolutePath
    }

    override fun play() {
        nleEditorContext!!.videoPlayer.play()
    }

    override fun pause() {
        nleEditorContext!!.videoPlayer.pause()
    }

    override fun canUndo(): Boolean {
        return nleEditorContext?.canUndo() == true
    }

    override fun canRedo(): Boolean {
        return nleEditorContext?.canRedo() == true
    }

    override fun getHeadDescription(): String? {
        return nleEditorContext?.nleEditor?.branch?.head?.description
    }

    override fun undo(): Boolean {
        return nleEditorContext!!.undo()
    }

    override fun updateUndoRedoViewState() {
        updateFunctionEnable()
    }

    override fun hasSetCover(): Boolean {
        return mHasSetCover
    }

    override fun redo(): Boolean {
        return nleEditorContext!!.redo()
    }


    private fun addAudio(musicInfo: SelectedMusicInfo) {
        nleEditorContext!!.audioEditor.addAudioTrack(
            AudioParam(
                musicInfo.title,
                musicInfo.path,
                startTime = (nleEditorContext?.videoPlayer?.curPosition()?.toLong() ?: 0) * 1000,
                isAudioEffect = musicInfo.type == EFFECT,
                mediaId = musicInfo.musicId
            )
        )
        DLog.d("添加audio音频后，选择audio轨道")
        nleEditorContext?.currentAudioSlot?.apply {
            trackPanel?.selectSlot(this)
        }
    }

    // 获取当前指针的位置 计算应该插入到的clip的index
    private fun getInsertIndex(): Int {
        if (trackPanel == null) {
            return nleEditorContext!!.nleMainTrack.slots.size
        }

        val (index, slot, playTime) = trackPanel.getCurrentSlotInfo() // CurrentSlotInfo(index=-1, slot=null, playTime=15767000)
        if (slot == null) {
            return nleEditorContext!!.nleMainTrack.slots.size
        }
        return if (slot.measuredEndTime - playTime > slot.duration / 2) index else index + 1
    }

    private fun getCurrentPosition(): Int {
        return nleEditorContext!!.videoPlayer.curPosition()
    }

    private fun getTotalDuration(): Int {
        return nleEditorContext!!.videoPlayer.totalDuration()
    }

    override fun cancelCoverAction() {
        nleEditorContext!!.selectedNleCoverTrackSlot = null
        nleEditorContext!!.selectedNleCoverTrack = null
        nleEditorContext!!.slotSelectChangeEvent.value = SelectSlotEvent(
            null,
            true
        )
        nleEditorContext?.nleEditor?.resetHead()//点击取消时撤回所有临时操作
        viewStateListener?.onCoverModeActivate(false)
        nleEditorContext!!.panelEvent.apply {
            if (coverIsTemplate) {
                postValue(PanelEvent(TEMPLATE_COVER, CLOSE))
                coverIsTemplate = false
            } else {
                postValue(PanelEvent(COVER, CLOSE))
            }
        }
        TrackConfig.FRAME_DURATION = preFrameDuration
    }

    override fun saveCover() {
        nleEditorContext!!.closeCoverTextPanelEvent.postValue(true)
        updateCover()
        mHasSetCover = true
    }

    private fun initCover() {
        val player = nleEditorContext?.videoPlayer?.player
        player?.let {
            trackPanel?.initVideoCover(player)
        }
    }

    private fun saveDraftCover(coverFile: File) {
        val player = nleEditorContext?.videoPlayer?.player
        player?.let {
            trackPanel?.getDraftCover(it, coverFile)
        }
    }

    private fun updateCover(refreshOnly: Boolean = false, tempBitmap: Bitmap? = null) {
        val player = nleEditorContext?.videoPlayer?.player
        player?.let {
            trackPanel?.updateVideoCover(player, refreshOnly, tempBitmap)
        }
    }

    override fun resetCover() {
        nleEditorContext?.resetCoverEvent?.value = true
    }

    /**
     * 开启全屏入口
     */
    override fun startFullScreen(
        resolution: TextView,
        bt_button: Button,
        fullScreenControl: ConstraintLayout
    ) {
        play()  //展开全屏预览时，视频自动播放
        fullScreenIVPlay?.setImageResource(R.drawable.ic_pause)
        trackPanel?.let {
            it.unSelectCurrentSlot()
            it.hide()
        }

        val fr_preview: FrameLayout = activity.findViewById(R.id.fr_preview)
        val lp = fr_preview.layoutParams as ConstraintLayout.LayoutParams
        lp.topMargin = 0
        fr_preview.layoutParams = lp

        val controlBar: ConstraintLayout = activity.findViewById(R.id.control)
        val functionBarContainer: FrameLayout = activity.findViewById(R.id.function_bar_container)
        val closeIcon =
            ThemeStore.previewUIConfig.exitFullScreenIconConfig.exitFullScreenIconDrawableRes
        val closeFullScreenButton: ImageView = activity.findViewById(R.id.iv_close_full_screen)
        val close: ImageView = activity.findViewById(R.id.close)

        val goneViewList: MutableList<View> = mutableListOf()
        goneViewList.add(controlBar)
        goneViewList.add(functionBarContainer)
        goneViewList.add(resolution)
        goneViewList.add(bt_button)
        goneViewList.add(close)

        val visibleViewList: MutableList<View> = mutableListOf()
        visibleViewList.add(fullScreenControl)
        visibleViewList.add(closeFullScreenButton)

        //按照「业务方」的风格配置
        if (ThemeStore.previewUIConfig.exitFullScreenIconConfig.enable) {
            if (closeIcon == 0) {  //表示业务方没配置自定义关闭按钮，则用系统默认的「x」来控制全屏
                visibleViewList.add(close)
                visibleViewList.remove(closeFullScreenButton)
                goneViewList.add(closeFullScreenButton)
            } else {  //业务方有配关闭全屏自定义的icon
                val position = ThemeStore.previewUIConfig.exitFullScreenIconConfig.position
                val bias = ThemeStore.previewUIConfig.exitFullScreenIconConfig.bias.toFloat()
                closeFullScreenButton.setImageResource(closeIcon)
                visibleViewList.add(closeFullScreenButton)
                val lp = closeFullScreenButton.layoutParams as ConstraintLayout.LayoutParams
                if (position == ExitFullScreenIConPosition.Left) {
                    lp.marginEnd = SizeUtil.dp2px(bias)
                } else if (position == ExitFullScreenIConPosition.Right) {
                    lp.marginStart = SizeUtil.dp2px(bias)
                }
                closeFullScreenButton.layoutParams = lp
            }
        }

        goneViewList.forEach {
            it.visibility = View.GONE
        }
        visibleViewList.forEach {
            it.visibility = View.VISIBLE
        }

        previewPanel?.fullScreenToggle(true)
    }

    /**
     * 关闭全屏入口
     */
    override fun closeFullScreen(
        resolution: TextView,
        bt_button: Button,
        fullScreenControl: ConstraintLayout
    ) {
        nleEditorContext!!.videoPlayer.refreshCurrentFrame()
        trackPanel?.show() // reshow
        val controlBar: ConstraintLayout = activity.findViewById(R.id.control) //hide control bar
        val close: ImageView = activity.findViewById(R.id.close)
        val functionBarContainer: FrameLayout =
            activity.findViewById(R.id.function_bar_container) // hide function bar container
        val closeFullScreenButton: ImageView = activity.findViewById(R.id.iv_close_full_screen)
        val goneViewList: MutableList<View> = mutableListOf()
        goneViewList.add(fullScreenControl)
        goneViewList.add(closeFullScreenButton)
        val visibleViewList: MutableList<View> = mutableListOf()
        visibleViewList.add(controlBar)
        visibleViewList.add(close)
        visibleViewList.add(functionBarContainer)
        visibleViewList.add(resolution)
        visibleViewList.add(bt_button)
        if (ThemeStore.previewUIConfig.exitFullScreenIconConfig.enable) {
            val closeFullScreenButton: ImageView = activity.findViewById(R.id.iv_close_full_screen)
            goneViewList.add(closeFullScreenButton)
        }


        goneViewList.forEach {
            it.visibility = View.GONE
        }
        visibleViewList.forEach {
            it.visibility = View.VISIBLE
        }

        val fr_preview: FrameLayout = activity.findViewById(R.id.fr_preview)
        val lp = fr_preview.layoutParams as ConstraintLayout.LayoutParams
        lp.topMargin = SizeUtil.dp2px(50f)
        fr_preview.layoutParams = lp

        previewPanel?.fullScreenToggle(false)
    }

    override fun initFullScreen(fullScreenSeekBar: FloatSliderView) {
        var isVideoPlayingWhenChangeSeekBar = false
        fullScreenIVPlay = activity.findViewById(R.id.full_screen_iv_play)
        this.fullScreenSeekBar = fullScreenSeekBar
        val themeColorRes = EditorSDK.instance.editorUIConfig()?.customizeGlobalUI()?.themeColorRes
        if (themeColorRes != null) {
            this.fullScreenSeekBar?.lineColor = ContextCompat.getColor(activity, themeColorRes)
        }
        //进度条初始化
        this.fullScreenSeekBar!!.setOnSliderChangeListener(object : OnFloatSliderChangeListener() {
            override fun onChange(value: Float) {  //进度条移动，就会动态改变视频的位置
                nleEditorContext!!.videoPlayer.seekToPosition(
                    (value * nleEditorContext!!.videoPlayer.totalDuration() / 100).toInt(),
                    SeekMode.EDITOR_SEEK_FLAG_LastSeek,
                    true
                )
            }

            override fun onBegin(value: Float) {
                super.onBegin(value)
                if (nleEditorContext!!.videoPlayer.isPlaying) { //若进入全屏时，正在播放
                    isVideoPlayingWhenChangeSeekBar = true //防抖，防止进度条抖动，先停止视频
                    pause()
                    fullScreenIVPlay?.setImageResource(R.drawable.ic_full_screen_play)
                }
            }

            override fun onFreeze(value: Float) {
                if (isVideoPlayingWhenChangeSeekBar) {
                    isVideoPlayingWhenChangeSeekBar = false
                    sleep(350)  //防抖，防止进度条抖动，延迟后重新播放
                    play() //防抖，防止进度条抖动，延迟后重新播放
                    fullScreenIVPlay?.setImageResource(R.drawable.ic_pause)
                }
            }
        })
        fullScreenIVPlay?.setOnClickListener(this)
    }

    fun setFullScreenSeekBarPosition(fullScreenSeekBar: FloatSliderView, pos: Float) {
        fullScreenSeekBar.currPosition = pos
    }

    override fun onClick(p0: View?) {
        if (p0!!.id == R.id.full_screen_iv_play) { //点击了全屏状态下的播放按钮
            if (nleEditorContext!!.videoPlayer.isPlayingInFullScreen) { //点击时处于播放状态，就进入暂停状态
                fullScreenIVPlay?.setImageResource(R.drawable.ic_full_screen_play)
                nleEditorContext!!.videoPlayer.pause()
                nleEditorContext!!.videoPlayer.refreshCurrentFrame()
                nleEditorContext!!.videoPlayer.isPlayingInFullScreen = false
            } else {
                fullScreenIVPlay?.setImageResource(R.drawable.ic_pause)
                nleEditorContext!!.videoPlayer.play()
                nleEditorContext!!.videoPlayer.refreshCurrentFrame()
                nleEditorContext!!.videoPlayer.isPlayingInFullScreen = true
            }
        }
    }

    /**
     * @return 裁剪后图片文件
     */
    private fun getSaveImageTempFile(): File {
        val dir = FileUtils.getExternalFilesDir(activity, STICKER_TEMP_DIR)
        return File(dir, System.currentTimeMillis().toString() + ".jpg")
    }

    private fun getSaveImageTempFileForAndroidR(): File {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
        return File(dir, System.currentTimeMillis().toString() + ".jpg")
    }

    /**
     * @@return 封面快照
     */
    private fun getSaveCoverSnapShot(): File {
        val dir = FileUtils.getExternalFilesDir(activity, COVER_SNAPSHOT)
        return File(dir, System.currentTimeMillis().toString() + ".jpg")
    }

    private fun getCoverStickerTrackBySlot(slot: NLETrackSlot?): NLETrack? {
        slot?.let {
            nleEditorContext!!.nleModel.cover.tracks.forEach { coverTrack ->
                val coverSticker = coverTrack.sortedSlots.first()
                if (coverTrack.isTrackSticker() && coverSticker.nleSlotId == slot.nleSlotId) {
                    return coverTrack
                }
            }
        }
        return null
    }

    private fun hasCoverSticker(): Boolean {
        return nleEditorContext!!.nleModel.cover.tracks.firstOrNull { it.getVETrackType() == Constants.TRACK_STICKER }
            ?.let {
                true
            } ?: false
    }

    private fun startEdit(
        activity: FragmentActivity,
        imagePath: String,
        replace: Boolean
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        val time = SystemClock.uptimeMillis()
        val curRatioVal = nleEditorContext!!.changeRatioEvent.value ?: 9f / 16
        val curRes = nleEditorContext!!.changeResolutionEvent.value ?: 720
        val res = Resolution(curRes, curRatioVal)
        val width = res.width
        val height = res.height

        val data = CutSameData(
            path = imagePath,
            editType = MediaConstant.EDIT_TYPE_VIDEO,
            width = width,
            height = height
        )
        val callback = object : CutSameEditActivity.Callback {
            // 裁剪结果回调
            override fun onEditEnd(data: CutSameData?) {
                if (data == null) {
                    DLog.d("imageCover----cover_album_pic_add  null")
                    return
                }

                val imageCoverInfo =
                    ImageCoverInfo(
                        imagePath,
                        cropLeftTop = PointF(data.veTranslateLUX, data.veTranslateLUY),
                        cropLeftBottom = PointF(data.veTranslateLUX, data.veTranslateRDY),
                        cropRightTop = PointF(data.veTranslateRDX, data.veTranslateLUY),
                        cropRightBottom = PointF(data.veTranslateRDX, data.veTranslateRDY)
                    )
                val cropArray = Crop.create(
                    imageCoverInfo.cropLeftTop,
                    imageCoverInfo.cropRightTop,
                    imageCoverInfo.cropLeftBottom,
                    imageCoverInfo.cropRightBottom
                ).toFloatArray()

                DLog.d("imageCover----${cropArray.toList()}")
                nleEditorContext!!.imageCoverInfo.postValue(imageCoverInfo)
            }
        }

        // 跳转图片裁剪页面
        CutSameEditActivity.startEdit(activity, data, "cover", "cover-$time", callback)
    }

    private val ckDraftModel: com.ss.ugc.android.editor.main.draft.DraftViewModel by lazy {
        ViewModelProvider(
                activity,
                ViewModelProvider.AndroidViewModelFactory.getInstance(activity.application)
        ).get(com.ss.ugc.android.editor.main.draft.DraftViewModel::class.java)
    }

    override fun saveTemplateDraft(): String? {
        val draftString = nleEditorContext!!.saveDraft()
        val draftSize = EditorCoreUtil.getDraftSize(nleEditorContext!!.nleModel)
        if (!TextUtils.isEmpty(draftString) && nleEditorContext!!.nleModel.hasClips()) {
            var coverPath = nleEditorContext!!.nleModel.cover?.snapshot?.resourceFile
            if (coverPath.isNullOrBlank()) {
                coverPath = coverForCompileDoneDraft
            }
            if (coverPath.isNullOrBlank()) {//没有设置过封面，使用第一个slot作为封面
                coverPath =
                        nleEditorContext!!.nleModel.getMainTrack()?.sortedSlots?.firstOrNull()?.mainSegment?.resource?.resourceFile
            }
            if (coverPath.isNullOrBlank()) {
                generateCoverImageForDraft()
                coverPath = coverForCompileDoneDraft
            }
            return realSaveCKDraft(coverPath, draftString, draftSize)
        }
        return null
    }

    private fun realSaveCKDraft(path: String, data: String, size: Long): String? {
        val updateTime = System.currentTimeMillis()
        var draft = ckDraftModel.getDraft(uuid)
        if (draft != null) {
            draft.updateTime = updateTime
            draft.draftData = data
            draft.duration = getTotalDuration().toLong()
            draft.icon = path
            draft.draftSize = size
        } else {
            uuid = UUID.randomUUID().toString()
            draft = com.ss.ugc.android.editor.base.draft.DraftItem(
                    data,
                    getTotalDuration().toLong(),
                    path,
                    updateTime,
                    updateTime,
                    uuid!!,
                    size
            )
        }
        ckDraftModel.saveDraft(draft)
        Toaster.show(activity.getString(R.string.ck_tips_draft_save_success))
        return uuid
    }

    private fun restoreCKDraft() {
        val draft = ckDraftModel.getDraft(uuid)
        if (draft != null && !TextUtils.isEmpty(draft.draftData)) {
            initDraftEditor()
            val state = nleEditorContext!!.restoreDraft(draft.draftData)
            if (state.swigValue() != SUCCESS.swigValue()) {
                Toaster.show(activity.getString(R.string.ck_log_draft_recover_failed) + state.swigValue())
            } else {
                nleEditorContext!!.videoPlayer.seek(0)
                viewStateListener?.onPlayTimeChanged(
                        FileUtil.stringForTime(getCurrentPosition()),
                        FileUtil.stringForTime(getTotalDuration())
                )
                mHandler.postDelayed({ initCover() }, 100)
            }
        } else {
            Toaster.show(activity.getString(R.string.ck_tips_draft_recover_failed))
        }
    }
}