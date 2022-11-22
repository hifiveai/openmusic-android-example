package com.ss.ugc.android.editor.bottom.videoeffect

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.ies.nle.editor_jni.NLESegmentEffect
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import com.bytedance.ies.nle.editor_jni.NLETrack
import com.bytedance.ies.nle.editor_jni.NLETrackType
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.core.*
import kotlinx.android.synthetic.main.btm_panel_canvas_blur.*
import java.util.*

/**
 * @date: 2021/2/25
 */

class ApplyEffectFragment : BaseUndoRedoFragment<VideoEffectViewModel>(),
    EffectRVAdapter.OnItemClickListener {

    private var adapter: EffectRVAdapter? = null
    private var rvFilter: RecyclerView? = null
    private var rlShowFilter: RelativeLayout? = null

    companion object {
        const val DEFAULT_VALUE = 1.0f

        @JvmStatic
        fun newInstance(): ApplyEffectFragment {
            return ApplyEffectFragment()
        }
    }

    override fun provideEditorViewModel(): VideoEffectViewModel {
        return viewModelProvider(this).get(VideoEffectViewModel::class.java)
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.fragment_effect_apply
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_applied_range))
        rvFilter = view.findViewById(R.id.rc_filter)
        rlShowFilter = view.findViewById(R.id.rl_show_filter)
        adapter = EffectRVAdapter(view.context, this)
        adapter?.applyTrack = viewModel.getApplyTrackId()
        rvFilter?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvFilter?.adapter = adapter
        loadList()
        viewModel.nleEditorContext.selectedTrackSlotEvent.observe(this) { event ->
            if (NLESegmentEffect.dynamicCast(event?.nleTrackSlot?.mainSegment) == null) {
                pop()//切换到非特效轨道时，关闭当前面板
            }
        }
    }

    private fun loadList() {
        val mItems: MutableList<EffectApplyItem> = ArrayList()
        var effectTrack = viewModel.nleEditorContext.nleModel.tracks.firstOrNull {
            it.trackType == NLETrackType.EFFECT
        }
        if (effectTrack == null) {
            effectTrack = NLETrack().apply {
                this.extraTrackType = NLETrackType.EFFECT
                this.trackType == NLETrackType.EFFECT
            }
            viewModel.nleEditorContext.nleModel.addTrack(effectTrack)
        }
        val item = EffectApplyItem(effectTrack, null)
        mItems.add(item)

        val curPosition = viewModel.nleEditorContext.videoPlayer.curPosition()

        viewModel.nleEditorContext.nleModel.tracks.filter {
            it.getVETrackType() == Constants.TRACK_VIDEO
        }.forEach {
            it.sortedSlots?.forEachIndexed { _, nleTrackSlot ->
                run {
                    if (curPosition in nleTrackSlot.startTime / 1000..nleTrackSlot.measuredEndTime / 1000) {
                        val filterItem = EffectApplyItem(it, nleTrackSlot)
                        if (it.mainTrack) {
                            mItems.add(1, filterItem)
                        } else {
                            mItems.add(filterItem)
                        }
                        return@forEachIndexed
                    }
                }
            }
        }
        adapter?.setFilterList(mItems)
    }

    override fun onUpdateUI() {
        updateSelectedIndex(false)
    }

    private fun updateSelectedIndex(isForce:Boolean) {
        val applyItem: EffectApplyItem? = viewModel.getApplyTrackId()
        applyItem?.apply {
            if (isForce || adapter?.applyTrack?.track?.name !== track?.name ) {
                adapter!!.applyTrack = applyItem
            }
        } ?: closeFragment()

    }

    override fun onItemClick(applyItem: EffectApplyItem?, position: Int) {
        viewModel.applyTrack( applyItem )
        adapter?.applyTrack =  applyItem
        viewModel.onDone()
    }
}