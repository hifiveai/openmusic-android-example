package com.ss.ugc.android.editor.bottom.panel.audiofilter

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ss.ugc.android.editor.base.fragment.BasePanelFragment
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import com.ss.ugc.android.editor.base.theme.resource.FirstNullItemConfig
import com.ss.ugc.android.editor.base.theme.resource.ItemSelectorConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceImageConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceTextConfig
import com.ss.ugc.android.editor.base.theme.resource.TextPosition.DOWN
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.R.drawable
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.api.video.AudioFilterParam
import com.ss.ugc.android.editor.core.getSlotExtra
import com.ss.ugc.android.editor.core.listener.OnUndoRedoListener
import com.ss.ugc.android.editor.core.listener.Operation
import com.ss.ugc.android.editor.core.listener.SimpleUndoRedoListener
import com.ss.ugc.android.editor.core.setSlotExtra
import com.ss.ugc.android.editor.core.toMilli
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.fragment_audio_filter.*

/**
 * @date: 2021/6/28
 * @desc: 变声面板
 */
class AudioFilterFragment : BasePanelFragment<BaseEditorViewModel>() {

    companion object {
        const val TAG = "AudioFilterFragment"
    }

    private val editorContext: NLEEditorContext by lazy {
        EditViewModelFactory.viewModelProvider(this).get(NLEEditorContext::class.java)
    }

    private val onUndoRedoListener: OnUndoRedoListener = object : SimpleUndoRedoListener() {
        override fun after(op: Operation, succeed: Boolean) {
            DLog.d("AudioFilterFragment::UndoRedoListener::succeed=$succeed, Operation=$op")
            if (succeed) {
                rvChangeVoices?.apply {
                    val savedPositionStr = editorContext.getSlotExtra(Constants.AUDIO_FILTER_POSITION)
                    if (!savedPositionStr.isNullOrEmpty()) {
                        val savedPosition = savedPositionStr.toInt()
                        val list = getResourceListAdapter()?.resourceList
                        if (!list.isNullOrEmpty()) {
                            val item = list[savedPosition]
                            selectItem(item.path)
                            editorContext.videoPlayer.play()
                        }
                    } else {
                        selectItem("")
                    }
                }
            }
        }
    }

    override fun provideEditorViewModel(): BaseEditorViewModel {
        return BaseEditorViewModel(requireActivity())
    }
    override fun getContentViewLayoutId(): Int {
        return R.layout.fragment_audio_filter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editorContext.addUndoRedoListener(onUndoRedoListener)
        setPanelName(getString(R.string.ck_change_voice))
        rvChangeVoices?.apply {
            val config = ResourceViewConfig.Builder()
                .panelKey(DefaultResConfig.AUDIO_FILTER)
                .layoutManager(LinearLayoutManager(context, 0, false))
                .nullItemInFirstConfig(
                    FirstNullItemConfig(
                        addNullItemInFirst = true,
                        enableSelector = true,
                        nullItemResource = drawable.round_drawable
                    )
                )
                .resourceTextConfig(ResourceTextConfig(enableText = true, textPosition = DOWN))
                .resourceImageConfig(
                    ResourceImageConfig(
                        imageWidth = 60,
                        imageHeight = 60,
                        backgroundResource = drawable.bg_transparent
                    )
                )
                .selectorConfig(
                    ItemSelectorConfig(
                        enableSelector = true,
                        selectorWidth = 65,
                        selectorHeight = 65,
                        selectorBorderRes = R.drawable.item_bg_selected_round
                    )
                )
                .build()
            init(config)
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    val savedPositionStr = editorContext.getSlotExtra(Constants.AUDIO_FILTER_POSITION)
                    if (!savedPositionStr.isNullOrEmpty()) {
                        val savedPosition = savedPositionStr.toInt()
                        val list = getResourceListAdapter()?.resourceList
                        if (!list.isNullOrEmpty()) {
                            selectItem(list[savedPosition].path)
                            getRecyclerView()?.scrollToPosition(savedPosition)
                        }
                    } else {
                        selectItem("")
                    }
                }
            })
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    // 读出相应音色的声音
                    item?.let {
                        if (position == 0 && config.enableFirstNullItem()) {
                            selectItem("")
                            //取消变声
                            cancelAudioFilter()
                        } else {
                            if (!it.path.isNullOrEmpty()) {
                                selectItem(it.path)
                                //应用变声
                                applyAudioFilter(item, position)
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        editorContext.removeUndoRedoListener(onUndoRedoListener)
    }

    private fun cancelAudioFilter() {
        editorContext.setSlotExtra(Constants.AUDIO_FILTER_POSITION, "")
        editorContext.audioEditor.cancelAudioFilter()
        playRange()
    }

    private fun applyAudioFilter(item: ResourceItem, position: Int) {
        editorContext.setSlotExtra(Constants.AUDIO_FILTER_POSITION, position.toString())
        DLog.d(TAG, "applyAudioFilter::name = ${item.name}, path = ${item.path}")
        editorContext.audioEditor.applyAudioFilter(
            AudioFilterParam(item.path, item.name)
        )
        playRange()
    }

    private fun playRange() {
        editorContext.selectedNleTrackSlot?.apply {
            editorContext.videoPlayer.playRange(startTime.toMilli().toInt(), endTime.toMilli().toInt())
        }
    }
}