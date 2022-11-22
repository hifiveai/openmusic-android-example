package com.ss.ugc.android.editor.bottom.panel.canvas

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.bytedance.ies.nle.editor_jni.NLEResType
import com.bytedance.ies.nle.editor_jni.NLESegmentVideo
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.resourceview.*
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.theme.resource.*
import com.ss.ugc.android.editor.base.utils.runOnUiThread
import com.ss.ugc.android.editor.base.view.FuncItemDecoration
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.btm_panel_canvas_blur.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class CanvasBlurFragment : BaseUndoRedoFragment<CanvasBlurViewModel>(), CoroutineScope {

    private var currentSlot: NLETrackSlot? = null

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    override fun getContentViewLayoutId() = R.layout.btm_panel_canvas_blur

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_canvas_blur))
        initView()
        currentSlot = viewModel.nleEditorContext.selectedNleTrackSlot
        initListener()
    }

    private fun initView() {

        canvas_blur_list?.apply {
            val config = ResourceViewConfig.Builder()
                .itemDecoration(FuncItemDecoration(10))
                .nullItemInFirstConfig(
                    FirstNullItemConfig(
                        addNullItemInFirst = true,
                        nullItemResource = ThemeStore.bottomUIConfig.filterPanelViewConfig.nullFilterBgRes,
                        enableSelector = true,
                        nullItemIcon = ThemeStore.bottomUIConfig.filterPanelViewConfig.nullFilterIconRes
                    )
                )
                .panelKey(DefaultResConfig.CANVAS_BLUR_PANEL)
                .downloadIconConfig(DownloadIconConfig(true))
                .selectorConfig(
                    ItemSelectorConfig(
                        true,
                        56,
                        56,
                        selectorBorderRes = ThemeStore.getSelectBorderRes()
                    )
                )
                .resourceImageConfig(
                    ResourceImageConfig(
                        50,
                        50
                    )
                )
                .resourceTextConfig(
                    ResourceTextConfig(false)
                )
                .iconStyle(IconStyle.VIDEO_FRAME)
                .build()
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    selectCurrentCanvasBlur()
                    getCurrentVideoFrame {
                        it.add(0, null)
                        runOnUiThread {
                            canvas_blur_list?.updateMultiItemIcon(it, 0, 4)
                        }
                    }
                }
            })
            init(config)
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    val noneName = getString(R.string.ck_none)
                    if (position == 0) {
                        viewModel.setOriginCanvas()
                        selectItemByName(noneName)
                    } else {
                        viewModel.setCanvasBlur(item?.blurRadius ?: 0f)
                        selectItemByName(item?.blurRadius?.toString() ?: noneName)
                    }
                }
            })
        }
    }

    private fun initListener() {
        viewModel.nleEditorContext.slotSelectChangeEvent.observe(this, Observer { selectedSlot ->
            if (this.lifecycle.currentState == Lifecycle.State.RESUMED && selectedSlot != null && selectedSlot.slot != null) {
                if (viewModel.selectSlotFromMainTrack(selectedSlot.slot!!) && currentSlot?.id != selectedSlot.slot!!.id) {
                    canvas_blur_list?.selectCurrentCanvasBlur()
                    getCurrentVideoFrame {
                        it.add(0, null)
                        runOnUiThread {
                            canvas_blur_list?.updateMultiItemIcon(it, 0, 4)
                        }
                    }
                    currentSlot = selectedSlot.slot
                }
            }
        })
        viewModel.nleEditorContext.closeCanvasPanelEvent.observe(this, Observer { close ->
            close?.let {
                if (this.lifecycle.currentState == Lifecycle.State.RESUMED && close) {
                    pop()
                }
            }
        })
        apply_all_blur_group.setOnClickListener {
            viewModel.applyCanvasToAllSlot()
            Toaster.show(this.getString(R.string.ck_has_apply_all))
        }
    }

    override fun provideEditorViewModel(): CanvasBlurViewModel {
        return EditViewModelFactory.viewModelProvider(this).get(CanvasBlurViewModel::class.java)
    }

    override fun onUpdateUI() {
        canvas_blur_list?.selectCurrentCanvasBlur(true)
    }

    private fun getCurrentVideoFrame(onSuccess: (videoFrameList: ArrayList<Bitmap?>) -> Unit) {
        val select =
            viewModel.nleEditorContext.selectedNleTrackSlot
        select?.let {
            val convertToSegmentVideo = NLESegmentVideo.dynamicCast(select.mainSegment)
            val timeStamp =
                viewModel.nleEditorContext.videoPlayer.curPosition() - select.startTime.toInt() / 1000 + convertToSegmentVideo.timeClipStart.toInt() / 1000
            viewModel.nleEditorContext.videoFrameLoader?.loadVideoFrame(
                convertToSegmentVideo.resource.resourceFile,
                timeStamp,
                convertToSegmentVideo.type == NLEResType.IMAGE
            ) { blurIcon ->
                val blurBitmapList = arrayListOf<Bitmap?>()
                blurBitmapList.add(blurIcon)
                for (i in 1..3) {
                    val newBlurIcon = blurIcon.copy(blurIcon.config, true)
                    blurBitmapList.add(newBlurIcon)
                }
                onSuccess(blurBitmapList)
            }
        }
    }

    private fun ResourceListView.selectCurrentCanvasBlur(isRefresh: Boolean = true) {
        val initBlur = viewModel.getSelectedSlotCanvasBlur()
        val isBlurCanvas = initBlur != null
        val isOriginCanvas = viewModel.isOriginCanvas()
        if (isBlurCanvas && !isOriginCanvas) {
            selectItemByName(initBlur.toString(), isRefresh)
        } else if (isOriginCanvas) {
            selectItemByName(getString(R.string.ck_none), isRefresh)
        } else {
            selectItemByName("", isRefresh)
        }
    }
}