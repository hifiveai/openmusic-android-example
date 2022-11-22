package com.ss.ugc.android.editor.preview

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.monitior.ReportUtils.nleModel
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.LogUtils
import com.ss.ugc.android.editor.base.viewmodel.PreviewStickerViewModel
import com.ss.ugc.android.editor.base.viewmodel.VideoMaskViewModel
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.toMilli
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import com.ss.ugc.android.editor.preview.infosticker.InfoStickerGestureAdapter
import com.ss.ugc.android.editor.preview.infosticker.InfoStickerGestureListener
import com.ss.ugc.android.editor.preview.infosticker.InfoStickerGestureManager
import com.ss.ugc.android.editor.preview.infosticker.InfoStickerGestureView
import com.ss.ugc.android.editor.preview.infosticker.OnInfoStickerDisPlayChangeListener
import com.ss.ugc.android.editor.preview.mask.VideoMaskDrawPresenter
import com.ss.ugc.android.editor.preview.subvideo.IVideoChecker
import com.ss.ugc.android.editor.preview.subvideo.OnVideoDisplayChangeListener
import com.ss.ugc.android.editor.preview.subvideo.SubVideoGestureManager
import com.ss.ugc.android.editor.preview.subvideo.SubVideoViewModel
import com.ss.ugc.android.editor.preview.subvideo.VideoGestureAdapter
import com.ss.ugc.android.editor.preview.subvideo.VideoGestureLayout

class PreviewPanel : Fragment(), IPreviewPanel {

    companion object {
        const val TAG = "PreviewPanel"
    }

    private var root: View? = null
    private var surfaceView: SurfaceView? = null
    private var frameLayout: FrameLayout? = null
    private var gestureLayout: VideoGestureLayout? = null
    private var infoStickerGestureView: InfoStickerGestureView? = null

    private var fullScreenControl: ConstraintLayout? = null
    private var subVideoGestureManager: SubVideoGestureManager? = null
    private var infoStickerGestureManager: InfoStickerGestureManager? = null
    private var previewStickerViewModel: PreviewStickerViewModel? = null
    var onVideoDisplayChangeListener: OnVideoDisplayChangeListener? = null
        private set
    var onInfoStickerDisPlayChangeListener: OnInfoStickerDisPlayChangeListener? = null
        private set
    var onViewPrepareListener: OnViewPrepareListener? = null
        private set
    val nleEditorContext: NLEEditorContext by lazy {
        viewModelProvider(this).get(NLEEditorContext::class.java)
    }

    fun setOnViewPrepareListener(onViewPrepareListener: OnViewPrepareListener) {
        this.onViewPrepareListener = onViewPrepareListener
    }

    val subVideoViewModel: SubVideoViewModel by lazy {
        viewModelProvider(requireActivity()).get(SubVideoViewModel::class.java)
    }
    private val videoMaskViewModel: VideoMaskViewModel by lazy {
        viewModelProvider(requireActivity()).get(VideoMaskViewModel::class.java)
    }

//    private val editModel by lazy {
//        viewModelProvider(activity!!).get(EditorMainViewModel::class.java)
//    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        surfaceView = SurfaceView(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_preview, container, false)
        frameLayout = root?.findViewById(R.id.surface_preview_fr)
        frameLayout?.addView(surfaceView)
        gestureLayout = root?.findViewById(R.id.gesture_layout_preview)
//        onViewPrepareListener?.onVideoGestureViewPrepare(gestureLayout!!)
        infoStickerGestureView = root?.findViewById(R.id.infoStickerEditorView)
        surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }

            override fun surfaceCreated(holder: SurfaceHolder) {
            }
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        previewStickerViewModel =
            viewModelProvider(requireActivity()).get(PreviewStickerViewModel::class.java)
//        editModel.initInfoStickerViewModel(activity!!)
//        // 贴纸初始化需要用到surfaceview的宽高
//        infoStickerGestureView?.apply {
//
//            editModel.previewStickerViewModel.gestureViewModel.surfaceSize.value = SurfaceSize(width, height)
//        }

        surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
            }

            override fun surfaceCreated(p0: SurfaceHolder) {
            }
        })
    }

    override fun init(previewPanelConfig: PreviewPanelConfig?) {
        ThemeStore.previewUIConfig?.stickerEditViewConfig?.let {
            getInfoStikerGestureManager()?.setInfoStickerViewConfig(it)
        }
        ThemeStore.previewUIConfig?.videoEditViewConfig?.let {
            getSubVideoGestureManager()?.setGestureViewConfig(it)
        }
    }

//    override fun enableEditVideoView(enable: Boolean) {
////        gestureLayout?.enableEdit = enable
//        if (enable) {
//            switchEditType(EditTypeEnum.VIDEO)
//        } else {
//            switchEditType(EditTypeEnum.DEFAULT)
//        }
//    }

//    override fun enableEditStickerView(enable: Boolean) {
////        infoStickerGestureView?.enableEdit = enable
//        if (enable) {
//            switchEditType(EditTypeEnum.STICKER)
//        } else {
//            switchEditType(EditTypeEnum.DEFAULT)
//        }
//    }

    override fun switchEditType(editTypeEnum: EditTypeEnum, videoFrameColor: Int) {
        clearOtherEditTypeLifeCycle(editTypeEnum)
        DLog.d("switchEditType ${editTypeEnum}")

        when (editTypeEnum) {
            EditTypeEnum.VIDEO -> {
                gestureLayout?.enableEdit = true
                infoStickerGestureView?.enableEdit = false
                gestureLayout?.apply {
                    val adapter = VideoGestureAdapter(subVideoViewModel, object : IVideoChecker {
                        override fun canMove(slot: NLETrackSlot?): Boolean {
                            return isInVideo(slot)
                        }
                    })
                    setAdapter(adapter)
                    adapter.viewHolder.apply {
                        videoFramePainter.updateRectColor(videoFrameColor)
                        setOnVideoDisplayChangeListener(onVideoDisplayChangeListener)
                        onViewPrepareListener?.onVideoGestureViewPrepare(this)
                    }
                }
            }
            EditTypeEnum.STICKER -> {
                infoStickerGestureView?.onClear()
                gestureLayout?.enableEdit = false
                infoStickerGestureView?.enableEdit = true
                infoStickerGestureView?.setInfoStickerGestureListener(InfoStickerGestureListener())
                infoStickerGestureView?.setAdapter(InfoStickerGestureAdapter(previewStickerViewModel!!))
                infoStickerGestureView?.setOnInfoStickerDisPlayChangeListener(
                    onInfoStickerDisPlayChangeListener
                )
                onViewPrepareListener?.onInfoStikerViewPrepare(infoStickerGestureView!!)
            }
            EditTypeEnum.VIDEO_MASK -> {
                gestureLayout?.enableEdit = true
                infoStickerGestureView?.enableEdit = false
                gestureLayout?.apply {
                    if (maskAdapter == null) {
                        maskAdapter = VideoMaskDrawPresenter(videoMaskViewModel, this)
                    }
                    setAdapter(maskAdapter!!)
                }
            }
            EditTypeEnum.CROP -> {
            }
            else -> {
                infoStickerGestureView?.enableEdit = false
                infoStickerGestureView?.enableEdit = false
            }
        }
    }

    private fun isInVideo(slot: NLETrackSlot?): Boolean {
        slot?.apply {
            return subVideoViewModel.nleEditorContext.videoPlayer.curPosition() !in
                    slot.startTime.toMilli() until slot.endTime.toMilli()
        }
        return true
    }

    var maskAdapter: VideoMaskDrawPresenter? = null

    fun clearOtherEditTypeLifeCycle(editTypeEnum: EditTypeEnum) {
        val type = EditTypeEnum.values()
        for (t in type) {
            if (t == editTypeEnum) continue
            when (t) {
                EditTypeEnum.STICKER -> {
                    infoStickerGestureView?.onClear()
                }
                EditTypeEnum.VIDEO -> {
                    gestureLayout?.onClear()
                }
                EditTypeEnum.VIDEO_MASK -> {
                    maskAdapter?.detach()
                }
                else -> {
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                previewStickerViewModel?.apply {
                    nleEditorContext.changeRatioEvent.observe(this.activity) {
                        it?.let {
                            changeStickerSize(it)
                        }
                    }
                }
                surfaceView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        }
        surfaceView?.viewTreeObserver?.addOnGlobalLayoutListener(listener)
    }

    fun changeStickerSize(ratio: Float) {
        val params = infoStickerGestureView!!.layoutParams as FrameLayout.LayoutParams
        val canvasSize = getCanvasSize(ratio)
        if (canvasSize?.width == 0 || canvasSize?.height == 0) return
        params.width = canvasSize!!.width
        params.height = canvasSize!!.height
        infoStickerGestureView!!.layoutParams = params
        Log.e("test-k", "----width =" + params.width + "height = " + params.width)
    }

    private fun getCanvasSize(ratio: Float): Size? {
        var size: Size? = null
        infoStickerGestureView?.apply {
            val previewWidth = surfaceView!!.measuredWidth
            val previewHeight = surfaceView!!.measuredHeight
            val viewRatio = previewWidth * 1f / previewHeight
            size = if (ratio >= viewRatio) {
                Size(previewWidth, (previewWidth / ratio).toInt())
            } else {
                Size((previewHeight * ratio).toInt(), previewHeight)
            }
        }
        return size
    }

    override fun getPreViewSurface(): SurfaceView? {
        if (surfaceView != null) {
            return surfaceView
        }
        return root?.findViewById(R.id.gesture_layout_preview)
    }

    override fun setOnVideoDisplayChangeListener(listener: OnVideoDisplayChangeListener) {
        this.onVideoDisplayChangeListener = listener
    }

    override fun setOnInfoStickerDisPlayChangeListener(listener: OnInfoStickerDisPlayChangeListener) {
        this.onInfoStickerDisPlayChangeListener = listener
    }

    override fun getSubVideoGestureManager(): SubVideoGestureManager? {
        if (subVideoGestureManager == null) {
            subVideoGestureManager = SubVideoGestureManager.getInstance(this)
        }
        return subVideoGestureManager
    }

    override fun getInfoStikerGestureManager(): InfoStickerGestureManager? {
        if (infoStickerGestureManager == null) {
            infoStickerGestureManager = InfoStickerGestureManager.getInstance(this)
        }
        return infoStickerGestureManager
    }

    override fun show(
        activity: FragmentActivity,
        @IdRes container: Int
    ) {
        activity.supportFragmentManager.beginTransaction().add(container, this)
            .commitNowAllowingStateLoss()
    }

    override fun fullScreenToggle(isEnableFullScreen: Boolean) {
        if (isEnableFullScreen) {
            val layoutParams = surfaceView?.layoutParams
            layoutParams?.height = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
            surfaceView?.layoutParams = layoutParams

            infoStickerGestureView?.enableEdit = false
        }
        gestureLayout?.isInFullScreen = isEnableFullScreen
        //由于更新画布需要延迟50ms，因此先记录下更新画布前的状态，在更新画布后，还原该状态
        val isPlayingBeforeUpdateCanvas = nleEditorContext.videoPlayer.isPlaying
        surfaceView?.post {
            nleEditorContext.videoPlayer.refreshCurrentFrame()
            // 强制刷新一下画布，此接口刷新画布后会自动暂停视频
            nleEditorContext.canvasEditor.setRatio((nleModel!!.canvasRatio))
            if (isPlayingBeforeUpdateCanvas) { //若进入全凭时的状态是播放状态，则更新画布后也让视频进入播放状态
                nleEditorContext.videoPlayer.play()
            }
        }
    }
}
