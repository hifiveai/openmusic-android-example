package com.ss.ugc.android.editor.main.cover

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.bytedance.ies.nle.editor_jni.NLEEditorListener
import com.bytedance.ies.nle.editor_jni.NLETrack
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.bytedance.ies.nlemedia.SeekMode
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.utils.runOnUiThread
import com.ss.ugc.android.editor.base.viewmodel.PreviewStickerViewModel
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import com.ss.ugc.android.editor.main.R
import com.ss.ugc.android.editor.track.PlayPositionState
import com.ss.ugc.android.editor.track.SeekInfo
import com.ss.ugc.android.editor.track.TrackPanel
import com.ss.ugc.android.editor.track.TrackPanelActionListener
import java.util.concurrent.TimeUnit

class FrameCoverFragment : BaseUndoRedoFragment<FrameCoverViewModel>() {

    private var trackPanel: TrackPanel? = null

    private var previewStickerViewModel: PreviewStickerViewModel? = null

    private val nleEditorListener: NLEEditorListener = object : NLEEditorListener() {
        override fun onChanged() {
            runOnUiThread {
                val model = viewModel.nleEditorContext.nleModel  //获取nle model
                trackPanel?.updateNLEModel(model) //更新model，nle数据更新后，通过这个接口来更新track视图
            }
        }
    }

    override fun getContentViewLayoutId() = R.layout.fragment_frame_cover

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideBottomBar()

        activity?.let {
            previewStickerViewModel = viewModelProvider(it).get(PreviewStickerViewModel::class.java)
        }
        viewModel.nleEditorContext.nleEditor.addConsumer(nleEditorListener)
        trackPanel = findViewById(R.id.cover_track_panel) as TrackPanel
        trackPanel?.setIsCoverMode(true)
        trackPanel?.updateNLEModel(viewModel.nleEditorContext.nleModel)
        trackPanel?.trackPanelActionListener = object : TrackPanelActionListener {
            override fun onVideoPositionChanged(seekInfo: SeekInfo) {
                // 用户滑动轨道，位置变化
                if (seekInfo.isFromUser) {
                    viewModel.nleEditorContext.videoPlayer.pause()
                    viewModel.nleEditorContext.videoPlayer.seekToPosition(
                        TimeUnit.MICROSECONDS.toMillis(seekInfo.position).toInt(),
                        SeekMode.values()[seekInfo.seekFlag],
                        false
                    )
                    viewModel.nleEditorContext.nleModel.cover.videoFrameTime = seekInfo.position
                }
                viewModel.nleEditorContext.videoPositionEvent.value = seekInfo.position
                previewStickerViewModel!!.onVideoPositionChange(seekInfo.position)
            }

            override fun onSegmentSelect(nleTrack: NLETrack?, nleTrackSlot: NLETrackSlot?) {
            }

            override fun onTransitionClick(segment: NLETrackSlot, nextSegment: NLETrackSlot) {
            }

            override fun onAddResourceClick() {
            }

            override fun onScaleBegin() {
            }

            override fun onScaleEnd() {
            }

            override fun onScale(scaleRatio: Float) {
            }

            override fun onClip(slot: NLETrackSlot, startDiff: Long, duration: Long) {
            }

            override fun onMove(
                fromTrackIndex: Int,
                toTrackIndex: Int,
                slot: NLETrackSlot,
                newStart: Long,
                currPosition: Long
            ) {
            }

            override fun onMainTrackMoveSlot(
                nleTrackSlot: NLETrackSlot,
                fromIndex: Int,
                toIndex: Int
            ) {
            }

            override fun onStartAndDuration(
                slot: NLETrackSlot,
                start: Int,
                duration: Int,
                side: Int
            ) {
            }

            override fun onUpdateVideoCover() {
            }

            override fun onAudioMuteClick(isAllMute: Boolean, needUpdate: Boolean) {
            }

            override fun onSaveSnapShot(bitmap: Bitmap, isInit: Boolean, refreshOnly: Boolean) {

            }

            override fun onKeyframeSelected(keyframe: NLETrackSlot?) {
            }

            override fun getSelectedKeyframe(): NLETrackSlot? {
                return null
            }

            /**
             * 是否需要忽略播放时自动点击下一个slot
             */
            override fun ignoreSelectSlotOnPlay(): Boolean {
                return viewModel.nleEditorContext.videoPlayer.isInPlayRange()
            }
        }
        trackPanel?.updatePlayState(PlayPositionState(viewModel.currentCoverVideoFrameTime(), true))
      //  viewModel.nleEditorContext.videoPlayer.seek(viewModel.currentCoverVideoFrameTime().toInt())
    }

    override fun provideEditorViewModel() = viewModelProvider(this)
        .get(FrameCoverViewModel::class.java)

    override fun onUpdateUI() {}

    override fun onDestroy() {
        super.onDestroy()
        viewModel.nleEditorContext.nleEditor.removeConsumer(nleEditorListener)
    }

    fun seekToOriginalPosition() {
        trackPanel?.updatePlayState(PlayPositionState(0, true))
        viewModel.seekToZeroPosition()
        viewModel.nleEditorContext.nleModel.cover.videoFrameTime = 0
    }
}